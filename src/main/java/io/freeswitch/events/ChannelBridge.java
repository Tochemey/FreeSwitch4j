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

import java.util.UUID;

/**
 * This event is raised when two calls have connected using the bridge
 * application.
 * 
 * @author Arsene Tochemey GANDOTE
 *
 */
public class ChannelBridge extends AbstractEvent {

	/**
	 * @param event
	 */
	public ChannelBridge(EslEvent event) {
		super(event);
	}

	/**
	 * Gets the call direction
	 * 
	 * @return String value of the event data "Call-Direction"
	 */
	public String callDirection() {
		return _event.eventHeaders().get("Call-Direction");
	}

	/**
	 * Gets the end point disposition. It describes the state of the call.
	 * 
	 * @return String value of the event data "endpoint_disposition"
	 */
	public String endpointDisposition() {
		return _event.eventHeaders().get("variable_endpoint_disposition");
	}

	/**
	 * Gets the A-leg UUID.
	 * 
	 * @return UUID value of the A-leg UUID.
	 */
	public UUID alegUUID() {
		return UUID.fromString(_event.eventHeaders().get("Bridge-A-Unique-ID"));
	}

	/**
	 * Gets the B-leg UUID.
	 * 
	 * @return UUID value of the B-leg UUID.
	 */
	public UUID blegUUID() {
		return UUID.fromString(_event.eventHeaders().get("Bridge-B-Unique-ID"));
	}

}
