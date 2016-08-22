package meeuwer.kalah.logic.impl;

import meeuwer.kalah.logic.Board;
import meeuwer.kalah.logic.Game;
import meeuwer.kalah.logic.GameClient;
import meeuwer.kalah.logic.Player;

import javax.inject.Inject;
import java.util.EnumMap;
import java.util.function.Consumer;

public class GameImpl implements Game {

    @Inject
    private Board board;
    private EnumMap<Player, GameClient> clients = new EnumMap<>(Player.class);
    private final Object lock = new Object();

    @Override
    public void join(GameClient client) {
        synchronized (lock) {
            if (isStarted()) {
                throw new IllegalStateException("Game has already started.");
            }

            for (Player player : Player.values()) {
                if (!clients.containsKey(player)) {
                    clients.put(player, client);
                    client.onJoin(this, player);
                    break;
                }
            }

            if (isStarted()) {
                forEachClient(GameClient::onStart);
            }
        }
    }

    @Override
    public void move(GameClient client, int pitIdx) {
        synchronized (lock) {
            if (!board.getNextPlayer().isPresent()) {
                throw new IllegalArgumentException("Game is over.");
            }
            if (board.getNextPlayer().get() != client.getPlayer()) {
                throw new IllegalArgumentException("Wrong next player.");
            }
            board = board.makeMove(pitIdx);
            forEachClient(c -> c.onMove(board));
            if (!board.getNextPlayer().isPresent()) {
                forEachClient(c -> c.onEnd(board.getWinner()));
            }
        }
    }

    @Override
    public void part(GameClient client) {
        synchronized (lock) {
            clients.get(client.getPlayer().opponent()).onAbandon();
        }
    }

    @Override
    public boolean isStarted() {
        synchronized (lock) {
            return clients.size() == Player.values().length;
        }
    }

    private void forEachClient(Consumer<GameClient> consumer) {
        for (Player player : Player.values()) {
            GameClient client = clients.get(player);
            if (client != null) {
                consumer.accept(client);
            }
        }
    }

}
