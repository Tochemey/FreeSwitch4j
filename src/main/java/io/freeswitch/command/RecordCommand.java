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
 * Record. Record is used to record voice messages, such as in a voicemail
 * system.
 *
 * @author Arsene Tochemey GANDOTE
 */
public class RecordCommand extends BaseCommand {

    /**
     * File to record
     */
    public String recordFile;

    /**
     * how many seconds of audio below silence_thresh will be tolerated before
     * the recording stops. When omitted, the default value is 3.
     */
    public long silenceHit;

    /**
     * the energy level below which is considered silence.
     */
    public long silenceTreshold;

    /**
     * the maximum duration of the recording in seconds.
     */
    public long timeLimit;

    public RecordCommand() {
        silenceHit = 3;
    }

    @Override
    public String argument() {
        return String.format("%1s %4d %4d %4d", recordFile, timeLimit,
                silenceTreshold, silenceHit);
    }

    @Override
    public String command() {
        return "record";
    }

}
