package ch.bind.philib.net;

import java.io.IOException;
import java.net.SocketAddress;

public interface NetFactory {

	Connection openClient(SocketAddress endpoint) throws IOException;

	NetServer openServer(SocketAddress bindAddress, ConsumerFactory factory) throws IOException;

}
