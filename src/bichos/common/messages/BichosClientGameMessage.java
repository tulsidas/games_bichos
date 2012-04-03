package bichos.common.messages;

import org.apache.mina.common.IoSession;

import bichos.common.ifaz.ClientGameMessage;
import bichos.common.ifaz.SaloonHandler;

import common.ifaz.BasicServerHandler;
import common.messages.FixedLengthMessageAdapter;

public abstract class BichosClientGameMessage extends FixedLengthMessageAdapter
        implements ClientGameMessage {

    public abstract void execute(IoSession session, SaloonHandler salon);

    public void execute(IoSession session, BasicServerHandler serverHandler) {
        execute(session, (SaloonHandler) serverHandler);
    }
}
