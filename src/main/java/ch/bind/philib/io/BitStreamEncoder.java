package ch.bind.philib.io;

public class BitStreamEncoder {

//	private static final int DEFAULT_INITIAL_SIZE = 16;

	private long[] encoded;
	
	private int numEncoded;

	private long active;
	
	private int bitsLeft;

	public BitStreamEncoder() {
//		this.data = new byte[DEFAULT_INITIAL_SIZE];
//		this.capacity = DEFAULT_INITIAL_SIZE * 8;
		bitsLeft = 64;
	}
	
	
	
	public void writeByte(int value) {
		ensureSizeBytes(1);
	}

	private void ensureSizeBytes(int num) {
		
	}
}
