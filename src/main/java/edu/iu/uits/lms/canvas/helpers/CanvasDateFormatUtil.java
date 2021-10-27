package edu.iu.uits.lms.canvas.helpers;

/*-
 * #%L
 * LMS Canvas Services
 * %%
 * Copyright (C) 2015 - 2021 Indiana University
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

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Created by chmaurer on 6/14/17.
 */
@Slf4j
public class CanvasDateFormatUtil {

    /**
     * Format of string to use when results are coming from canvas
     */
    public static final String CANVAS_DATE_FORMAT = "yyyy-MM-dd'T'hh:mm:ss'Z'";

    /**
     * Format of string to use when results are coming from canvas and only the date is needed
     */
    public static final String CANVAS_DATE_ONLY_FORMAT = "yyyy-MM-dd";

    /**
     * Date display format with timezone (ie Jan 28 2019 9:59:00 PM MST)
     */
    public static final String DISPLAY_FORMAT_WITH_TZ = "MMM d yyy h:mm:ss a z";

    public static final String DEFAULT_TIME_ZONE = "America/Indianapolis";

    /**
     *
     * @param dateString Input date string
     * @return Date representation of the given dateString. Uses {@link DateTimeFormatter}.ISO_ZONED_DATE_TIME
     * format for parsing the string.
     */
    public static Date string2Date(String dateString) {
        return string2Date(dateString,  DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    /**
     * Convert a string into a Date
     *
     * @param dateString String representation of a date
     * @param format Format of the date string
     * @return Date representation of the given dateString formatted using the given {@link DateTimeFormatter}.
     */
    public static Date string2Date(String dateString, DateTimeFormatter format) {
        if (dateString != null) {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateString, format);
            return Date.from(zonedDateTime.toInstant());
        }

        return null;
    }

    public static OffsetDateTime string2OffsetDateTime(String dateString) {
        return string2OffsetDateTime(dateString, DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    public static OffsetDateTime string2OffsetDateTime(String dateString, DateTimeFormatter format) {
        if (dateString != null) {
            return OffsetDateTime.parse(dateString, format);
        }

        return null;
    }

    /**
     * Convert a string into a Date (no time component)
     *
     * @param dateString String representation of a date
     * @return Date representation of the given dateString. Uses {@link DateTimeFormatter}.ISO_ZONED_DATE_TIME
     * format for parsing the string. Time is truncated to midnight. Date returned based on UTC.
     */
    public static Date string2DateOnly(String dateString) {
        if (dateString != null) {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_ZONED_DATE_TIME);
            Instant withTime = Instant.from(zonedDateTime.toInstant());
            Instant withoutTime = withTime.truncatedTo(ChronoUnit.DAYS);

            return Date.from(withoutTime);
        }
        return null;
    }


    /**
     *
     * @param daysDiff Days difference from now (can be positive or negative)
     * @return a {@link OffsetDateTime} based on the current date adjusted daysDiff. A positive daysDiff will be in the future;
     * if daysDiff is negative, will be in the past.
     */
    public static OffsetDateTime getAdjustedDate(int daysDiff) {
        OffsetDateTime adjustedDate = OffsetDateTime.now(ZoneOffset.UTC).plusDays(daysDiff);
        log.debug(daysDiff + " days adjusted from today: " + adjustedDate);
        return adjustedDate;
    }

    /**
     * @return the end date for manually-created courses. Will be the date one year from now, 11:59 pm (Indy time),
     * on the previous day
     */
    public static OffsetDateTime getCalculatedCourseEndDate() {
        OffsetDateTime adjustedZDT = OffsetDateTime.now(ZoneId.of(DEFAULT_TIME_ZONE))
                .withHour(23)
                .withMinute(59)
                .withSecond(0)
                .withNano(0)
                .plusYears(1)
                .minusDays(1);
        log.debug("Yesterday, one year from now, at 11:59 pm Indy time: " + adjustedZDT);

        return adjustedZDT;
    }

    /**
     *
     * @param isoDateString a date string in ISO 8601 format (the date format returned by Canvas)
     * @param timeZone optional - if null, will default to {@link CanvasDateFormatUtil#DEFAULT_TIME_ZONE}
     * @param format String format for your date display (ie {@link CanvasDateFormatUtil#DISPLAY_FORMAT_WITH_TZ})
     * @return a user-friendly String display of the given date in the given time zone and format
     */
    public static String formatISODateForDisplay(String isoDateString, String timeZone, String format) {
        String formattedDate = "";
        if (isoDateString != null) {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(isoDateString, DateTimeFormatter.ISO_ZONED_DATE_TIME);
            formattedDate = formatZDTForDisplay(zonedDateTime, timeZone, format);
        }

        return formattedDate;
    }

    /**
     *
     * @param date Date object that you want to convert for display
     * @param timeZone optional - if null, will default to {@link CanvasDateFormatUtil#DEFAULT_TIME_ZONE}
     * @param format String format for your date display (ie {@link CanvasDateFormatUtil#DISPLAY_FORMAT_WITH_TZ})
     * @return a user-friendly String display of the given date in the given time zone and format
     */
    public static String formatDateForDisplay(Date date, String timeZone, String format) {
        String formattedDate = "";
        if (date != null) {
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of(DEFAULT_TIME_ZONE));
            formattedDate = formatZDTForDisplay(zonedDateTime, timeZone, format);
        }

        return formattedDate;
    }

    /**
     *
     * @param zdt ZonedDateTime that you want to convert for display
     * @param timeZone optional - if null, will default to {@link CanvasDateFormatUtil#DEFAULT_TIME_ZONE}
     * @param format String format for your date display (ie {@link CanvasDateFormatUtil#DISPLAY_FORMAT_WITH_TZ})
     * @return a user-friendly String display of the given date in the given time zone and format
     */
    public static String formatZDTForDisplay(ZonedDateTime zdt, String timeZone, String format) {
        String formattedDate = "";
        if (zdt != null) {
            ZoneId zoneId = ZoneId.of(DEFAULT_TIME_ZONE);
            if (timeZone != null && !timeZone.trim().isEmpty()) {
                zoneId = ZoneId.of(timeZone);
            }

            ZonedDateTime localized = zdt.withZoneSameInstant(zoneId);
            formattedDate = localized.format(DateTimeFormatter.ofPattern(format));
        }

        return formattedDate;
    }
}
