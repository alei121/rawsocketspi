package com.github.alei121.rawsocketspi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * This interacts with the native code,
 * supporting the basic open/close and read/write.
 * 
 * @author Alan Lei
 */
public class RawSocket implements ByteChannel, GatheringByteChannel {
    public static native void nativeInit();
    public static native int nativeOpen(String ifName);
    public static native void nativeClose(int sd);
    public static native int nativeRead(int sd, ByteBuffer bb, int offset, int length);
    public static native int nativeWrite(int sd, ByteBuffer bb, int offset, int length);
    public static native String[] nativeGetInterfaceNames();
    public static native int nativeGetHardwareAddress(int sd, ByteBuffer bb);

    private static boolean isJniLoaded = false;
    private static String LIB_NAME = "META-INF/lib/rawsocketspi-jni-" + RawSocket.class.getPackage().getImplementationVersion() + ".so";

    int sd = -1;
    String ifName;

    public RawSocket() throws IOException {
    	synchronized (RawSocket.class) {
			if (!isJniLoaded) {
				InputStream in = getClass().getClassLoader().getResourceAsStream(LIB_NAME);
				File file = File.createTempFile("tmp-rawsocketspi-jni-", "-so");
				file.deleteOnExit();
				Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
				System.load(file.getAbsolutePath());

				nativeInit();
				
				isJniLoaded = true;
			}
		}
	}
    
    public void open(String ifName) throws IOException {
    	this.ifName = ifName;
        sd = nativeOpen(ifName);
        if (sd == -1) throw new IOException("Unable to open socket for " + ifName);
    }

    @Override
    public void close() throws IOException {
        nativeClose(sd);
        sd = -1;
    }

    @Override
    public int read(ByteBuffer bb) throws IOException {
        int pos = bb.position();
        int len = nativeRead(sd, bb, pos, bb.remaining());
        if (len == -1) throw new IOException("nativeRead error");
        bb.position(pos + len);
        return len;
    }
    
    public long read(ByteBuffer[] bbs, int offset, int length) throws IOException {
    	long len = 0;
        for (int i = 0; i < length; i++) {
            len += read(bbs[offset + i]);
        }
        return len;
    }

    @Override
    public int write(ByteBuffer bb) throws IOException {
        int pos = bb.position();
        int len = nativeWrite(sd, bb, pos, bb.remaining());
        if (len == -1) throw new IOException("nativeWrite error");
        bb.position(pos + len);
        return len;
    }

    @Override
    public boolean isOpen() {
        return (sd != -1);
    }

    @Override
    public long write(ByteBuffer[] bbs) throws IOException {
        // TODO optimize later with native gathering
        ByteBuffer oneBB = ByteBuffer.allocateDirect(2048);
        for (ByteBuffer bb : bbs) {
            oneBB.put(bb);
        }
        oneBB.flip();
        return write(oneBB);
    }

    @Override
    public long write(ByteBuffer[] bbs, int offset, int length) throws IOException {
        ByteBuffer oneBB = ByteBuffer.allocateDirect(2048);
        for (int i = 0; i < length; i++) {
            oneBB.put(bbs[offset + i]);
        }
        oneBB.flip();
        return write(oneBB);
    }
    
    public byte[] getHardwareAddress() {
    	ByteBuffer bb = ByteBuffer.allocateDirect(32);
    	int len = nativeGetHardwareAddress(sd, bb);
    	byte[] address = new byte[len];
    	for (int i = 0; i < len; i++) {
    		address[i] = bb.get(i);
    	}
    	return address;
    }
    
    @Override
    public String toString() {
        return "[" + ifName + "]";
    }

    /**
     * This method calls native code to get all interface names.
     * Note that NetworkInterface from Java API does not yield all interfaces, but only those configured with ip address.
     * 
     * @return Names of interfaces available
     */
    public static String[] getInterfaceNames() {
    	return nativeGetInterfaceNames();
    }
}
