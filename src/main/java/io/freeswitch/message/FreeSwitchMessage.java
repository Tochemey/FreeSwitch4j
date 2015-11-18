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
import io.freeswitch.codec.FreeSwitchMessageHeaders.HeaderName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Arsene Tochemey GANDOTE
 * @author david varnes
 */
public class FreeSwitchMessage {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final Map<HeaderName, String> headers = new HashMap<FreeSwitchMessageHeaders.HeaderName, String>();
    private final List<String> body = new ArrayList<String>();

    private Integer contentLength = null;

    /**
     * All the received message headers in a map keyed by
     * {@link FreeSwitchMessageHeaders.HeaderName}. The string mapped value is the parsed content of
     * the header line (ie, it does not include the header name).
     *
     * @return map of header values
     */
    public Map<FreeSwitchMessageHeaders.HeaderName, String> headers() {
        return headers;
    }

    /**
     * Convenience method
     *
     * @param headerName as a {@link FreeSwitchMessageHeaders.HeaderName}
     * @return true if an only if there is a header entry with the supplied
     * header name
     */
    public boolean hasHeader(FreeSwitchMessageHeaders.HeaderName headerName) {
        return headers.containsKey(headerName);
    }

    /**
     * Convenience method
     *
     * @param headerName as a {@link HeaderName}
     * @return same as getHeaders().get( headerName )
     */
    public String headerValue(FreeSwitchMessageHeaders.HeaderName headerName) {
        return headers.get(headerName);
    }

    /**
     * Convenience method
     *
     * @return true if and only if a header exists with name "Content-Length"
     */
    public boolean hasContentLength() {
        return headers.containsKey(FreeSwitchMessageHeaders.HeaderName.CONTENT_LENGTH);
    }

    /**
     * Convenience method
     *
     * @return integer value of header with name "Content-Length"
     */
    public Integer contentLength() {
        if (contentLength != null) {
            return contentLength;
        }
        if (hasContentLength()) {
            contentLength = Integer.valueOf(headers.get(FreeSwitchMessageHeaders.HeaderName.CONTENT_LENGTH));
        }
        return contentLength;
    }

    /**
     * Convenience method
     *
     * @return header value of header with name "Content-Type"
     */
    public String contentType() {
        return headers.get(FreeSwitchMessageHeaders.HeaderName.CONTENT_TYPE);
    }

    /**
     * Any received message body lines
     *
     * @return list with a string for each line received, may be an empty list
     */
    public List<String> bodyLines() {
        return body;
    }

    /**
     * Used by the {@link FreeSwitchDecoder}.
     *
     * @param name
     * @param value
     */
    public void addHeader(FreeSwitchMessageHeaders.HeaderName name, String value) {
        log.debug("adding header [{}] [{}]", name, value);
        headers.put(name, value);
    }

    /**
     * Used by the {@link FreeSwitchDecoder}
     *
     * @param line
     */
    public void addBodyLine(String line) {
        if (line == null) {
            return;
        }
        body.add(line);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("FreeSwitchMessage: contentType=[");
        sb.append(contentType());
        sb.append("] headers=");
        sb.append(headers.size());
        sb.append(", body=");
        sb.append(body.size());
        sb.append(" lines.");

        return sb.toString();
    }

}
