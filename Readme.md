Java FreeSwitch library
========================================

## **Overview**
This library helps interact with the FreeSwitch via its mod_event_socket. For more information about the mod_event_socket refer to [FreeSwitch web site](https://freeswitch.org/confluence/display/FREESWITCH/mod_event_socket). Also it offers more flexibility for extension by any other developer who picks the source code. 

In its current state it can help build IVR applications more quickly. 

## **Features**
The framework in its current state can be used to interact with FreeSwitch easily in:
* Inbound mode [Event_Socket_Inbound](https://freeswitch.org/confluence/display/FREESWITCH/mod_event_socket#mod_event_socket-Inbound)
* Outbound mode [Event_Socket_Outbound](https://wiki.freeswitch.org/wiki/Event_Socket_Outbound)
* One good thing it has is that you can implement your own FreeSwitch message decoder if you do not want to use the built-in ones

## **License**
[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)

## **Notes**
It has not been fully tested. When the tests are done it will be confirmed on this page. Thank you.