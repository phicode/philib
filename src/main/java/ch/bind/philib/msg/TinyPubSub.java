package ch.bind.philib.msg;

public interface TinyPubSub {

	Subscription subscribe(String channelName, MessageHandler handler);

	void addDeadLetterHandler(MessageHandler handler);

	void removeDeadLetterHandler(MessageHandler handler);

	void publishSync(String channelName, Object message);

	void publishAsync(String channelName, Object message);

}
