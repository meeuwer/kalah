package meeuwer.kalah.logic;

import java.util.Optional;

public interface GameEventConsumer {

    void onJoin(Game game, Player player);
    void onStart();
    void onMove(Board board);
    void onEnd(Optional<Player> winner);
    void onAbandon();

}
