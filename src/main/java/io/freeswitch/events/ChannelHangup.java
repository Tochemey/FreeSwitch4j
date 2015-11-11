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

import io.freeswitch.common.HangupCauses;

/**
 * This event is raised whenever a call is hangup.
 * 
 * @author Arsene Tochemey GANDOTE
 *
 */
public class ChannelHangup extends AbstractEvent {

	/**
	 * @param event
	 */
	public ChannelHangup(EslEvent event) {
		super(event);
	}

	/**
	 * Gets the hangup cause.
	 * 
	 * @return HnagupCause value of the hangup cause.
	 */
	public HangupCauses reason() {
		if (_event.eventHeaders().containsKey("Hangup-Cause")) {
			String cause = _event.eventHeaders().get("Hangup-Cause");
			return Enum.valueOf(HangupCauses.class, cause);
		}

		return HangupCauses.NONE;
	}
}
