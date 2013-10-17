package ch.bind.philib.exp.chan;

public interface RxChannel {
	
	public Object receive();

	public Object poll();
}
