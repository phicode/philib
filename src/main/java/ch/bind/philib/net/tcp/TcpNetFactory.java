package ch.bind.philib.net.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import ch.bind.philib.net.Connection;
import ch.bind.philib.net.NetFactory;
import ch.bind.philib.net.NetSelector;
import ch.bind.philib.net.NetServer;
import ch.bind.philib.net.SocketAddresses;
import ch.bind.philib.net.impl.SimpleNetSelector;

public class TcpNetFactory implements NetFactory {

	@Override
	public Connection openClient(SocketAddress endpoint) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NetServer openServer(SocketAddress bindAddress) throws IOException {
		TcpServer server = new TcpServer();
//		InetSocketAddress endpoint = SocketAddresses.wildcard(1234);
		NetSelector sel = SimpleNetSelector.open();
		server.open(sel, bindAddress);
		return server;
	}

}
