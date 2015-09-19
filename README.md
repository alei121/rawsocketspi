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

Blocking read/write only, no selector
---
rawsocketspi only implements blocking read/write.

With better CPUs, OSs, VMs..., computer systems are now more capable of handling large number of threads.
For raw socket, even using one thread per port, the number of threads is still small because the number of physical network ports are limited by hardware and space.
The intention of socket selector was to reduce the number of threads, in which it seems unnecessary in this case.

It is the author believes that application implementation should take the simplest form.
If implementation warrents for a large number of threads, it should be handled by the lower layer (e.g. OS, VM, CPU...etc) instead of adjusting design to reduce threads.


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

