package com.uofantarctica.riot;

import org.apache.ignite.IgniteCountDownLatch;
import org.apache.ignite.IgniteException;

import java.util.concurrent.TimeUnit;

public class RiotLatch implements IgniteCountDownLatch {
	private final IgniteCountDownLatch latch;

	public RiotLatch(IgniteCountDownLatch latch) {
		this.latch = latch;
	}

	@Override
	public String name() {
		return latch.name();
	}

	@Override
	public int count() {
		return latch.count();
	}

	@Override
	public int initialCount() {
		return latch.initialCount();
	}

	@Override
	public boolean autoDelete() {
		return latch.autoDelete();
	}

	@Override
	public void await() throws IgniteException {
		latch.await();
	}

	@Override
	public boolean await(long timeout) throws IgniteException {
		return latch.await(timeout);
	}

	@Override
	public boolean await(long timeout, TimeUnit unit) throws IgniteException {
		return latch.await(timeout, unit);
	}

	@Override
	public int countDown() throws IgniteException {
		return latch.countDown();
	}

	@Override
	public int countDown(int val) throws IgniteException {
		return latch.countDown(val);
	}

	@Override
	public void countDownAll() throws IgniteException {
		latch.countDownAll();
	}

	@Override
	public boolean removed() {
		return false;
	}

	@Override
	public void close() {
		latch.close();
	}
}
