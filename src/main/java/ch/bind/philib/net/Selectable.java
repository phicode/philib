package ch.bind.philib.net;

import java.nio.channels.SelectableChannel;

public interface Selectable {

	SelectableChannel getChannel();

	int getSelectorOps();

	boolean handle(int selectOp);
	
	void closed();

}
