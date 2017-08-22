# Messaging patterns

Reactive messaging patterns using Scala and Akka.

### Message types
1. _Command message_
2. _Event message_
3. _Document message_

### Patterns
1. **Pipes and Filters** - `org.messaging.patterns.pipes`<br />
![Alt text](img/pipes_and_filters.png?raw=true)<br />

2. **Message Router** - `org.messaging.patterns.router`<br />
![Alt text](img/message_router.png?raw=true)<br />

3. **Message Translator** - `org.messaging.patterns.translator`<br />
![Alt text](img/message_translator.png?raw=true)<br />

4. **Message Endpoint** - `org.messaging.patterns.endpoint`<br />
![Alt text](img/message_endpoint.png?raw=true)<br />

### Channel types
1. **Point-to-Point Channel** - `org.messaging.channel.pointopoint`<br />
![Alt text](img/point_to_point_channel.png?raw=true)<br />

2. **Publish-Subscribe Channel** - `org.messaging.channel.publishsubscribe`<br />
![Alt text](img/publish_subscribe_channel.png?raw=true)<br />

3. **Datatype Channel** - `org.messaging.channel.datatype`<br />
![Alt text](img/datatype_channel.png?raw=true)<br />

4. **Invalid Message Channel** - `org.messaging.channel.invalid`<br />
![Alt text](img/invalid_message_channel.png?raw=true)<br />

5. **Dead Letter Channel** - `org.messaging.channel.deadletter`<br />
![Alt text](img/dead_letter_channel.png?raw=true)<br />

6. **Guaranteed Delivery** - `org.messaging.channel.guaranteeddelivery`<br />
![Alt text](img/guaranteed_delivery.png?raw=true)<br />

7. _Channel Adapter_
8. _Message Bridge_
9. _Message Bus_
