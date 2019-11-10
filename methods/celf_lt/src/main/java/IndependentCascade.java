import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class IndependentCascade {

    public static Set<Long> doIndependentCascade(Set<Long> seedSet, Map<Long, Map<Long, Float>> inputNodeMap) {
        Set<Long> returnActivatedSet = new HashSet<>();
        Set<Long> visitedSet = new HashSet<>();
        Set<Long> activatedSet = new HashSet<>();
        activatedSet.addAll(seedSet);
        do {
//            activatedSet = activatedSet.stream().flatMap(l -> {
//                Map<Long, Float> linkedMap = inputNodeMap.get(l);
//                if (linkedMap != null) {
//                    return linkedMap.keySet().stream().filter(k -> !visitedSet.contains(k)).filter(k -> {
//                        visitedSet.add(k);
//                        if (Math.random() > linkedMap.get(k)) {
//                            return false;
//                        }
//                        return true;
//                    }).collect(Collectors.toSet()).stream();
//                }
//                return new HashSet<Long>().stream();
//            }).collect(Collectors.toSet());
            Set<Long> tempSet = new HashSet<>();
            for (Long seed : activatedSet) {
                Map<Long, Float> linkedMap = inputNodeMap.get(seed);
                if (linkedMap != null) {
                    for (Long node : linkedMap.keySet()) {
                        if (!visitedSet.contains(node)) {
                            visitedSet.add(node);
                            if (Math.random() < linkedMap.get(node)) {
                                tempSet.add(node);
                            }
                        }
                    }
                }
            }
            activatedSet = tempSet;
//            System.out.println(activatedSet.size());
//            System.out.println(visitedSet.size());
            returnActivatedSet.addAll(activatedSet);
        } while (activatedSet.size() > 0);
        return returnActivatedSet;
    }
}
