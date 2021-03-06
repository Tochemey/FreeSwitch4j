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
 * HangupCommand. It is used to hangup a live call with a specific reason.
 *
 * @author Arsene Tochemey GANDOTE
 */
public class HangupCommand extends BaseCommand {

    public HangupCommand(UUID channelId, String reason) {
        this._command = String.format(
                "sendmsg  %1$s\ncall-command: %2$s\nhangup-cause: %3$s", channelId,
                "hangup", reason);
    }

    @Override
    public String argument() {
        return "";
    }

    @Override
    public String command() {
        return this._command;
    }

}
