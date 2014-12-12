rawsocketspi
============

Java SPI for raw socket

Support for Linux raw socket only, the library uses Java SPI to provide easy access to network packet data via Java DatagramChannel.

Usage
---
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

Maven
---
```xml
<dependency>
	<groupId>com.github.alei121</groupId>
	<artifactId>rawsocketspi</artifactId>
	<classifier>amd64-Linux</classifier>
	<version>1.0.0</version>
</dependency>
```


Test setup
---
Testing was done in VMWare Fusion:
* 2 custom networks vmnet2 and vmnet3 (no dhcp, no nat, no host connect)
* 1 VM running Ubuntu 1G RAM 20G disk for build and run (add vmnet2 and vmnet3)
* 2 VMs running Ubuntu 512G RAM for testing (one on vmnet2, one on vmnet3)

For reference my environment is:
* VMWare Fusion 7.1.0
* Ubuntu 14.04 and 12.04
* OpenJDK 7

---
To run examples on Ubuntu with openjdk:

`mvn clean install`

`export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/`

`/usr/lib/jvm/java-7-openjdk-amd64/bin/java -cp rawsocketspi-dist/target/rawsocketspi-1.0-SNAPSHOT-amd64-Linux.jar:rawsocketspi-examples/target/rawsocketspi-examples-1.0-SNAPSHOT.jar com.github.alei121.rawsocketspi.examples.SimpleCapture eth1`

`/usr/lib/jvm/java-7-openjdk-amd64/bin/java -cp rawsocketspi-dist/target/rawsocketspi-1.0-SNAPSHOT-amd64-Linux.jar:rawsocketspi-examples/target/rawsocketspi-examples-1.0-SNAPSHOT.jar com.github.alei121.rawsocketspi.examples.SimpleBridge eth1 eth2`



More details to come...

