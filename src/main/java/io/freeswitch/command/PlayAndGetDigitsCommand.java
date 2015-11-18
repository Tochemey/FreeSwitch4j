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
 * PlayAndGetDigits. Play a prompt and get digits. {@link https
 * ://wiki.freeswitch.org/wiki/Misc._Dialplan_Tools_play_and_get_digits}
 *
 * @author Arsene Tochemey GANDOTE
 */
public class PlayAndGetDigitsCommand extends BaseCommand {

    /**
     * Maximum number of digits to fetch (maximum value of 128)
     */
    public int maxNumberOfDigits;

    /**
     * Minimum number of digits to fetch (minimum value of 0)
     */
    public int minNumberOfDigits;

    /**
     * digits used to end input if less than <maxNumberOfDigits> digits have
     * been pressed. (Typically '#')
     */
    public char terminators;

    /**
     * numbers of tries for the sound to play
     */
    public int retries;

    /**
     * Regular expression to match digits
     */
    public String regex;

    /**
     * Inter-digit timeout; number of milliseconds allowed between digits; once
     * this number is reached, PAGD assumes that the caller has no more digits
     * to dial.
     */
    public int digitTimeout;

    /**
     * Number of milliseconds to wait for a dialed response after the file
     * playback ends and before PAGD does a retry.
     */
    public int timeout;

    /**
     * Sound file to play while digits are fetched.
     */
    public String audioFile;

    /**
     * Channel variable into which digits should be placed.
     */
    public String variableName;

    /**
     * Sound file to play when digits don't match the regexp.
     */
    public String invalidFile;

    public PlayAndGetDigitsCommand() {
        maxNumberOfDigits = 128;
        minNumberOfDigits = 0;
        terminators = '#';
        retries = 1;
        regex = "1234567890*#";
        digitTimeout = (2 * 1000);
        timeout = (5 * 1000);
        invalidFile = "silence_stream://150";
    }

    @Override
    public String argument() {
        return String.format("%1d %2d %3d %4d '%5s' '%6s' %7s %8s %9s %10d",
                minNumberOfDigits, maxNumberOfDigits, retries, timeout,
                terminators, audioFile, invalidFile, variableName, regex,
                digitTimeout);
    }

    @Override
    public String command() {
        return "play_and_get_digits";
    }

}
