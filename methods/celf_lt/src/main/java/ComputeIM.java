import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ComputeIM {

    public static void main(String[] args) throws IOException, URISyntaxException {
        long startTime = System.currentTimeMillis();
        InputStream stream = ComputeIM.class.getClassLoader().getResourceAsStream("data/twitter_graph.txt");
        Map<Long, Map<Long, Float>> nodeWeightMap = TwitterCombinedReader.getNodeMapFromFile(stream);
        Map<Long, Integer> weightMap = nodeWeightMap.keySet().parallelStream().collect(Collectors.toMap(l -> l, l -> nodeWeightMap.get(l).size()));

//        Set<Long> resultSet = GreedyComputation.compute(nodeWeightMap, weightMap);
        Set<Long> resultSet = CELFComputation.compute(nodeWeightMap, weightMap);
        try (FileWriter fileWriter = new FileWriter("out.txt")) {
            fileWriter.write(resultSet.stream().map(r -> "'" + r + "'").collect(Collectors.toSet()).toString());
            fileWriter.flush();
        }
        System.out.println(resultSet.stream().map(r -> "'" + r + "'").collect(Collectors.toSet()).toString());
        System.out.println(System.currentTimeMillis() - startTime);
    }
}
