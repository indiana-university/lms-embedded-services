package edu.iu.uits.lms.iuonly.services;

/*-
 * #%L
 * lms-canvas-iu-custom-services
 * %%
 * Copyright (C) 2015 - 2022 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import edu.iu.uits.lms.iuonly.exceptions.CanvasDataServiceException;
import edu.iu.uits.lms.iuonly.model.CloseExpireCourse;
import edu.iu.uits.lms.iuonly.model.Enrollment;
import edu.iu.uits.lms.iuonly.model.ListWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by chmaurer on 11/10/15.
 */
@Profile("denodo")
@Service
@Slf4j
public class CanvasDataServiceImpl {

    @Autowired
    @Qualifier("denododb")
    DataSource dataSource;

    /**
     * Validate that we have a good connection to canvasdata
     * @param conn
     * @throws CanvasDataServiceException
     */
    private void validateConnection(Connection conn) throws CanvasDataServiceException {
        boolean valid = false;
        try {
            if (conn != null && conn.isValid(5)) {
                valid = true;
            }
        } catch (SQLException e) {
            log.error("Error validating canvasdata connection", e);
        }
        if (!valid) {
            String message = "Null or invalid connection to canvasdata";
            log.error(message);
            //TODO Add in some sort of notification to alert team that denodo is having problems
            throw new CanvasDataServiceException(message);
        }
    }

    public Map<String, String> getActiveUserMapOfIuUsernameToCanvasId(ListWrapper iuUsernameWrapper) throws CanvasDataServiceException {
        Map<String, String> userMap = new HashMap<>();

        if (iuUsernameWrapper == null || iuUsernameWrapper.getListItems() == null || iuUsernameWrapper.getListItems().isEmpty()) {
            return userMap;
        }

        List<String> iuUsernames = iuUsernameWrapper.getListItems();

        // denodo can't seem to handle queries over 1000 in the where.  Not just in the IN but total (even OR'd wheres)
        // so we'll go through the list 1000 at a time to build the map

        final int MAX_LIST_SIZE = 1000;

        int startIndex = 0;

        while (startIndex < iuUsernames.size()) {
            int endIndex = startIndex + MAX_LIST_SIZE;

            if (endIndex > iuUsernames.size()) {
                endIndex = iuUsernames.size();
            }

            List<String> subListIuUsernames = iuUsernames.subList(startIndex, endIndex);

            String whereUsernameClause = LmsSqlUtils.buildWhereInClause("pseudonyms.unique_id", subListIuUsernames, false, true);

            String sql =
                    "SELECT users.user_id AS canvas_id, pseudonyms.unique_id AS iu_username, pseudonyms.workflow_state AS status " +
                            "FROM iu_la.cd_users_flt users " +
                            "INNER JOIN iu_la.cd_pseudonyms_flt pseudonyms ON users.user_id = pseudonyms.user_id " +
                            "WHERE " + whereUsernameClause + " AND pseudonyms.workflow_state = 'active'";

            PreparedStatement ps = null;
            ResultSet rs = null;
            Connection conn = getConnection();
            validateConnection(conn);
            try {
                ps = conn.prepareStatement(sql);

                rs = ps.executeQuery();

                while (rs.next()) {
                    String username = rs.getString("iu_username");
                    String canvasId = rs.getString("canvas_id");

                    if (username != null && !username.isEmpty() && canvasId != null && !canvasId.isEmpty()) {
                        userMap.put(username, canvasId);
                    }
                }
            } catch (SQLException e) {
                log.error("uh oh", e);
                throw new IllegalStateException(e);
            } finally {
                close(conn, ps, rs);
            }

            startIndex = endIndex;
        }

        return userMap;
    }

    public List<Enrollment> getRosterStatusInfo(String canvasCourseId) throws CanvasDataServiceException {
        String sql = """
                select users.user_id AS canvas_user_id,
                      users.sortable_name AS name,
                      pseudonyms.unique_id AS username,
                      roles.name AS role,
                      course_sections.name AS section,
                      enrollments.workflow_state AS status,
                      enrollments.created_at As createdDate,
                      enrollments.updated_at AS updatedDate
                  FROM iu_la.cd_courses_flt courses
                    INNER JOIN iu_la.cd_course_sections_flt course_sections ON (courses.course_id = course_sections.course_id)
                    INNER JOIN iu_la.cd_enrollments_flt enrollments ON (course_sections.course_section_id = enrollments.course_section_id)
                    INNER JOIN iu_la.cd_users_flt users ON (enrollments.user_id = users.user_id)
                    INNER JOIN iu_la.cd_pseudonyms_flt pseudonyms ON (users.user_id = pseudonyms.user_id)
                    INNER JOIN iu_la.cd_roles_flt roles ON (enrollments.role_id = roles.roles_id)
                  where courses.course_id = ? and users.sortable_name != 'Student, Test'
                    AND pseudonyms.workflow_state = 'active'
                  ORDER BY users.sortable_name, users.user_id, course_sections.name, roles.name desc, pseudonyms.unique_id desc
                """;

        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = getConnection();
        validateConnection(conn);

        List<Enrollment> rosterStatusInfoList = new ArrayList<>();
        List<Enrollment> updatedRosterStatusInfoList = new ArrayList<>();

        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(canvasCourseId));
            rs = ps.executeQuery();

