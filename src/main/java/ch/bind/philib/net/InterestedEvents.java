package ch.bind.philib.net;

import ch.bind.philib.net.events.Event;

// not actually deprecated, but the name sucks
// it should be shorter and more precise as to its intentions
@Deprecated
public enum InterestedEvents {

	RECEIVE(true, false, Event.READ), //
	SENDABLE(false, true, Event.WRITE), //
	SENDABLE_RECEIVE(true, true, Event.READ_WRITE);

	private final boolean receive;

	private final boolean sendable;

	private final int eventMask;

	private InterestedEvents(boolean receive, boolean sendable, int eventMask) {
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
