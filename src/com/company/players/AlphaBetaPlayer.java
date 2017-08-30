package com.company.players;

import com.company.game.Board;
import com.company.utils.Pieces;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// A Minimax with Alpha Beta Pruning implementation.
public class AlphaBetaPlayer implements Player {

    // The alpha beta player color.
    private Pieces player;

    // Search depth of alpha beta.
    private final int SEARCH_DEPTH = 2;

    // Statistics
//    private int evaluated = 0;
//    private int size = 0;
//    private int sizeHelper = 0;

    // Default constructor pretty much does nothing.
    public AlphaBetaPlayer() {
        player = Pieces.EMPTY;
    }

    // Initializes the AlphaBetaPlayer to a Piece color.
    public AlphaBetaPlayer(Pieces player) {
        this.player = player;
    }

    // Chooses a move to make and makes it.
    @Override
    public void makeMove(Board board) {
//        evaluated = 0;
//        size = 0;

        MoveNode move = alphaBeta(board, new MoveNode(), SEARCH_DEPTH, Double.MIN_VALUE, Double.MAX_VALUE, true);

//        System.out.println("Nodes evaluated: " + evaluated);
//        System.out.println("Max size of tree: " + size);
        System.out.println("MOVE FOUND: " + move);

        board.setValue(move.move.x, move.move.y, player);
    }

    // The Alpha Beta Pruning algorithm.
    private MoveNode alphaBeta(Board board, MoveNode preMove, int depth, double a, double b, boolean maximizingPlayer) {
//        evaluated++;
//        size++;

        // Since heuristic calculations are expensive, only calculate them when needed, so at leaves.
        if (depth == 0) {
//            sizeHelper++;

            preMove.heuristic = board.calculateHeuristic(player);
            return new MoveNode(preMove);
        }

        // Maximizing player wants to maximize the heuristic value.
        if (maximizingPlayer) {
            MoveNode max = new MoveNode(Integer.MIN_VALUE);

            for (Point move : possibleMoves(board)) {

                board.setValue(move.x, move.y, player);
                preMove.move = move;

                MoveNode bestChild = alphaBeta(board, preMove, depth - 1, a, b, false);
                board.clearValue(move.x, move.y);

                max = (max.heuristic >= bestChild.heuristic) ? max : bestChild;
                a = Math.max(a, max.heuristic);

                // Max cutoff.
                if (a >= b) {
//                    size -= sizeHelper;
//                    sizeHelper = 0;

                    break;
                }
            }

//            size -= sizeHelper;
//            sizeHelper = 0;

            return max;

        // Minimizing player wants to minimize the heuristic value.
        } else {
            MoveNode min = new MoveNode(Integer.MAX_VALUE);

            for (Point move : possibleMoves(board)) {

                board.setValue(move.x, move.y, Pieces.changePlayer(player));
                preMove.move = move;

                MoveNode bestChild = alphaBeta(board, preMove, depth - 1, a, b, true);
                board.clearValue(move.x, move.y);

                min = (min.heuristic <= bestChild.heuristic) ? min : bestChild;
                b = Math.min(b, min.heuristic);

                // Min cutoff.
                if (b <= a) {
//                    size -= sizeHelper;
//                    sizeHelper = 0;

                    break;
                }
            }
//            size -= sizeHelper;
//            sizeHelper = 0;

            return min;
        }
    }

    // Gets a list containing all possible moves.
    private List<Point> possibleMoves(Board board) {
        List<Point> ret = new ArrayList<>();

        for (int row = 0; row < board.getDimensions(); row++) {
            for (int col = 0; col < board.getDimensions(); col++) {

                if (board.getValue(row, col).equals(Pieces.EMPTY)) {
                    ret.add(new Point(row, col));
                }
            }
        }

        return ret;
    }

    // Class representing a move. Used to store a point representing where to move and a heuristic that corresponds to
    // the move.
    class MoveNode {

        Point move;
        double heuristic;

        public MoveNode() {
            move = null;
            heuristic = 0;
        }

        public MoveNode(int heuristic) {
            move = null;
            this.heuristic = heuristic;
        }

        public MoveNode(MoveNode moveNode) {
            this.move = new Point(moveNode.move);
            this.heuristic = moveNode.heuristic;
        }

        public MoveNode(Point move) {
            this.move = new Point(move);
            heuristic = 0;
        }

        public double getHeuristic() {
            return heuristic;
        }

        @Override
        public String toString() {
            return move.toString();
        }
    }
}
