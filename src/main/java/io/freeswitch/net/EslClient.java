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

import io.freeswitch.IProtocolListener;
import io.freeswitch.commands.ApiCommand;
import io.freeswitch.commands.BgApiCommand;
import io.freeswitch.commands.EventCommand;
import io.freeswitch.commands.ExitCommand;
import io.freeswitch.commands.FilterCommand;
import io.freeswitch.commands.LogCommand;
import io.freeswitch.commands.NoEventsCommand;
import io.freeswitch.commands.NologCommand;
import io.freeswitch.commands.SendMsgCommand;
import io.freeswitch.common.LogLevels;
import io.freeswitch.events.EslEvent;
import io.freeswitch.events.IEventsListener;
import io.freeswitch.message.CommandReply;
import io.freeswitch.message.EslMessage;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to connect to a running FreeSWITCH Event Socket Library module,
 * as a client.
 * <p>
 * This class provides what the FreeSWITCH documentation refers to as an
 * 'Inbound' connection to the Event Socket module. That is, with reference to
 * the socket listening on the FreeSWITCH server, this client occurs as an
 * inbound connection to the server.
 * <p>
 * See <a href="http://wiki.freeswitch.org/wiki/Mod_event_socket">http://wiki.
 * freeswitch.org/wiki/Mod_event_socket</a>
 *
 * @author Arsene Tochemey GANDOTE
 *
 */
public class EslClient {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final List<IEventsListener> eventListeners = new CopyOnWriteArrayList<IEventsListener>();
    private final Executor eventListenerExecutor = Executors
            .newSingleThreadExecutor(new ThreadFactory() {
                AtomicInteger threadNumber = new AtomicInteger(1);

                public Thread newThread(Runnable r) {
                    return new Thread(r, "EslEventNotifier-"
                            + threadNumber.getAndIncrement());
                }
            });

    private final Executor backgroundJobListenerExecutor = Executors
            .newSingleThreadExecutor(new ThreadFactory() {
                AtomicInteger threadNumber = new AtomicInteger(1);

                public Thread newThread(Runnable r) {
                    return new Thread(r, "EslBackgroundJobNotifier-"
                            + threadNumber.getAndIncrement());
                }
            });

    private AtomicBoolean authenticatorResponded = new AtomicBoolean(false);
    private boolean authenticated;
    private CommandReply authenticationResponse;
    private Channel channel;

    /*
     * Internal observer of the ESL protocol
     */
    private final IProtocolListener protocolListener = new IProtocolListener() {
        public void authResponseReceived(CommandReply response) {
            authenticatorResponded.set(true);
            authenticated = response.isOk();
            authenticationResponse = response;
            log.debug("Auth response success={}, message=[{}]", authenticated,
                    response.replyText());
        }

        public void disconnected() {
            log.info("Disconnected ..");
        }

        public void eventReceived(final EslEvent event) {
            log.debug("Event received [{}]", event);
            /*
             * Notify listeners in a different thread in order to: - not to
             * block the IO threads with potentially long-running listeners -
             * generally be defensive running other people's code Use a
             * different worker thread pool for async job results than for event
             * driven events to keep the latency as low as possible.
             */
            if (event.eventName().equals("BACKGROUND_JOB")) {
                for (final IEventsListener listener : eventListeners) {
                    backgroundJobListenerExecutor.execute(new Runnable() {
                        public void run() {
                            try {
                                listener.backgroundJobEventReceived(event);
                            } catch (Throwable t) {
                                log.error(
                                        "Error caught notifying listener of job result ["
                                        + event + ']', t);
                            }
                        }
                    });
                }
            } else {
                for (final IEventsListener listener : eventListeners) {
                    eventListenerExecutor.execute(new Runnable() {
                        public void run() {
                            try {
                                listener.eventReceived(event);
                            } catch (Throwable t) {
                                log.error(
                                        "Error caught notifying listener of event ["
                                        + event + ']', t);
                            }
                        }
                    });
                }
            }
        }
    };

    public void addEventListener(IEventsListener listener) {
        if (listener != null) {
            eventListeners.add(listener);
        }
    }

    /**
     * Sends a FreeSWITCH API command to the server and blocks, waiting for an
     * immediate response from the server.
     * <p/>
     * The outcome of the command from the server is returned in an
     * {@link EslMessage} object.
     *
     * @param command API command to send
     * @param arg command arguments
     * @return an {@link EslMessage} containing command results
     */
    public EslMessage api(String command, String arg) {
        checkConnected();
        ClientHandler handler = (ClientHandler) channel.pipeline().last();

        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(command)) {
            sb.append(command);
        }
        if (!StringUtils.isEmpty(arg)) {
            sb.append(' ');
            sb.append(arg);
        }

        // Here no command is really set to be executed
        if (StringUtils.isEmpty(sb.toString())) {
            return null;
        }

