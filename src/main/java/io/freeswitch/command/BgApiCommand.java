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

import java.util.UUID;

/**
 * BgApiCommand.
 * It is used to Execute an API command in a thread different from
 * the main thread running freeSwitch. In other words it sends an api command
 * (non-blocking mode) this will let you execute a job in the background and the
 * result will be sent as an event with an indicated uuid to match the reply to
 * the command)
 *
 * @author Arsene Tochemey GANDOTE
 */
public class BgApiCommand extends BaseCommand {

    /**
     * The command Id. Each bgapi command can explicitly have an Id since bgapi
     * generates a UUID for each command executed on FreeSwitch
     */
    public UUID CommandId;

    public BgApiCommand(String command, String argument) {
        this._command = String.format("%s %s", command, argument);
    }

    @Override
    public String argument() {
        return this._command;
    }

    @Override
    public String command() {
        return "bgapi";
    }

}
