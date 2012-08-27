package ch.bind.philib.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class FinishedFuture<T> implements Future<T> {

	private final T value;

	public FinishedFuture(T value) {
		this.value = value;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		// cancellation is impossible because this task finished already
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return true;
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		return value;
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return value;
	}
}
