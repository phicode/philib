package ch.bind.philib.net;

import java.net.InetAddress;

public interface NetConsumer {

	void connect(NetQueue queue, InetAddress clientAddress);

	void disconnect(NetQueue queue, InetAddress clientAddress);

}
