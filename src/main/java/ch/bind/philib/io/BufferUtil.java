package ch.bind.philib.io;

import java.nio.ByteBuffer;

public final class BufferUtil {

	private BufferUtil() {
	}

	public static ByteBuffer append(ByteBuffer dst, ByteBuffer src) {
		int cap = dst.capacity();
		int pos = dst.position();
		int lim = dst.limit();

		int inSrc = src.remaining();
		int availableBack = cap - lim;
		if (availableBack >= inSrc) {
			// there is enough room at the end of the buffer, make that room
			// visible
			dst.position(lim);
			dst.limit(cap);
			dst.put(src);
			// update position and limit to reflect the old+new data
			dst.position(pos);
			dst.limit(lim + inSrc);
			return dst;
		}
		int inDst = dst.remaining();
		// there is not enough room, but maybe there is some room in front?
		if (pos != 0 && (pos + availableBack) >= inSrc) {
			dst.compact();
			dst.position(inDst);
			dst.limit(cap);
			dst.put(src);
			dst.flip(); // lim=pos ; pos = 0
			return dst;
		}

		int required = inDst + inSrc;
		// a new, bigger buffer is required
		ByteBuffer buf = dst.isDirect() ? //
		ByteBuffer.allocateDirect(required)
				: ByteBuffer.allocate(required);
		buf.put(dst);
		buf.put(src);
		buf.flip(); // pos=0; lim=required
		return buf;
	}
}
