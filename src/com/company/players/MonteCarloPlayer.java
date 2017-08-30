package com.company.players;

import com.company.game.Board;
import com.company.utils.Pieces;
import com.company.utils.utils;

import java.awt.*;
import java.util.*;
import java.util.List;

// Represents a standard MCTS player.
public class MonteCarloPlayer implements Player{

    // Time limit is set to 3 seconds right now.
    private final long TIME_LIMIT = 3000;

    // Exploration constant was optimized by the literature.
    private final double EXPLORATION_CONSTANT = 1 / Math.sqrt(2);

//    private int numSimulations = 0;
//    private int size = 0;

    // Color MCTS plays as.
    private Pieces player;

    // Default constructor pretty much does nothing.
    public MonteCarloPlayer() {
        player = Pieces.EMPTY;
    }

    // Initializes MCTS to the given color.
    public MonteCarloPlayer(Pieces player) {
        this.player = player;
    }

    // Makes a move using MCTS algorithm.
    @Override
    public void makeMove(Board board) {
//        numSimulations = 0;
//        size = 0;

        MoveNode move = MonteCarloSearch(board, player);

//        System.out.println("Number of simulations: " + numSimulations);
//        System.out.println("Max size of tree: " + size);
        System.out.println("MOVE FOUND: " + move);

        board.setValue(move.move.x, move.move.y, player);
    }

    // MCTS algorithm. Uses UCT, then random simulation, then backup while time allows. Finally uses max child to select
    // a move.
    private MoveNode MonteCarloSearch(Board board, Pieces curPlayer) {
        MoveNode root = new MoveNode(board);
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < TIME_LIMIT) {
            tuple terminalNode = treePolicy(new Board(board), root, curPlayer);
            Pieces winningPlayer = defaultPolicy(terminalNode.player, terminalNode.board);
            backup(winningPlayer, terminalNode.player, terminalNode.terminalMove);
        }

