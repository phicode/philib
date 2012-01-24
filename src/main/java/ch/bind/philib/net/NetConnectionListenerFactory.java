package ch.bind.philib.net;

public interface NetConnectionListenerFactory {

	NetConnectionListener acceptConnection(NetConnection connection);

}
