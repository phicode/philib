package ch.bind.philib.exp.chan;

import java.io.Closeable;

public interface TxChannel extends Closeable {
	public boolean trySend(Object message) ;
	public void send(Object message);
}
