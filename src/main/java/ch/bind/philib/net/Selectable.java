package ch.bind.philib.net;

import java.nio.channels.SelectableChannel;

public interface Selectable {

	SelectableChannel getChannel();

	int getSelectorOps();

	void handle(int selectOp);

}
