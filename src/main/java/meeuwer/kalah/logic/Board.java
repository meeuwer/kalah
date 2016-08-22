package meeuwer.kalah.logic;

import java.util.Optional;

public interface Board {

    Board makeMove(int pitIdx);
    Optional<Player> getNextPlayer();
    Optional<Player> getWinner();

}
