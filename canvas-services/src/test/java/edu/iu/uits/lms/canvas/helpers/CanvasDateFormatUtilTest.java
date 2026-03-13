package edu.iu.uits.lms.canvas.helpers;

/*-
 * #%L
 * LMS Canvas Services
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

