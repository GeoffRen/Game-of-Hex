package com.company.players;

import com.company.game.Board;

// All players derive from this.
public interface Player {

    // All players need to at least make a move.
    void makeMove(Board board);
}
