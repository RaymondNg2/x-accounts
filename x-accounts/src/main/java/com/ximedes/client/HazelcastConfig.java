package com.ximedes.client;

import com.hazelcast.config.Config;
import com.hazelcast.config.ExecutorConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastConfig {

	public static final String HAZELCAST_NAME = "x-accounts";
	public static final String HAZELCAST_PASSWORD = "x-accounts.hazelcast.password";
	public static final String HAZELCAST_MULTICASTGROUP = "232.57.108.73";

	public static HazelcastInstance hazelcastInstance() {

		Config config = new Config();
		config.getGroupConfig().setName(HAZELCAST_NAME);
		config.getGroupConfig().setPassword(HAZELCAST_PASSWORD);

		JoinConfig join = config.getNetworkConfig().getJoin();
		join.getMulticastConfig().setEnabled(true).setMulticastGroup(HAZELCAST_MULTICASTGROUP);
		ExecutorConfig executorConfig = config.getExecutorConfig("executor");
		executorConfig.setPoolSize(1).setQueueCapacity(50).setStatisticsEnabled(true);

		return Hazelcast.newHazelcastInstance(config);
	}
}
