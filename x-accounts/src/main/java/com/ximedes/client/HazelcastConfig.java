package com.ximedes.client;

import static com.hazelcast.core.Hazelcast.newHazelcastInstance;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastConfig {
    private static final String HAZELCAST_NAME = "x-accounts";
    private static final String HAZELCAST_PASSWORD = "x-accounts.hazelcast.password";

    public static HazelcastInstance hazelcastInstance() {
        final Config config = new Config();
        config.getGroupConfig().setName(HAZELCAST_NAME);
        config.getGroupConfig().setPassword(HAZELCAST_PASSWORD);

        final JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        final TcpIpConfig tcpIpConfig = new TcpIpConfig();
        tcpIpConfig.setEnabled(true);
        tcpIpConfig.addMember("172.31.3.74"); // master0
        tcpIpConfig.addMember("172.31.15.49"); // slave0
        joinConfig.setTcpIpConfig(tcpIpConfig);

        final MulticastConfig multicastConfig = new MulticastConfig();
        multicastConfig.setEnabled(false);
        joinConfig.setMulticastConfig(multicastConfig);

        final NetworkConfig networkConfig = new NetworkConfig();
        networkConfig.setJoin(joinConfig);
        networkConfig.setPort(5702);
        networkConfig.setPortAutoIncrement(false);

        return newHazelcastInstance(config);
    }
}
