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

import org.apache.commons.lang3.StringUtils;

/**
 * Speaks a string or file of text to the channel using the defined speech
 * engine.
 * 
 * @author Arsene Tochemey GANDOTE
 *
 */
public class SpeakCommand extends BaseCommand {

	/**
	 * TTS engine
	 */
	public String engine;

	/**
	 * TTS engine voice
	 */
	public String voice;

	/**
	 * Text to read
	 */
	public String text;

	/**
	 * Timer name
	 */
	public String timerName;

	public SpeakCommand() {
		engine = "flite";
		voice = "kal";
	}

	@Override
	public String argument() {
		return engine + "|" + voice + "|" + text
				+ (!StringUtils.isEmpty(timerName) ? "|" + timerName : "");
	}

	@Override
	public String command() {
		return "speak";
	}

}
