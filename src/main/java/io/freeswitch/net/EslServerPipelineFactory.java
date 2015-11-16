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

import io.freeswitch.codecs.FreeSwitchDecoder;

import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

/**
 * @author Arsene Tochemey GANDOTE
 *
 */
public abstract class EslServerPipelineFactory implements ChannelPipelineFactory {

    public org.jboss.netty.channel.ChannelPipeline getPipeline() throws Exception {
        org.jboss.netty.channel.ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("encoder", new org.jboss.netty.handler.codec.string.StringEncoder());
        pipeline.addLast("decoder", new FreeSwitchDecoder(8192, true));
        // Add an executor to ensure separate thread for each upstream message from here
        pipeline.addLast("executor", new ExecutionHandler(
                new OrderedMemoryAwareThreadPoolExecutor(16, 1048576, 1048576)));

        // now the inbound client logic
        pipeline.addLast("clientHandler", buildHandler());

        return pipeline;
    }

    protected abstract AbstractEslServerHandler buildHandler();
}
