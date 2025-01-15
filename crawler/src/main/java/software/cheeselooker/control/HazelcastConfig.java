package software.cheeselooker.control;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastConfig {

    public HazelcastInstance getHazelcastInstance() {
        Config config = new Config();
        
        config.setClusterName("shared-cluster");
        config.getJetConfig().setEnabled(true);

        // Network configuration
        NetworkConfig network = config.getNetworkConfig();
        network.setPort(5701).setPortAutoIncrement(true);

        // Join configuration: Enable multicast
        JoinConfig join = network.getJoin();
        join.getMulticastConfig()
            .setEnabled(true)
            .setMulticastGroup("224.2.2.3") // Multicast group (can be customized)
            .setMulticastPort(54327)        // Multicast port
            .setMulticastTimeoutSeconds(2) // Timeout for multicast discovery
            .addTrustedInterface("192.168.1.*"); // Optional: restrict to local network

        // Disable other join methods to prevent conflicts
        join.getTcpIpConfig().setEnabled(false);
        join.getAwsConfig().setEnabled(false);
        
        // Set Hazelcast properties
        config.setProperty("hazelcast.max.no.heartbeat.seconds", "60");
        config.setProperty("hazelcast.logging.level", "DEBUG");

        return Hazelcast.newHazelcastInstance(config);
    }
}
