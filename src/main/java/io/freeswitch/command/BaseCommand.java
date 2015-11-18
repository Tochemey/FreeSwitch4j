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

import io.freeswitch.common.UuidFactory;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Arsene Tochemey GANDOTE
 */
public abstract class BaseCommand {

    /**
     * Command sequence number. It helps identify each command that is sent to
     * the FreeSwitch server
     */
    private final UUID sequence;
    /**
     * Additional data to add to the command
     */
    public Object optional;
    /**
     * Property that helps set the command argument
     */
    protected String _command;


    public BaseCommand() {
        this.sequence = UuidFactory.create();
    }

    public UUID getSequence() {
        return this.sequence;
    }

    /**
     * The command argument
     */
    public abstract String argument();

    /**
     * The command name
     */
    public abstract String command();

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        BaseCommand cmd = (BaseCommand) obj;
        return cmd.toString().equals(toString()) && this.sequence.equals(cmd.getSequence());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.sequence);
        hash = 31 * hash + Objects.hashCode(this.command());
        hash = 31 * hash + Objects.hashCode(this.argument());
        return hash;
    }

    @Override
    public String toString() {
        return String.format("%s %s", command(), argument());
    }
}
