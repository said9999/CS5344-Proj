import java.util.*;
import java.util.stream.Collectors;

public class CELFComputation {
    public static Set<Long> doCELF(Set<Long> candidateSet, Map<Long, Map<Long, Float>> nodeWeightMap) {
        Set<Long> resultSet = new HashSet<>();
        Map<Long, Set<Long>> currentExpansions = new HashMap<>();
        Set<Long> seedSet = new HashSet<>();
        candidateSet.parallelStream().forEach(node -> {
            Set<Long> tempSet = new HashSet<>(seedSet);
            seedSet.add(node);
            currentExpansions.put(node, IndependentCascade.doIndependentCascade(seedSet, nodeWeightMap));
        });
        List<Long> sortedByScoreSet = currentExpansions.entrySet().stream()
                .sorted(Comparator.comparingInt(k -> currentExpansions.get(k).size()).reversed())
                .map(k -> k.getKey()).collect(Collectors.toList());
        seedSet.add(sortedByScoreSet.get(0));
        candidateSet.remove(sortedByScoreSet.get(0));
        while (resultSet.size() < ComputeIM.seedSetSize) {

        }
        return resultSet;
    }
}
