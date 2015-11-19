package io.freeswitch.inbound;

import io.freeswitch.event.EslEvent;
import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.UUID;

/**
 * Created by Arsene Tochemey GANDOTE on 19/11/2015.
 */
public class DefaultFreeSwitchHandler extends FreeSwitchHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * instance of the connected freeSwitch
     */
    private FreeSwitchSession _session;

    @Override
    protected void handleConnectResponse(ChannelHandlerContext ctx, EslEvent event) {
        if(StringUtils.equalsIgnoreCase(event.eventName(), "CHANNEL_DATA")){
            // Let us get the necessary data to set the session
            SocketAddress address = ctx.getChannel().getRemoteAddress();
            UUID sessionId = event.uniqueId();
            UUID callerId = UUID.fromString(event.eventHeaders().get("Caller-Unique-ID"));
            String callerIdNumber = event.eventHeaders().get("Caller-Caller-ID-Number");
            String destinationNumber = event.eventHeaders().get("Channel-Destination-Number");
            _session = new FreeSwitchSession(sessionId, callerId, callerIdNumber, destinationNumber, event.eventHeaders(), address);
            return;
        }
        throw new IllegalStateException( "Unexpected event after connect: [" + event.eventName() + ']' );
    }

    @Override
    protected void handleDisconnectionNotice(ChannelHandlerContext ctx) {

    }

    @Override
    protected void handleEslEvent(ChannelHandlerContext ctx, EslEvent event) {
        // // TODO: 19/11/2015 implements the events handlers
    }

    @Override
    protected void handleException(ChannelHandlerContext ctx, Throwable cause) {
        // // TODO: 19/11/2015 implements the exception handling
    }
}
