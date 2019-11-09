import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ComputeIM {
    public static void main(String[] args) throws IOException {
        String linkPath = "data/twitter_combined.txt";
        String weightPath = "src/main/resources/data/twitter_graph.txt";
        String outputPath = "outfile.txt";
        Map<Long, Map<Long, Float>> nodeWeightMap = TwitterCombinedReader.getNodeMapFromFile(weightPath);
        Map<Long, Integer> weightMap = nodeWeightMap.keySet().parallelStream().collect(Collectors.toMap(l -> l, l -> nodeWeightMap.get(l).size()));
        Long key = weightMap.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();
        long mill = System.currentTimeMillis();
        Set<Long> resultSet = IndependentCascade.doIndependentCascade(key, nodeWeightMap);

        System.out.println(resultSet.size());
        System.out.println(System.currentTimeMillis() - mill);
    }
}
