package rawsocketspi.examples;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import rawsocketspi.RSSelectorProvider;
import rawsocketspi.RSSocketAddress;

public class PrintPacket {

	public static void main(String[] args) throws Exception {
		RSSocketAddress address = new RSSocketAddress("eth1");
		
		RSSelectorProvider provider = new RSSelectorProvider();
		DatagramChannel channel = provider.openDatagramChannel();
		
		channel.bind(address);

		ByteBuffer bb = ByteBuffer.allocateDirect(2048);

		try {
			while (true) {
				bb.clear();
				channel.read(bb);
				bb.flip();
				String s = ByteHelper.toStringTrimmed(bb);
				System.out.println("bb=" + s);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		channel.disconnect();
	}
}
