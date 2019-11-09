import java.util.*;
import java.util.stream.Collectors;

public class GreedyComputation {
    public static Set<Long> compute(Set<Long> candidateSet, Map<Long, Map<Long, Float>> nodeWeightMap) {
        Map<Long, Set<Long>> currentExpansions = new HashMap<>();
        Long startTime = System.currentTimeMillis();
        Set<Long> seedSet = new HashSet<>();
        while (seedSet.size() < ComputeIM.seedSetSize) {
            candidateSet.parallelStream().forEach(node -> {
                Set<Long> tempSet = new HashSet<>(seedSet);
                tempSet.add(node);
                currentExpansions.put(node, IndependentCascade.doIndependentCascade(tempSet, nodeWeightMap));
            });
            Long seed = currentExpansions.entrySet().stream()
                    .max(Comparator.comparingInt(k -> k.getValue().size())).get().getKey();
            seedSet.add(seed);
            System.out.println(seedSet);
            candidateSet.remove(seed);
            currentExpansions.remove(seed);
            System.out.println("Completed one iteration, spent: " + (System.currentTimeMillis() - startTime) + "ms");
        }
        return seedSet;
    }
}