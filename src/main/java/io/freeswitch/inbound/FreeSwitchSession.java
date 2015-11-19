package io.freeswitch.inbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Arsene Tochemey GANDOTE on 19/11/2015.
 */
public class FreeSwitchSession {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Unique Identifier of the connected freeSwitch. It is the same as the Unique-ID of the connected call.
     */
    private final UUID _sessionId;

    /**
     * The caller id. Value of the channel variable Caller-Unique-ID
     */
    private final UUID _callerId;

    /**
     * The caller id number. Value of the channel variable Caller-Caller-ID-Number
     */
    private final String _callerIdNumber;

    /**
     * The destination number. Value of the channel variable Channel-Destination-Number
     */
    private final String _destinationNumber;

    /**
     * Other channel variables
     */
    private final Map<String, String> _channelVariables;

    /**
     * Connection details
     */
    private final SocketAddress _address;

    public FreeSwitchSession(UUID sessionId, UUID callerId, String callerIdNumber, String destinationNumber, Map<String, String> variables, SocketAddress address) {
        _sessionId = sessionId;
        _callerId = callerId;
        _callerIdNumber = callerIdNumber;
        _destinationNumber = destinationNumber;
        _channelVariables = variables;
        _address = address;
    }

    public UUID sessionId() {
        return _sessionId;
    }

    public UUID callerId() {
        return _callerId;
    }

    public String callerIdNumber() {
        return _callerIdNumber;
    }

    public String destinationNumber() {
        return _destinationNumber;
    }

    public Map<String, String> channelVariables() {
        return _channelVariables;
    }

    public SocketAddress address() {
        return _address;
    }
}
