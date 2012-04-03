package bichos.client;

import static pulpcore.image.Colors.WHITE;
import static pulpcore.image.Colors.rgb;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import pulpcore.CoreSystem;
import pulpcore.Input;
import pulpcore.Stage;
import pulpcore.animation.Easing;
import pulpcore.animation.Timeline;
import pulpcore.animation.event.TimelineEvent;
import pulpcore.image.CoreFont;
import pulpcore.image.CoreImage;
import pulpcore.scene.Scene;
import pulpcore.sound.Playback;
import pulpcore.sound.Sound;
import pulpcore.sprite.Button;
import pulpcore.sprite.Group;
import pulpcore.sprite.ImageSprite;
import pulpcore.sprite.Label;
import pulpcore.sprite.Sprite;
import pulpcore.sprite.TextField;
import bichos.common.ifaz.GameHandler;
import bichos.common.messages.client.DigMessage;
import bichos.common.model.DigResult;
import client.InGameChatArea;
import client.PingScene;
import client.PulpcoreUtils;
import client.DisconnectedScene.Reason;

import common.game.AbandonRoomMessage;
import common.game.ProximoJuegoMessage;
import common.messages.chat.RoomChatMessage;
import common.messages.server.RoomJoinedMessage;
import common.model.AbstractRoom;
import common.model.TwoPlayerRoom;
import common.model.User;

public class BichosScene extends PingScene implements GameHandler {

   private static final int[] RANDOM_COLORS = new int[] { rgb(0x00e300),
         rgb(0xf6f300), rgb(0x00bff6), rgb(0x2bc5ad), rgb(0xff0099),
         rgb(0xff9900) };

   private static final int LABEL_X = 80;

   private static final int LABEL_W = 219;

   private GameConnector connection;

   private User currentUser, oponente;

   private TwoPlayerRoom room;

   private boolean mustDisconnect;

   // me toca jugar
   private boolean miTurno;

   // el momento en que tengo que abandonar
   private long relojGlobal, relojLocal;

   // lo que falta para que me rajen
   private int tiempoGlobal, tiempoLocal;

   private InGameChatArea chatBox;

   private TextField chatTF;

   private Button sendChat, abandonGame, disableSounds;

   private Button nuevoJuegoSi, nuevoJuegoNo;

   private int colorYo, colorOtro;

   private CoreFont din13, din13white, din24, din30w;

   private Label turno, timerLabelGlobal, timerLabelLocal;

   private Label finalLabel, finalLabel2;

   private Label bichosRestantes, bichosYo, bichosOtro;

   // las celdas
   private ImageSprite[][] celdas;

   private ImageSprite recuadroYo, recuadroOtro, cursor;

   private CoreImage[] imgCeldas;

   private Sound tic, fondo, beep, bad, good;

   private Playback fondoPB;

   // donde ya dispare
   private Set<Sprite> yaDisparado;

   private Group tablero;

   private static final int SIZE = 16;

   public BichosScene(GameConnector connection, User usr, TwoPlayerRoom room) {
      super(connection);

      this.connection = connection;
      this.currentUser = usr;
      this.room = room;

      // inject
      connection.setGameHandler(this);

      tablero = new Group();
      celdas = new ImageSprite[16][16];

      yaDisparado = new HashSet<Sprite>();
   }

