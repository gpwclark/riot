package com.uofantarctica.riot;

import org.apache.ignite.IgniteCountDownLatch;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static com.uofantarctica.riot.IgnitionAwaitTest.TEST_HUBS;

public class RiotRuleTest {
	private static final Logger log = LoggerFactory.getLogger(RiotRuleTest.class);
	RiotLatch latch;
	private final long timeout = 30000L;

	@Rule
	public RiotRule riotRule = new RiotRule("ignite", 47500, timeout, TEST_HUBS);

	@Before
	public void setUp() {
		latch = riotRule.countDownLatch( "integrationTestCountDownLatch", TEST_HUBS);
	}

	@After
	public void tearDown() {
		riotRule.close();
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
