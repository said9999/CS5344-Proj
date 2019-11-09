import java.util.*;
import java.util.stream.Collectors;

public class CELFComputation {
    public static final int currentCandidateSetSize = 300;
    public static final int seedSetSize = 100;

    public static Set<Long> compute(Map<Long, Map<Long, Float>> nodeWeightMap, Map<Long, Integer> nodeEdgeCountMap) {
        Set<Long> candidateSet = getCandidateSet(nodeEdgeCountMap);
        Map<Long, Set<Long>> currentExpansions = new HashMap<>();
        Map<Long, Integer> marginGainMap = new HashMap<>();
        Long startTime = System.currentTimeMillis();
        Set<Long> seedSet = new HashSet<>();
        int currentExpansionSize;
        candidateSet.parallelStream().forEach(node -> {
            Set<Long> tempSet = new HashSet<>(seedSet);
            tempSet.add(node);
            Set<Long> result = IndependentCascade.doIndependentCascade(tempSet, nodeWeightMap);
            currentExpansions.put(node, result);
            marginGainMap.put(node, result.size());
        });

        List<Long> seedList = currentExpansions.entrySet().stream()
                .sorted(Comparator.comparingInt(k -> k.getValue().size()))
                .map(k -> k.getKey()).collect(Collectors.toList());
        Long seed = seedList.get(0);

        out:
        while (seedSet.size() < seedSetSize) {
            currentExpansionSize = currentExpansions.get(seed).size();
            System.out.println(seedSet);
            System.out.println(currentExpansionSize);
            System.out.println("Completed one iteration, spent: " + (System.currentTimeMillis() - startTime) + "ms");


            int marginGain = currentExpansions.get(seed).size();
            seedSet.add(seed);
            seedList.remove(seed);
            currentExpansions.remove(seed);
            candidateSet.remove(seed);
            marginGainMap.remove(seed);

            for (Long node : candidateSet) {
                Set<Long> tempSet = new HashSet<>(seedSet);
                tempSet.add(node);
                Set<Long> result = IndependentCascade.doIndependentCascade(tempSet, nodeWeightMap);
                currentExpansions.put(node, result);
                int marginGainCurrentNode = result.size() - marginGain;
                marginGainMap.put(node, marginGainCurrentNode);
                if (marginGain > marginGainMap.get(seedList.get(0))) {
                    seed = node;
                    continue out;
                }
            }
            seedList = marginGainMap.entrySet().stream()
                    .sorted(Comparator.comparingInt(k -> k.getValue()))
                    .map(k -> k.getKey()).collect(Collectors.toList());
            seed = seedList.get(0);

        }

        return seedSet;
    }

    private static Set<Long> getCandidateSet(Map<Long, Integer> nodeEdgeCountMap) {
        Set<Long> candidateSet = nodeEdgeCountMap.keySet().stream().sorted(Comparator.comparingInt(nodeEdgeCountMap::get).reversed()).limit(currentCandidateSetSize).collect(Collectors.toSet());
        return candidateSet;
    }
}
