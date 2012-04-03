package bichos.common.messages.server;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.mina.common.ByteBuffer;

import bichos.common.ifaz.GameHandler;
import bichos.common.ifaz.GameMessage;
import bichos.common.model.DigResult;

import common.messages.TaringaProtocolEncoder;
import common.messages.VariableLengthMessageAdapter;

public class DigResultMessage extends VariableLengthMessageAdapter implements
        GameMessage {

    private int i, j, res;

    private boolean mio;

    private Collection<DigResult> drs;

    private Boolean gameOver;

    public DigResultMessage() {
    }

    public DigResultMessage(int i, int j, int res, boolean mio) {
        this.i = i;
        this.j = j;
        this.res = res;
        this.mio = mio;
    }

    public DigResultMessage(Collection<DigResult> drs, boolean mio) {
        this.drs = drs;
        this.mio = mio;
    }

    public void execute(GameHandler game) {
        if (drs == null) {
            if (mio) {
                game.resultado(i, j, res);
            }
            else {
                game.resultadoEnemigo(i, j, res);
            }
        }
        else {
            if (mio) {
                game.resultado(drs);
            }
            else {
                game.resultadoEnemigo(drs);
            }
        }
        
        if (gameOver != null) {
            game.finJuego(gameOver.booleanValue());
        }
    }

    public void setGameOver(Boolean gameOver) {
        this.gameOver = gameOver;
    }

    @Override
    public ByteBuffer encodedContent() {
        ByteBuffer ret = ByteBuffer.allocate(64);
        ret.setAutoExpand(true);

        ret.put((byte) i);
        ret.put((byte) j);
        ret.put((byte) res);
        ret.put(mio ? TaringaProtocolEncoder.TRUE
                : TaringaProtocolEncoder.FALSE);

        if (drs != null) {
            ret.putShort((short) drs.size());
            for (DigResult dr : drs) {
                DigResult.writeTo(dr, ret);
            }
        }
        else {
            ret.putShort((short) 0);
        }

        if (gameOver != null) {
            ret.put(gameOver ? TaringaProtocolEncoder.TRUE
                    : TaringaProtocolEncoder.FALSE);
        }
        else {
            ret.put(TaringaProtocolEncoder.NULL);
        }

        ret.flip();
        return ret;
    }

    @Override
    public void decode(ByteBuffer buff) {
        i = buff.get();
        j = buff.get();
        res = buff.get();

        mio = buff.get() == TaringaProtocolEncoder.TRUE;

        short size = buff.getShort();
        if (size > 0) {
            drs = new ArrayList<DigResult>(size);
            for (short i = 0; i < size; i++) {
                drs.add(DigResult.readFrom(buff));
            }
        }

        byte gOver = buff.get();
        if (gOver != TaringaProtocolEncoder.NULL) {
            gameOver = gOver == TaringaProtocolEncoder.TRUE;
        }
    }

    @Override
    public byte getMessageId() {
        return (byte) 0x83;
    }
}