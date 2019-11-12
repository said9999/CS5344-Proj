import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LinearThreshold {

    public static Set<Long> doLinearThreshold(Set<Long> seedSet, Map<Long, Map<Long, Float>> inputNodeMap) {
        Map<Long, Float> thresholdMap = new HashMap<>();
        Random random = new Random();
        inputNodeMap.keySet().forEach(k -> thresholdMap.put(k, random.nextFloat()));
        Set<Long> returnActivatedSet = new HashSet<>();
        Set<Long> visitedSet = new HashSet<>();
        Set<Long> activatedSet = new HashSet<>(seedSet);
        do {
            Map<Long, Float> concurrentNodeValueMap = new HashMap<>();
            activatedSet.forEach(l -> {
                Map<Long, Float> linkedMap = inputNodeMap.get(l);
                if (linkedMap != null) {
                    linkedMap.keySet().stream().filter(k -> !visitedSet.contains(k)).forEach(k -> {
                        if (concurrentNodeValueMap.get(k) == null) {
                            concurrentNodeValueMap.put(k, linkedMap.get(k));
                        } else {
                            concurrentNodeValueMap.put(k, concurrentNodeValueMap.get(k) + linkedMap.get(k));
                        }
                    });
                }
            });
            activatedSet.stream().filter(l -> inputNodeMap.get(l) != null).forEach(l -> visitedSet.addAll(inputNodeMap.get(l).keySet()));
//            System.out.println(visitedSet.size());
            activatedSet = concurrentNodeValueMap.keySet().stream().filter(k -> thresholdMap.get(k) != null)
                    .filter(k -> !(thresholdMap.get(k) > concurrentNodeValueMap.get(k))).collect(Collectors.toSet());
            returnActivatedSet.addAll(activatedSet);
        } while (activatedSet.size() > 0);
        return returnActivatedSet;
    }
}
