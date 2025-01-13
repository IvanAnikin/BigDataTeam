package software.cheeselooker;

import org.openjdk.jmh.annotations.*;
import software.cheeselooker.exceptions.QueryEngineException;
import software.cheeselooker.implementations.CommonQueryEngine;
import software.cheeselooker.model.QueryEngine;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import software.cheeselooker.control.HazelcastConfig;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 5, time = 1)
public class QueryEngineTest {

    @State(Scope.Benchmark)
    public static class HazelcastState {
        public HazelcastInstance hazelcastInstance;

        @Param({"man", "immediate imminent"})
        public String word;

        @Setup(Level.Trial)
        public void setup() {

            hazelcastInstance = HazelcastConfig.getHazelcastInstance();
        }

        @TearDown(Level.Trial)
        public void tearDown() {
            if (hazelcastInstance != null) {
                hazelcastInstance.shutdown();
            }
        }
    }

    @Benchmark
    public void benchmarkQueryEngine(HazelcastState state) {
        QueryEngine queryEngine = new CommonQueryEngine(state.hazelcastInstance);
        try {
            queryEngine.query(new String[]{state.word});
        } catch (QueryEngineException e) {
            throw new RuntimeException(e);
        }
    }
}
