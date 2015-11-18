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

/**
 * @author Arsene Tochemey GANDOTE
 */
public class Dtmf extends AbstractEvent {

    /**
     * @param event
     */
    public Dtmf(EslEvent event) {
        super(event);
    }

    /**
     * Gets the digit entered
     *
     * @return char value of the entered digit.
     */
    public char digit() {
        String digits = _event.eventHeaders().get("DTMF-Digit");
        return digits.charAt(0);
    }

    /**
     * Gets the digit duration.
     *
     * @return long value of the duration of the entered digit.
     */
    public long duration() {
        String duration = _event.eventHeaders().get("DTMF-Duration");
        return Long.parseLong(duration);
    }
}
