package meeuwer.kalah.logic;

public enum Player {

    NORTH, SOUTH;

    public Player opponent() {
        switch (this) {
        case NORTH: return SOUTH;
        case SOUTH: return NORTH;
        }
        throw new IllegalStateException();
    }

}
