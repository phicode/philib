package ch.bind.philib.msg;

public interface MessageBroker {

	Subscription subscribe(String channel, MessageHandler handler);

	void addDeadLetterHandler(DeadLetterHandler handler);

	void removeDeadLetterHandler(DeadLetterHandler handler);

	void publishSync(String channel, Object message) throws InterruptedException;

	void publishAsync(String channel, Object message);

}
