package com.company.players;

import com.company.game.Board;
import com.company.utils.Pieces;
import com.company.utils.utils;

import java.awt.*;
import java.util.*;
import java.util.List;

// Represents a MCTS player enhanced by AMAF heuristic and an opening book.
public class EnhancedMonteCarloPlayer implements Player{

    // Time limit is set to 3 seconds right now.
    private final long TIME_LIMIT = 3000;

    // Exploration constant was optimized by the literature.
    private final double EXPLORATION_CONSTANT = 1 / Math.sqrt(2);

//    private int numSimulations = 0;
//    private int size = 0;

    // Color MCTS plays as.
    private Pieces player;

    // Default constructor pretty much does nothing.
    public EnhancedMonteCarloPlayer() {
        player = Pieces.EMPTY;
    }

    // Initializes MCTS to the given color.
    public EnhancedMonteCarloPlayer(Pieces player) {
        this.player = player;
    }

    // Makes a move using MCTS algorithm. The opening book is used instead if its the first move.
    @Override
    public void makeMove(Board board) {
//        numSimulations = 0;
//        size = 0;

        List<Point> opponentMoves = new LinkedList<>();
        int count = countMoves(board, opponentMoves);

        MoveNode move = (count == 0) ? openingBook(player, count, opponentMoves) : MonteCarloSearch(board, player);

//        System.out.println("Number of simulations: " + numSimulations);
//        System.out.println("Max size of tree: " + size);
        System.out.println("MOVE FOUND: " + move);

        board.setValue(move.move.x, move.move.y, player);
    }

    // Counts how many moves have occured thus far.
    private int countMoves(Board board, List<Point> opponentMoves) {
        int count = 0;
        for (int row = 0; row < board.getDimensions(); row++) {
            for (int col = 0; col < board.getDimensions(); col++) {

                if (board.getValue(row, col).equals(player)) {
                    count++;

                } else if (!board.getValue(row, col).equals(Pieces.EMPTY)) {
                    opponentMoves.add(new Point(row, col));
                }
            }
        }

        return count;
    }

    // Produces a predetermined move based on the board state.
    private MoveNode openingBook(Pieces player, int count, List<Point> opponentMoves) {
        switch (count) {
            case 0:
                if (player.equals(Pieces.WHITE)) {
                    return new MoveNode(new Point(3, 3));

                } else {
                    Point opponentMove = new Point(opponentMoves.get(0));
                    if (opponentMove.x == 3 && opponentMove.y == 3) {
                        return new MoveNode(new Point(3, 4));

                    } else if (opponentMove.x == 3 && opponentMove.y == 4) {
                        return new MoveNode(new Point(3, 3));

                    } else if (opponentMove.x == 4 && opponentMove.y == 3) {
                        return new MoveNode(new Point(4, 4));

                    } else if (opponentMove.x == 4 && opponentMove.y == 4) {
                        return new MoveNode(new Point(4, 3));

                    } else if (opponentMove.x <= 3 && opponentMove.y <= 3) {
                        return new MoveNode(new Point(3, 3));

                    } else if (opponentMove.x <= 3 && opponentMove.y >= 4) {
                        return new MoveNode(new Point(3, 4));

                    } else if (opponentMove.y <= 3) {
                        return new MoveNode(new Point(4, 3));

                    } else {
                        return new MoveNode(new Point(4, 4));
                    }
                }
            default:
                throw new IllegalArgumentException("Opening book should not be used here!");
        }
    }

    // MCTS algorithm. Uses UCT, then random simulation, then backup while time allows. Finally uses max child to select
    // a move.
    private MoveNode MonteCarloSearch(Board board, Pieces curPlayer) {
        MoveNode root = new MoveNode(board);
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < TIME_LIMIT) {
            tuple terminalNode = treePolicy(new Board(board), root, curPlayer);
            DefaultNode defaultNode = defaultPolicy(terminalNode.player, terminalNode.board);
            backup(defaultNode.player, defaultNode.whitePlaySet, defaultNode.blackPlaySet, terminalNode.player, terminalNode.terminalMove);
        }

