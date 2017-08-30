package com.company.utils;

import com.company.game.Board;

import java.awt.*;
import java.util.*;
import java.util.List;

public class utils {

    // Returns a valid integer
    public static int validInt(Scanner console) {
        while (!console.hasNextInt()) {
            System.out.println("ERROR: Please enter an integer.");
            console.next();
            console.nextLine();
        }

        return console.nextInt();
    }

    public static boolean outOfBounds(int row, int col, Board board) {
        if (row < 0 || row >= board.getDimensions()) {
            System.out.println("ERROR: Row out of bounds. Please enter a valid move.");
            return true;
        }

        if (col < 0 || col >= board.getDimensions()) {
            System.out.println("ERROR: Column out of bounds. Please enter a valid move.");
            return true;
        }

        return false;
    }

    public static boolean inRange(int row, int col, Board board) {
        return (row >= 0) && (row < board.getDimensions()) && (col >= 0) && (col < board.getDimensions());
    }

    public static boolean overlappingMove(int row, int col, Board board) {
        if (board.getValue(row, col).equals(Pieces.WHITE)) {
            System.out.println("ERROR: White piece already exists here. Please enter a valid move.");
            return true;
        }

        if (board.getValue(row, col).equals(Pieces.BLACK)) {
            System.out.println("ERROR: Black piece already exists here. Please enter a valid move.");
            return true;
        }

        return false;
    }

    public static boolean overLappingMoveCheck(int row, int col, Board board) {
        return board.getValue(row, col).equals(Pieces.EMPTY);
    }

    public static boolean terminateWhite(Board board) {
        Set<Point> used = new HashSet<>();

        for (int row = 0; row < board.getDimensions(); row++) {

            if (board.getValue(row, 0).equals(Pieces.WHITE)) {
                if (board.getDimensions() == 1) {
                    return true;
                }

                Point cur = new Point(row, 0);
                used.add(cur);

                for (Point neighbor : neighboringPoints(Pieces.WHITE, row, 0, board)) {

                    if (terminateHelper(Pieces.WHITE, neighbor.x, neighbor.y, used, board)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean terminateBlack(Board board) {
        Set<Point> used = new HashSet<>();

        for (int col = 0; col < board.getDimensions(); col++) {

            if (board.getValue(0, col).equals(Pieces.BLACK)) {
                if (board.getDimensions() == 1) {
                    return true;
                }

                Point cur = new Point(0, col);
                used.add(cur);

                for (Point neighbor : neighboringPoints(Pieces.BLACK, 0, col, board)) {

                    if (terminateHelper(Pieces.BLACK, neighbor.x, neighbor.y, used, board)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static boolean terminateHelper(Pieces player, int row, int col, Set<Point> used, Board board) {
        if (used.contains(new Point(row, col))) {
            return false;
        }

        switch (player) {
            case WHITE:
                if (col == board.getDimensions() - 1) {
                    return true;
                }
                break;
            case BLACK:
                if (row == board.getDimensions() - 1) {
                    return true;
                }
                break;
            default:
                break;
        }

        Point cur = new Point(row, col);
        used.add(cur);

        for (Point neighbor : neighboringPoints(player, row, col, board)) {

            if (terminateHelper(player, neighbor.x, neighbor.y, used, board)) {
                return true;
            }
        }

        used.remove(cur);

        return false;
    }

    private static List<Point> neighboringPoints(Pieces player, int row, int col, Board board) {
        List<Point> neighbors = new ArrayList<>();

        addToNeighbors(player, neighbors, row + 1, col, board);
        addToNeighbors(player, neighbors, row - 1, col, board);
        addToNeighbors(player, neighbors, row, col + 1, board);
        addToNeighbors(player, neighbors, row, col - 1, board);
        addToNeighbors(player, neighbors, row + 1, col - 1, board);
        addToNeighbors(player, neighbors, row - 1, col + 1, board);

        return neighbors;
    }

    private static void addToNeighbors(Pieces player, List<Point> neighbors, int row, int col, Board board) {
        if (inRange(row, col, board) && board.getValue(row, col).equals(player)) {
            neighbors.add(new Point(row, col));
        }
    }
}
