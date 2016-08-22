package meeuwer.kalah.logic;

public interface Game {

    void join(GameClient client);
    void move(GameClient client, int pitIdx);
    void part(GameClient client);
    boolean isStarted();

}
