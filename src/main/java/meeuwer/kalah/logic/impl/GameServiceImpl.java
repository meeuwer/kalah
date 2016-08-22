package meeuwer.kalah.logic.impl;

import meeuwer.kalah.logic.Game;
import meeuwer.kalah.logic.GameClient;
import meeuwer.kalah.logic.GameService;

import javax.inject.Inject;
import javax.inject.Provider;

public class GameServiceImpl implements GameService {

    @Inject
    private Provider<Game> gameProvider;
    private Game game;
    private final Object lock = new Object();

    @Override
    public void joinGame(GameClient client) {
        synchronized (lock) {
            if (game == null || game.isStarted()) {
                game = gameProvider.get();
            }
        }
        game.join(client);
    }

}
