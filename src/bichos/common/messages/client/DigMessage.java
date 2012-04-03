package bichos.common.messages.client;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;

import bichos.common.ifaz.SaloonHandler;
import bichos.common.messages.BichosClientGameMessage;

public class DigMessage extends BichosClientGameMessage {

   private int i, j;

   public DigMessage() {
   }

   public DigMessage(int i, int j) {
      this.i = i;
      this.j = j;
   }

   @Override
   public void execute(IoSession session, SaloonHandler salon) {
      salon.dig(session, i, j);
   }

   @Override
   public String toString() {
      return "Dig (" + i + ", " + j + ")";
   }

   @Override
   public int getContentLength() {
      // 2 bytes
      return 2;
   }

   @Override
   protected void encodeContent(ByteBuffer buff) {
      buff.put((byte) i).put((byte) j);
   }

   @Override
   public void decode(ByteBuffer buff) {
      i = buff.get();
      j = buff.get();
   }

   @Override
   public byte getMessageId() {
      return (byte) 0x82;
   }
}
