import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class TwitterCombinedReader {

    public static Map<Long, Map<Long, Float>> getNodeMapFromFile(InputStream inputStream) throws IOException {
        Map<Long, Map<Long, Float>> returnNodeMap = new HashMap<>();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line = bufferedReader.readLine();
            while (line != null) {
                String lines[] = line.split("\\s+");
                Long nodeId = Long.parseLong(lines[1]);
                Map<Long, Float> nodeMap1 = returnNodeMap.getOrDefault(nodeId, new HashMap<>());
                nodeMap1.put(Long.parseLong(lines[0]), Float.parseFloat(lines[2]));
                returnNodeMap.putIfAbsent(nodeId,nodeMap1);
                line = bufferedReader.readLine();
            }
        }
        return returnNodeMap;
    }
}