            while (rs.next()) {
                Enrollment enrollment = new Enrollment();
                enrollment.setCanvasUserId(rs.getString("canvas_user_id"));
                enrollment.setName(rs.getString("name"));
                enrollment.setUsername(rs.getString("username"));
                enrollment.setRole(rs.getString("role"));
                enrollment.setSection(rs.getString("section"));
                enrollment.setStatus(rs.getString("status"));
                enrollment.setCreatedDate(rs.getTimestamp("createdDate"));
                enrollment.setUpdatedDate(rs.getTimestamp("updatedDate"));

                rosterStatusInfoList.add(enrollment);
            }

            updatedRosterStatusInfoList = removeDuplicates(rosterStatusInfoList);
            log.info("Found " + updatedRosterStatusInfoList.size() + " enrollment records for Canvas course " + canvasCourseId);

        } catch (SQLException e) {
            log.error("error getting roster information for course " + canvasCourseId, e);
            throw new IllegalStateException(e);
        } finally {
            close(conn, ps, rs);
        }
        return updatedRosterStatusInfoList;
    }

    private List<Enrollment> removeDuplicates(List<Enrollment> rosterStatusInfoList) {
        List<Enrollment> updatedRosterStatusInfoList = new ArrayList<>();
        String reportInfoUserId = "";
        String reportInfoRole = "";
        String reportInfoSection = "";
        String reportInfoStatus = "";

        for (Enrollment enrollment : rosterStatusInfoList) {
            String userId = enrollment.getCanvasUserId();
            String role = enrollment.getRole();
            String section = enrollment.getSection();
            String status = enrollment.getStatus();

            //if the user is the new user, definitely add that user's record in.
            // if the user the same, check role, section, status. If one or any of them is different, add the record in,
            // otherwise, (means it is the duplicate), ignore it.
            if ((userId != null) && !userId.equals(reportInfoUserId)) {
                updatedRosterStatusInfoList.add(enrollment);

                reportInfoUserId = userId;
                reportInfoRole = role;
                reportInfoSection = section;
                reportInfoStatus = status;
            } else if ((role != null && !role.equals(reportInfoRole))
                  || (section != null && !section.equals(reportInfoSection))
                  || (status != null && !status.equals(reportInfoStatus))) {
                updatedRosterStatusInfoList.add(enrollment);

                reportInfoUserId = userId;
                reportInfoRole = role;
                reportInfoSection = section;
                reportInfoStatus = status;
            } else {
                continue;
            }
        }
        return updatedRosterStatusInfoList;
    }

    public List<CloseExpireCourse> getManuallyCreatedCoursesWithTerm() throws CanvasDataServiceException {
        List<CloseExpireCourse> notificationCourses = new ArrayList<>();
        String sql = """
                select distinct
                  courses.course_id AS canvas_id,
                  courses.name AS coursename,
                  courses.conclude_at AS end_date,
                  enrollment_terms.enrollment_terms_id AS canvas_term_id,
                  communication_channels.path AS emailAddress
                FROM iu_la.cd_courses_flt courses
                  INNER JOIN iu_la.cd_course_sections_flt course_sections ON (courses.course_id = course_sections.course_id)
                  INNER JOIN iu_la.cd_enrollments_flt enrollments ON (course_sections.course_section_id = enrollments.course_section_id)
                  INNER JOIN iu_la.cd_users_flt users ON (enrollments.user_id = users.user_id)
                  INNER JOIN iu_la.cd_pseudonyms_flt pseudonyms ON (users.user_id = pseudonyms.user_id)
                  INNER JOIN iu_la.cd_roles_flt roles ON (enrollments.role_id = roles.roles_id)
                  INNER JOIN iu_la.cd_enrollment_terms_flt enrollment_terms ON (courses.enrollment_term_id = enrollment_terms.enrollment_terms_id)
                  INNER JOIN iu_la.cd_communication_channels_flt communication_channels ON (communication_channels.user_id = users.user_id)
                where courses.sis_source_id is null
                  and roles.base_role_type='TeacherEnrollment'
                  and courses.workflow_state != 'deleted'
                  and roles.workflow_state != 'deleted'
                  and enrollments.workflow_state = 'active'
                  and communication_channels.position=1
                order by courses.course_id
                """;

        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = getConnection();
        validateConnection(conn);
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            Calendar cal = Calendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone("UTC"));

            while (rs.next()) {
                CloseExpireCourse course = new CloseExpireCourse();
                course.setCanvasCourseId(rs.getString("canvas_id"));
                course.setCourseName(rs.getString("coursename"));
                course.setEndDate(rs.getTimestamp("end_date", cal));
                course.setTermId(rs.getString("canvas_term_id"));
                course.setEmailAddress(rs.getString("emailAddress"));
                notificationCourses.add(course);
            }
        } catch (SQLException e) {
            log.error("Error getting data", e);
            throw new IllegalStateException(e);
        } finally {
            close(conn, ps, rs);
        }
        return notificationCourses;
    }

    /**
     * Retrieves a list of results from the database based on the provided SQL query.
     *
     * This method executes the given SQL statement and maps the results to a list of objects
     * of the specified class type. It handles SQL exceptions and ensures that resources are
     * properly closed after execution.
     *
     * @param sql   The SQL query to be executed. It should be a valid SQL statement that
     *              returns a result set.
     * @param clazz The class type to which the result set will be mapped. This class must
     *              have a no-argument constructor and appropriate setters for the fields
     *              that correspond to the columns in the result set.
     * @param <T>   The type of the objects in the returned list. This is a generic type
     *              that allows for flexibility in the type of objects returned.
     * @return A list of objects of type T, populated with the data retrieved from the
     *         database. If no results are found, an empty list is returned.
     * @throws CanvasDataServiceException If there is an error during the execution of the
     *                                     SQL statement or while parsing the result set.
     * @throws IllegalStateException If an error occurs while accessing the database or
     *                               mapping the results to the specified class type.
     */
    public <T> List<T> getSqlResults(String sql, Class<T> clazz) throws CanvasDataServiceException {
        List<T> objList;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = getConnection();
        validateConnection(conn);
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            objList = parseResultSet(rs, clazz);
        } catch (SQLException | InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            log.error("Error getting data", e);
            throw new IllegalStateException(e);
        } finally {
            close(conn, ps, rs);
        }
        return objList;
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

    /**
     * Parses a {@link ResultSet} and maps its rows to a list of objects of the specified class type.
     *
     * This method utilizes reflection to instantiate objects of the specified class and populate their fields
     * with values retrieved from the {@link ResultSet}. It also supports custom field naming strategies
     * through the use of the {@link JsonNaming} annotation, allowing for flexible mapping between database
     * column names and object field names.
     *
     * @param <T> the type of objects to be created from the {@link ResultSet}
     * @param rs the {@link ResultSet} containing the data to be parsed
     * @param clazz the class of the objects to be created
     * @return a {@link List} of objects of type {@code T} populated with data from the {@link ResultSet}
     * @throws SQLException if a database access error occurs or this method is called on a closed {@link ResultSet}
     * @throws InstantiationException if the class that declares the underlying field represents an abstract class
     * @throws IllegalAccessException if this {@code Field} object is enforcing Java language access control
     *         and the underlying field is inaccessible
     * @throws NoSuchMethodException if a matching method is not found
     * @throws InvocationTargetException if the underlying method throws an exception
     *
     * @see JsonNaming
     * @see PropertyNamingStrategy
     */
    private <T> List<T> parseResultSet(ResultSet rs, Class<T> clazz) throws SQLException, InstantiationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        List<T> objList = new ArrayList<>();

        Field[] fields = clazz.getDeclaredFields();

        // Check for an annotation that tells us how to translate the field name
        JsonNaming jsonNaming = clazz.getAnnotation(JsonNaming.class);
        Class<? extends PropertyNamingStrategy> strategyClass = null;
        PropertyNamingStrategy strategy = null;

        if (jsonNaming != null) {
            strategyClass = jsonNaming.value();
            strategy = strategyClass.getConstructor().newInstance();
            log.debug("Using strategy class {} to get fields", strategyClass.getName());
        }

        while (rs.next()) {
            T obj = clazz.getDeclaredConstructor().newInstance();

            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();

                // See if there's a strategy to use (snake case, camel case, etc
                if (strategyClass != null) {
                    Method invokeMethod = strategyClass.getMethod("translate", String.class);
                    fieldName = (String) invokeMethod.invoke(strategy, fieldName);
                }

                log.trace("Looking up field '{}'", fieldName);
                Object value = rs.getObject(fieldName);

                if (value != null) {
                    field.set(obj, value);
                }
            }
            objList.add(obj);
        }
        return objList;
    }
}
