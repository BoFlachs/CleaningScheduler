package cleaningscheduler.persistence.VanillaSQL;

import scala.collection.immutable.Map;
import scala.collection.immutable.Map$;

public class ScalaMapBuilder<K, V> {
    private Map<K, V> map = Map$.MODULE$.empty();

    public void update(K key, V value){
        map = map.updated(key, value);
    }

    public Map<K, V> getMap(){
        return map;
    }
}
