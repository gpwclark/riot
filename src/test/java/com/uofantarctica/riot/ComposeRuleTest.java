package com.uofantarctica.riot;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.DockerPort;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import org.apache.ignite.IgniteCountDownLatch;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.uofantarctica.riot.IgnitionAwaitTest.TEST_HUBS;

public class ComposeRuleTest {
	private static final Logger log = LoggerFactory.getLogger(ComposeRuleTest.class);
	private final long timeout = 120000L;
	private final int IGNITE_DISCOVERY_PORT = 47500;
	public static final String IGNITE_SERVICE = "apacheignite";
	public static final int COMPOSE_HUBS = TEST_HUBS + 1;
	private RiotRule riotRule;
	private RiotLatch latch;

	@Rule
	public DockerComposeRule docker = DockerComposeRule.builder()
		.file("src/test/resources/docker-compose-rule.yml")
		.removeConflictingContainersOnStartup(true)
		.waitingForService(IGNITE_SERVICE, HealthChecks.toHaveAllPortsOpen())
		.build();

	/*
	@Rule
	public RiotRule riotRule = new RiotRule("127.0.0.1", 47500, timeout, COMPOSE_HUBS);

	@Rule
	public RuleChain ruleChain = RuleChain.outerRule(docker)
			.around(riotRule);
			*/

	@Before
	public void setUp() {
		log.debug("compose set up");
		DockerPort dockerPort = docker.containers().container(IGNITE_SERVICE).port(IGNITE_DISCOVERY_PORT);
		riotRule = new RiotRule(dockerPort.getIp(),
				dockerPort.getExternalPort(),
				timeout,
				0,
				"127.0.0.1",
				47500);
		latch = riotRule.countDownLatch( "integrationTestCountDownLatch", COMPOSE_HUBS);
		log.debug("finish compose set up");
	}

	@After
	public void tearDown() throws IOException, InterruptedException {
		//TODO resultsFolder
		riotRule.close();
		docker.dockerCompose().down();
	}

	@Test
	public void startIgniteAndSyncTest() {
		Assert.assertTrue("is true", true);
		log.debug("Countdown latch.");
		Assert.assertTrue("Latch should have decremented to below the max count.", latch.countDown() < COMPOSE_HUBS);
		log.debug("Wait for clients.");
		Assert.assertTrue("Ignite clients did not countdown the latch.",
				latch.await(timeout, TimeUnit.MILLISECONDS));
	}
}
