package rawsocketspi;

import java.net.SocketAddress;

/**
 * RawSocket addressing is the network interface name.
 * 
 * @author Alan Lei
 */
public class RSSocketAddress extends SocketAddress {
	private static final long serialVersionUID = 1L;

	private String intf;
	
	public RSSocketAddress(String intf) {
		this.intf = intf;
	}
	
	public String getIntf() {
		return intf;
	}
}
