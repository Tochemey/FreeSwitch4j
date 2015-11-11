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

/**
 * Resume. socket_resume: If this variable is set to true, the dialplan will
 * resume execution with the next action after the call to the socket
 * application. This can be used for example to allow you to do something
 * intelligent in the dialplan if your IVR application gets killed in an unclean
 * way. If there is a bridge active when the disconnect happens, it is killed.
 * To do this from your application after the socket is already connected, issue
 * the resume command.
 * 
 * @author Arsene Tochemey GANDOTE
 *
 */
public class ResumeCommand extends BaseCommand {

	@Override
	public String argument() {
		return "";
	}

	@Override
	public String command() {
		return "resume";
	}

}
