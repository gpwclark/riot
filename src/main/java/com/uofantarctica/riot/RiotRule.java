package com.uofantarctica.riot;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCountDownLatch;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.BasicAddressResolver;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.DiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RiotRule implements TestRule {
	private static final Logger log = LoggerFactory.getLogger(RiotRule.class);
	private final Ignite ignite;
	private final int riotSize;
	private static final long DEFAULT_TIMEOUT = 45000L;
	private long timeout = DEFAULT_TIMEOUT;
	private final static int LOCAL_IGNITE_DISCOVERY = 47501;
	private final static int LOCAL_IGNITE_COMMS = 47101;

	public RiotRule(int riotSize) {
		this.riotSize = riotSize;
		ignite = Ignition
				.start(getClass().getClassLoader().getResource("ignite-client.xml").getFile());
	}

	public RiotRule(int riotSize, long timeout) {
		this(riotSize);
		this.timeout = timeout;
	}

	public RiotRule(String ip, int port, long timeout, int riotSize) {
		this.timeout = timeout;
		IgniteConfiguration igniteConfiguration = new IgniteConfiguration();

		TcpDiscoveryVmIpFinder tcpDiscoveryVmIpFinder = new TcpDiscoveryVmIpFinder();
		tcpDiscoveryVmIpFinder.registerAddresses(getAddresses(ip, port));

		TcpDiscoverySpi tcpDiscoverySpi = new TcpDiscoverySpi();
		tcpDiscoverySpi.setLocalPort(LOCAL_IGNITE_DISCOVERY);
		tcpDiscoverySpi.setIpFinder(tcpDiscoveryVmIpFinder);

		igniteConfiguration.setNetworkTimeout(timeout)
				.setCommunicationSpi(new TcpCommunicationSpi().setLocalPort(LOCAL_IGNITE_COMMS))
				.setDiscoverySpi(tcpDiscoverySpi);

		ignite = Ignition.start(igniteConfiguration);
		this.riotSize = riotSize;
	}

	public RiotRule(String ip, int port, long timeout, int riotSize, String resolvIp, int resolvPort) {
		this.timeout = timeout;
		IgniteConfiguration igniteConfiguration = new IgniteConfiguration();

		setAddressResolver(igniteConfiguration, ip, port);

		TcpDiscoverySpi tcpDiscoverySpi = new TcpDiscoverySpi();
		tcpDiscoverySpi.setLocalPort(LOCAL_IGNITE_DISCOVERY);

		/*
		TcpDiscoveryVmIpFinder tcpDiscoveryVmIpFinder = new TcpDiscoveryVmIpFinder();
		tcpDiscoveryVmIpFinder.registerAddresses(getAddresses(ip, port));
		tcpDiscoverySpi.setIpFinder(tcpDiscoveryVmIpFinder);
		*/


		igniteConfiguration.setNetworkTimeout(timeout)
						.setCommunicationSpi(new TcpCommunicationSpi().setLocalPort(LOCAL_IGNITE_COMMS))
						.setDiscoverySpi(tcpDiscoverySpi);

		ignite = Ignition.start(igniteConfiguration);
		this.riotSize = riotSize;
	}

	public List<InetSocketAddress> getAddresses(String ip, int port) {
		InetSocketAddress addr = new InetSocketAddress(ip, port);
		List<InetSocketAddress> addresses = new ArrayList<>();
		addresses.add(addr);
		return addresses;
	}

	public void setAddressResolver(IgniteConfiguration config, String ip, int port) {
		BasicAddressResolver addressResolver = null;
		Map<String, String> address = new HashMap<>();
		address.put(ip, Integer.toString(port));
		try {
			addressResolver = new BasicAddressResolver(address);
			config.setAddressResolver(addressResolver);
		} catch (UnknownHostException e) {
			log.error("Failed to find host: {} on port: {}", ip, port, e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public Statement apply(Statement base, Description description) {
		return new RiotStatement(base, description);
	}

	@Rule
	public RiotLatch countDownLatch(String name, int count) {
		return new RiotLatch(ignite.countDownLatch(name,
				count,
				true,
				true));
	}

	public void close() {
		ignite.close();
	}

	private final class RiotStatement extends Statement {

		private final Statement base;
		private final Description description;

		private RiotStatement(Statement base, Description description) {
			this.base = base;
			this.description = description;
		}

		@Override
		public void evaluate() throws Throwable {
			try {
				if (riotSize > 0) {
					waitOnPeers();
				}
				log.debug("run test");
				base.evaluate();
				log.debug("Test passed.");
			} catch (Throwable t) {
				log.debug("Test failed.");
				throw t;
			}
		}
	}

	private void waitOnPeers() {
		log.debug("Get latch.");
		IgniteCountDownLatch latch = ignite.countDownLatch("riotRuleStartLatch",
				riotSize,
				false,
				true);
		log.debug("countdown.");
		latch.countDown();
		log.debug("wait for {}ms", timeout);
		Assert.assertTrue("Did not discover any peers", latch.await(timeout, TimeUnit.MILLISECONDS));
	}
}
