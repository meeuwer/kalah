package meeuwer.kalah.logic;

public interface GameClient extends GameEventConsumer {

    Player getPlayer();
    void move(int pitIdx);
    void part();

}
