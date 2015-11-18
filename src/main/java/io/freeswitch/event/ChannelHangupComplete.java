/*
 * Copyright 2015.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.freeswitch.event;

import io.freeswitch.common.HangupCauses;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * This event is raised whenever a call is hanged up.
 *
 * @author Arsene Tochemey GANDOTE
 */
public class ChannelHangupComplete extends AbstractEvent {

    /**
     * @param event
     */
    public ChannelHangupComplete(EslEvent event) {
        super(event);
    }

    /**
     * Gets the hangup cause.
     *
     * @return HangupCause value of the hangup cause.
     */
    public HangupCauses reason() {
        if (_event.eventHeaders().containsKey("Hangup-Cause")) {
            String cause = _event.eventHeaders().get("Hangup-Cause");
            return Enum.valueOf(HangupCauses.class, cause);
        }

        return HangupCauses.NONE;
    }

    /**
     * Gets the call start time. Timestamp when the call was answered (eg, SIP
     * 200 OK is received), in ISO 8601 format (YYYY-MM-DD hh:mm:ss), in the
     * local timezone (not UTC). If the call is not answered, this will be an
     * empty string.
     *
     * @return DateTime value of the call start time.
     * @see DateTime
     */
    public DateTime startTime() {
        DateTimeFormatter iso = ISODateTimeFormat.basicDateTimeNoMillis();
        return iso.parseDateTime(_event.eventHeaders().get(
                "variable_start_stamp"));
    }

    /**
     * Gets the call end time. Timestamp when the call was hung up, in ISO 8601
     * format (YYYY-MM-DD hh:mm:ss), in the local timezone (not UTC).
     *
     * @return DateTime value of the call end time
     * @see DateTime
     */
    public DateTime endTime() {
        DateTimeFormatter iso = ISODateTimeFormat.basicDateTimeNoMillis();
        return iso.parseDateTime(_event.eventHeaders()
                .get("variable_end_stamp"));
    }

    /**
     * Gets the billing value of the call in seconds. The answered or billing
     * span of the calls in seconds, i.e. "end_stamp - answer_stamp". Should
     * only be > 0 in calls where HANGUP_CAUSE == NORMAL_CLEARING (16).
     *
     * @return long value of the billing span.
     */
    public long billSec() {
        return Long.parseLong(_event.eventHeaders().get("variable_billsec"));
    }

    /**
     * Gets the call duration. The entire duration of the call, end to end in
     * seconds, i.e. "start_stamp - end_stamp". Duration should always be >=
     * billsec.
     *
     * @return long value of the call duration.
     */
    public long duration() {
        return Long.parseLong(_event.eventHeaders().get("variable_duration"));
    }

    /**
     * Gets the time the call has been answered. Timestamp when the call was
     * answered (eg, SIP 200 OK is received), in ISO 8601 format (YYYY-MM-DD
     * hh:mm:ss), in the local timezone (not UTC). If the call is not answered,
     * this will be an empty string.
     *
     * @return DateTime value of the answer time.
     */
    public DateTime answerTime() {
        String stamp = _event.eventHeaders().get("variable_answer_stamp");
        if (StringUtils.isEmpty(stamp))
            return null;
        DateTimeFormatter iso = ISODateTimeFormat.basicDateTimeNoMillis();
        return iso.parseDateTime(stamp);
    }
}
