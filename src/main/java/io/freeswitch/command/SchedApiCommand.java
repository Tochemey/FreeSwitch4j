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
package io.freeswitch.command;

/**
 * SchedApi. Schedule an API call in the future.
 *
 * @author Arsene Tochemey GANDOTE
 */
public class SchedApiCommand extends BaseCommand {
    /**
     * The command to schedule
     */
    private BaseCommand _command;

    /**
     * Group name
     */
    private String _groupName;

    /**
     * Asynchronous mode or not
     */
    private boolean _async;

    /**
     * How many times to execute the command
     */
    private boolean _repetitive;

    /**
     * specifies the number of seconds to wait before executing the command.
     * _unixTime is the UNIX timestamp at which the command should be executed
     */
    private long _unixTime;

    public SchedApiCommand(BaseCommand command, long time, String groupName,
                           boolean async, boolean repetitive) {
        _command = command;
        _unixTime = time;
        _groupName = groupName;
        _async = async;
        _repetitive = repetitive;
    }

    @Override
    public String argument() {
        String arg = String.format("+%1d %2s %3s %4s", _unixTime, _groupName,
                _command.toString(), _async ? "&" : "");
        if (_repetitive)
            arg = String.format("@%1d %2s %3s %4s", _unixTime, _groupName,
                    _command, _async ? "&" : "");
        return arg;
    }

    @Override
    public String command() {
        return "sched_api";
    }

}
