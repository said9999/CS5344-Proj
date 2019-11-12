import java.util.*;
import java.util.stream.Collectors;

public class CELFComputation {
    public static final int currentCandidateSetSize = 3000;
    public static final int seedSetSize = 10;

    public static Set<Long> compute(Map<Long, Map<Long, Float>> nodeWeightMap, Map<Long, Integer> nodeEdgeCountMap) {
        Set<Long> candidateSet = getCandidateSet(nodeEdgeCountMap);
        Map<Long, Set<Long>> currentExpansions = new HashMap<>();
        Map<Long, Integer> marginGainMap = new HashMap<>();
        long startTime = System.currentTimeMillis();
        Set<Long> seedSet = new HashSet<>();
        candidateSet.parallelStream().forEach(node -> {
            Set<Long> tempSet = new HashSet<>(seedSet);
            tempSet.add(node);
            Set<Long> result = LinearThreshold.doLinearThreshold(tempSet, nodeWeightMap);
            currentExpansions.put(node, result);
            marginGainMap.put(node, result.size());
        });

        List<Long> seedList = currentExpansions.entrySet().stream()
                .sorted(Comparator.comparingInt(k -> k.getValue().size()))
                .map(Map.Entry::getKey).collect(Collectors.toList());
        Long seed = seedList.get(0);

        out:
        while (seedSet.size() < seedSetSize) {
            Set<Long> currentSeedExpansions = currentExpansions.get(seed);
            seedSet.add(seed);

            System.out.println(seedSet);
            System.out.println(currentSeedExpansions.size());
            System.out.println("Completed one iteration, spent: " + (System.currentTimeMillis() - startTime) + "ms");

            seedList.remove(seed);
            currentExpansions.remove(seed);
            candidateSet.remove(seed);
            marginGainMap.remove(seed);

            for (Long node : candidateSet) {
                Set<Long> tempSet = new HashSet<>(seedSet);
                tempSet.add(node);
                Set<Long> result = LinearThreshold.doLinearThreshold(tempSet, nodeWeightMap);
                currentExpansions.put(node, result);
                Set<Long> comparisonSet = new HashSet<>(currentSeedExpansions);
                comparisonSet.addAll(result);
                int marginGain = comparisonSet.size() - currentSeedExpansions.size();
                marginGainMap.put(node, marginGain);
                if (marginGain > marginGainMap.get(seedList.get(0))) {
                    seed = node;
                    continue out;
                }
            }
            seedList = marginGainMap.entrySet().stream()
                    .sorted(Comparator.comparingInt(Map.Entry::getValue))
                    .map(Map.Entry::getKey).collect(Collectors.toList());
            seed = seedList.get(0);

        }

        return seedSet;
    }

    private static Set<Long> getCandidateSet(Map<Long, Integer> nodeEdgeCountMap) {
        Set<Long> candidateSet = nodeEdgeCountMap.keySet().stream().sorted(Comparator.comparingInt(nodeEdgeCountMap::get).reversed()).limit(currentCandidateSetSize).collect(Collectors.toSet());
        return candidateSet;
//        return new HashSet<>(nodeEdgeCountMap.keySet());
    }
}
