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
package io.freeswitch.event;

/**
 * Interface for observers wanting to be notified of incoming FreeSWITCH Event Socket event.
 * <p>
 * Incoming event arrive asynchronously and are processed into two queues, one for server
 * initiated event, and one for the results of client requested background jobs.
 * <p>
 * Each queue is serviced by a different thread pool (to ensure lowest latency for event-driven event)
 * and each queue is guaranteed to be processed (and listeners notified) in the order in which the
 * event are received off the wire.
 * <p>
 * This design ensures that incoming event processing is not blocked by any long-running listener process.
 * However multiple listeners will be notified sequentially, and so one slow listener can cause latency
 * to other listeners.
 *
 * @author david varnes
 * @author Arsene Tochemey GANDOTE
 */
public interface IEventsListener {
    /**
     * Signal of a server initiated event.
     *
     * @param event as an {@link EslEvent}
     */
    void eventReceived(EslEvent event);

    /**
     * Signal of an event containing the result of a client requested background job.  The Job-UUID will
     * be available as an event header of that name.
     *
     * @param event as an {@link EslEvent}
     */
    void backgroundJobEventReceived(EslEvent event);
}
