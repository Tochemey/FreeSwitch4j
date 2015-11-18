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
package io.freeswitch.message;

import io.freeswitch.codec.FreeSwitchMessageHeaders;

/**
 * @author Arsene Tochemey GANDOTE
 */
public class CommandReply {
    private final String _command;
    private final String _replyText;
    private final FreeSwitchMessage _response;
    private final boolean _success;

    public CommandReply(String command, FreeSwitchMessage response) {
        this._command = command;
        this._response = response;
        this._replyText = response.headerValue(FreeSwitchMessageHeaders.HeaderName.REPLY_TEXT);
        this._success = _replyText.startsWith("+OK");
    }

    /**
     * @return the original command sent to the server
     */
    public String command() {
        return _command;
    }

    /**
     * @return true if and only if the response Reply-Text line starts with
     * "+OK"
     */
    public boolean isOk() {
        return _success;
    }

    /**
     * @return the full response Reply-Text line.
     */
    public String replyText() {
        return _replyText;
    }

    /**
     * @return {@link FreeSwitchMessage} the full response from the server
     */
    public FreeSwitchMessage response() {
        return _response;
    }

}
