package lms.iuonly.services;

import io.swagger.annotations.Api;
import lms.iuonly.model.SudsClass;
import lms.iuonly.model.SudsCourse;
import lms.iuonly.model.SudsFerpaEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/suds")
@Slf4j
@Api(tags = "suds")
public class SudsServiceImpl extends BaseService {
    private static final String SUDS_COURSE_COLUMNS = "year, term, descrshort, campus, iu_dept_cd, iu_course_cd, iu_site_id, descr, iu_crseld_status, iu_scs_flag, status, iu_active, class_nbr, strm, iu_instrc_mode_des, iu_etext_isbns";
    private static final String SUDS_COURSE_TABLE = "sysadm.ps_iu_oncext_clas";
    private static final String SUDS_ROSTER_FERPA_COLUMNS = "ferpa, iu_ims_username";
    private static final String SUDS_ROSTER_TABLE = "sysadm.ps_iu_oncext_rstr";
    private static final String SUDS_CLASS_COLUMNS = "crse_id, crse_offer_nbr, strm, institution, class_nbr";
    private static final String SUDS_CLASS_TABLE = "sysadm.ps_class_tbl";




    @Autowired
    DataSource dataSource;

    @GetMapping("/course/siteid")
    @PreAuthorize("#oauth2.hasScope('" + READ_SCOPE + "')")
    public SudsCourse getSudsCourseBySiteId(@RequestParam(value = "id", required = false) String siteId) {
        SudsCourse sudsCourse = null;
        Connection conn = getConnection();

        String sql = "select " + SUDS_COURSE_COLUMNS + " from " + SUDS_COURSE_TABLE + " where iu_site_id = ?";
        log.debug("Executing SQL: " + sql + " with query parameters: " + siteId);

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, siteId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                sudsCourse = translateRsToSudsCourse(rs);
            }
            if (sudsCourse == null) {
                log.warn("Could not find SudsCourseBySiteId:" + siteId);
                return null;
            }
        } catch (SQLException e) {
            log.error("Error getting suds course", e);
            throw new IllegalStateException();
        } finally {
            close(conn, stmt, rs);
        }

        return sudsCourse;
    }

    @GetMapping("/ferpa")
    @PreAuthorize("#oauth2.hasScope('" + READ_SCOPE + "')")
    public List<SudsFerpaEntry> getFerpaEntriesByListOfSisUserIds(@RequestParam List<String> iuImsUsernames,
                                                                  @RequestParam boolean justYs) {
        long start = System.currentTimeMillis();
        List<SudsFerpaEntry> entries = new ArrayList<>();
        Connection conn = getConnection();

        String sql = "select distinct " + SUDS_ROSTER_FERPA_COLUMNS + " from " + SUDS_ROSTER_TABLE + " where " +
              LmsSqlUtils.buildWhereInClause("iu_ims_username", iuImsUsernames, false);

        if (justYs) {
            sql = sql + " and ferpa = ?";
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(sql);
            if (justYs) {
                stmt.setString(1, "Y");
            }
            rs = stmt.executeQuery();

            while (rs.next()) {
                SudsFerpaEntry entry = translateRsToFerpaSudsEntry(rs);
                entries.add(entry);
            }
            if (entries.isEmpty()) {
                return entries;
            }
        } catch (SQLException e) {
            log.error("Error getting ferpa entries ", e);
            throw new IllegalStateException(e);
        } finally {
            close(conn, stmt, rs);
        }
        long end = System.currentTimeMillis();
        log.debug("getFerpaEntriesByListOfSisUserIds took " + (end - start) + " millis");
        return entries;
    }

    @GetMapping("/isSisCourse")
    @PreAuthorize("#oauth2.hasScope('" + READ_SCOPE + "')")
    public boolean isLegitSisCourse(@RequestParam String iu_site_id, @RequestParam String strm) {
        // translating iu_site_id into a SudsCourse object that will be useful in getSudsClassByCourse
        SudsCourse sudsCourse = new SudsCourse();
        sudsCourse.setSTerm(strm);

        // parsing is lame! But, this will get everything after the last dash of the iu_site_id
        String[] idSplit = iu_site_id.split("-");
        int dashes = idSplit.length - 1;
        if (dashes == 4) {
            String classNumber = idSplit[4];
            sudsCourse.setClassNumber(classNumber);

            // got the stuff, now send it to getSudsClassByCourse to see if it finds anything
            SudsClass sudsClass = getSudsClassByCourse(sudsCourse, false);
            return sudsClass != null;
        } else {
            return false;
        }
    }

    @GetMapping("/class/course")
    @PreAuthorize("#oauth2.hasScope('" + READ_SCOPE + "')")
    public SudsClass getSudsClassByCourse(@RequestParam SudsCourse sudsCourse, @RequestParam boolean includeCampus) {
        long start = System.currentTimeMillis();

        SudsClass sudsClass = null;
        Connection conn = getConnection();

        String sql = "select " + SUDS_CLASS_COLUMNS + " from " + SUDS_CLASS_TABLE + " " +
              "where strm = ? and class_nbr = ?";

        if (includeCampus) {
            sql += " and campus = ?";
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, sudsCourse.getSTerm());
            stmt.setString(2, sudsCourse.getClassNumber());
            if (includeCampus) {
                stmt.setString(3, sudsCourse.getCampus());
            }
            rs = stmt.executeQuery();

            if (rs.next()) {
                sudsClass = translateRsToSudsClass(rs);
            }
            if (sudsClass == null) {
                log.warn("Could not find SudsClassByCourse:" + sudsCourse);
                return null;
            }
        } catch (SQLException e) {
            log.error("Error getting suds class by course", e);
            throw new IllegalStateException(e);
        } finally {
            close(conn, stmt, rs);
        }
        long end = System.currentTimeMillis();
        log.debug("getSudsClassByCourse took " + (end - start) + " millis");
        return sudsClass;
    }

    private SudsClass translateRsToSudsClass(ResultSet rs) {
        SudsClass sudsClass = new SudsClass();
        try {
            //crse_id, crse_offer_nbr, strm, institution, class_nbr";
            sudsClass.setCourseId(rs.getString(1));
            sudsClass.setCourseOfferNumber(rs.getString(2));
            sudsClass.setSTerm(rs.getString(3));
            sudsClass.setInstitution(rs.getString(4));
            sudsClass.setClassNumber(rs.getString(5));
        } catch (SQLException e) {
            log.error("Error: ", e);
            throw new IllegalStateException(e);
        }
        return sudsClass;
    }

    /*
     * simply translates JDBC into POJO without closing ResultSet
     */
    private SudsCourse translateRsToSudsCourse(ResultSet rs) {
        SudsCourse sudsCourse = new SudsCourse();
        try {
            sudsCourse.setYear(rs.getString(1));
            sudsCourse.setTerm(rs.getString(2));
            sudsCourse.setDescriptionShort(rs.getString(3));
            sudsCourse.setCampus(rs.getString(4));
            sudsCourse.setIuDeptCd(rs.getString(5));
            sudsCourse.setIuCourseCd(rs.getString(6));
            sudsCourse.setIuSiteId(rs.getString(7));
            sudsCourse.setDescription(rs.getString(8));
            sudsCourse.setIuCourseLoadStatus(rs.getString(9));
            sudsCourse.setIuScsFlag(rs.getString(10));
            sudsCourse.setStatus(rs.getString(11));
            sudsCourse.setIuActive(rs.getString(12));
            sudsCourse.setClassNumber(rs.getString(13));
            sudsCourse.setSTerm(rs.getString(14));
            sudsCourse.setInstructionMode(rs.getString(15));
            sudsCourse.setEtextIsbns(rs.getString(16));
        } catch (SQLException e) {
            log.error("Error: ", e);
            throw new IllegalStateException(e);
        }
        return sudsCourse;
    }

    /*
     * simply translates JDBC into POJO without closing ResultSet
     */
    private SudsFerpaEntry translateRsToFerpaSudsEntry(ResultSet rs) {
        SudsFerpaEntry entry = new SudsFerpaEntry();
        try {
            entry.setFerpa(rs.getString(1));
            entry.setIuImsUsername(rs.getString(2));
        } catch (SQLException e) {
            log.error("Error:", e);
            throw new IllegalStateException(e);
        }
        return entry;
    }

    private Connection getConnection() {
        try {
            if (dataSource != null) {
                return dataSource.getConnection();
            }
        } catch (SQLException sqle) {
            log.error("Error getting connection", sqle);
        }

        return null;
    }

    private void close(Connection connection, Statement statement, ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException sqle) {
            log.error("Error closing resultset ", sqle);
        }

        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException sqle) {
            log.error("Error closing statement ", sqle);
        }

        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException sqle) {
            log.error("Error closing connection ", sqle);
        }
    }
}