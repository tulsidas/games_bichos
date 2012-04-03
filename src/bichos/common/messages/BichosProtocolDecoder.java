package bichos.common.messages;

import bichos.common.messages.client.DigMessage;
import bichos.common.messages.server.DigResultMessage;
import bichos.common.messages.server.NewGameMessage;
import bichos.common.messages.server.StartGameMessage;

import common.messages.TaringaProtocolDecoder;

public class BichosProtocolDecoder extends TaringaProtocolDecoder {

    public BichosProtocolDecoder() {
        classes.put(new NewGameMessage().getMessageId(), NewGameMessage.class);
        classes.put(new StartGameMessage().getMessageId(),
                StartGameMessage.class);
        classes.put(new DigMessage().getMessageId(),
                DigMessage.class);
        classes.put(new DigResultMessage().getMessageId(),
                DigResultMessage.class);
    }

}
