package com.company.game;

import com.company.utils.Pieces;
import com.company.utils.Side;

import java.awt.*;
import java.util.*;

// A glorified square matrix class.
public class Board {

    // Constants that represent pieces used in a numerical 2-d array.
    private final int EMPTY = 0;
    private final int PLAYER = Integer.MAX_VALUE;
    private final int OPPONENT = Integer.MIN_VALUE;

    private int dimensions;
    private Pieces[][] grid;

    // Default constructor leaves everything as empty;
    public Board() {
        dimensions = 0;
        grid = null;
    }

    // Creates a new Board of the specified dimensions.
    // Initializes everything to EMPTY.
    public Board(int dimensions) {
        this.dimensions = dimensions;
        grid = new Pieces[dimensions][dimensions];

        for (Pieces[] row : grid) {
            Arrays.fill(row, Pieces.EMPTY);
        }
    }

    // Creates a new Board from another Board.
    public Board(Board board) {
        dimensions = board.dimensions;
        grid = arrayCopy(board.grid);
    }

    // Helper method to copy arrays because two dimensional arrays have trouble being copied.
    private Pieces[][] arrayCopy(Pieces[][] arr) {
        Pieces[][] ret = new Pieces[dimensions][dimensions];

        for (int row = 0; row < dimensions; row++) {
            for (int col = 0; col < dimensions; col++) {
                ret[row][col] = arr[row][col];
            }
        }

        return ret;
    }

    // Returns the value associated with the location in the Board.
    public Pieces getValue(int row, int col) {
        validLocation(row, col);
        return grid[row][col];
    }

    // Sets the value at the specified location in the Board.
    public void setValue(int row, int col, Pieces val) {
        validLocation(row, col);
        grid[row][col] = val;
    }

    // Sets the value at a specified location in the Board to EMPTY.
    public void clearValue(int row, int col) {
        validLocation(row, col);
        grid[row][col] = Pieces.EMPTY;
    }

    // Returns true if the location is in bounds.
    private void validLocation(int row, int col) {
        if (row < 0 || row >= dimensions) {
            throw new IllegalArgumentException("Invalid row");
        }

        if (col < 0 || col >= dimensions) {
            throw new IllegalArgumentException("Invalid column");
        }
    }

