/*
 * Copyright (c) 2006-2011 Philipp Meinen <philipp@bind.ch>
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

package ch.bind.philib.io;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class RingImplTest {

	@Test
	public void f() {
		Ring<Integer> ring = new RingImpl<Integer>();

		// 0..999
		for (int i = 0; i < 1000; i++) {
			ring.addBack(i);
		}
		// 1999..1234,0..999
		for (int i = 1234; i < 2000; i++) {
			ring.addFront(i);
		}
		// 1999..1234,0..999,-1..-99
		for (int i = -1; i > -100; i--) {
			ring.addBack(i);
		}

		// reduce to 0..999,-1..-99
		for (int i = 1999; i >= 1234; i--) {
			assertEquals(ring.poll(), Integer.valueOf(i));
		}

		// reduce to -1..-99
		for (int i = 0; i < 1000; i++) {
			assertEquals(ring.poll(), Integer.valueOf(i));
		}

		// reduce to empty
		for (int i = -1; i > -100; i--) {
			assertEquals(ring.poll(), Integer.valueOf(i));
		}

		assertNull(ring.poll());
	}
}