   @Override
   public void load() {
      // fonts
      din13 = CoreFont.load("imgs/DIN13.font.png");
      din24 = CoreFont.load("imgs/DIN24.font.png").tint(WHITE);
      CoreFont din30 = CoreFont.load("imgs/DIN30.font.png");
      din30w = din30.tint(WHITE);
      din13white = din13.tint(WHITE);

      // forms

      add(new ImageSprite("imgs/fondo.jpg", 0, 0));

      imgCeldas = CoreImage.load("imgs/celdas.png").split(15);

      // pastito de fondo
      for (int i = 0; i < SIZE; i++) {
         for (int j = 0; j < SIZE; j++) {
            celdas[i][j] = new ImageSprite(imgCeldas[0], 10 + i * 24,
                  60 + j * 24);
            tablero.add(celdas[i][j]);
         }
      }
      add(tablero);

      // label con datos de la sala
      add(new Label(din13white, "por " + room.getPuntosApostados() + " puntos",
            400, 5));

      recuadroYo = new ImageSprite(imgCeldas[13], 0, 0);
      recuadroYo.visible.set(false);
      add(recuadroYo);

      recuadroOtro = new ImageSprite(imgCeldas[12], 0, 0);
      recuadroOtro.visible.set(false);
      add(recuadroOtro);

      // chat box
      chatBox = new InGameChatArea(din13, 413, 152, 295, 250);
      add(chatBox);

      // campo de texto donde se chatea
      chatTF = new TextField(din13, din13white, "", 413, 420, 281, -1);
      add(chatTF);

      // boton para enviar el chat (asociado al ENTER)
      sendChat = new Button(CoreImage.load("imgs/btn-send.png").split(3), 697,
            414);
      sendChat.setKeyBinding(Input.KEY_ENTER);
      add(sendChat);

      // mi color
      colorYo = RANDOM_COLORS[(int) (Math.random() * RANDOM_COLORS.length)];

      // el otro (si hay)
      for (User otro : room.getPlayers()) {
         if (!otro.equals(currentUser)) {
            oponente = otro;
            drawNames();
            break;
         }
      }

      // boton abandonar
      abandonGame = new Button(CoreImage.load("imgs/btn-abandonar.png")
            .split(3), 300, 0);
      add(abandonGame);

      // mute
      disableSounds = new Button(CoreImage.load("imgs/sonidos.png").split(6),
            255, 30, true);
      disableSounds.setSelected(CoreSystem.isMute());
      disableSounds.setPixelLevelChecks(false);
      add(disableSounds);

      nuevoJuegoSi = new Button(CoreImage.load("imgs/btn-si.png").split(3),
            100, 200);
      nuevoJuegoSi.enabled.set(false);
      nuevoJuegoSi.setPixelLevelChecks(false);

      nuevoJuegoNo = new Button(CoreImage.load("imgs/btn-no.png").split(3),
            200, 200);
      nuevoJuegoNo.enabled.set(false);
      nuevoJuegoNo.setPixelLevelChecks(false);

      turno = new Label(din13white, "Esperando oponente", 600, 40);

      bichosRestantes = new Label(din30w, "51", 535, 18);
      bichosYo = new Label(din30, "0", 465, 65);
      bichosOtro = new Label(din30, "0", 465, 105);

      finalLabel = new Label(din30w, "", 0, 130);
      finalLabel.visible.set(false);
      add(finalLabel);
      finalLabel2 = new Label(din30w, "", 0, 160);
      finalLabel2.visible.set(false);
      add(finalLabel2);

      // animo el alpha para que titile
      Timeline alphaCycle = new Timeline();
      int dur = 1000;
      alphaCycle.animate(turno.alpha, 255, 0, dur, Easing.NONE, 0);
      alphaCycle.animate(turno.alpha, 0, 255, dur, Easing.NONE, dur);
      alphaCycle.loopForever();
      addTimeline(alphaCycle);
      add(turno);

      // timer (en un nuevo layer para estar encima de todo)
      timerLabelGlobal = new Label(din13white, "", 0, 0);
      timerLabelLocal = new Label(din13white, "", 0, 0);

      Group g = new Group();
      g.add(timerLabelGlobal);
      g.add(timerLabelLocal);
      addLayer(g);

      cursor = new ImageSprite(imgCeldas[14], 0, 0);
      cursor.visible.set(false);
      add(cursor);

      // sounds
      tic = Sound.load("sfx/tic.ogg");
      fondo = Sound.load("sfx/fondo.ogg");
      bad = Sound.load("sfx/bad.ogg");
      good = Sound.load("sfx/good.ogg");
      beep = Sound.load("sfx/beep.ogg");

      resetRelojGlobal();

      // envio mensaje que me uni a la sala correctamente
      connection.send(new RoomJoinedMessage());
   }

   public void unload() {
      if (fondoPB != null) {
         fondoPB.stop();
      }

      if (mustDisconnect) {
         connection.disconnect();
      }
   }

