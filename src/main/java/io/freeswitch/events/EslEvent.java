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
package io.freeswitch.events;

import io.freeswitch.codecs.HeaderParser;
import io.freeswitch.message.EslHeaders;
import io.freeswitch.message.EslHeaders.Name;
import io.freeswitch.message.EslHeaders.Value;
import io.freeswitch.message.EslMessage;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FreeSWITCH Event Socket <strong>events</strong> are decoded into this data
 * object.
 * <p>
 * An ESL event is modelled as a collection of text lines. An event always has
 * several eventHeader lines, and optionally may have some eventBody lines. In
 * addition the messageHeaders of the original containing {@link EslMessage}
 * which carried the event are also available.
 * <p>
 * The eventHeader lines are parsed and cached in a map keyed by the eventHeader
 * name string. An event is always expected to have an "Event-Name" eventHeader.
 * Commonly used eventHeader names are coded in {@link EslEventHeaderNames}
 * <p>
 * Any eventBody lines are cached in a list.
 * <p>
 * The messageHeader lines from the original message are cached in a map keyed
 * by {@link EslHeaders.Name}.
 * 
 * @author david varnes
 * @author Arsene Tochemey GANDOTE
 * @see EventHeaders
 */
public class EslEvent {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final Map<Name, String> messageHeaders;
	private final Map<String, String> eventHeaders;
	private final List<String> eventBody;
	private boolean decodeEventHeaders = true;

	public EslEvent(EslMessage rawMessage) {
		this(rawMessage, false);
	}

	public EslEvent(EslMessage rawMessage, boolean parseCommandReply) {
		messageHeaders = rawMessage.headers();
		eventHeaders = new HashMap<String, String>(rawMessage.bodyLines()
				.size());
		eventBody = new ArrayList<String>();
		// plain or xml body
		if (rawMessage.contentType().equals(Value.TEXT_EVENT_PLAIN)) {
			parsePlainBody(rawMessage.bodyLines());
		} else if (rawMessage.contentType().equals(Value.TEXT_EVENT_XML)) {
			throw new IllegalStateException("XML events are not yet supported");
		} else if (rawMessage.contentType().equals(Value.COMMAND_REPLY)
				&& parseCommandReply) {
			parsePlainBody(rawMessage.bodyLines());
		} else {
			throw new IllegalStateException("Unexpected EVENT content-type: "
					+ rawMessage.contentType());
		}
	}

	/**
	 * The message headers of the original ESL message from which this event was
	 * decoded. The message headers are stored in a map keyed by
	 * {@link EslHeaders.Name}. The string mapped value is the parsed content of
	 * the header line (ie, it does not include the header name).
	 * 
	 * @return map of header values
	 */
	public Map<Name, String> headers() {
		return messageHeaders;
	}

	/**
	 * The event headers of this event. The headers are parsed and stored in a
	 * map keyed by the string name of the header, and the string mapped value
	 * is the parsed content of the event header line (ie, it does not include
	 * the header name).
	 * 
	 * @return map of event header values
	 */
	public Map<String, String> eventHeaders() {
		return eventHeaders;
	}

	/**
	 * Any event body lines that were present in the event.
	 * 
	 * @return list of decoded event body lines, may be an empty list.
	 */
	public List<String> eventBodyLines() {
		return eventBody;
	}

	/**
	 * Convenience method.
	 * 
	 * @return the string value of the event header "Event-Name"
	 */
	public String eventName() {
		return eventHeaders().get(EventHeaders.EVENT_NAME);
	}

	/**
	 * Convenience method.
	 * 
	 * @return long value of the event header "Event-Date-Timestamp"
	 */
	public long eventDateTimestamp() {
		return Long
				.valueOf(eventHeaders().get(EventHeaders.EVENT_DATE_TIMESTAMP));
	}

	/**
	 * Convenience method.
	 * 
	 * @return long value of the event header "Event-Date-Local"
	 */
	public String eventDateLocal() {
		return eventHeaders().get(EventHeaders.EVENT_DATE_LOCAL);
	}

	/**
	 * Convenience method.
	 * 
	 * @return long value of the event header "Event-Date-GMT"
	 */
	public String eventDateGmt() {
		return eventHeaders().get(EventHeaders.EVENT_DATE_GMT);
	}

	/**
	 * Convenience method.
	 * 
	 * @return UUID value of the event header "Unique-ID"
	 */
	public UUID uniqueId() {
		String uniqueId = eventHeaders().get(EventHeaders.UNIQUE_ID);
		return UUID.fromString(uniqueId);
	}

	/**
	 * Convenience method.
	 * 
	 * @return true if the eventBody list is not empty.
	 */
	public boolean hasEventBody() {
		return !eventBody.isEmpty();
	}

	private void parsePlainBody(final List<String> rawBodyLines) {
		boolean isEventBody = false;
		for (String rawLine : rawBodyLines) {
			if (!isEventBody) {
				// split the line
				String[] headerParts = HeaderParser.splitHeader(rawLine);
				if (decodeEventHeaders) {
					try {
						String decodedValue = URLDecoder.decode(headerParts[1],
								"UTF-8");
						log.trace("decoded from: [{}]", headerParts[1]);
						log.trace("decoded   to: [{}]", decodedValue);
						eventHeaders.put(headerParts[0], decodedValue);
					} catch (UnsupportedEncodingException e) {
						log.warn("Could not URL decode [{}]", headerParts[1]);
						eventHeaders.put(headerParts[0], headerParts[1]);
					}
				} else {
					eventHeaders.put(headerParts[0], headerParts[1]);
				}
				if (headerParts[0].equals(EventHeaders.CONTENT_LENGTH)) {
					// the remaining lines will be considered body lines
					isEventBody = true;
				}
			} else {
				// ignore blank line (always is one following the content-length
				if (rawLine.length() > 0) {
					eventBody.add(rawLine);
				}
			}
		}

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("EslEvent: name=[");
		sb.append(eventName());
		sb.append("] headers=");
		sb.append(messageHeaders.size());
		sb.append(", eventHeaders=");
		sb.append(eventHeaders.size());
		sb.append(", eventBody=");
		sb.append(eventBody.size());
		sb.append(" lines.");

		return sb.toString();
	}

}
