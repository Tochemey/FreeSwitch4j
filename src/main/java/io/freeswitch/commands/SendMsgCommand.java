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

import java.util.UUID;

/**
 * @author Arsene Tochemey GANDOTE
 *
 */
public class SendMsgCommand extends BaseCommand {

	final UUID EMPTY_UUID = new UUID(0L, 0L);

	/**
	 * channel Id
	 */
	private UUID _channelId;

	/**
	 * application to be executed
	 */
	private String _application;

	/**
	 * application argument
	 */
	private String _argument;

	/**
	 * States whether to execute the application in blocking mode or not.
	 */
	private boolean _eventLock;

	/**
	 * Number of times to execute the application
	 */
	private int _loop;

	public SendMsgCommand(UUID channelId, String app, String arg, int loop,
			boolean blockingMode) {
		_channelId = channelId;
		_application = app;
		_argument = arg;
		_loop = loop;
		_eventLock = blockingMode;
	}

	public SendMsgCommand(String app, String arg, int loop, boolean blockingMode) {
		_channelId = EMPTY_UUID;
		_application = app;
		_argument = arg;
		_loop = loop;
		_eventLock = blockingMode;
	}

	public SendMsgCommand(String app, String arg) {
		_channelId = EMPTY_UUID;
		_application = app;
		_argument = arg;
		_loop = 1;
		_eventLock = false;
	}

	@Override
	public String argument() {
		return "";
	}

	@Override
	public String command() {
		String cmd = "";
		if (_channelId.equals(EMPTY_UUID)) {
			cmd = String
					.format("sendmsg  call-command: %1s\nexecute-app-name: %2s\nexecute-app-arg: %3s\nloops: %4d",
							"execute", _application, _argument, _loop);
		} else {
			cmd = String
					.format("sendmsg  %1s\ncall-command: %2s\nexecute-app-name: %3s\nexecute-app-arg: %4s\nloops: %5d",
							_channelId.toString(), "execute", _application,
							_argument, _loop);
		}

		if (_eventLock)
			cmd += String.format("\nevent-lock: %s", "true");

		return cmd;
	}

}
