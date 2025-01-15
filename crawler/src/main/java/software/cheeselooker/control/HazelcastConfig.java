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

        // Enable multicast for automatic discovery
        network.getJoin().getTcpIpConfig().setEnabled(false);
        network.getJoin().getMulticastConfig().setEnabled(true);

        // Optional: Adjust multicast settings if needed
        network.getJoin().getMulticastConfig()
            .setMulticastGroup("224.2.2.3")  // Default multicast group
            .setMulticastPort(54327);        // Default port

        config.setProperty("hazelcast.max.no.heartbeat.seconds", "60");
        config.setProperty("hazelcast.logging.level", "DEBUG");

        return Hazelcast.newHazelcastInstance(config);
    }
}
