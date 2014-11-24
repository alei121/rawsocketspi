rawsocketspi
============

Java SPI for raw socket

Support for Linux raw socket only, the library uses Java SPI to provide easy access to network packet data via Java DatagramChannel.

Requires:
* Linux
* Maven
* JDK 7
* gcc

Test setup:
...

API usage:
...

Example usage:
...

Maven:
...


---

To run on Ubuntu with openjdk:

`mvn clean install`

`export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/`

`/usr/lib/jvm/java-7-openjdk-amd64/bin/java -cp rawsocketspi-dist/target/rawsocketspi-1.0-SNAPSHOT-amd64-Linux.jar:rawsocketspi-examples/target/rawsocketspi-examples-1.0-SNAPSHOT.jar rawsocketspi.examples.PrintPacket eth1`

`/usr/lib/jvm/java-7-openjdk-amd64/bin/java -cp rawsocketspi-dist/target/rawsocketspi-1.0-SNAPSHOT-amd64-Linux.jar:rawsocketspi-examples/target/rawsocketspi-examples-1.0-SNAPSHOT.jar rawsocketspi.examples.SimpleBridge eth1 eth2`



More details to come...

