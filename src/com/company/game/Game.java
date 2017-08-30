package com.company.game;

import com.company.players.*;
import com.company.utils.Decisions;
import com.company.utils.GoingFirst;
import com.company.utils.Pieces;
import com.company.utils.utils;

import java.awt.*;
import java.util.*;
import java.util.List;

// Plays the game with a selected mode and order.
public class Game {

    // The board size is set to 8 for this project.
    public static final int BOARD_SIZE = 8;

    private Player playerOne;
    private Player playerTwo;
    private Board board;

    // Default constructor creates a game with two human players.
    public Game() {
        playerOne = new HumanPlayer();
        playerTwo = new HumanPlayer();
        board = new Board(BOARD_SIZE);
    }

    // Creates a game with the appropriate mode and order.
    public Game(Decisions modeType, GoingFirst order) {
        board = new Board(BOARD_SIZE);

        switch (modeType) {
            case PVP:
                playerOne = new HumanPlayer(Pieces.WHITE);
                playerTwo = new HumanPlayer(Pieces.BLACK);
                break;
            case PVE1:
                playerOne = order.equals(GoingFirst.FIRST) ? new HumanPlayer(Pieces.WHITE) : new AlphaBetaPlayer(Pieces.WHITE);
                playerTwo = order.equals(GoingFirst.FIRST) ? new AlphaBetaPlayer(Pieces.BLACK) : new HumanPlayer(Pieces.BLACK);
                break;
            case PVE2:
                playerOne = order.equals(GoingFirst.FIRST) ? new HumanPlayer(Pieces.WHITE) : new MonteCarloPlayer(Pieces.WHITE);
                playerTwo = order.equals(GoingFirst.FIRST) ? new MonteCarloPlayer(Pieces.BLACK) : new HumanPlayer(Pieces.BLACK);
                break;
            case PVE3:
                playerOne = order.equals(GoingFirst.FIRST) ? new HumanPlayer(Pieces.WHITE) : new EnhancedMonteCarloPlayer(Pieces.WHITE);
                playerTwo = order.equals(GoingFirst.FIRST) ? new EnhancedMonteCarloPlayer(Pieces.BLACK) : new HumanPlayer(Pieces.BLACK);
                break;
            case EVE1:
                playerOne = order.equals(GoingFirst.FIRST) ? new AlphaBetaPlayer(Pieces.WHITE) : new MonteCarloPlayer(Pieces.WHITE);
                playerTwo = order.equals(GoingFirst.FIRST) ? new MonteCarloPlayer(Pieces.BLACK) : new AlphaBetaPlayer(Pieces.BLACK);
                break;
            case EVE2:
                playerOne = order.equals(GoingFirst.FIRST) ? new AlphaBetaPlayer(Pieces.WHITE) : new EnhancedMonteCarloPlayer(Pieces.WHITE);
                playerTwo = order.equals(GoingFirst.FIRST) ? new EnhancedMonteCarloPlayer(Pieces.BLACK) : new AlphaBetaPlayer(Pieces.BLACK);
                break;
            case EVE3:
                playerOne = order.equals(GoingFirst.FIRST) ? new MonteCarloPlayer(Pieces.WHITE) : new EnhancedMonteCarloPlayer(Pieces.WHITE);
                playerTwo = order.equals(GoingFirst.FIRST) ? new EnhancedMonteCarloPlayer(Pieces.BLACK) : new MonteCarloPlayer(Pieces.BLACK);
                break;
            default:
                throw new IllegalArgumentException("Bad decision");
        }
    }

    // Prints instructions and then plays the selected game. Players 1 and 2 alternate moves until someone wins.
    // The current board is printed every turn.
    public void play() {
        System.out.println("Starting game now!");
        System.out.println("Make a move by entering a location on the board.");
        System.out.println("The format is two valid numbers separated by a space.");
        System.out.println("Example: \"0 4\" would select the 5th tile from the left in the top row.");
        System.out.println();
        board.print();

        while (true) {
            System.out.println("Player 1's turn. Please make a move.");
            playerOne.makeMove(board);

            board.print();

            if (utils.terminateWhite(board)) {
                System.out.println("\t\t**************");
                System.out.println("******************************");
                System.out.println("Congratulations! Player 1 won!");
                System.out.println("******************************");
                System.out.println("\t\t**************");
                break;
            }

            System.out.println("Player 2's turn. Please make a move.");
            playerTwo.makeMove(board);

            board.print();

            if (utils.terminateBlack(board)) {
                System.out.println("\t\t**************");
                System.out.println("******************************");
                System.out.println("Congratulations! Player 2 won!");
                System.out.println("******************************");
                System.out.println("\t\t**************");
                break;
            }
        }
    }
}
