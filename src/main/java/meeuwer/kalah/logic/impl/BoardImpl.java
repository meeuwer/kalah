package meeuwer.kalah.logic.impl;

import meeuwer.kalah.logic.Board;
import meeuwer.kalah.logic.Player;

import java.util.Arrays;
import java.util.Optional;

public class BoardImpl implements Board {

    private static final int SIDE_COUNT = 2;
    private static final int STORE_COUNT = SIDE_COUNT;

    private int[] arr;
    private Player player = Player.NORTH;
    private int pitCount;

    public BoardImpl(int pitCount, int stoneCount) {
        this.pitCount = pitCount;
        arr = new int[(pitCount * SIDE_COUNT) + STORE_COUNT];
        Arrays.fill(arr, stoneCount);
        for (Player player : Player.values()) {
            emptyPit(getStoreOffset(player));
        }
    }

    private BoardImpl(BoardImpl board) {
        this.arr = board.arr;
        this.player = board.player;
        this.pitCount = board.pitCount;
    }

    // For testing purposes.
    BoardImpl(Player player, int... arr) {
        this.arr = arr;
        this.player = player;
        this.pitCount = (arr.length - STORE_COUNT) / SIDE_COUNT;
    }

    @Override
    public Optional<Player> getNextPlayer() {
        return Optional.ofNullable(player);
    }

    @Override
    public Board makeMove(int pitIdx) {
        if (arr[getPitOffset(player, pitIdx)] == 0) {
            throw new IllegalArgumentException("Pit is empty");
        }

        BoardImpl nextBoard = new BoardImpl(this);
        nextBoard.doMakeMove(pitIdx);
        return nextBoard;
    }

    @Override
    public Optional<Player> getWinner() {
        int north = arr[getStoreOffset(Player.NORTH)];
        int south = arr[getStoreOffset(Player.SOUTH)];
        if (north == south) {
            return Optional.empty();
        }
        return Optional.of(north > south ? Player.NORTH : Player.SOUTH);
    }

    private void doMakeMove(int pitIdx) {
        Player nextPlayer = player.opponent();

        int ownStoreOffset = getStoreOffset(player);
        int opponentStoreOffset = getStoreOffset(player.opponent());
        int i = getPitOffset(player, pitIdx);

        // Grab the stones.
        int n = arr[i];
        emptyPit(i);

        // Start with the next pit.
        i++;

        // While we still have stones at hand...
        while (n > 0) {
            // Get the correct pit offset with modulo arithmetic.
            i = i % arr.length;

            // Skip opponent's store.
            if (i != opponentStoreOffset) {
                // Put stone to the pit.
                arr[i]++;
                n--;
            }

            if (n == 0) {
                // Check if next turn is player's again.
                if (i == ownStoreOffset) {
                    nextPlayer = player;
                }

                // Check if the last pit was empty before we put the stone into it.
                if (i != ownStoreOffset && arr[i] == 1) {
                    int oppositeOffset = getOppositePitOffset(i);
                    arr[ownStoreOffset] += arr[oppositeOffset] + 1;

                    emptyPit(i);
                    emptyPit(oppositeOffset);
                }

                break;
            }

            // Move on to the next pit.
            i++;
        }

        // Check if game is over.
        if (isGameOver()) {
            nextPlayer = null;
        }

        this.player = nextPlayer;
    }

    private boolean isGameOver() {
        for (Player player : Player.values()) {
            if (isAllEmpty(player)) {
                collectStones(player.opponent());
                return true;
            }
        }
        return false;
    }

    private boolean isAllEmpty(Player player) {
        int startPitOffset = getPitOffset(player, 0);
        for (int i = startPitOffset; i < startPitOffset + pitCount; i++) {
            if (arr[i] != 0) {
                return false;
            }
        }
        return true;
    }

    private void collectStones(Player player) {
        int acc = 0;
        int offset = getPitOffset(player, 0);
        for (int i = 0; i < pitCount; i++) {
            acc += arr[offset];
            emptyPit(offset);
            offset++;
        }
        arr[offset] += acc;
    }

    private int getPitOffset(Player player, int pitIdx) {
        return player == Player.NORTH
                ? pitIdx
                : pitCount + pitIdx + 1;
    }

    private int getStoreOffset(Player player) {
        return player == Player.NORTH
                ? pitCount
                : pitCount * SIDE_COUNT + 1;
    }

    private int getOppositePitOffset(int offset) {
        return pitCount * SIDE_COUNT - offset;
    }

    private void emptyPit(int idx) {
        arr[idx] = 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            if (i < arr.length - 1) {
                sb.append(',');
            }
        }
        sb.append(']');
        return player != null ? sb + " " + player : sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BoardImpl board = (BoardImpl) o;

        if (!Arrays.equals(arr, board.arr)) return false;
        return player == board.player;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(arr);
        result = 31 * result + (player != null ? player.hashCode() : 0);
        return result;
    }

}
