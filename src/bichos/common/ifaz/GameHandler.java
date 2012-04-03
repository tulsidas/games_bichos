package bichos.common.ifaz;

import java.util.Collection;

import bichos.common.model.DigResult;
import common.ifaz.BasicGameHandler;

public interface GameHandler extends BasicGameHandler {

    void resultado(int i, int j, int res);

    void resultado(Collection<DigResult> drs);

    void resultadoEnemigo(int i, int j, int res);

    void resultadoEnemigo(Collection<DigResult> drs);
}
