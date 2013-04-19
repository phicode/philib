package ch.bind.philib.msg;

public interface Subscription {

	String getChannelName();

	void cancel();

	boolean isActive();

}
