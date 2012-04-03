package bichos.common.messages.server;

import org.apache.mina.common.ByteBuffer;

import bichos.common.ifaz.GameHandler;
import bichos.common.ifaz.GameMessage;

import common.messages.FixedLengthMessageAdapter;
import common.messages.TaringaProtocolEncoder;

public class StartGameMessage extends FixedLengthMessageAdapter implements
      GameMessage {

   private boolean start;

   public StartGameMessage() {
   }

   public StartGameMessage(boolean start) {
      this.start = start;
   }

   public void execute(GameHandler game) {
      game.startGame(start);
   }

   @Override
   public int getContentLength() {
      return 1;
   }

   @Override
   protected void encodeContent(ByteBuffer buff) {
      buff.put(start ? TaringaProtocolEncoder.TRUE
            : TaringaProtocolEncoder.FALSE);
   }

   @Override
   public void decode(ByteBuffer buff) {
      start = buff.get() == TaringaProtocolEncoder.TRUE;
   }

   @Override
   public byte getMessageId() {
      return (byte) 0x81;
   }
}
