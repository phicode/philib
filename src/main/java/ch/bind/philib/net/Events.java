package ch.bind.philib.net;

import ch.bind.philib.net.events.SelectOps;

public enum Events {

	RECEIVE(true, false, SelectOps.READ), //
	SENDABLE(false, true, SelectOps.WRITE), //
	SENDABLE_RECEIVE(true, true, SelectOps.READ_WRITE);

	private final boolean receive;

	private final boolean sendable;

	private final int eventMask;

	private Events(boolean receive, boolean sendable, int eventMask) {
		this.receive = receive;
		this.sendable = sendable;
		this.eventMask = eventMask;
	}

	public boolean hasReceive() {
		return receive;
	}

	public boolean hasSendable() {
		return sendable;
	}

	public int getEventMask() {
		return eventMask;
	}
}
