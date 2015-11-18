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
 * GetVarCommand.
 * It is used to retrieve a channel variable based upon the channel id
 *
 * @author Arsene Tochemey GANDOTE
 */
public class GetVarCommand extends BaseCommand {

    public GetVarCommand(UUID channelId, String variable) {
        this._command = String.format("%s %s", channelId, variable);
    }

    @Override
    public String argument() {
        return this._command;
    }

    @Override
    public String command() {
        return "uuid_getvar";
    }

}
