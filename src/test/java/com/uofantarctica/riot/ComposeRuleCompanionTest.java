package com.uofantarctica.riot;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static com.uofantarctica.riot.ComposeRuleTest.COMPOSE_HUBS;

public class ComposeRuleCompanionTest {
	private static final Logger log = LoggerFactory.getLogger(ComposeRuleCompanionTest.class);
	private RiotLatch latch;
	private final long timeout = 30000L;

	@Rule
	public RiotRule riotRule = new RiotRule("apacheignite", 47500, timeout, 0);

	@Before
	public void setUp() {
		//riotRule = new RiotRule("apacheignite", 47500, timeout, TEST_HUBS, "blah", 47500);
		log.debug("Starting set up for companion test.");
		latch = riotRule.countDownLatch( "integrationTestCountDownLatch", COMPOSE_HUBS);
		log.debug("Finish set up for companion test.");
	}

	@After
	public void tearDown() {
		riotRule.close();
	}

	@Test
	public void startIgniteAndSyncTest() {
		log.debug("Countdown latch.");
		Assert.assertTrue("Latch should have decremented to below the max count.",
				latch.countDown() < COMPOSE_HUBS);
		log.debug("Wait for clients.");
		Assert.assertTrue("Ignite clients did not countdown the latch.",
				latch.await(timeout, TimeUnit.MILLISECONDS));
		log.debug("We did it");
	}
}
