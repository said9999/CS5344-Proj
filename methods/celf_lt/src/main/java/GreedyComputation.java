import java.util.*;
import java.util.stream.Collectors;

public class GreedyComputation {


    public static final int greedyCandidateSetSize = 300;
    public static final int greedySeedSetSize = 100;

    public static Set<Long> compute(Map<Long, Map<Long, Float>> nodeWeightMap, Map<Long, Integer> nodeEdgeCountMap) {
        Set<Long> candidateSet = getCandidateSet(nodeEdgeCountMap);
        Map<Long, Set<Long>> currentExpansions = new HashMap<>();
        Long startTime = System.currentTimeMillis();
        Set<Long> seedSet = new HashSet<>();
        int currentExpansionSize;
        while (seedSet.size() < greedySeedSetSize) {
            candidateSet.parallelStream().forEach(node -> {
                Set<Long> tempSet = new HashSet<>(seedSet);
                tempSet.add(node);
                currentExpansions.put(node, IndependentCascade.doIndependentCascade(tempSet, nodeWeightMap));
            });

            Long seed = currentExpansions.entrySet().stream()
                    .max(Comparator.comparingInt(k -> k.getValue().size())).get().getKey();
            seedSet.add(seed);
            currentExpansionSize = currentExpansions.get(seed).size();
            candidateSet.remove(seed);
            currentExpansions.remove(seed);
            System.out.println(seedSet);
            System.out.println(currentExpansionSize);
            System.out.println("Completed one iteration, spent: " + (System.currentTimeMillis() - startTime) + "ms");
        }
        return seedSet;
    }

    private static Set<Long> getCandidateSet(Map<Long, Integer> nodeEdgeCountMap) {
        Set<Long> candidateSet = nodeEdgeCountMap.keySet().stream().sorted(Comparator.comparingInt(nodeEdgeCountMap::get).reversed()).limit(greedyCandidateSetSize).collect(Collectors.toSet());
        return candidateSet;
    }
}