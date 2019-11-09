import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class IndependentCascade {

    Set<Long> visitedSet = new HashSet<>();

    public static Set<Long> doIndependentCascade(Long key, Map<Long, Map<Long, Float>> inputNodeMap) {
        Set<Long> returnActivatedSet = new HashSet<>();
        Set<Long> activatedSet = new HashSet<>();
        Set<Long> visitedSet = new HashSet<>();
        activatedSet.add(key);
        do {
            Map<Long, Float> concurrentNodeValueMap = new ConcurrentHashMap<>();
            activatedSet.parallelStream().forEach(l -> {
                Map<Long, Float> linkedMap = inputNodeMap.get(l);
                if (linkedMap != null) {
                    linkedMap.keySet().parallelStream().filter(k -> !visitedSet.contains(k)).forEach(k -> {
                        if (concurrentNodeValueMap.get(k) == null) {
                            concurrentNodeValueMap.put(k, linkedMap.get(k));
                        } else {
                            concurrentNodeValueMap.put(k, concurrentNodeValueMap.get(k) + linkedMap.get(k));
                        }
                    });
                    visitedSet.addAll(linkedMap.keySet());
                }
            });
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
