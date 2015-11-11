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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point to run a socket client that a running FreeSWITCH Event Socket
 * Library module can make outbound connections to.
 * <p>
 * This class provides for what the FreeSWITCH documentation refers to as
 * 'Outbound' connections from the Event Socket module. That is, with reference
 * to the module running on the FreeSWITCH server, this client accepts an
 * outbound connection from the server module.
 * <p>
 * See <a href="http://wiki.freeswitch.org/wiki/Mod_event_socket">http://wiki.
 * freeswitch.org/wiki/Mod_event_socket</a>
 * 
 * @author Arsene Tochemey GANDOTE
 *
 */
public class EslServer {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Binding port.
	 */
	private final int port;

	private final int backlog;

	private final ServerBootstrap bootstrap = new ServerBootstrap();

	private final EventLoopGroup bossGroup;
	private final EventLoopGroup workerGroup;

	private ChannelFuture future;

	/**
	 * 
	 */
	public EslServer(int port, int backlog,
			EslServerInitializer socketClientInitializer) {
		this.port = port;
		this.backlog = backlog;
		this.bossGroup = new NioEventLoopGroup();
		this.workerGroup = new NioEventLoopGroup();
		bootstrap.group(bossGroup, workerGroup);
		bootstrap.channel(NioServerSocketChannel.class);
		bootstrap.option(ChannelOption.SO_BACKLOG, this.backlog);
		bootstrap.childOption(ChannelOption.SO_LINGER, 1);
		bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
		bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.handler(new LoggingHandler(LogLevel.INFO));
		bootstrap.childHandler(socketClientInitializer);
	}

	/**
	 * start()
	 * 
	 * @throws InterruptedException
	 */
	public void start() throws InterruptedException {

		future = this.bootstrap.bind(new InetSocketAddress(port)).sync();
		log.info("SocketClient waiting for connections on port [{}] ...", port);
	}

	/**
	 * stop()
	 * @throws InterruptedException
	 */
	public void stop() throws InterruptedException {
		// Wait until the server socket is closed.
		future.channel().closeFuture().sync();
		// Shut down all event loops to terminate all threads.
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();

		// Wait until all threads are terminated.
		bossGroup.terminationFuture().sync();
		workerGroup.terminationFuture().sync();
	}
}
