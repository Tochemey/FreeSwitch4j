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
package io.freeswitch.net;

import io.freeswitch.events.EslEvent;
import io.freeswitch.message.EslHeaders.Name;
import io.freeswitch.message.EslHeaders.Value;
import io.freeswitch.message.EslMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Arsene Tochemey GANDOTE
 * @author david varnes
 */
public abstract class AbstractEslClientHandler extends
        SimpleChannelInboundHandler<Object> {

    public static final String MESSAGE_TERMINATOR = "\n\n";
    public static final String LINE_TERMINATOR = "\n";

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Lock syncLock = new ReentrantLock();
    private final Queue<SyncCallback> syncCallbacks = new ConcurrentLinkedQueue<SyncCallback>();

    /**
     *
     */
    public AbstractEslClientHandler() {
    }

    private static class SyncCallback {

        private static final Logger log = LoggerFactory
                .getLogger(SyncCallback.class);
        private final CountDownLatch latch = new CountDownLatch(1);
        private EslMessage response;

        /**
         * Block waiting for the countdown latch to be released, then return the
         * associated response object.
         *
         * @return
         */
        EslMessage get() {
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
        void handle(EslMessage response) {
            this.response = response;
            log.trace("releasing latch for response [{}]", response);
            latch.countDown();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        if (msg instanceof EslMessage) {
            EslMessage message = (EslMessage) msg;
            String contentType = message.contentType();
            if (contentType.equals(Value.TEXT_EVENT_PLAIN)
                    || contentType.equals(Value.TEXT_EVENT_XML)) {
                // transform into an event
                EslEvent eslEvent = new EslEvent(message);
                handleEslEvent(ctx, eslEvent);
            } else {
                handleEslMessage(ctx, (EslMessage) msg);
            }
            return;
        }
        throw new IllegalStateException("Unexpected message type: "
                + msg.getClass());
    }

    protected abstract void handleEslEvent(ChannelHandlerContext ctx,
            EslEvent event);

    protected abstract void handleAuthRequest(ChannelHandlerContext ctx);

    protected abstract void handleDisconnectionNotice();

    protected void handleEslMessage(ChannelHandlerContext ctx,
            EslMessage message) {
        log.info("Received message: [{}]", message);
        String contentType = message.contentType();

        if (contentType.equals(Value.API_RESPONSE)) {
            log.debug("Api response received [{}]", message);
            syncCallbacks.poll().handle(message);
        } else if (contentType.equals(Value.COMMAND_REPLY)) {
            log.debug("Command reply received [{}]", message);
            syncCallbacks.poll().handle(message);
        } else if (contentType.equals(Value.AUTH_REQUEST)) {
            log.debug("Auth request received [{}]", message);
            handleAuthRequest(ctx);
        } else if (contentType.equals(Value.TEXT_DISCONNECT_NOTICE)) {
            log.debug("Disconnect notice received [{}]", message);
            handleDisconnectionNotice();
        } else {
            log.warn("Unexpected message content type [{}]", contentType);
        }
    }

    /**
     * Synthesise a synchronous command/response by creating a callback object
     * which is placed in queue and blocks waiting for another IO thread to
     * process an incoming {@link EslMessage} and attach it to the callback.
     *
     * @param channel
     * @param command single string to send
     * @return the {@link EslMessage} attached to this command's callback
     */
    public EslMessage sendSyncSingleLineCommand(Channel channel,
            final String command) {
        SyncCallback callback = new SyncCallback();
        syncLock.lock();
        try {
            syncCallbacks.add(callback);
            String request = command + MESSAGE_TERMINATOR;
            log.debug("Command sent to freeSwitch [{}]", request);
            ChannelFuture future = channel.writeAndFlush(request);
            future.awaitUninterruptibly();
            // Now we are sure the future is completed.
            assert future.isDone();

            if (!future.isSuccess()) {
                log.warn("Error [{}]", future.cause());
            }
        } finally {
            syncLock.unlock();
        }

        // Block until the response is available
        return callback.get();
    }

    /**
     * Synthesise a synchronous command/response by creating a callback object
     * which is placed in queue and blocks waiting for another IO thread to
     * process an incoming {@link EslMessage} and attach it to the callback.
     *
     * @param channel
     * @param command List of command lines to send
     * @return the {@link EslMessage} attached to this command's callback
     */
    public EslMessage sendSyncMultiLineCommand(Channel channel,
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
            channel.writeAndFlush(sb.toString());
        } finally {
            syncLock.unlock();
        }

        // Block until the response is available
        return callback.get();
    }

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
        EslMessage response = sendSyncSingleLineCommand(channel, command);
        if (response.hasHeader(Name.JOB_UUID)) {
            return response.headerValue(Name.JOB_UUID);
        } else {
            throw new IllegalStateException(
                    "Missing Job-UUID header in bgapi response");
        }
    }

}
