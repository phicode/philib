/*
 * Copyright (c) 2013 Philipp Meinen <philipp@bind.ch>
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

package wip.test.net.core.tcp;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import wip.src.net.core.Connection;
import wip.src.net.core.tcp.AsyncConnectFuture;
import ch.bind.philib.TestUtil;

public class AsyncConnectFutureTest {

	private Connection conn;

	@BeforeMethod
	public void beforeMethod() {
		conn = Mockito.mock(Connection.class);
	}

	@AfterMethod
	public void afterMethod() {
		Mockito.verifyNoMoreInteractions(conn);
		conn = null;
	}

	@Test(expectedExceptions = TimeoutException.class)
	public void timeoutGet() throws Exception {
		AsyncConnectFuture<Connection> future = new AsyncConnectFuture<Connection>(conn);
		future.get(1, TimeUnit.MILLISECONDS);
	}

	@Test(timeOut = 500)
	public void regularGet() throws Exception {
		final AsyncConnectFuture<Connection> future = new AsyncConnectFuture<Connection>(conn);
		new Thread(new Runnable() {

			@Override
			public void run() {
				TestUtil.sleepOrFail(100);
				future.setFinishedOk();
			}
		}).start();

		assertTrue(future.get() == conn);
		assertTrue(future.get(1, TimeUnit.MILLISECONDS) == conn);
		assertTrue(future.isDone());
		assertFalse(future.isCancelled());
	}

	@Test(timeOut = 500)
	public void getWithExecutionException() throws Exception {
		final AsyncConnectFuture<Connection> future = new AsyncConnectFuture<Connection>(conn);
		final Exception exc = new IOException();
		new Thread(new Runnable() {

			@Override
			public void run() {
				TestUtil.sleepOrFail(100);
				future.setFailed(exc);
			}
		}).start();

		try {
			future.get();
			fail();
		} catch (Exception e) {
			assertTrue(e instanceof ExecutionException);
			assertTrue(e.getCause() == exc);
		}

		try {
			future.get(1, TimeUnit.MILLISECONDS);
			fail();
		} catch (Exception e) {
			assertTrue(e instanceof ExecutionException);
			assertTrue(e.getCause() == exc);
		}
		assertTrue(future.isDone());
		assertFalse(future.isCancelled());
	}

	@Test(timeOut = 500)
	public void cancel() throws Exception {
		final AsyncConnectFuture<Connection> future = new AsyncConnectFuture<Connection>(conn);
		new Thread(new Runnable() {

			@Override
			public void run() {
				TestUtil.sleepOrFail(100);
				assertTrue(future.cancel(true));
			}
		}).start();

		try {
			future.get(250, TimeUnit.MILLISECONDS);
			fail();
		} catch (Exception e) {
			assertTrue(e instanceof CancellationException);
		}
		Mockito.verify(conn).close();
		assertTrue(future.isDone());
		assertTrue(future.isCancelled());
	}

	@Test(timeOut = 500)
	public void noCancelOnDone() throws Exception {
		final AsyncConnectFuture<Connection> future = new AsyncConnectFuture<Connection>(conn);
		future.setFinishedOk();
		assertFalse(future.cancel(true));
		assertTrue(future.get() == conn);
		assertTrue(future.isDone());
		assertFalse(future.isCancelled());
	}
}
