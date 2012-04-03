package bichos.client;

import pulpcore.image.CoreImage;
import pulpcore.scene.Scene;
import pulpcore.sprite.ImageSprite;
import pulpcore.sprite.Sprite;
import client.AbstractGameConnector;
import client.AbstractLobbyScene;

import common.model.AbstractRoom;
import common.model.TwoPlayerRoom;
import common.model.User;

public class LobbyScene extends AbstractLobbyScene {

    public LobbyScene(User user, AbstractGameConnector connection) {
        super(user, connection);
    }

    @Override
    protected Scene getGameScene(AbstractGameConnector connection, User usr,
            AbstractRoom room) {
        return new BichosScene((GameConnector) connection, usr, (TwoPlayerRoom) room);
    }

    @Override
    protected Sprite getGameImage() {
        return new ImageSprite(CoreImage.load("imgs/logo-bichos.png"), 495, 10);
    }
}
