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
        
        // Configure TCP/IP discovery across local network
        network.getJoin().getMulticastConfig().setEnabled(false);
        network.getJoin().getTcpIpConfig()
            .addMember("192.168.1.*") // Broad range for local network machines
            .setEnabled(true);

        network.getJoin().getTcpIpConfig().setConnectionTimeoutSeconds(30);
        config.setProperty("hazelcast.max.no.heartbeat.seconds", "60");
        config.setProperty("hazelcast.logging.level", "DEBUG");

        return Hazelcast.newHazelcastInstance(config);
    }
}
