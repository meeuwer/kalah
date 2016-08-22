package meeuwer.kalah;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import meeuwer.kalah.logic.Board;
import meeuwer.kalah.logic.Game;
import meeuwer.kalah.logic.GameService;
import meeuwer.kalah.logic.impl.BoardImpl;
import meeuwer.kalah.logic.impl.GameImpl;
import meeuwer.kalah.logic.impl.GameServiceImpl;
import meeuwer.kalah.transport.WsGameEndpoint;

import static meeuwer.kalah.Constants.PITS_PER_SIDE;
import static meeuwer.kalah.Constants.STONES_PER_PIT;

public class AppModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Game.class).to(GameImpl.class);
        bind(GameService.class).to(GameServiceImpl.class).asEagerSingleton();
        bind(WsGameEndpoint.class).asEagerSingleton();
    }

    @Provides
    Board provideBoard() {
        return new BoardImpl(PITS_PER_SIDE, STONES_PER_PIT);
    }

}
