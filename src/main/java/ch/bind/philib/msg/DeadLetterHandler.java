package ch.bind.philib.msg;

public interface DeadLetterHandler {

	void deadLetter(String channel, Object message);
}
