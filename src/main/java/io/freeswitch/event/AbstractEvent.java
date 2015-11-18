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

import java.util.List;
import java.util.Map;

/**
 * This class helps wrap all event received
 *
 * @author Arsene Tochemey GANDOTE
 */
public abstract class AbstractEvent {

    /**
     * FreeSwitch event.
     */
    protected final EslEvent _event;

    /**
     *
     */
    public AbstractEvent(EslEvent event) {
        _event = event;
    }

    /**
     * Gets the event headers. It is very useful to grab all the desired event
     * headers.
     *
     * @return map of event header values
     */
    public Map<String, String> headers() {
        return _event.eventHeaders();
    }

    /**
     * Gets the event body lines
     *
     * @return list of decoded event body lines, may be an empty list.
     */
    public List<String> bodyLines() {
        return _event.eventBodyLines();
    }

}
