package ch.bind.philib.net;

public interface NetConsumer {

	void receive(NetConnection connection, byte[] message);
	
}
