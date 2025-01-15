
package software.cheeselooker.control;


import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;


public class HazelcastConfig {

    public HazelcastInstance getHazelcastInstance() {
        Config config = new Config();
        
        config.setClusterName("shared-cluster");
        config.getJetConfig().setEnabled(true);

        NetworkConfig network = config.getNetworkConfig();
        network.setPort(5701).setPortAutoIncrement(true);
        network.getJoin().getTcpIpConfig()
            .addMember("193.145.130.8") 
            .addMember("193.145.130.7") 
            .setEnabled(true);
            
        network.getJoin().getMulticastConfig().setEnabled(false);

        network.getJoin().getTcpIpConfig().setConnectionTimeoutSeconds(30);
        config.setProperty("hazelcast.max.no.heartbeat.seconds", "60");
        config.setProperty("hazelcast.logging.level", "DEBUG");


        return Hazelcast.newHazelcastInstance(config);
    }
}