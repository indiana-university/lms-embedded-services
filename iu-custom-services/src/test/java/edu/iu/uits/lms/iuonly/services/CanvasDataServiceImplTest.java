package edu.iu.uits.lms.iuonly.services;

/*-
 * #%L
 * lms-canvas-iu-custom-services
 * %%
 * Copyright (C) 2015 - 2026 Indiana University
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

import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CanvasDataServiceImpl#getSqlResults}, focused on {@code parseResultSet}'s
 * column-to-field mapping - specifically, that a class field with no corresponding column in the
 * result set (e.g. a field added for a query that doesn't yet select it) is left unset instead of
 * throwing. The Denodo JDBC driver's {@code findColumn()} throws {@link java.sql.SQLException}
 * ("Field name not found") for any column name it doesn't recognize, which {@code getObject(String)}
 * calls internally - this reproduced that failure and verifies the fix.
 * <p>
 * {@code CanvasDataServiceImpl} is loaded as a real Spring bean (mirroring {@code UserServiceTest} in
 * canvas-services) rather than constructed and wired by hand, so its {@code @Autowired}/{@code @Qualifier}
 * dependency is exercised the same way it would be in production. {@code @ActiveProfiles("denodo")} is
 * required since the class itself is {@code @Profile("denodo")}-gated.
 */
@SpringBootTest(classes = {CanvasDataServiceImpl.class})
@ActiveProfiles("denodo")
class CanvasDataServiceImplTest {

    @Autowired
    private CanvasDataServiceImpl service;

    @MockitoBean
    @Qualifier("denododb")
    private DataSource dataSource;

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private ResultSetMetaData resultSetMetaData;

    @Data
    static class TestRecord {
        private String id;
        private String optionalField;
    }

    @BeforeEach
    void setUp() throws Exception {
        // Plain Mockito mocks, not Spring beans - these model one JDBC call chain off the mocked
        // DataSource, not a dependency CanvasDataServiceImpl itself has autowired.
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        resultSetMetaData = mock(ResultSetMetaData.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(anyInt())).thenReturn(true);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
    }

    @Test
    void fieldWithNoMatchingColumnIsLeftUnsetInsteadOfThrowing() throws Exception {
        // The result set only has an "id" column - no column for TestRecord's "optionalField".
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("id");
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getObject("id")).thenReturn("abc123");
        // Mirrors the real Denodo driver's behavior for an unrecognized column name: without the
        // column-existence guard in parseResultSet, this is exactly what would blow up the whole
        // query. Stubbed explicitly so the test fails loudly (instead of quietly passing on
        // Mockito's default "unstubbed call returns null" behavior) if that guard is ever removed.
        when(resultSet.getObject("optionalField")).thenThrow(new SQLException("Field name not found"));

        List<TestRecord> results = service.getSqlResults("select id from foo", TestRecord.class);

        assertEquals(1, results.size());
        assertEquals("abc123", results.get(0).getId());
        assertNull(results.get(0).getOptionalField());
    }

    @Test
    void columnLookupIsCaseInsensitive() throws Exception {
        // Column labels aren't guaranteed to match a field's exact casing - confirm the fix's
        // existence check still matches "ID" against the "id" field.
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("ID");
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getObject("id")).thenReturn("abc123");

        List<TestRecord> results = service.getSqlResults("select id from foo", TestRecord.class);

        assertEquals("abc123", results.get(0).getId());
    }

    @Test
    void allFieldsPopulatedWhenAllColumnsPresent() throws Exception {
        when(resultSetMetaData.getColumnCount()).thenReturn(2);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("id");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("optionalField");
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getObject("id")).thenReturn("abc123");
        when(resultSet.getObject("optionalField")).thenReturn("present");

        List<TestRecord> results = service.getSqlResults("select id, optionalField from foo", TestRecord.class);

        assertEquals("abc123", results.get(0).getId());
        assertEquals("present", results.get(0).getOptionalField());
    }
}
