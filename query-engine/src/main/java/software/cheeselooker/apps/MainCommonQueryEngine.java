package software.cheeselooker.apps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import software.cheeselooker.control.Command;
import software.cheeselooker.control.SearchEngineCommand;
import software.cheeselooker.exceptions.QueryEngineException;
import software.cheeselooker.implementations.CommonQueryEngine;
import software.cheeselooker.implementations.SearchInput;
import software.cheeselooker.implementations.SearchOutput;
import software.cheeselooker.ports.Input;
import software.cheeselooker.ports.Output;

import java.nio.file.Path;
import java.nio.file.Paths;


import java.util.List;
import java.util.Map;

@SpringBootApplication
@RestController
public class MainCommonQueryEngine {

    public static void main(String[] args) {
        SpringApplication.run(MainCommonQueryEngine.class, args);
        System.out.println("\nSpringApplication.run");
    }

    @GetMapping("/api/query-engine/search")
    public String search(@RequestParam(name = "query") String query) {

        System.out.println("\nGET request to /api/query-engine/search with query: " + query);

        String response = "";
        
        try {
            Path bookDatalakePath = Paths.get(System.getProperty("user.dir"), "/data/datalake");
            Path invertedIndexPath = Paths.get(System.getProperty("user.dir"), "/data/datamart");
            Path metadataPath = Paths.get(System.getProperty("user.dir"), "/data/metadata/metadata.csv");

            Input input = new SearchInput();
            Output output = new SearchOutput();
            CommonQueryEngine queryEngine = new CommonQueryEngine(
                    metadataPath.toString(),
                    bookDatalakePath.toString(),
                    invertedIndexPath.toString()
            );

            Command searchEngineCommand = new SearchEngineCommand(input, output, queryEngine);

            
            // searchEngineCommand.execute();
            List<Map<String, Object>> results = searchEngineCommand.execute2(query);

            // response = "Test response";
            response = results.toString();

        } catch (QueryEngineException e) {
            response = "Error: " + e.getMessage();
        }

        return response;
    }

    public static class SearchRequest {
        private String query;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }
}
