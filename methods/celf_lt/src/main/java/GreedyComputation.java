import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GreedyComputation {
    public static final int greedyCandidateSetSize = 3000;
    public static final int greedySeedSetSize = 1000;

    public static Set<Long> compute(Map<Long, Map<Long, Float>> nodeWeightMap, Map<Long, Integer> nodeEdgeCountMap) {
        Set<Long> candidateSet = getCandidateSet(nodeEdgeCountMap);
        Map<Long, Set<Long>> currentExpansions = new ConcurrentHashMap<>();
        Map<Long, Set<Long>> sumExpansions = new HashMap<>();
        Long startTime = System.currentTimeMillis();
        Set<Long> seedSet = new HashSet<>();
        Set<Long> lastExpansion = new HashSet<>();
        while (seedSet.size() < greedySeedSetSize) {
            //lastExpansion must be effectively final
            Set<Long> finalLastExpansion = lastExpansion;
            candidateSet.parallelStream().forEach(node -> {
                Set<Long> tempSet = new HashSet<>(seedSet);
                tempSet.add(node);
                Set<Long> tempExpansionSet = new HashSet<>(finalLastExpansion);
                Set<Long> resultSet = IndependentCascade.doIndependentCascade(tempSet, nodeWeightMap);
                tempExpansionSet.addAll(resultSet);
                sumExpansions.put(node, tempExpansionSet);
                currentExpansions.put(node, resultSet);
            });

            Long seed = sumExpansions.entrySet().stream()
                    .max(Comparator.comparingInt(k -> k.getValue().size())).get().getKey();
            lastExpansion = currentExpansions.get(seed);
            int currentExpansionSize = currentExpansions.get(seed).size();
            seedSet.add(seed);
            candidateSet.remove(seed);
            sumExpansions.remove(seed);
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