        return selectMax(root);
    }

    // Chooses the best child according to UCB1 until a node with unexpanded children is encountered. A child is then
    // expanded.
    private tuple treePolicy(Board curBoard, MoveNode root, Pieces curPlayer) {

        // If can expand, then expand
        if (root.possibleChildren != root.children.size()) {
            return expand(curBoard, root, curPlayer);
        }

        double max = Double.MIN_VALUE;
        MoveNode bestChild = new MoveNode();

        for (MoveNode child : root.children) {

            double UCT = calculateUCT(root, child);
            if (UCT > max) {
                max = UCT;
                bestChild = child;
            }
        }

        curBoard.setValue(bestChild.move.x, bestChild.move.y, curPlayer);
        return treePolicy(curBoard, bestChild, Pieces.changePlayer(curPlayer));
    }

    // Expands a MoveNode by adding an unexpanded MoveNode to the children of another MoveNode.
    private tuple expand(Board curBoard, MoveNode root, Pieces curPlayer) {
//        size++;

        for (int row = 0; row < curBoard.getDimensions(); row++) {
            for (int col = 0; col < curBoard.getDimensions(); col++) {

                if (curBoard.getValue(row, col).equals(Pieces.EMPTY) &&
                        !root.children.contains(new MoveNode(new Point(row, col)))) {

                    curBoard.setValue(row, col, curPlayer);

                    MoveNode expandedNode = new MoveNode(new Point(row, col), root, curBoard);
                    root.children.add(expandedNode);

                    return new tuple(curPlayer, curBoard, expandedNode);
                }
            }
        }

        throw new RuntimeException("ERROR: No Empty spots on board");
    }

    // The UCB1 algorithm.
    private double calculateUCT(MoveNode root, MoveNode child) {
        double exploitation = (double) child.numWins / child.numPlays;
        double exploration = Math.sqrt(2.0 * Math.log(root.numPlays) / child.numPlays);
        return exploitation + 2 * EXPLORATION_CONSTANT * exploration;
    }

    // Conducts a random simulation of the remaining EMPTY tiles.
    // Returns the winner of this random simulation (Hex always has a winner).
    private Pieces defaultPolicy(Pieces player, Board curBoard) {
//        numSimulations++;

        List<Point> emptyTiles = new LinkedList<>();

        for (int row = 0; row < curBoard.getDimensions(); row++) {
            for (int col = 0; col < curBoard.getDimensions(); col++) {

                if (curBoard.getValue(row, col).equals(Pieces.EMPTY)) {
                    emptyTiles.add(new Point(row, col));
                }
            }
        }

        Collections.shuffle(emptyTiles);

        for (Point tile : emptyTiles) {
            player = Pieces.changePlayer(player);
            curBoard.setValue(tile.x, tile.y, player);
        }

        return utils.terminateBlack(curBoard) ? Pieces.BLACK : Pieces.WHITE;
    }

    // Backs up the statistics. All node's numPlays are incremented and the winning player's numWins are incremented.
    private void backup(Pieces winningPlayer, Pieces curPlayer, MoveNode curMove) {
        curMove.numPlays++;

        if (curMove.parent == null) {
            return;
        }

        if (curPlayer.equals(winningPlayer)) {
            curMove.numWins++;
        }

        backup(winningPlayer, Pieces.changePlayer(curPlayer), curMove.parent);
    }

    // Selects the MoveNode with the best win/plays ration.
    private MoveNode selectMax(MoveNode root) {
        double max = Double.MIN_VALUE;
        MoveNode bestChild = null;

        for (MoveNode child : root.children) {

            double reward = (double) child.numWins / child.numPlays;
            if (reward > max) {
                max = reward;
                bestChild = child;
            }
        }

        return bestChild;
    }

    // Represents a move. Contains MCTS statistics like number of plays and wins in order to compute a reward value.
    class MoveNode {

        Point move;
        int numPlays;
        int numWins;

        // This MoveNode's parent in the MCTS tree.
        MoveNode parent;

        // All of this MoveNode's children (i.e. expanded nodes).
        List<MoveNode> children;

        // Allows a fast check to see if the node is fully expanded. This is calculated once every time a node is added
        // whereas without this, this would have to be checked every new level.
        int possibleChildren;

        public MoveNode() {
            move = null;
            parent = null;
            children = new LinkedList<>();
            possibleChildren = 0;
            numPlays = 0;
            numWins = 0;
        }

        public MoveNode(Board board) {
            move = null;
            parent = null;
            children = new LinkedList<>();
            countPossibleChildren(board);
            numPlays = 0;
            numWins = 0;
        }

        public MoveNode(Point move) {
            this.move = move;
            parent = null;
            children = new LinkedList<>();
            possibleChildren = 0;
            numPlays = 0;
            numWins = 0;
        }

        public MoveNode(Point move, MoveNode parent, Board board) {
            this.move = new Point(move);
            this.parent = parent;
            children = new LinkedList<>();
            countPossibleChildren(board);
            numPlays = 0;
            numWins = 0;
        }

        // Determines how many children of this move are possible.
        private void countPossibleChildren(Board board) {
            int count = 0;

            for (int row = 0; row < board.getDimensions(); row++) {
                for (int col = 0; col < board.getDimensions(); col++) {

                    if (board.getValue(row, col).equals(Pieces.EMPTY)) {
                        count++;
                    }
                }
            }

            possibleChildren = count;
        }

        @Override
        public String toString() {
            return move.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            if (!(obj instanceof MoveNode)) {
                return false;
            }

            MoveNode moveNode = (MoveNode) obj;

            return move.equals(moveNode.move);
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + move.hashCode();
            return result;
        }
    }

    // Class holding data relating a terminalMove by a player that created a board.
    class tuple {

        final Pieces player;
        final Board board;
        final MoveNode terminalMove;

        public tuple(Pieces player, Board board, MoveNode terminalMove) {
            this.player = player;
            this.board = board;
            this.terminalMove = terminalMove;
        }
    }
}

