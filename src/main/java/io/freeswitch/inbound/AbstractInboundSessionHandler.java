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
package io.freeswitch.inbound;

import io.freeswitch.codec.FreeSwitchMessageHeaders.HeaderName;
import io.freeswitch.codec.FreeSwitchMessageHeaders.HeaderValue;
import io.freeswitch.command.ConnectCommand;
import io.freeswitch.event.EslEvent;
import io.freeswitch.message.FreeSwitchMessage;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Arsene Tochemey GANDOTE
 */
public abstract class AbstractInboundSessionHandler extends SimpleChannelUpstreamHandler {

    public static final String MESSAGE_TERMINATOR = "\n\n";
    public static final String LINE_TERMINATOR = "\n";
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    private final Lock syncLock = new ReentrantLock();
    private final Queue<SyncCallback> syncCallbacks = new ConcurrentLinkedQueue<SyncCallback>();

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {

        Channel channel = ctx.getChannel();
        // Have received a connection from FreeSWITCH server, send connect
        // response
        log.debug(
                "Received new connection from server [{}], sending connect message",
                channel.getLocalAddress().toString());
        ConnectCommand connect = new ConnectCommand();
        FreeSwitchMessage response = sendSyncSingleLineCommand(ctx.getChannel(),
                connect.toString());
        // The message decoder for outbound, treats most of this incoming
        // message as an 'event' in
        // message body, so it parse now
        EslEvent channelDataEvent = new EslEvent(response, true);
        // Let implementing sub classes choose what to do next
        handleConnectResponse(ctx, channelDataEvent);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
        if (e.getMessage() instanceof FreeSwitchMessage) {
            FreeSwitchMessage message = (FreeSwitchMessage) e.getMessage();
            String contentType = message.contentType();
            if (contentType.equals(HeaderValue.TEXT_EVENT_PLAIN)
                    || contentType.equals(HeaderValue.TEXT_EVENT_XML)) {
                // transform into an event
                EslEvent eslEvent = new EslEvent(message);
                handleEslEvent(ctx, eslEvent);
            } else {
                handleEslMessage(ctx, (FreeSwitchMessage) e.getMessage());
            }
            return;
        }
        throw new IllegalStateException("Unexpected message type: "
                + e.getMessage().getClass());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        handleException(ctx, e.getCause());
    }

    protected abstract void handleConnectResponse(ChannelHandlerContext ctx,
                                                  EslEvent event);

    protected abstract void handleDisconnectionNotice();

    protected abstract void handleEslEvent(ChannelHandlerContext ctx,
                                           EslEvent event);

    protected void handleEslMessage(ChannelHandlerContext ctx,
                                    FreeSwitchMessage message) {
        log.info("Received message: [{}]", message);
        String contentType = message.contentType();

        if (contentType.equals(HeaderValue.API_RESPONSE)) {
            log.debug("Api response received [{}]", message);
            syncCallbacks.poll().handle(message);
        } else if (contentType.equals(HeaderValue.COMMAND_REPLY)) {
            log.debug("Command reply received [{}]", message);
            syncCallbacks.poll().handle(message);
        } else if (contentType.equals(HeaderValue.TEXT_DISCONNECT_NOTICE)) {
            log.debug("Disconnect notice received [{}]", message);
            handleDisconnectionNotice();
        } else {
            log.warn("Unexpected message content type [{}]", contentType);
        }
    }

    protected abstract void handleException(ChannelHandlerContext ctx,
                                            Throwable cause);

    /**
     * Returns the Job UUID of that the response event will have.
     *
     * @param channel
     * @param command
     * @return Job-UUID as a string
     */
    public String sendAsyncCommand(Channel channel, final String command) {
        /*
         * Send synchronously to get the Job-UUID to return, the results of the
         * actual job request will be returned by the server as an async event.
         */
        FreeSwitchMessage response = sendSyncSingleLineCommand(channel, command);
        if (response.hasHeader(HeaderName.JOB_UUID)) {
            return response.headerValue(HeaderName.JOB_UUID);
        } else {
            throw new IllegalStateException(
                    "Missing Job-UUID header in bgapi response");
        }
    }

    /**
     * synthesize a synchronous command/response by creating a callback object
     * which is placed in queue and blocks waiting for another IO thread to
     * process an incoming {@link FreeSwitchMessage} and attach it to the callback.
     *
     * @param channel
     * @param command List of command lines to send
     * @return the {@link FreeSwitchMessage} attached to this command's callback
     */
    public FreeSwitchMessage sendSyncMultiLineCommand(Channel channel,
                                                      final List<String> commandLines) {
        SyncCallback callback = new SyncCallback();
        // Build command with double line terminator at the end
        StringBuilder sb = new StringBuilder();
        for (String line : commandLines) {
            sb.append(line);
            sb.append(LINE_TERMINATOR);
        }
        sb.append(LINE_TERMINATOR);

        syncLock.lock();
        try {
            syncCallbacks.add(callback);
            channel.write(sb.toString());
        } finally {
            syncLock.unlock();
        }

        // Block until the response is available
        return callback.get();
    }

    /**
     * Synthesise a synchronous command/response by creating a callback object
     * which is placed in queue and blocks waiting for another IO thread to
     * process an incoming {@link FreeSwitchMessage} and attach it to the callback.
     *
     * @param channel
     * @param command single string to send
     * @return the {@link FreeSwitchMessage} attached to this command's callback
     */
    public FreeSwitchMessage sendSyncSingleLineCommand(Channel channel,
                                                       final String command) {
        SyncCallback callback = new SyncCallback();
        syncLock.lock();
        try {
            syncCallbacks.add(callback);
            channel.write(command + MESSAGE_TERMINATOR);
        } finally {
            syncLock.unlock();
        }

        // Block until the response is available
        return callback.get();
    }

    private static class SyncCallback {

        private static final Logger log = LoggerFactory
                .getLogger(SyncCallback.class);
        private final CountDownLatch latch = new CountDownLatch(1);
        private FreeSwitchMessage response;

        /**
         * Block waiting for the countdown latch to be released, then return the
         * associated response object.
         *
         * @return
         */
        FreeSwitchMessage get() {
            try {
                log.trace("awaiting latch ... ");
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            log.trace("returning response [{}]", response);
            return response;
        }

        /**
         * Attach this response to the callback and release the countdown latch.
         *
         * @param response
         */
        void handle(FreeSwitchMessage response) {
            this.response = response;
            log.trace("releasing latch for response [{}]", response);
            latch.countDown();
        }
    }

}
