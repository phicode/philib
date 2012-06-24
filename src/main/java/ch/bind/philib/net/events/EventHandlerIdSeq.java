package ch.bind.philib.net.events;

import java.util.concurrent.atomic.AtomicLong;

public final class EventHandlerIdSeq {

	private static final AtomicLong SEQ = new AtomicLong(0);

	private EventHandlerIdSeq() {
	}

	public static long nextEventHandlerId() {
		return SEQ.getAndIncrement();
	}
}
