/*
 * Copyright (c) 2012 Philipp Meinen <philipp@bind.ch>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ch.bind.philib.util;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import ch.bind.philib.io.SafeCloseUtil;
import ch.bind.philib.lang.ServiceState;

/**
 * TODO
 * 
 * @author Philipp Meinen
 */

public class MappedLongList implements Closeable {

	// 1GiB
	private static final long MAP_BYTES_PER_BUCKET = 1024 * 1024 * 1024;

	private final RandomAccessFile raf;

	private final FileChannel channel;

	private final long numElements;

	private final MappedByteBuffer[] buckets;

	private final ServiceState state = new ServiceState();

	public MappedLongList(RandomAccessFile raf, FileChannel channel, long numElements, MappedByteBuffer[] buckets) {
		this.raf = raf;
		this.channel = channel;
		this.numElements = numElements;
		this.buckets = buckets;
		state.setOpen();
	}

	@Override
	public void close() {
		if (state.isClosingOrClosed()) {
			return;
		}
		state.setClosing();
		for (int i = 0; i < buckets.length; i++) {
			buckets[i] = null;
		}
		SafeCloseUtil.close(channel);
		SafeCloseUtil.close(raf);
		state.setClosed();
	}

	public static MappedLongList map(String filename, long offset, long numElements) throws FileNotFoundException, IOException {
		RandomAccessFile raf = null;
		long totalMapSize = numElements * 8; // sizeof(long)
		int numBuckets = 1;
		if (totalMapSize > MAP_BYTES_PER_BUCKET) {
			numBuckets = (int) (totalMapSize / MAP_BYTES_PER_BUCKET);
			if ((numBuckets * MAP_BYTES_PER_BUCKET) < totalMapSize) {
				// round up
				numBuckets++;
			}
		}
		MappedByteBuffer[] buckets = new MappedByteBuffer[numBuckets];
		try {
			raf = new RandomAccessFile(filename, "rw");
			FileChannel channel = raf.getChannel();
			for (int bucket = 0; bucket < numBuckets; bucket++) {
				long position = offset + (bucket * MAP_BYTES_PER_BUCKET);
				long size = Math.max(MAP_BYTES_PER_BUCKET, totalMapSize - (bucket * MAP_BYTES_PER_BUCKET));
				MappedByteBuffer mbb = channel.map(MapMode.READ_WRITE, position, size);
				buckets[bucket] = mbb;
			}
			return new MappedLongList(raf, channel, numElements, buckets);
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			SafeCloseUtil.close(raf);
			throw e;
		}
	}

}
