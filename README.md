rawsocketspi
============

Java SPI for raw socket

Support for Linux raw socket only, the library uses Java SPI to provide easy access to network packet data via Java DatagramChannel.

Example usage:
```java
  import java.nio.channels.DatagramChannel;
  import com.github.alei121.rawsocketspi.RSSelectorProvider;
  import com.github.alei121.rawsocketspi.RSSocketAddress;

  // Create DatagramChannel and bind it
	RSSelectorProvider provider = new RSSelectorProvider();
	DatagramChannel channel = provider.openDatagramChannel();
	RSSocketAddress address = new RSSocketAddress("eth1");
	channel.bind(address);
```

Maven:
```xml
<dependency>
	<groupId>com.github.alei121</groupId>
	<artifactId>rawsocketspi</artifactId>
	<classifier>amd64-Linux</classifier>
	<version>1.0.0</version>
</dependency>
```

Development requires:
* Linux
* Maven
* JDK 7
* gcc

Test setup:
...



---

To run examples on Ubuntu with openjdk:

`mvn clean install`

`export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/`

`/usr/lib/jvm/java-7-openjdk-amd64/bin/java -cp rawsocketspi-dist/target/rawsocketspi-1.0-SNAPSHOT-amd64-Linux.jar:rawsocketspi-examples/target/rawsocketspi-examples-1.0-SNAPSHOT.jar com.github.alei121.rawsocketspi.examples.SimpleCapture eth1`

`/usr/lib/jvm/java-7-openjdk-amd64/bin/java -cp rawsocketspi-dist/target/rawsocketspi-1.0-SNAPSHOT-amd64-Linux.jar:rawsocketspi-examples/target/rawsocketspi-examples-1.0-SNAPSHOT.jar com.github.alei121.rawsocketspi.examples.SimpleBridge eth1 eth2`



More details to come...

