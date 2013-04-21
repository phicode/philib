package ch.bind.philib.msg;

public interface MessageHandler {

	boolean handleMessage(String channelName, Object message);

}
