package rawsocketspi.examples;

import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Formatter;

import rawsocketspi.RSSelectorProvider;
import rawsocketspi.RSSocketAddress;

/**
 * A simple packet capture application.
 * Prints dst mac, src mac and content.
 * 
 * @author Alan Lei
 */
public class SimpleCapture {
	private static String bbToString(ByteBuffer bb, String divider, int count) {
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb);
		for (int i = 0; i < count; i++) {
			if (i < (count - 1)) {
				formatter.format("%02X" + divider, bb.get() & 0xFF);
			} else {
				formatter.format("%02X", bb.get() & 0xFF);
			}
		}
		formatter.close();
		return sb.toString();
	}

	public static void main(String[] args) throws Exception {
		RSSocketAddress address = new RSSocketAddress(args[0]);
		
		RSSelectorProvider provider = new RSSelectorProvider();
		DatagramChannel channel = provider.openDatagramChannel();
		channel.bind(address);

		ByteBuffer bb = ByteBuffer.allocateDirect(2048);
		while (true) {
			bb.clear();
			channel.read(bb);
			bb.flip();
			System.out.println("dstMac=" + bbToString(bb, ":", 6) + " srcMac="
					+ bbToString(bb, ":", 6));
			int count = bb.remaining();
			String trail = "";
			if (count > 20) {
				count = 20;
				trail = "...";
			}
			System.out.println("  content=" + bbToString(bb, " ", count)
					+ trail);
		}
	}
}
