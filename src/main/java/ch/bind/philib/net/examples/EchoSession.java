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

package ch.bind.philib.net.examples;

import java.io.IOException;
import java.nio.ByteBuffer;
import ch.bind.philib.net.PureSessionBase;
import ch.bind.philib.validation.Validation;

public class EchoSession extends PureSessionBase {

	private long lastInteractionNs;

	@Override
	public void receive(ByteBuffer data) {
		lastInteractionNs = System.nanoTime();
		try {
			int rem = data.remaining();
			Validation.isTrue(rem > 0);
			send(data);
			// if (num != rem) {
			// //
			// System.out.printf("cant echo back! only %d out of %d was sent.%n",
			// // num, rem);
			// pendingWrites.addFront(pending);
			// break;
			// }
			// numGoodSends++;
			// releaseBuffer(pending);
			// pending = pendingWrites.poll();
			// }
			// if (numGoodSends > 1) {
			// System.out.println("good sends: " + numGoodSends);
			// }
		} catch (IOException e) {
			e.printStackTrace();
			try {
				close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		// }
	}

	@Override
	public void closed() {
		// this.closed = true;
		System.out.printf("closed() rx=%d, tx=%d%n", getRx(), getTx());
	}

	public long getLastInteractionNs() {
		return lastInteractionNs;
	}
}
