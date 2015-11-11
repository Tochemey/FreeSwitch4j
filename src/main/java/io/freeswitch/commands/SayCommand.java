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

import io.freeswitch.SayGender;
import io.freeswitch.SayMethod;
import io.freeswitch.SayType;

/**
 * Say. The say application will use the pre-recorded sound files to read or say
 * various things like dates, times, digits, etc. The say application can read
 * digits and numbers as well as dollar amounts, date/time values and IP
 * addresses. It can also spell out alpha-numeric text, including punctuation
 * marks.
 * 
 * @author Arsene Tochemey GANDOTE
 *
 */
public class SayCommand extends BaseCommand {

	/**
	 * Text to read.
	 */
	private String _text;

	/**
	 * say language or module
	 */
	public String language;

	/**
	 * say type
	 * 
	 * @see SayType
	 */
	public SayType type;

	/**
	 * say method
	 * 
	 * @see SayMethod
	 */
	public SayMethod method;

	/**
	 * say gender
	 * 
	 * @see SayGender
	 */
	public SayGender gender;

	public SayCommand(String text) {
		_text = text;
	}

	@Override
	public String argument() {
		return language + " " + type.toString() + " "
				+ method.toString().replace("_", "/") + " " + gender.toString()
				+ " " + _text;
	}

	@Override
	public String command() {
		return "say";
	}

}
