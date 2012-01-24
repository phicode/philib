package ch.bind.philib.net;

import java.io.IOException;
import java.net.SocketAddress;

public interface NetFactory {

	NetConnection openClient(SocketAddress endpoint) throws IOException;

	NetServer openServer(SocketAddress bindAddress, NetConnectionListenerFactory factory) throws IOException;

}
