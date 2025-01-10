package ulpgc;

import org.openjdk.jmh.annotations.*;
import software.cheeselooker.control.Command;
import software.cheeselooker.control.CrawlerCommand;
import software.cheeselooker.control.HazelcastConfig;
import software.cheeselooker.implementations.ReaderFromWeb;
import software.cheeselooker.implementations.StoreInDatalake;
import software.cheeselooker.ports.ReaderFromWebInterface;
import software.cheeselooker.ports.StoreInDatalakeInterface;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;


import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 5, time = 1)
public class CrawlerBenchmark {
    @Benchmark
    public void crawler() {
        
        HazelcastInstance hazelcastInstance = new HazelcastConfig().getHazelcastInstance();

        ReaderFromWebInterface reader = new ReaderFromWeb();
        StoreInDatalakeInterface store = new StoreInDatalake(hazelcastInstance);
        Command crawlerCommand = new CrawlerCommand(reader, store, hazelcastInstance);
        
        crawlerCommand.download();
    }
}
