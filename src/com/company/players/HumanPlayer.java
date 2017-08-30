package com.company.players;

import com.company.game.Board;
import com.company.utils.Pieces;
import com.company.utils.utils;

import java.util.Scanner;

// Represents a human player.
public class HumanPlayer implements Player {

    // What color the human is playing as.
    private Pieces player;

    // The default constructor pretty much does nothing.
    public HumanPlayer() {
        player = Pieces.EMPTY;
    }

    // Initializes the HumanPlayer to a color.
    public HumanPlayer(Pieces pieces) {
        this.player = pieces;
    }

    // A move is made by a human player by input.
    @Override
    public void makeMove(Board board) {
        Scanner console = new Scanner(System.in);

        int row = utils.validInt(console);
        int col = utils.validInt(console);

        while (utils.outOfBounds(row, col, board) || utils.overlappingMove(row, col, board)) {
            row = utils.validInt(console);
            col = utils.validInt(console);
        }

        board.setValue(row, col, player);
    }
}
