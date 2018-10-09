package com.uofantarctica.riot;

import com.palantir.docker.compose.connection.DockerPort;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCountDownLatch;
import org.apache.ignite.Ignition;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class IgnitionAwaitTest {
	private static final Logger log = LoggerFactory.getLogger(IgnitionAwaitTest.class);
	public static final int TEST_HUBS = 3;
	public static Ignite ignite;

	IgniteCountDownLatch latch;
	long timeout = 30000L;

	@Before
	public void setUp() {
		ignite = Ignition.start(getClass().getClassLoader().getResource("ignite-client.xml"));
		latch = ignite.countDownLatch("integrationTestCountDownLatch",
				TEST_HUBS,
				false,
				true);
	}

	@After
	public void tearDown() {
		ignite.close();
	}

	@Test
	public void startIgniteAndSyncTest() {
		log.debug("Countdown latch.");
		Assert.assertTrue("Latch should have decremented to below the max count.", latch.countDown() < TEST_HUBS);
		log.debug("Wait for clients.");
		Assert.assertTrue("Ignite clients did not countdown the latch.",
				latch.await(timeout, TimeUnit.MILLISECONDS));
	}
}