   @Override
   public void update(int elapsedTime) {
      super.update(elapsedTime);

      if (disableSounds.isClicked()) {
         CoreSystem.setMute(disableSounds.isSelected());
      }
      else if (nuevoJuegoSi.enabled.get() && nuevoJuegoSi.isClicked()) {
         nuevoJuegoSi.enabled.set(false);
         remove(nuevoJuegoSi);
         nuevoJuegoNo.enabled.set(false);
         remove(nuevoJuegoNo);

         finalLabel.setText("Esperando respuesta del oponente...");
         finalLabel.visible.set(true);
         finalLabel2.visible.set(false);
         PulpcoreUtils.centerSprite(finalLabel, LABEL_X, LABEL_W);

         connection.send(new ProximoJuegoMessage(true));
      }
      else if (nuevoJuegoNo.enabled.get() && nuevoJuegoNo.isClicked()) {
         // aviso que no
         connection.send(new ProximoJuegoMessage(false));

         invokeLater(new Runnable() {
            public void run() {
               // y me rajo al lobby
               setScene(new LobbyScene(currentUser, connection));
            }
         });
      }

      if (miTurno) {
         relojGlobal -= elapsedTime;
         relojLocal -= elapsedTime;

         // actualizacion del timer
         int tg = Math.round(relojGlobal / 1000);
         int tl = Math.round(relojLocal / 1000);

         if (tg < 0 || tl < 0) {
            abandonGame();
         }

         if (tg != tiempoGlobal) {
            tiempoGlobal = tg;

            timerLabelGlobal.setText(Integer.toString(tg));
            timerLabelGlobal.alpha.set(0xff);

            if (tg >= 10) {
               timerLabelGlobal.x.set(285);
               timerLabelGlobal.y.set(35);
            }
            else if (tg < 10) {
               timerLabelGlobal.x.set(170);
               timerLabelGlobal.y.set(210);

               beep.play();

               timerLabelGlobal.alpha.animateTo(0, 500);
               timerLabelGlobal.width.animateTo(100, 500);
               timerLabelGlobal.height.animateTo(100, 500);
               timerLabelGlobal.x.animateTo(timerLabelGlobal.x.get() - 50, 500);
               timerLabelGlobal.y.animateTo(timerLabelGlobal.y.get() - 50, 500);
            }
         }

         if (tl != tiempoLocal) {
            tiempoLocal = tl;

            timerLabelLocal.setText(Integer.toString(tl));
            timerLabelLocal.alpha.set(0xff);

            if (tl >= 10) {
               timerLabelLocal.x.set(315);
               timerLabelLocal.y.set(35);
            }
            else if (tl < 10) {
               timerLabelLocal.x.set(170);
               timerLabelLocal.y.set(210);

               beep.play();

               timerLabelLocal.alpha.animateTo(0, 500);
               timerLabelLocal.width.animateTo(100, 500);
               timerLabelLocal.height.animateTo(100, 500);
               timerLabelLocal.x.animateTo(timerLabelLocal.x.get() - 50, 500);
               timerLabelLocal.y.animateTo(timerLabelLocal.y.get() - 50, 500);
            }
         }

         cursor.visible.set(false);

         for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
               ImageSprite celda = celdas[i][j];
               if (celda.isMouseOver()) {
                  // dibujo cursor ahi
                  cursor.setLocation(celda.x.get(), celda.y.get());
                  cursor.visible.set(true);
               }

               if (celda.isMouseReleased()) {
                  if (!yaDisparado.contains(celda)) {
                     connection.send(new DigMessage(i, j));
                     setMiTurno(false);
                  }
               }
            }
         }
      }

      if (sendChat.isClicked() && chatTF.getText().trim().length() > 0) {
         connection.send(new RoomChatMessage(chatTF.getText()));

         chatBox.addLine(currentUser.getName() + ": " + chatTF.getText());
         chatTF.setText("");
      }
      else if (abandonGame.enabled.get() && abandonGame.isClicked()) {
         abandonGame();
      }
   }

   public void disconnected() {
      invokeLater(new Runnable() {
         public void run() {
            Stage.setScene(new client.DisconnectedScene(Reason.FAILED));
         }
      });
   }

   public void incomingChat(final User from, final String msg) {
      invokeLater(new Runnable() {
         public void run() {
            tic.play();

            chatBox.addLine(from.getName() + ": " + msg);
         }
      });
   }

   public void oponenteAbandono(boolean enJuego, User user) {
      if (enJuego) {
         finalLabel.setText("¡Tu oponente abandono!");
      }
      else {
         finalLabel.setText("No quiso jugar otro");
      }

      PulpcoreUtils.centerSprite(finalLabel, LABEL_X, LABEL_W);
      finalLabel.visible.set(true);
      finalLabel2.visible.set(false);

      nuevoJuegoSi.visible.set(false);
      nuevoJuegoNo.visible.set(false);

      addEvent(new TimelineEvent(2000) {
         @Override
         public void run() {
            setScene(new LobbyScene(currentUser, connection));
         }
      });
   }

   public void roomJoined(AbstractRoom room, User user) {
      if (!user.equals(currentUser)) {
         oponente = user;
         drawNames();
      }
   }

   public void updatePoints(int puntos) {
      // actualizo puntos
      currentUser.setPuntos(puntos);

      invokeLater(new Runnable() {
         public void run() {
            // obligo a contestar o que vuelva al lobby
            setMiTurno(true);

            if (currentUser.getPuntos() >= room.getPuntosApostados()) {
               add(nuevoJuegoSi);
               nuevoJuegoSi.enabled.set(true);
               add(nuevoJuegoNo);
               nuevoJuegoNo.enabled.set(true);

               finalLabel2.setText("¿Otro partido?");
               finalLabel2.visible.set(true);
               PulpcoreUtils.centerSprite(finalLabel2, LABEL_X, LABEL_W);
            }
            else {
               // no me alcanza para jugar otro

               // aviso que no
               connection.send(new ProximoJuegoMessage(false));

               // y me rajo al lobby
               setScene(new LobbyScene(currentUser, connection));
            }
         }
      });
   }

   private final void setScene(final Scene s) {
      mustDisconnect = false;
      Stage.setScene(s);
   }

   private void drawNames() {
      invokeLater(new Runnable() {
         public void run() {
            int MAX_SIZE = 10;

            String yo = currentUser.getName().toUpperCase();
            if (yo.length() > MAX_SIZE) {
               yo = yo.substring(0, MAX_SIZE);
            }
            String otro = oponente.getName().toUpperCase();
            if (otro.length() > MAX_SIZE) {
               otro = otro.substring(0, MAX_SIZE);
            }

            do {
               // uno distinto!
               colorOtro = RANDOM_COLORS[(int) (Math.random() * RANDOM_COLORS.length)];
            }
            while (colorOtro == colorYo);

            add(new Label(din24, yo, 520, 65));
            add(new Label(din24, otro, 520, 110));

            // bichos
            add(bichosRestantes);
            add(bichosYo);
            add(bichosOtro);
         }
      });
   }

   private void abandonGame() {
      // envio abandono
      connection.send(new AbandonRoomMessage());

      invokeLater(new Runnable() {
         public void run() {
            // me rajo al lobby
            setScene(new LobbyScene(currentUser, connection));
         }
      });
   }

   private void setMiTurno(boolean miTurno) {
      if (miTurno) {
         turno.setText("Te toca");

         // 30s para jugar
         relojLocal = 30 * 1000;
      }
      else {
         turno.setText("Esperando jugada");
      }

      this.miTurno = miTurno;
   }

   // /////////////////
   // GameHandler
   // /////////////////
   public void newGame() {
      setMiTurno(false);

      // resetear tablero a pastito
      for (int i = 0; i < SIZE; i++) {
         for (int j = 0; j < SIZE; j++) {
            celdas[i][j].setImage(imgCeldas[0]);
         }
      }

      bichosRestantes.setText("51");
      bichosYo.setText("0");
      bichosOtro.setText("0");

      recuadroYo.visible.set(false);
      recuadroOtro.visible.set(false);

      // habilito tablero
      tablero.enabled.set(true);

      // reseteo variables
      yaDisparado.clear();

      resetRelojGlobal();

      invokeLater(new Runnable() {
         public void run() {
            turno.setText("Esperando oponente");

            // XXX workaround para que no aparezca como cliqueado
            nuevoJuegoNo.update(0);
            nuevoJuegoSi.update(0);

            finalLabel.visible.set(false);
            finalLabel2.visible.set(false);

            // visibilizo y habilito el boton de abandonar
            abandonGame.visible.set(true);
            abandonGame.enabled.set(true);
         }
      });
   }

   public void startGame(final boolean start) {
      invokeLater(new Runnable() {
         public void run() {
            setMiTurno(start);

            // sonido ambiental start!
            if (fondoPB == null) {
               fondoPB = fondo.loop();
            }
         }
      });
   }

   public void finJuego(final boolean victoria) {
      invokeLater(new Runnable() {
         public void run() {
            if (victoria) {
               // gane
               finalLabel.setText("¡Ganaste! Sos groso");
            }
            else {
               // perdi
               finalLabel.setText("¡Perdiste, papanatas!");
               // TODO haha.play();
            }

            finalLabel.visible.set(true);
            PulpcoreUtils.centerSprite(finalLabel, LABEL_X, LABEL_W);

            // invisibilizo y deshabilito el boton de abandonar
            abandonGame.visible.set(false);
            abandonGame.enabled.set(false);

            // deshabilito tablero
            tablero.enabled.set(false);

            // cambio texto del cartel
            turno.setText("Actualizando puntos");

            // espero a que lleguen los puntos
            setMiTurno(false);
         }
      });
   }

   public void resultadoEnemigo(final int i, final int j, final int res) {
      invokeLater(new Runnable() {
         public void run() {
            dig(i, j, res, false);
            (res < 0 ? good : bad).play();
            cursor.visible.set(false);

            // recuadro
            ImageSprite sprite = celdas[i][j];
            recuadroOtro.visible.set(true);
            recuadroOtro.setLocation(sprite.x.get(), sprite.y.get());
         }
      });
   }

   public void resultadoEnemigo(final Collection<DigResult> drs) {
      invokeLater(new Runnable() {
         public void run() {
            bad.play();
            cursor.visible.set(false);

            for (DigResult dr : drs) {
               dig(dr.i, dr.j, dr.res, false);
            }
         }
      });
   }

   public void resultado(final int i, final int j, final int res) {
      invokeLater(new Runnable() {
         public void run() {
            dig(i, j, res, true);
            (res < 0 ? good : bad).play();
            cursor.visible.set(false);

            if (res < 0) {
               // encontre bichito, sumo 10 segundos
               relojGlobal += 10 * 1000;
            }

            // recuadro
            ImageSprite sprite = celdas[i][j];
            recuadroYo.visible.set(true);
            recuadroYo.setLocation(sprite.x.get(), sprite.y.get());
         }
      });
   }

   public void resultado(final Collection<DigResult> drs) {
      invokeLater(new Runnable() {
         public void run() {
            bad.play();
            cursor.visible.set(false);

            for (DigResult dr : drs) {
               dig(dr.i, dr.j, dr.res, true);
            }
         }
      });
   }

   /**
    * Dibuja la imagen correspondiente al resultado
    * 
    * @param i
    *           fila
    * @param j
    *           columna
    * @param res
    *           resultado
    * @param bicho
    *           la imagen a poner si habia bicho
    */
   private void dig(int i, int j, int res, boolean yo) {
      ImageSprite sprite = celdas[i][j];
      // agregar la casilla para no poder cliquearla
      yaDisparado.add(sprite);

      // cambio la imagen
      if (res < 0) {
         // MINA
         CoreImage bicho = yo ? imgCeldas[10] : imgCeldas[11];
         sprite.setImage(bicho);

         setMiTurno(yo);

         int restantes = Integer.parseInt(bichosRestantes.getText());
         bichosRestantes.setText(Integer.toString(restantes - 1));

         Label lbl = yo ? bichosYo : bichosOtro;
         int cant = Integer.parseInt(lbl.getText());
         lbl.setText(Integer.toString(cant + 1));
      }
      else {
         // num
         sprite.setImage(imgCeldas[res + 1]);
         setMiTurno(!yo);
      }
   }

   private void resetRelojGlobal() {
      // 5' de juego
      relojGlobal = 5 * 60 * 1000;
   }
}

// TODO poner por cuanto se esta jugando