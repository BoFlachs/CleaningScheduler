package cleaningscheduler.persistence.VanillaSQL;

import scala.collection.immutable.List;
import scala.collection.immutable.List$;

public class ScalaListBuilder<E> {
    private List<E> list = List$.MODULE$.empty();

    public void add(E element) {list = list.$colon$colon(element);}

    public List<E> getList() {return list.reverse();}
}
