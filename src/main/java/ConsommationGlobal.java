import java.util.List;

/**
 * Created by gnut3ll4 on 08/03/16.
 */
public class ConsommationGlobal {
    float restant;
    List<ConsommationDate> consommations;

    public ConsommationGlobal(float restant, List<ConsommationDate> consommations) {
        this.restant = restant;
        this.consommations = consommations;
    }

    public ConsommationGlobal() {
    }
}
