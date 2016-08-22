package meeuwer.kalah.transport.impl;

import meeuwer.kalah.logic.Board;
import meeuwer.kalah.logic.Game;
import meeuwer.kalah.logic.GameClient;
import meeuwer.kalah.logic.Player;
import meeuwer.kalah.transport.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import static meeuwer.kalah.transport.Event.*;

public class WsGameClient implements GameClient {

    private static final Logger LOG = LoggerFactory.getLogger(WsGameClient.class);

    private Session session;
    private Game game;
    private Player player = null;

    public WsGameClient(Session session) {
        this.session = session;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public void move(int pitIdx) {
        game.move(this, pitIdx);
    }

    @Override
    public void part() {
        game.part(this);
    }

    @Override
    public void onJoin(Game game, Player player) {
        this.game = game;
        this.player = player;
        sendMessage(JOINED, player);
    }

    @Override
    public void onStart() {
        sendMessage(STARTED);
    }

    @Override
    public void onMove(Board board) {
        sendMessage(UPDATED, board);
    }

    @Override
    public void onEnd(Optional<Player> winner) {
        sendMessage(FINISHED, (winner.isPresent() ? winner.get() : "NONE"));
    }

    @Override
    public void onAbandon() {
        sendMessage(ABANDONED);
    }

    private void sendMessage(Event event, Object... objects) {
        String text = event + Arrays.stream(objects)
                .map(Object::toString)
                .collect(Collectors.joining(" ", " ", ""));
        try {
            session.getBasicRemote().sendText(text);
        } catch (IOException ioe) {
            LOG.error("Failed to send message.", ioe);
        }
    }

}
