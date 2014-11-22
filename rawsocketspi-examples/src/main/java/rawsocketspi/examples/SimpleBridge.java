package rawsocketspi.examples;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;

import rawsocketspi.RSSelectorProvider;
import rawsocketspi.RSSocketAddress;

public class SimpleBridge {
	List<DatagramChannel> channels = new ArrayList<DatagramChannel>();
	List<BridgeLink> bridgeLinks = new ArrayList<>();

	public SimpleBridge(List<DatagramChannel> channels) {
		this.channels = channels;
		for (DatagramChannel channel : channels) {
			bridgeLinks.add(new BridgeLink(channel));
		}
	}

	public void start() {
		for (BridgeLink bridgeLink : bridgeLinks) {
			bridgeLink.start();
		}
	}

	public void stop() {
		for (BridgeLink bridgeLink : bridgeLinks) {
			bridgeLink.pleaseStop();
		}
	}

	class BridgeLink extends Thread {
		DatagramChannel channel;
		ByteBuffer bb = ByteBuffer.allocateDirect(2048);
		boolean keepRunning = true;

		public BridgeLink(DatagramChannel channel) {
			this.channel = channel;
		}

		public void pleaseStop() {
			keepRunning = false;
			interrupt();
		}

		void sendToOthers(ByteBuffer bb) throws IOException {
			for (DatagramChannel c : channels) {
				if (c != channel) {
					bb.rewind();
					c.write(bb);
				}
			}
		}

		public void run() {
			while (keepRunning) {
				try {
					bb.clear();
					channel.read(bb);
					bb.flip();
					sendToOthers(bb);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		List<DatagramChannel> channels = new ArrayList<DatagramChannel>();
		
		for (String arg : args) {
			RSSocketAddress address = new RSSocketAddress(arg);
			RSSelectorProvider provider = new RSSelectorProvider();
			DatagramChannel channel = provider.openDatagramChannel();
			channel.bind(address);
			channels.add(channel);
		}
		
		SimpleBridge bridge = new SimpleBridge(channels);
		bridge.start();
		Thread.sleep(60000);
		bridge.stop();
	}
}
