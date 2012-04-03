package bichos.server;

import org.apache.mina.common.IoSession;

import server.AbstractSaloon;
import bichos.common.ifaz.SaloonHandler;

import common.ifaz.POSTHandler;

public class BichosSaloon extends AbstractSaloon implements SaloonHandler {

    public BichosSaloon(int id, POSTHandler poster) {
        super(id, poster);
    }

    @Override
    protected BichosServerRoom getRoom(IoSession session) {
        return (BichosServerRoom) super.getRoom(session);
    }

    public void createRoom(IoSession session, int puntos) {
        createRoom(session, puntos, new BichosServerRoom(this, session, puntos));
    }

    public void dig(IoSession session, int i, int j) {
        getRoom(session).dig(session, i, j);
    }
}
