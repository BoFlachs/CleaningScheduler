import cleaningscheduler.persistence.VanillaSQL.ScalaMapBuilder;
import org.junit.jupiter.api.Test;

public class SQLRepoTests {
    @Test
    public void scalaMapBuilderShouldAddEntries(){
        ScalaMapBuilder<Integer, Integer> mapBuilder = new ScalaMapBuilder<>();

        mapBuilder.update(1, 8);
        mapBuilder.update(2, 4);

        scala.collection.immutable.Map<Integer, Integer> result = mapBuilder.getMap();

        assert(result.size() == 2);
    }
}
