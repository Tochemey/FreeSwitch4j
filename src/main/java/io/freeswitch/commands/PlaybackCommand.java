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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;


/**
 * Playback. Plays a sound file on the current channel.
 * 
 * @author Arsene Tochemey GANDOTE
 *
 */
public class PlaybackCommand extends BaseCommand {

	/**
	 * Playback channel variables
	 */
	private List<ChannelVariable> _channelVariables;

	/**
	 * Number of times to play the audio file
	 */
	private int _loop;

	public int loop() {
		return _loop;
	}

	/**
	 * Audio file to play
	 */
	private String _audioFile;

	public PlaybackCommand() {
		_channelVariables = new ArrayList<ChannelVariable>();
		_loop = 1;
		_audioFile = "";
	}

	public PlaybackCommand(String audioFile) {
		_audioFile = audioFile;
		_loop = 1;
		_channelVariables = new ArrayList<ChannelVariable>();
	}

	public PlaybackCommand(String audioFile, List<ChannelVariable> variables) {
		_audioFile = audioFile;
		_loop = 1;
		_channelVariables = variables;
	}

	public PlaybackCommand(String audioFile, int loop, List<ChannelVariable> variables) {
		_audioFile = audioFile;
		_loop = loop;
		_channelVariables = variables;
	}

	@Override
	public String argument() {
		String variables = "";
		if (_channelVariables.isEmpty()) {
			for (ChannelVariable channelVariable : _channelVariables) {
				variables += channelVariable.toString() + ",";
			}

			variables = "{" + StringUtils.removeEnd(variables, ",") + "}";
		}

		return String.format("%1s %2s", variables, _audioFile);
	}

	@Override
	public String command() {
		return "playback";
	}

}
