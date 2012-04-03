package bichos.server;

import java.util.Collection;
import java.util.Map;

import org.apache.mina.common.IoSession;

import server.TwoPlayersServerRoom;
import bichos.common.messages.server.DigResultMessage;
import bichos.common.messages.server.NewGameMessage;
import bichos.common.messages.server.StartGameMessage;
import bichos.common.model.DigResult;
import bichos.server.model.Tablero;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import common.messages.server.UpdatedPointsMessage;
import common.model.AbstractRoom;
import common.model.TwoPlayerRoom;

public class BichosServerRoom extends TwoPlayersServerRoom {

    private Tablero tablero;

    private Map<IoSession, Integer> mines;

    private static final int MINAS = 51;

    private boolean gameOn;

    public BichosServerRoom(BichosSaloon saloon, IoSession session, int puntos) {
        super(saloon, session, puntos);

        tablero = new Tablero(16, MINAS);

        mines = Maps.newHashMap();
    }

    @Override
    public AbstractRoom createRoom() {
        return new TwoPlayerRoom(getId(), puntosApostados, getUsers());
    }

    @Override
    public void startNuevoJuego() {
        tablero.reset();
        mines.clear();

        player1.write(new NewGameMessage());
        player2.write(new NewGameMessage());

        startGame();
    }

    @Override
    public void startGame() {
        mines.put(player1, 0);
        mines.put(player2, 0);

        // jugando
        setEnJuego(true);

        // todavia puede abandonar
        gameOn = false;

        player1.write(new StartGameMessage(true));
        player2.write(new StartGameMessage(false));
    }

    public void dig(IoSession session, int i, int j) {
        IoSession otro = getOtherPlayer(session);

        int res = tablero.getNum(i, j);

        if (res == 0) {
            // fill
            Collection<DigResult> drs = Sets.newTreeSet();
            tablero.fill(i, j, drs);

            session.write(new DigResultMessage(drs, true));
            otro.write(new DigResultMessage(drs, false));

            return;
        }
        else if (res == -1) {
            // al primer bicho ya no puede abandonar
            gameOn = true;

            // mina
            int numMinas = mines.get(session);
            numMinas++;
            mines.put(session, numMinas);

            if (numMinas == (MINAS / 2) + 1) {
                DigResultMessage resSession = new DigResultMessage(i, j, res,
                        true);
                resSession.setGameOver(true);

                DigResultMessage resOtro = new DigResultMessage(i, j, res,
                        false);
                resOtro.setGameOver(false);

                // mando resultado
                session.write(resSession);
                otro.write(resOtro);

                // transfiero puntos
                int newPoints[] = saloon.transferPoints(session, otro,
                        puntosApostados);

                // mando puntos (si siguen conectados)
                if (session != null) {
                    session.write(new UpdatedPointsMessage(newPoints[0]));
                }
                if (otro != null) {
                    otro.write(new UpdatedPointsMessage(newPoints[1]));
                }

                setEnJuego(false);

                return; // no mando DigResultMessage de nuevo
            }
        }

        session.write(new DigResultMessage(i, j, res, true));
        otro.write(new DigResultMessage(i, j, res, false));
    }

    @Override
    public boolean isGameOn() {
        return gameOn;
    }
}