    // Returns the length of the Board.
    public int getDimensions() {
        return dimensions;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Board)) {
            return false;
        }

        Board board = (Board) obj;

        return dimensions == board.dimensions &&
                Arrays.deepEquals(grid, board.grid);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + dimensions;
        result = 31 * result + Arrays.deepHashCode(grid);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int row = 0; row < dimensions; row++) {
            for (int col = 0; col < dimensions; col++) {
                sb.append(grid[row][col].getVal() + " ");
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    // Prints the Board in a nicely formatted manner.
    public void print() {

        // Prints the row number labels
        String whiteSpace = "     ";
        System.out.print("       " + 0);
        for (int num = 1; num < dimensions; num++) {
            System.out.print(whiteSpace + num);
        }

        System.out.println();

        // Prints the first hexagons.
        printTopBorder();
        printMiddle(0, 0);
        printBottomBorder(0);

        // Prints the rest of the board.
        int numWhiteSpace = 3;
        for (int row = 1; row < dimensions; row++) {
            printMiddle(numWhiteSpace * row, row);
            printBottomBorder(numWhiteSpace * row);
        }

        // Prints the BLACK label.
        System.out.print(new String(new char[(int)(dimensions * 3.5)]).replace("\0", " "));
        System.out.println(new String(new char[dimensions]).replace("\0", "BLACK "));
    }

    // Prints the   v   pattern.
    //            v   v
    private void printTopBorder() {
        String whiteSpace = "     ";
        String token = "v";
        System.out.print("       " + token);
        for (int top = 1; top < dimensions; top++) {
            System.out.print(whiteSpace + token);
        }

        System.out.println();

        System.out.print("    " + token);
        for (int topMid = 0; topMid < dimensions; topMid++) {
            System.out.print(whiteSpace + token);
        }

        System.out.println();
    }

    // Prints the middle | labeled with whoever's piece is occupying the tile.
    // Also prints the row and WHITE labels.
    private void printMiddle(int bufferSize, int row) {
        System.out.print(new String(new char[bufferSize]).replace("\0", " "));
        System.out.print(row + "   ");

        String whiteSpace = "  ";
        String token = "|";
        System.out.print(token);
        for (int mid = 0; mid < dimensions; mid++) {
            System.out.print(whiteSpace + grid[row][mid] + whiteSpace + token);
        }

        System.out.println(" WHITE");
    }

    // Prints the bottom    v   v   pattern.
    //                        v
    private void printBottomBorder(int bufferSize) {
        System.out.print(new String(new char[bufferSize]).replace("\0", " "));

        String whiteSpace = "     ";
        String token = "v";
        System.out.print("    " + token);
        for (int bottomMid = 0; bottomMid < dimensions; bottomMid++) {
            System.out.print(whiteSpace + token);
        }

        System.out.println();

        System.out.print(new String(new char[bufferSize]).replace("\0", " "));

        System.out.print("       " + token);
        for (int bottom = 0; bottom < dimensions; bottom++) {
            System.out.print(whiteSpace + token);
        }

        System.out.println();
    }

    // Calculates the heuristic using two-distance.
    public double calculateHeuristic(Pieces player) {

        int[][] pieceOne = new int[dimensions][dimensions];
        int[][] pieceTwo = new int[dimensions][dimensions];

        for (int row = 0; row < dimensions; row++) {
            pieceOne[row][0] = 1;
            pieceTwo[row][dimensions - 1] = 1;
        }

        boardToIntCopy(pieceOne, Pieces.changePlayer(player));
        boardToIntCopy(pieceTwo, Pieces.changePlayer(player));

        calculateWhiteLeft(pieceOne, player);
        calculateWhiteRight(pieceTwo, player);

        int twoDistanceWhite = getMinVal(pieceOne, pieceTwo);

        pieceOne = new int[dimensions][dimensions];
        pieceTwo = new int[dimensions][dimensions];

        Arrays.fill(pieceOne[0], 1);
        Arrays.fill(pieceTwo[dimensions - 1], 1);

        boardToIntCopy(pieceOne, player);
        boardToIntCopy(pieceTwo, player);

        calculateBlackTop(pieceOne, player);
        calculateBlackBottom(pieceTwo, player);

        int twoDistanceBlack = getMinVal(pieceOne, pieceTwo);

        return player.equals(Pieces.BLACK) ? (double) twoDistanceWhite / twoDistanceBlack :
                                             (double) twoDistanceBlack / twoDistanceWhite;
    }

    // Translates a Pieces 2-d array to an int 2-d array.
    private void boardToIntCopy(int[][] arr, Pieces player) {
        Pieces opponent = Pieces.changePlayer(player);

        for (int row = 0; row < dimensions; row++) {
            for (int col = 0; col < dimensions; col++) {

                if (grid[row][col].equals(player)) {
                    arr[row][col] = PLAYER;
                }

                if (grid[row][col].equals(opponent)) {
                    arr[row][col] = OPPONENT;
                }
            }
        }
    }

    // Calculates the two-distances for the WHITE piece to the LEFT edge by setting each cell to the appropriate value.
    private void calculateWhiteLeft(int[][] whiteLeft, Pieces player) {
        Queue<Point> skipped = new LinkedList<>();

        for (int col = 1; col < dimensions; col++) {
            for (int row = 0; row < dimensions; row++) {

                if (whiteLeft[row][col] == EMPTY && !secondMin(Side.LEFT, whiteLeft, row, col, false)) {
                    skipped.add(new Point(row, col));
                }
            }
        }

        while (!skipped.isEmpty()) {
            Point skippedPoint = skipped.poll();
            secondMin(Side.LEFT, whiteLeft, skippedPoint.x, skippedPoint.y, true);
        }
    }

    // Calculates the two-distances for the WHITE piece to the RIGHT edge by setting each cell to the appropriate value.
    private void calculateWhiteRight(int[][] whiteRight, Pieces player) {
        Queue<Point> skipped = new LinkedList<>();

        for (int col = dimensions - 2; col >= 0; col--) {
            for (int row = 0; row < dimensions; row++) {

                if (whiteRight[row][col] == EMPTY && !secondMin(Side.RIGHT, whiteRight, row, col, false)) {
                    skipped.add(new Point(row, col));
                }
            }
        }

        while (!skipped.isEmpty()) {
            Point skippedPoint = skipped.poll();
            secondMin(Side.RIGHT, whiteRight, skippedPoint.x, skippedPoint.y, true);
        }
    }

    // Calculates the two-distances for the BLACK piece to the TOP edge by setting each cell to the appropriate value.
    private void calculateBlackTop(int[][] blackTop, Pieces player) {
        Queue<Point> skipped = new LinkedList<>();

        for (int row = 1; row < dimensions; row++) {
            for (int col = 0; col < dimensions; col++) {

                if (blackTop[row][col] == EMPTY && !secondMin(Side.TOP, blackTop, row, col, false)) {
                    skipped.add(new Point(row, col));
                }
            }
        }

        while (!skipped.isEmpty()) {
            Point skippedPoint = skipped.poll();
            secondMin(Side.TOP, blackTop, skippedPoint.x, skippedPoint.y, true);
        }
    }

    // Calculates the two-distances for the BLACK piece to the BOTTOM edge by setting each cell to the appropriate value.
    private void calculateBlackBottom(int[][] blackBottom, Pieces player) {
        Queue<Point> skipped = new LinkedList<>();

        for (int row = dimensions - 2; row >= 0; row--) {
            for (int col = 0; col < dimensions; col++) {

                if (blackBottom[row][col] == EMPTY && !secondMin(Side.BOTTOM, blackBottom, row, col, false)) {
                    skipped.add(new Point(row, col));
                }
            }
        }

        while (!skipped.isEmpty()) {
            Point skippedPoint = skipped.poll();
            secondMin(Side.BOTTOM, blackBottom, skippedPoint.x, skippedPoint.y, true);
        }
    }

    // If there are two neighboring points, set the location in arr to the second lowest value + 1.
    // Else, if skipped is true, then set the location in arr to the lowest value + 1 or infinity if there is no
    // neighboring value.
    // Returns true if a second lowest neighbor was found.
    public boolean secondMin(Side side, int[][] arr, int row, int col, boolean skipped) {
        Set<Point> connections = new HashSet<>();
        fillConnections(arr, row, col, connections);

        Set<Point> locations = new HashSet<>();
        fillLocations(side, arr, locations, connections);

        int min = Integer.MAX_VALUE;
        int secondMin =  Integer.MAX_VALUE;

        for (Point loc : locations) {

            int val = 0;
            if (inRange(loc.x, loc.y)) {
                val = arr[loc.x][loc.y];
            }

            if (val < min) {

                secondMin = min;
                min = val;

            } else if (val < secondMin) {
                    secondMin = val;
            }
        }

        if (secondMin != Integer.MAX_VALUE) {
            arr[row][col] = secondMin + 1;
            return true;
        }

        if (skipped) {
            if (min != Integer.MAX_VALUE) {
                arr[row][col] = min + 1;

            } else {
                arr[row][col] = EMPTY;
            }
        }

        return false;
    }

    // Finds all points that make up any bridges connected to a tile.
    private void fillConnections(int[][] arr, int row, int col, Set<Point> connections) {
        connections.add(new Point(row, col));

        Point coord = new Point(row + 1, col);
        if (isConnection(row + 1, col, arr) && !connections.contains(coord)) {
            fillConnections(arr, row + 1, col, connections);
        }

        coord = new Point(row - 1, col);
        if (isConnection(row - 1, col, arr) && !connections.contains(coord)) {
            fillConnections(arr, row - 1, col, connections);
        }

        coord = new Point(row, col + 1);
        if (isConnection(row, col + 1, arr) && !connections.contains(coord)) {
            fillConnections(arr, row, col + 1, connections);
        }

        coord = new Point(row, col - 1);
        if (isConnection(row, col - 1, arr) && !connections.contains(coord)) {
            fillConnections(arr, row, col - 1, connections);
        }

        coord = new Point(row + 1, col - 1);
        if (isConnection(row + 1, col - 1, arr) && !connections.contains(coord)) {
            fillConnections(arr, row + 1, col - 1, connections);
        }

        coord = new Point(row - 1, col + 1);
        if (isConnection(row - 1, col + 1, arr) && !connections.contains(coord)) {
            fillConnections(arr, row - 1, col + 1, connections);
        }
    }

    // Finds all occupied neighbors of a bridge.
    private void fillLocations(Side side, int[][] arr, Set<Point> locations, Set<Point> connections) {
        for (Point tile : connections) {

            Point coord = new Point(tile.x + 1, tile.y);
            if (sideValidCell(side, coord.x, coord.y, arr) && !locations.contains(coord)) {
                locations.add(coord);
            }

            coord = new Point(tile.x - 1, tile.y);
            if (sideValidCell(side, coord.x, coord.y, arr) && !locations.contains(coord)) {
                locations.add(coord);
            }

            coord = new Point(tile.x, tile.y + 1);
            if (sideValidCell(side, coord.x, coord.y, arr) && !locations.contains(coord)) {
                locations.add(coord);
            }

            coord = new Point(tile.x, tile.y - 1);
            if (sideValidCell(side, coord.x, coord.y, arr) && !locations.contains(coord)) {
                locations.add(coord);
            }

            coord = new Point(tile.x + 1, tile.y - 1);
            if (sideValidCell(side, coord.x, coord.y, arr) && !locations.contains(coord)) {
                locations.add(coord);
            }

            coord = new Point(tile.x - 1, tile.y + 1);
            if (sideValidCell(side, coord.x, coord.y, arr) && !locations.contains(coord)) {
                locations.add(coord);
            }
        }
    }

    // Finds the min value in a 2-d array.
    private int getMinVal(int[][] pieceOne, int[][] pieceTwo) {
        int min = Integer.MAX_VALUE;
        for (int row = 0; row < dimensions; row++) {
            for (int col = 0; col < dimensions; col++) {

                if (validCell(row, col, pieceOne) && validCell(row, col, pieceTwo)) {
                    int val = Math.abs(pieceOne[row][col] + pieceTwo[row][col]);
                    if (val < min) {
                        min = val;
                    }
                }

            }
        }

        return min;
    }

    // Returns true if a point is in range and occupied by PLAYER.
    private boolean isConnection(int row, int col, int[][] arr) {
        return inRange(row, col) && (arr[row][col] == PLAYER);
    }

    // Returns true if the location isn't occupied or out of bounds.
    private boolean validCell(int row, int col, int[][] arr) {
        return inRange(row, col) && notOccupied(row, col, arr);
    }

    // Returns true if the location isn't occupied or out of bounds. This out of bounds is subjective based on what edge
    // is being tested for.
    private boolean sideValidCell(Side side, int row, int col, int[][] arr) {
        switch(side) {
            case LEFT:
                return (col < dimensions) && (row >= 0) && (row < dimensions) && (col < 0 || notOccupied(row, col, arr));
            case RIGHT:
                return (col >= 0) && (row >= 0) && (row < dimensions) && (col >= dimensions || notOccupied(row, col, arr));
            case TOP:
                return (col >= 0) && (col < dimensions) && (row < dimensions) && (row < 0 || notOccupied(row, col, arr));
            case BOTTOM:
                return (col >= 0) && (col < dimensions) && (row >= 0) && (row >= dimensions || notOccupied(row, col, arr));
            default:
                throw new IllegalArgumentException("Wrong side entered");
        }
    }

    // Returns true if the point is in bounds of the Board.
    private boolean inRange(int row, int col) {
        return (row >= 0) && (row < dimensions) && (col >= 0) && (col < dimensions);
    }

    // Returns true if the point is not occupied. In this context it means if there's a two-distance heuristic
    // associated with the cell.
    private boolean notOccupied(int row, int col, int[][] arr) {
        return (arr[row][col] != EMPTY) && (arr[row][col] != PLAYER) && (arr[row][col] != OPPONENT);
    }
}
