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
package io.freeswitch.outbound;

import io.freeswitch.IProtocolListener;
import io.freeswitch.codec.FreeSwitchMessageHeaders.HeaderValue;
import io.freeswitch.command.AuthCommand;
import io.freeswitch.event.EslEvent;
import io.freeswitch.message.CommandReply;
import io.freeswitch.message.FreeSwitchMessage;
import org.jboss.netty.channel.ChannelHandlerContext;

/**
 * @author Arsene Tochemey GANDOTE
 */
public class DefaultFreeSwitchClientHandler extends FreeSwitchClientHandler {

    private final String password;
    private final IProtocolListener listener;

    public DefaultFreeSwitchClientHandler(String password, IProtocolListener listener) {
        this.password = password;
        this.listener = listener;
    }

    @Override
    protected void handleEslEvent(ChannelHandlerContext ctx, EslEvent event) {
        log.debug("Received event: [{}]", event);
        listener.eventReceived(event);
    }

    @Override
    protected void handleAuthRequest(ChannelHandlerContext ctx) {
        log.debug("Auth requested, sending [auth {}]", "*****");
        AuthCommand auth = new AuthCommand(password);
        FreeSwitchMessage response = sendSyncCommand(ctx.getChannel(), auth.toString());
        log.debug("Auth response [{}]", response);
        if (response.contentType().equals(HeaderValue.COMMAND_REPLY)) {
            CommandReply commandResponse = new CommandReply("auth " + password,
                    response);
            listener.authResponseReceived(commandResponse);
        } else {
            log.error("Bad auth response message [{}]", response);
            throw new IllegalStateException("Incorrect auth response");
        }
    }

    @Override
    protected void handleDisconnectionNotice() {
        log.debug("Received disconnection notice");
        listener.disconnected();
    }

}
