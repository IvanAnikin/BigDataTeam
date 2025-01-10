package software.cheeselooker.control;

import software.cheeselooker.exceptions.QueryEngineException;


import java.util.List;
import java.util.Map;


public interface Command {
    void execute() throws QueryEngineException;
    List<Map<String, Object>> execute2(String Input) throws QueryEngineException;
}