        return selectMax(root);
    }

    // Chooses the best child according to UCB1 until a node with unexpanded children is encountered. A child is then
    // expanded. This policy takes AMAF values into account.
    private tuple treePolicy(Board curBoard, MoveNode root, Pieces curPlayer) {

        // If can expand, then expand
        if (root.possibleChildren != root.children.size()) {
            return expand(curBoard, root, curPlayer);
        }

        double max = Double.MIN_VALUE;
        MoveNode bestChild = new MoveNode();

        for (MoveNode child : root.children) {

            double heuristic = calculateAMAF(root, child);
            if (heuristic > max) {
                max = heuristic;
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

    // Calculates UCB1 taking AMAF values in account.
    private double calculateAMAF(MoveNode root, MoveNode child) {
        double exploitation = (double) (child.numWins + child.numAMAFWins) / (child.numPlays + child.numAMAFPlays);
        double exploration = Math.sqrt(2.0 * Math.log(root.numPlays + root.numAMAFPlays) / (child.numPlays + child.numAMAFPlays));
        return exploitation + 2.0 * EXPLORATION_CONSTANT * exploration;
    }

    // Conducts a random simulation of the remaining EMPTY tiles.
    // Returns the winner of this random simulation (Hex always has a winner).
    // So that AMAF values can be backed up, it also returns the moves that each player made during the simulation.
    private DefaultNode defaultPolicy(Pieces player, Board curBoard) {
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
        Set<MoveNode> whitePlaySet = new HashSet<>();
        Set<MoveNode> blackPlaySet = new HashSet<>();

        for (Point tile : emptyTiles) {
            player = Pieces.changePlayer(player);
            curBoard.setValue(tile.x, tile.y, player);

            if (player.equals(Pieces.WHITE)) {
                whitePlaySet.add(new MoveNode(tile));

            } else {
                blackPlaySet.add(new MoveNode(tile));
            }
        }

        return new DefaultNode(utils.terminateBlack(curBoard) ? Pieces.BLACK : Pieces.WHITE, whitePlaySet, blackPlaySet);
    }

    // Backs up the statistics. All node's numPlays are incremented and the winning player's numWins are incremented.
    // Cutoff AMAF is incorporated too. AMAF is updated up until the first row in the MCTS tree.
    private void backup(Pieces winningPlayer, Set<MoveNode> whitePlaySet, Set<MoveNode> blackPlaySet,
                        Pieces curPlayer, MoveNode curMove) {

        curMove.numPlays++;

        if (curMove.parent == null) {
            return;
        }

        if (curPlayer.equals(winningPlayer)) {
            curMove.numWins++;

            // If this is currently the winning player, then all its children are of the losing color. So update all
            // losing plays for AMAF.
            for (MoveNode child : curMove.children) {
                if (winningPlayer.equals(Pieces.BLACK) && whitePlaySet.contains(child)) {
                    child.numAMAFPlays++;

                } else if (blackPlaySet.contains(child)) {
                    child.numAMAFPlays++;
                }
            }
        } else {

            // If this is currently the losing player, then all its children are of the winning color. So update all
            // winning plays for AMAF.
            for (MoveNode child : curMove.children) {
                if (winningPlayer.equals(Pieces.BLACK) && blackPlaySet.contains(child)) {
                    child.numAMAFPlays++;
                    child.numAMAFWins++;

                } else if (whitePlaySet.contains(child)) {
                    child.numAMAFPlays++;
                    child.numAMAFWins++;
                }
            }
        }

        backup(winningPlayer, whitePlaySet, blackPlaySet, Pieces.changePlayer(curPlayer), curMove.parent);
    }

    // Selects the MoveNode with the best win/plays ration.
    private MoveNode selectMax(MoveNode root) {
        double max = Double.MIN_VALUE;
        MoveNode bestChild = null;

        for (MoveNode child : root.children) {

            double reward = (double) (child.numWins + child.numAMAFWins) / (child.numPlays + child.numAMAFPlays);
            if (reward > max) {
                max = reward;
                bestChild = child;
            }
        }

        return bestChild;
    }

    // Represents a move. Contains MCTS and AMAF statistics like number of plays and wins in order to compute a reward
    // value.
    class MoveNode {

        Point move;
        int numPlays;
        int numWins;
        int numAMAFPlays;
        int numAMAFWins;

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
            numAMAFPlays = 0;
            numAMAFWins = 0;
        }

        public MoveNode(Board board) {
            move = null;
            parent = null;
            children = new LinkedList<>();
            countPossibleChildren(board);
            numPlays = 0;
            numWins = 0;
            numAMAFPlays = 0;
            numAMAFWins = 0;
        }

        public MoveNode(Point move) {
            this.move = move;
            parent = null;
            children = new LinkedList<>();
            possibleChildren = 0;
            numPlays = 0;
            numWins = 0;
            numAMAFPlays = 0;
            numAMAFWins = 0;
        }

        public MoveNode(Point move, MoveNode parent, Board board) {
            this.move = new Point(move);
            this.parent = parent;
            children = new LinkedList<>();
            countPossibleChildren(board);
            numPlays = 0;
            numWins = 0;
            numAMAFPlays = 0;
            numAMAFWins = 0;
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

    // Class holding data representing the end state of a random simulation. The player is the winning player and the
    // sets contain the moves made by each player during the simulation.
    class DefaultNode {

        final Pieces player;
        final Set<MoveNode> whitePlaySet;
        final Set<MoveNode> blackPlaySet;

        public DefaultNode(Pieces player, Set<MoveNode> whitePlaySet, Set<MoveNode> blackPlaySet) {
            this.player = player;
            this.whitePlaySet = new HashSet<>(whitePlaySet);
            this.blackPlaySet = new HashSet<>(blackPlaySet);
        }
    }
}

