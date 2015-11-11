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

import java.util.List;
import java.util.UUID;

/**
 * Event emitted by FreeSwitch when a bgapi is executed.
 * 
 * @author Arsene Tochemey GANDOTE
 *
 */
public class BackgroundJob extends AbstractEvent{

	/**
	 * @param event
	 */
	public BackgroundJob(EslEvent event) {
		super(event);
	}

	/**
	 * jobId(). It returns the background job id
	 * 
	 * @return UUID the job id
	 */
	public UUID jobId() {
		String uniqueId = _event.eventHeaders().get(EventHeaders.JOB_UUID);
		return UUID.fromString(uniqueId);
	}

	/**
	 * It returns the command executed by bgapi.
	 * 
	 * @return the job command.
	 */
	public String commandName() {
		return _event.eventHeaders().get(EventHeaders.JOB_COMMAND);
	}

	/**
	 * It returns the argument of the command executed by bgapi.
	 * 
	 * @return
	 */
	public String commandArg() {
		return _event.eventHeaders().get("Job-Command-Arg");
	}

	/**
	 * It returns the result of the command executed by bgapi.
	 * @return
	 */
	public List<String> result() {
		return _event.eventBodyLines();
	}
}
