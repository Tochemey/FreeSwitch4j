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
 * This event is raised whenever an application has completed its execution.
 *
 * @author Arsene Tochemey GANDOTE
 */
public class ChannelExecuteComplete extends AbstractEvent {

    /**
     * @param event
     */
    public ChannelExecuteComplete(EslEvent event) {
        super(event);
    }

    /**
     * Gets the application that has completed its execution.
     *
     * @return String value of the application that has completed its execution.
     */
    public String application() {
        return _event.eventHeaders().get("Application");
    }

    /**
     * Gets the application argument
     *
     * @return String value of the application argument.
     */
    public String applicationData() {
        return _event.eventHeaders().get("Application-Data");
    }

    /**
     * Gets the application response
     *
     * @return String value of the application response.
     */
    public String applicationResponse() {
        return _event.eventHeaders().get("Application-Response");
    }
}
