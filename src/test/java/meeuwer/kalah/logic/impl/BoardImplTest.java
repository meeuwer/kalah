package meeuwer.kalah.logic.impl;

import meeuwer.kalah.logic.Board;
import org.testng.annotations.Test;

import static meeuwer.kalah.logic.Player.NORTH;
import static meeuwer.kalah.logic.Player.SOUTH;
import static org.testng.Assert.assertEquals;

public class BoardImplTest {

    @Test
    public void testRegularMove() {
        Board actual = new BoardImpl(
                NORTH,
                5, 1, 1, 1, 1, 1,   0,
                0, 0, 0, 0, 0, 1,   0
        ).makeMove(0);

        Board expected = new BoardImpl(
                SOUTH,
                0, 2, 2, 2, 2, 2,   0,
                0, 0, 0, 0, 0, 1,   0
        );

        assertEquals(actual, expected);
    }

    @Test
    public void testMoveEndingInStore() {
        Board actual = new BoardImpl(
                NORTH,
                6, 0, 0, 0, 0, 0,   0,
                0, 0, 0, 0, 0, 1,   0
        ).makeMove(0);

        Board expected = new BoardImpl(
                NORTH,
                0, 1, 1, 1, 1, 1,   1,
                0, 0, 0, 0, 0, 1,   0
        );

        assertEquals(actual, expected);
    }

    @Test
    public void testMoveEndingInEmptyPit() {
        Board actual = new BoardImpl(
                NORTH,
                5, 0, 0, 0, 0, 0,   0,
                1, 0, 0, 0, 0, 1,   0
        ).makeMove(0);

        Board expected = new BoardImpl(
                SOUTH,
                0, 1, 1, 1, 1, 0,   2,
                0, 0, 0, 0, 0, 1,   0
        );

        assertEquals(actual, expected);
    }

    @Test
    public void testMoveSpanningMultipleLapses() {
        Board actual = new BoardImpl(
                NORTH,
                28, 0, 0, 0, 0, 0,   0,
                0,  0, 0, 0, 0, 0,   0
        ).makeMove(0);

        Board expected = new BoardImpl(
                SOUTH,
                2, 3, 3, 2, 2, 2,   2,
                2, 2, 2, 2, 2, 2,   0
        );

        assertEquals(actual, expected);
    }

    @Test
    public void testWin() {
        Board actual = new BoardImpl(
                SOUTH,
                1, 0, 0, 0, 0, 0,   0,
                0, 0, 0, 0, 0, 1,   1
        ).makeMove(5);

        Board expected = new BoardImpl(
                null,
                0, 0, 0, 0, 0, 0,   1,
                0, 0, 0, 0, 0, 0,   2
        );

        assertEquals(actual, expected);
    }

    @Test
    public void testTie() {
        Board actual = new BoardImpl(
                SOUTH,
                1, 0, 0, 0, 0, 0,   0,
                0, 0, 0, 0, 0, 1,   0
        ).makeMove(5);

        Board expected = new BoardImpl(
                null,
                0, 0, 0, 0, 0, 0,   1,
                0, 0, 0, 0, 0, 0,   1
        );

        assertEquals(actual, expected);
    }

    @Test
    public void testStringRepresentation() {
        Board board = new BoardImpl(
                NORTH,
                1, 2, 3, 4,  5,  6,   20,
                7, 8, 9, 10, 11, 12,  40
        );
        String text = "[1,2,3,4,5,6,20,7,8,9,10,11,12,40] NORTH";

        assertEquals(board.toString(), text);
    }

    @Test
    public void testStringRepresentationWithNoMoreMoves() {
        Board board = new BoardImpl(
                null,
                1, 2, 3, 4,  5,  6,   20,
                7, 8, 9, 10, 11, 12,  40
        );
        String text = "[1,2,3,4,5,6,20,7,8,9,10,11,12,40]";

        assertEquals(board.toString(), text);
    }

}
