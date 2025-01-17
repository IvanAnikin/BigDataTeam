
package software.cheeselooker.control;


import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;


public class HazelcastConfig {

    public static HazelcastInstance getHazelcastInstance() {

        Config config = new Config();
        
        config.setClusterName("shared-cluster");
        config.getJetConfig().setEnabled(true);

        NetworkConfig network = config.getNetworkConfig();
        network.setPort(5703).setPortAutoIncrement(true);
        network.setPublicAddress(System.getenv("My_IP"));
        network.getJoin().getTcpIpConfig()
            .addMember(System.getenv("HOST_IP")) 
            .setEnabled(true);
            
        network.getJoin().getMulticastConfig().setEnabled(false);

        network.getJoin().getTcpIpConfig().setConnectionTimeoutSeconds(30);

        return Hazelcast.newHazelcastInstance(config);
    }
}
