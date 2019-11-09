import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class IndependentCascade {

    public static Set<Long> doIndependentCascade(Set<Long> seedSet, Map<Long, Map<Long, Float>> inputNodeMap) {
        Set<Long> returnActivatedSet = new HashSet<>();
        Set<Long> visitedSet = ConcurrentHashMap.newKeySet();
        Set<Long> activatedSet = new HashSet<>();
        activatedSet.addAll(seedSet);
        do {
            Map<Long, Float> concurrentNodeValueMap = new ConcurrentHashMap<>();
            activatedSet.parallelStream().forEach(l -> {
                Map<Long, Float> linkedMap = inputNodeMap.get(l);
                if (linkedMap != null) {
                    linkedMap.keySet().parallelStream().filter(k -> !visitedSet.contains(k)).forEach(k -> {
                        synchronized (k) {
                            if (concurrentNodeValueMap.get(k) == null) {
                                concurrentNodeValueMap.put(k, linkedMap.get(k));
                            } else {
                                concurrentNodeValueMap.put(k, concurrentNodeValueMap.get(k) + linkedMap.get(k));
                            }
                        }
                    });
                }
            });
            activatedSet.parallelStream().filter(l -> inputNodeMap.get(l) != null).forEach(l -> visitedSet.addAll(inputNodeMap.get(l).keySet()));
//            System.out.println(visitedSet.size());
            activatedSet = concurrentNodeValueMap.keySet().parallelStream().filter(k -> {
                if (Math.random() > concurrentNodeValueMap.get(k)) {
                    return false;
                }
                return true;
            }).collect(Collectors.toSet());
            returnActivatedSet.addAll(activatedSet);
        } while (activatedSet.size() > 0);
        return returnActivatedSet;
    }
}
