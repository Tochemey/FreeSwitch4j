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
package io.freeswitch.commands;


import io.freeswitch.ChannelVariable;
import io.freeswitch.IEndPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

/**
 * Originate. It is used to originate a new call.
 * 
 * @author Arsene Tochemey GANDOTE
 *
 */
public class OriginateCommand extends BaseCommand {

	/**
	 * originate channel variables
	 */
	private List<ChannelVariable> _channelVariables;

	private IEndPoint _destination;
	private IEndPoint _application;

	/**
	 * originate uuid.
	 */
	public UUID originateId;

	/**
	 * call session heartbeat. very useful while billing a call.
	 */
	public int sessionHeartbeat;

	/**
	 * Additional attributes to add to the originate command
	 */
	public String option;

	public OriginateCommand(IEndPoint destination, IEndPoint application) {
		_destination = destination;
		_application = application;
		_channelVariables = new ArrayList<ChannelVariable>();
	}

	@Override
	public String argument() {
		AddVariable("origination_uuid", originateId.toString());
		AddVariable("ignore_early_media", "true");
		AddVariable("enable_heartbeat_events", String.valueOf(sessionHeartbeat));

		String variables = "";
		if (_channelVariables.isEmpty()) {
			for (ChannelVariable channelVariable : _channelVariables) {
				variables += channelVariable.toString() + ",";
			}

			if (StringUtils.isEmpty(option))
				variables = "{" + StringUtils.removeEnd(variables, ",") + "}";
			else
				variables = "{" + option + ","
						+ StringUtils.removeEnd(variables, ",") + "}";
		}
		
		return String.format("%1s %2s %3s", variables, _destination.toDialString(), _application.toDialString());

	}

	@Override
	public String command() {
		return "originate";
	}

	/**
	 * Helps set originate channel variables
	 * 
	 * @param name
	 *            variable name
	 * @param value
	 *            variable value
	 */
	public void AddVariable(String name, String value) {
		_channelVariables.add(new ChannelVariable(name, value));
	}
}
