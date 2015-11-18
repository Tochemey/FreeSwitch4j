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
 * Specify event types to listen for. Note, this is not a filter out but rather
 * a "filter in," that is, when a filter is applied only the filtered values are
 * received. Multiple filters on a socket connection are allowed.
 *
 * @author Arsene Tochemey GANDOTE
 */
public class FilterCommand extends BaseCommand {

    /**
     *
     */
    public FilterCommand(String filter) {
        this._command = filter;
    }

    @Override
    public String argument() {
        return this._command;
    }

    @Override
    public String command() {
        return "filter";
    }

}
