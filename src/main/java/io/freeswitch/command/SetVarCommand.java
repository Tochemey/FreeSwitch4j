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
 * SetVar.
 * It is used to set channel variable
 *
 * @author Arsene Tochemey GANDOTE
 */
public class SetVarCommand extends BaseCommand {

    /**
     * channel Id
     */
    private UUID _channelId;

    /**
     * variable name
     */
    private String _name;

    /**
     * variable value
     */
    private String _value;

    public SetVarCommand(UUID channelId, String var, String val) {
        _channelId = channelId;
        _name = var;
        _value = val;
    }

    @Override
    public String argument() {
        return String.format("%1s %2s %3s", _channelId.toString(), _name,
                _value);
    }

    @Override
    public String command() {
        return "uuid_setvar";
    }

}
