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
package io.freeswitch.command;

/**
 * DivertEventsCommand. The divert_events switch is available to allow event
 * that an embedded script would expect to get in the inputcallback to be
 * diverted to the event socket.
 *
 * @author Arsene Tochemey GANDOTE
 */
public class DivertEventsCommand extends BaseCommand {

    public DivertEventsCommand(boolean on) {
        if (on)
            this._command = "on";
        else
            this._command = "off";
    }

    @Override
    public String argument() {
        return this._command;
    }

    @Override
    public String command() {
        return "divert_events";
    }

}
