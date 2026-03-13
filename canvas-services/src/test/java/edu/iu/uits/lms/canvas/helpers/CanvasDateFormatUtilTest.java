package edu.iu.uits.lms.canvas.helpers;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CanvasDateFormatUtilTest {

    @Test
    void testGetCalculatedCourseEndDate() {
        // Arrange
        String tz = CanvasDateFormatUtil.DEFAULT_TIME_ZONE;
        ZoneId zoneId = ZoneId.of(tz);
        // Use the current date as March 13, 2026 (per context)
        LocalDate now = LocalDate.of(2026, 3, 13);
        LocalDate expectedDate = now.plusYears(1).minusDays(1);
        LocalTime expectedTime = LocalTime.of(23, 59);
        ZonedDateTime expectedZdt = ZonedDateTime.of(expectedDate, expectedTime, zoneId);
        OffsetDateTime expectedOdt = expectedZdt.toOffsetDateTime();

        // Act
        OffsetDateTime actual = CanvasDateFormatUtil.getCalculatedCourseEndDate();

        // Assert
        assertEquals(expectedOdt.getYear(), actual.getYear(), "Year should match");
        assertEquals(expectedOdt.getMonth(), actual.getMonth(), "Month should match");
        assertEquals(expectedOdt.getDayOfMonth(), actual.getDayOfMonth(), "Day should match");
        assertEquals(expectedOdt.getHour(), actual.getHour(), "Hour should be 23");
        assertEquals(expectedOdt.getMinute(), actual.getMinute(), "Minute should be 59");
        assertEquals(expectedOdt.getOffset(), actual.getOffset(), "Offset should match Indy time zone for that date");
    }

    @Test
    void testDSTTransition() {
        // Arrange: pick a date near DST transition
        String tz = CanvasDateFormatUtil.DEFAULT_TIME_ZONE;
        ZoneId zoneId = ZoneId.of(tz);
        LocalDate now = LocalDate.of(2026, 3, 13); // DST starts March 8, 2026
        LocalDate expectedDate = now.plusYears(1).minusDays(1); // March 12, 2027
        LocalTime expectedTime = LocalTime.of(23, 59);
        ZonedDateTime expectedZdt = ZonedDateTime.of(expectedDate, expectedTime, zoneId);
        OffsetDateTime expectedOdt = expectedZdt.toOffsetDateTime();

        // Act
        OffsetDateTime actual = CanvasDateFormatUtil.getCalculatedCourseEndDate();

        // Assert
        assertEquals(expectedOdt.getOffset(), actual.getOffset(), "Offset should match DST rules for Indy");
    }
}

