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

package ch.bind.philib.lang;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import ch.bind.philib.TestUtil;

public class ServiceStateTest {

	@Test
	public void stateSequence() {
		ServiceState state = new ServiceState();
		verify(state, true, false, false, false);

		state.setOpen();
		verify(state, false, true, false, false);
		state.setOpen();
		verify(state, false, true, false, false);

		state.setClosing();
		verify(state, false, false, true, false);
		state.setClosing();
		verify(state, false, false, true, false);

		state.setClosed();
		verify(state, false, false, false, true);
		state.setClosed();
		verify(state, false, false, false, true);
	}

	@Test
	public void onlyForward() {
		ServiceState state = new ServiceState();
		verify(state, true, false, false, false);

		state.setClosing();
		verify(state, false, false, true, false);

		try {
			state.setOpen();
			fail();
		} catch (Exception e) {
			assertTrue(e instanceof IllegalStateException);
			verify(state, false, false, true, false);
		}

		state.setClosed();
		verify(state, false, false, false, true);

		try {
			state.setOpen();
			fail();
		} catch (Exception e) {
			assertTrue(e instanceof IllegalStateException);
			verify(state, false, false, false, true);
		}
	}

	@Test(timeOut = 1000)
	public void awaitStates() throws InterruptedException {
		final ServiceState state = new ServiceState();
		assertFalse(state.isOpen());

		long startTime = System.nanoTime();
		new Thread(new Runnable() {

			@Override
			public void run() {
				TestUtil.sleepOrFail(50);
				state.setOpen();
			}
		}).start();

		state.awaitOpen();
		assertTrue(state.isOpen());

		new Thread(new Runnable() {

			@Override
			public void run() {
				TestUtil.sleepOrFail(50);
				state.setClosing();
			}
		}).start();

		state.awaitClosing();
		assertTrue(state.isClosing());

		new Thread(new Runnable() {

			@Override
			public void run() {
				TestUtil.sleepOrFail(50);
				state.setClosed();
			}
		}).start();

		state.awaitClosed();
		assertTrue(state.isClosed());

		long totalTime = System.nanoTime() - startTime;
		assertTrue(totalTime > 140 * 1000 * 1000);
		assertTrue(totalTime < 250 * 1000 * 1000);
	}

	private static void verify(ServiceState state, boolean uninit, boolean open, boolean closing, boolean closed) {
		assertEquals(state.isUninitialized(), uninit);
		assertEquals(state.isOpen(), open);
		assertEquals(state.isClosing(), closing);
		assertEquals(state.isClosed(), closed);
		if (closing || closed) {
			assertTrue(state.isClosingOrClosed());
		} else {
			assertFalse(state.isClosingOrClosed());
		}
	}
}
