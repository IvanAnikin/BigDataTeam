package software.cheeselooker.control;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastConfig {

    public HazelcastInstance getHazelcastInstance() {
        Config config = new Config();
        
        config.setClusterName("shared-cluster");
        config.getJetConfig().setEnabled(true);

        NetworkConfig network = config.getNetworkConfig();
        network.setPort(5701).setPortAutoIncrement(true);

        // Enable TCP/IP discovery
        network.getJoin().getMulticastConfig().setEnabled(false);
        network.getJoin().getTcpIpConfig()
            .addMember("10.26.14.217") // Replace with the IP of the first computer
            .addMember("10.26.14.218") // Replace with the IP of the second computer
            .setEnabled(true);

        config.setProperty("hazelcast.max.no.heartbeat.seconds", "60");
        config.setProperty("hazelcast.logging.level", "DEBUG");

        return Hazelcast.newHazelcastInstance(config);
    }
}