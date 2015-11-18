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
package io.freeswitch;

import io.freeswitch.event.EslEvent;
import io.freeswitch.event.IEventsListener;
import io.freeswitch.message.FreeSwitchMessage;
import io.freeswitch.outbound.OutboundClient;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Arsene Tochemey GANDOTE
 */
public class OutboundClientTest {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private String host = "192.168.254.246";
    private int port = 8021;
    private String password = "ClueCon";

    public OutboundClientTest() {
    }

    @Test
    public void connect() throws InterruptedException {

        // instance of OutboundClient
        OutboundClient client = new OutboundClient();
        client.addEventListener(new IEventsListener() {

            public void eventReceived(EslEvent event) {
                log.info("Event received [{}]", event);
            }

            public void backgroundJobEventReceived(EslEvent event) {
                log.info("Background job result received [{}]", event);
            }
        });

        log.info("Client connecting ..");
        try {
            client.connect(host, port, password, 20);
        } catch (ConnectionFailure e) {
            log.error("Connect failed", e);
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
        log.info("Client connected ..");

        // client.setEventSubscriptions( "plain",
        // "heartbeat CHANNEL_CREATE CHANNEL_DESTROY BACKGROUND_JOB" );
        client.event("plain", "all");
        client.filter("Event-HeaderName", "heartbeat");
        client.noevents();
        client.event("plain", "all");
        client.filter("Event-HeaderName", "heartbeat");
        client.filter("Event-HeaderName", "channel_create");
        client.filter("Event-HeaderName", "background_job");
        FreeSwitchMessage response = client.api("sofia status", "");
        log.info("sofia status = [{}]", response.bodyLines().get(3));

        // wait to see the heartbeat event arrive
        Thread.sleep(25000);
        client.close();

    }

}
