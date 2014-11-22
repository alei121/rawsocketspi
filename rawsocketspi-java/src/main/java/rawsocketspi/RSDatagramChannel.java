package rawsocketspi;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

public class RSDatagramChannel extends DatagramChannel {
	private RawSocket socket;
	private RSSocketAddress address;

	protected RSDatagramChannel(SelectorProvider provider) throws IOException {
		super(provider);
		socket = new RawSocket();
	}

	@Override
	public MembershipKey join(InetAddress arg0, NetworkInterface arg1)
			throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public MembershipKey join(InetAddress arg0, NetworkInterface arg1,
			InetAddress arg2) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public SocketAddress getLocalAddress() throws IOException {
		return address;
	}

	@Override
	public <T> T getOption(SocketOption<T> arg0) throws IOException {
		// TODO support filtering in future
		return null;
	}

	@Override
	public Set<SocketOption<?>> supportedOptions() {
		// TODO support filtering in future
		return null;
	}

	@Override
	public DatagramChannel bind(SocketAddress local) throws IOException {
		this.address = (RSSocketAddress)local;
		socket.open(address.getIntf());
		return this;
	}

	@Override
	public DatagramChannel connect(SocketAddress remote) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public DatagramChannel disconnect() throws IOException {
		socket.close();
		return this;
	}

	@Override
	public SocketAddress getRemoteAddress() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isConnected() {
		return socket.isOpen();
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		return socket.read(dst);
	}

	@Override
	public long read(ByteBuffer[] dsts, int offset, int length)
			throws IOException {
		return socket.read(dsts, offset, length);
	}

	@Override
	public SocketAddress receive(ByteBuffer dst) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int send(ByteBuffer src, SocketAddress target) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> DatagramChannel setOption(SocketOption<T> name, T value)
			throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public DatagramSocket socket() {
		return null;
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		return socket.write(src);
	}

	@Override
	public long write(ByteBuffer[] srcs, int offset, int length)
			throws IOException {
		return socket.write(srcs, offset, length);
	}

	@Override
	protected void implCloseSelectableChannel() throws IOException {
		// Ignore
	}

	@Override
	protected void implConfigureBlocking(boolean block) throws IOException {
		if (!block) throw new UnsupportedOperationException();
	}

	public RawSocket getRawSocket() {
		return socket;
	}
}
