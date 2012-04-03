package bichos.common.messages.server;

import bichos.common.ifaz.GameHandler;
import bichos.common.ifaz.GameMessage;

import common.messages.FixedLengthMessageAdapter;

public class NewGameMessage extends FixedLengthMessageAdapter implements
        GameMessage {

    public void execute(GameHandler game) {
        game.newGame();
    }

    @Override
    public byte getMessageId() {
        return (byte) 0x80;
    }

   @Override
   public int getContentLength() {
      return 0;
   }
}
