import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ComputeIM {
    public static final int currentCandidateSetSize = 300;
    public static final int seedSetSize = 100;

    public static void main(String[] args) throws IOException {
        String weightPath = "src/main/resources/data/twitter_graph.txt";
        Map<Long, Map<Long, Float>> nodeWeightMap = TwitterCombinedReader.getNodeMapFromFile(weightPath);
        Map<Long, Integer> weightMap = nodeWeightMap.keySet().parallelStream().collect(Collectors.toMap(l -> l, l -> nodeWeightMap.get(l).size()));
        Set<Long> candidateSet = weightMap.keySet().stream().sorted(Comparator.comparingInt(weightMap::get).reversed()).limit(currentCandidateSetSize).collect(Collectors.toSet());
        //Set<Long> resultSet = CELFComputation.doCELF(candidateSet, nodeWeightMap);
        Set<Long> resultSet = GreedyComputation.compute(candidateSet, nodeWeightMap);
        System.out.println(resultSet);
        System.out.println(System.currentTimeMillis());
    }
}
