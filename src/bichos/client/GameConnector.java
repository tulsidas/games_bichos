package bichos.client;

import java.util.Collection;

import org.apache.mina.common.IoSession;

import bichos.common.ifaz.GameHandler;
import bichos.common.ifaz.GameMessage;
import bichos.common.messages.BichosProtocolDecoder;
import bichos.common.model.DigResult;
import client.AbstractGameConnector;

public class GameConnector extends AbstractGameConnector implements GameHandler {

    public GameConnector(String host, int port, int salon, String user,
            String pass, long version) {
        super(host, port, salon, user, pass, version,
                new BichosProtocolDecoder());
    }

    @Override
    public void messageReceived(IoSession sess, Object message) {
        super.messageReceived(sess, message);

        if (message instanceof GameMessage && gameHandler != null) {
            ((GameMessage) message).execute(this);
        }
    }

    // /////////////
    // GameHandler
    // /////////////
    public void resultado(int i, int j, int res) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).resultado(i, j, res);
        }
    }

    public void resultado(Collection<DigResult> drs) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).resultado(drs);
        }
    }

    public void resultadoEnemigo(int i, int j, int res) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).resultadoEnemigo(i, j, res);
        }
    }

    public void resultadoEnemigo(Collection<DigResult> drs) {
        if (gameHandler != null) {
            ((GameHandler) gameHandler).resultadoEnemigo(drs);
        }
    }
}
