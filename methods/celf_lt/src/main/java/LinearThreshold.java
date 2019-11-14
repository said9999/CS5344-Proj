import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LinearThreshold {

    public static Set<Long> doLinearThreshold(Set<Long> seedSet, Map<Long, Map<Long, Float>> inputNodeMap, Map<Long, Integer> indexNodeMap) {
        Map<Long, Float> thresholdMap = new HashMap<>();
        Random random = new Random();
        inputNodeMap.keySet().forEach(k -> thresholdMap.put(k, random.nextFloat()));
        Set<Long> returnActivatedSet = new HashSet<>();
        boolean[] visitedSet = new boolean[indexNodeMap.size()];
        Set<Long> activatedSet = new HashSet<>(seedSet);
        do {
            Map<Long, Float> concurrentNodeValueMap = new ConcurrentHashMap<>();
            activatedSet.parallelStream().forEach(l -> {
                Map<Long, Float> linkedMap = inputNodeMap.get(l);
                if (linkedMap != null) {
                    linkedMap.keySet().stream().filter(k -> indexNodeMap.get(k) != null).filter(k -> !visitedSet[indexNodeMap.get(k)]).forEach(k -> {
                        if (concurrentNodeValueMap.get(k) == null) {
                            concurrentNodeValueMap.put(k, linkedMap.get(k));
                        } else {
                            concurrentNodeValueMap.put(k, concurrentNodeValueMap.get(k) + linkedMap.get(k));
                        }
                    });
                }
            });
            activatedSet.parallelStream().forEach(l -> {
                Map<Long, Float> linkedMap = inputNodeMap.get(l);
                linkedMap.keySet().stream().filter(k -> indexNodeMap.get(k) != null).forEach(k -> visitedSet[indexNodeMap.get(k)] = true);
            });
//            System.out.println(visitedSet.size());
            activatedSet = concurrentNodeValueMap.keySet().parallelStream().filter(k -> thresholdMap.get(k) != null)
                    .filter(k -> thresholdMap.get(k) < concurrentNodeValueMap.get(k)).collect(Collectors.toSet());
            returnActivatedSet.addAll(activatedSet);

        } while (activatedSet.size() > 0);
        return returnActivatedSet;
    }
}
