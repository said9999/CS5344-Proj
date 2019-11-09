import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ComputeIM {

    public static void main(String[] args) throws IOException {
        String weightPath = "src/main/resources/data/twitter_graph.txt";
        Map<Long, Map<Long, Float>> nodeWeightMap = TwitterCombinedReader.getNodeMapFromFile(weightPath);
        Map<Long, Integer> weightMap = nodeWeightMap.keySet().parallelStream().collect(Collectors.toMap(l -> l, l -> nodeWeightMap.get(l).size()));

        Set<Long> resultSet = GreedyComputation.compute(nodeWeightMap, weightMap);
        System.out.println(resultSet);
        System.out.println(System.currentTimeMillis());
    }
}
