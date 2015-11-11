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
package io.freeswitch;

import io.freeswitch.events.EslEvent;
import io.freeswitch.message.CommandReply;

/**
 * End users of the {@link Client} should not need to use this class. 
 * @author Arsene Tochemey GANDOTE
 *
 */
public interface IProtocolListener {

	/**
	 * Raised when authentication response is received
	 * 
	 * @param response
	 */
	void authResponseReceived(CommandReply response);

	/**
	 * Raised whenever an event is received from FreeSwitch
	 * 
	 * @param event
	 *            FreeSwitch event
	 */
	void eventReceived(EslEvent event);

	/**
	 * Raised when the client is disconnected.
	 */
	void disconnected();
}
