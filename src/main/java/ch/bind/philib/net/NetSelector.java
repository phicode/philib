package ch.bind.philib.net;

import java.io.Closeable;

public interface NetSelector extends Runnable, Closeable {

	void register(Selectable selectable);

	void unregister(Selectable selectable);

	void reRegWithWrite(Selectable selectable);

	void reRegWithoutWrite(Selectable selectable);
}