        ApiCommand api = new ApiCommand(sb.toString());
        return handler.sendSyncSingleLineCommand(channel, api.toString());
    }

    /**
     * Submit a FreeSWITCH API command to the server to be executed in
     * background mode. A synchronous response from the server provides a UUID
     * to identify the job execution results. When the server has completed the
     * job execution it fires a BACKGROUND_JOB Event with the execution results.
     * <p/>
     * Note that this Client must be subscribed in the normal way to
     * BACKGOUND_JOB Events, in order to receive this event.
     *
     * @param command API command to send
     * @param arg command arguments
     * @return UUID Job-UUID that the server will tag result event with.
     */
    public UUID bgApi(String command, String arg) {
        checkConnected();
        ClientHandler handler = (ClientHandler) channel.pipeline().last();
        if (StringUtils.isEmpty(command)) {
            // Here no command is set to be executed.
            return null;
        }

        BgApiCommand bgapi = new BgApiCommand(command, arg);
        return UUID.fromString(handler.sendAsyncCommand(channel,
                bgapi.toString()));
    }

    public boolean canSend() {
        return channel != null && channel.isActive() && authenticated;
    }

    /**
     * Check whether the connection to the FreeSwitch mod_event_socket is alive
     * or not.
     */
    private void checkConnected() {
        if (!canSend()) {
            throw new IllegalStateException(
                    "Not connected to FreeSWITCH Event Socket");
        }
    }

    /**
     * Close the socket connection
     *
     * @return a {@link CommandResponse} with the server's response.
     */
    public CommandReply close() {
        checkConnected();
        ClientHandler handler = (ClientHandler) channel.pipeline().last();
        ExitCommand exit = new ExitCommand();
        EslMessage response = handler.sendSyncSingleLineCommand(channel,
                exit.toString());

        return new CommandReply(exit.toString(), response);
    }

    /**
     * Attempt to establish an authenticated connection to the nominated
     * FreeSWITCH ESL server socket. This call will block, waiting for an
     * authentication handshake to occur, or timeout after the supplied number
     * of seconds.
     *
     * @param host can be either ip address or hostname
     * @param port tcp port that server socket is listening on (set in
     * event_socket_conf.xml)
     * @param password server event socket is expecting (set in
     * event_socket_conf.xml)
     * @param timeoutSeconds number of seconds to wait for the server socket
     * before aborting
     * @throws io.freeswitch.net.ConnectionFailure
     * @throws java.lang.InterruptedException
     */
    public void connect(String host, int port, String password,
            int timeoutSeconds) throws ConnectionFailure, InterruptedException {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        // If already connected, disconnect first
        if (canSend()) {
            close();
        }

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

        // Add ESL handler
        ClientHandler handler = new ClientHandler(password, protocolListener);
        bootstrap.handler(new EslClientInitializer(handler));

        // Make the connection attempt.
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                1000 * timeoutSeconds);
        bootstrap.remoteAddress(host, port);
        ChannelFuture future = bootstrap.connect();
        future.awaitUninterruptibly();

        // Now we are sure the future is completed.
        assert future.isDone();

        // But may have failed anyway
        if (!future.isSuccess()) {
            log.warn("Failed to connect to [{}:{}]", host, port);
            channel = null;
            workerGroup.shutdownGracefully().awaitUninterruptibly();
            workerGroup.terminationFuture().awaitUninterruptibly();
            throw new ConnectionFailure("Could not connect to " + host + ":"
                    + port, future.cause());
        }

        channel = future.channel();
        // Wait for the authentication handshake to call back
        while (!authenticatorResponded.get()) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                // ignore
            }
        }

        if (!authenticated) {
            throw new ConnectionFailure("Authentication failed: "
                    + authenticationResponse.replyText());
        }
    }

    /**
     * Set the current event subscription for this connection to the server.
     * Examples of the events argument are:
     *
     * <pre>
     *   ALL
     *   CHANNEL_CREATE CHANNEL_DESTROY HEARTBEAT
     *   CUSTOM conference::maintenance
     *   CHANNEL_CREATE CHANNEL_DESTROY CUSTOM conference::maintenance sofia::register sofia::expire
     * </pre>
     *
     * Subsequent calls to this method replaces any previous subscriptions that
     * were set. </p> Note: current implementation can only process 'plain'
     * events.
     *
     * @param format can be { plain | xml }
     * @param events { all | space separated list of events }
     * @return a {@link CommandReply} with the server's response.
     */
    public CommandReply event(String format, String events) {
        // temporary hack
        if (!format.equals("plain")) {
            throw new IllegalStateException(
                    "Only 'plain' event format is supported at present");
        }

        checkConnected();
        ClientHandler handler = (ClientHandler) channel.pipeline().last();
        StringBuilder sb = new StringBuilder();
        if (format != null && !format.isEmpty()) {
            sb.append(format);
        }
        if (events != null && !events.isEmpty()) {
            sb.append(' ');
            sb.append(events);
        }

        // Here no command is really set to be executed
        if (StringUtils.isEmpty(sb.toString())) {
            return null;
        }
        EventCommand event = new EventCommand(sb.toString());
        EslMessage response = handler.sendSyncSingleLineCommand(channel,
                event.toString());
        return new CommandReply(event.toString(), response);
    }

    /**
     * Send a {@link SendMsg} command to FreeSWITCH. This client requires that
     * the {@link SendMsg} has a call UUID parameter.
     *
     * @param sendMsg a {@link SendMsg} with call UUID
     * @return a {@link CommandReply} with the server's response.
     */
    public CommandReply execute(SendMsgCommand sendMsg) {
        checkConnected();
        ClientHandler handler = (ClientHandler) channel.pipeline().last();
        EslMessage response = handler.sendSyncSingleLineCommand(channel,
                sendMsg.toString());

        return new CommandReply(sendMsg.toString(), response);
    }

    /**
     * Add an event filter to the current set of event filters on this
     * connection. Any of the event headers can be used as a filter. </p> Note
     * that event filters follow 'filter-in' semantics. That is, when a filter
     * is applied only the filtered values will be received. Multiple filters
     * can be added to the current connection. </p> Example filters:
     *
     * <pre>
     *    eventHeader        valueToFilter
     *    ----------------------------------
     *    Event-Name         CHANNEL_EXECUTE
     *    Channel-State      CS_NEW
     * </pre>
     *
     * @param eventHeader to filter on
     * @param valueToFilter the value to match
     * @return a {@link CommandReply} with the server's response.
     */
    public CommandReply filter(String eventHeader, String valueToFilter) {
        checkConnected();
        ClientHandler handler = (ClientHandler) channel.pipeline().last();
        StringBuilder sb = new StringBuilder();
        if (eventHeader != null && !eventHeader.isEmpty()) {
            sb.append(eventHeader);
        }
        if (valueToFilter != null && !valueToFilter.isEmpty()) {
            sb.append(' ');
            sb.append(valueToFilter);
        }

        if (StringUtils.isEmpty(sb.toString())) {
            return null;
        }

        FilterCommand filter = new FilterCommand(sb.toString());
        EslMessage response = handler.sendSyncSingleLineCommand(channel,
                filter.toString());
        return new CommandReply(filter.toString(), response);
    }

    /**
     * Delete an event filter from the current set of event filters on this
     * connection. See {@link EslClient.addEventFilter}
     *
     * @param eventHeader to remove
     * @param valueToFilter to remove
     * @return a {@link CommandReply} with the server's response.
     */
    public CommandReply filterDelete(String eventHeader, String valueToFilter) {
        checkConnected();
        ClientHandler handler = (ClientHandler) channel.pipeline().last();
        StringBuilder sb = new StringBuilder();
        if (eventHeader != null && !eventHeader.isEmpty()) {
            sb.append("delete ");
            sb.append(eventHeader);
        }
        if (valueToFilter != null && !valueToFilter.isEmpty()) {
            sb.append(' ');
            sb.append(valueToFilter);
        }
        if (StringUtils.isEmpty(sb.toString())) {
            return null;
        }

        FilterCommand filter = new FilterCommand(sb.toString());
        EslMessage response = handler.sendSyncSingleLineCommand(channel,
                filter.toString());
        return new CommandReply(filter.toString(), response);
    }

    /**
     * Cancel any existing event subscription.
     *
     * @return a {@link CommandReply} with the server's response.
     */
    public CommandReply noevents() {
        checkConnected();
        ClientHandler handler = (ClientHandler) channel.pipeline().last();
        NoEventsCommand noevents = new NoEventsCommand();
        EslMessage response = handler.sendSyncSingleLineCommand(channel,
                noevents.toString());
        return new CommandReply(noevents.toString(), response);
    }

    /**
     * Disable any logging previously enabled with setLogLevel().
     *
     * @return a {@link CommandReply} with the server's response.
     */
    public CommandReply nolog() {
        checkConnected();
        ClientHandler handler = (ClientHandler) channel.pipeline().last();
        EslMessage response = handler.sendSyncSingleLineCommand(channel,
                new NologCommand().toString());

        return new CommandReply("nolog", response);
    }

    /**
     * Enable log output.
     *
     * @param level using the same values as in console.conf
     * @return a {@link CommandReply} with the server's response.
     */
    public CommandReply setLogLevel(LogLevels level) {
        checkConnected();
        ClientHandler handler = (ClientHandler) channel.pipeline().last();
        LogCommand log = new LogCommand(level);
        EslMessage response = handler.sendSyncSingleLineCommand(channel,
                log.toString());

        return new CommandReply(log.toString(), response);
    }

}
