package com.company.utils;

// Represents Piece colors. Also represents the empty tile.
public enum Pieces {
    EMPTY(-1), WHITE(0), BLACK(1);

    private int val;

    private Pieces(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    // Gets a Pieces from its valid integer value.
    public static Pieces fromInteger(int x) {
        switch(x) {
            case -1:
                return EMPTY;
            case 0:
                return WHITE;
            case 1:
                return BLACK;
            default:
                return null;
        }
    }

    // Switches players.
    public static Pieces changePlayer(Pieces curPlayer) {
        return curPlayer.equals(BLACK) ? WHITE : BLACK;
    }

    @Override
    public String toString() {
        switch (this) {
            case EMPTY:
                return " ";
            case WHITE:
                return "W";
            case BLACK:
                return "B";
            default:
                throw new IllegalArgumentException("ERROR: Unknown Pieces: " + this);
        }
    }
}
