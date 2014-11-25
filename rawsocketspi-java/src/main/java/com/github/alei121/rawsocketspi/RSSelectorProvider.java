package com.github.alei121.rawsocketspi;

import java.io.IOException;
import java.net.ProtocolFamily;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;

/**
 * Java SPI provider for RawSocket.
 * Only supported method is openDatagramChannel
 * 
 * @author Alan Lei
 */
public class RSSelectorProvider extends SelectorProvider {

	@Override
	public DatagramChannel openDatagramChannel() throws IOException {
		return new RSDatagramChannel(this);
	}

	@Override
	public DatagramChannel openDatagramChannel(ProtocolFamily arg0)
			throws IOException {
		return null;
	}

	@Override
	public Pipe openPipe() throws IOException {
		return null;
	}

	@Override
	public AbstractSelector openSelector() throws IOException {
		return null;
	}

	@Override
	public ServerSocketChannel openServerSocketChannel() throws IOException {
		return null;
	}

	@Override
	public SocketChannel openSocketChannel() throws IOException {
		return null;
	}

}
