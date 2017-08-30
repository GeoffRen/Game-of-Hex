package com.company.game;

import com.company.utils.*;

import java.util.Scanner;

// Runs the program.
public class Driver {

    // Constant representing whether the player wants to continue playing or not
    private static final int RUN = 1;

    public static void main(String[] args) {
        System.out.println("-------------Welcome to the game of Hex!-------------");
        System.out.println();

        boolean running = true;
        while (running) {
            Scanner console = new Scanner(System.in);

            gameModeText();
            Decisions modeType = modeType();

            goingFirstText();
            GoingFirst order = order();

            Game game = new Game(modeType, order);
            game.play();

            running = runAgain();
            console.nextLine();
        }

        System.out.println("Thank you for playing!");
        System.out.println("----------Ending Program----------");
    }

    // Introduces the game modes.
    private static void gameModeText() {
        System.out.println("What mode do you want to play?");
        System.out.println("Select one of the following options: ");
        System.out.println("0 | PVP (Play against another person)");
        System.out.println("1 | PVE (Play against an alpha beta AI)");
        System.out.println("2 | PVE (Play against a monte carlo AI)");
        System.out.println("3 | PVE (Play against an enhanced monte carlo AI)");
        System.out.println("4 | EVE (Watch two AI play: alpha beta vs monte carlo)");
        System.out.println("5 | EVE (Watch two AI play: alpha beta vs enhanced monte carlo)");
        System.out.println("6 | EVE (Watch two AI play: monte carlo vs enhanced monte carlo)");
    }

    // Prompts for a valid mode.
    private static Decisions modeType() {
        Scanner console = new Scanner(System.in);
        Decisions modeType = Decisions.fromInteger(utils.validInt(console));

        while (!Decisions.contains(modeType)) {
            System.out.println("ERROR: Enter a number that appears in the left column.");
            gameModeText();
            modeType = Decisions.fromInteger(utils.validInt(console));
        }

        return modeType;
    }

    // Introduces the order prompt.
    private static void goingFirstText() {
        System.out.println("Who should go first?");
        System.out.println("Select one of the following options: ");
        System.out.println("0 | You go first");
        System.out.println("1 | The opponent goes first");
    }

    // Prompts for a valid order.
    private static GoingFirst order() {
        Scanner console = new Scanner(System.in);
        GoingFirst order = GoingFirst.fromInteger(utils.validInt(console));

        while (!GoingFirst.contains(order)) {
            System.out.println("ERROR: Enter a number that appears in the left column.");
            goingFirstText();
            order = GoingFirst.fromInteger(utils.validInt(console));
        }

        return order;
    }

    // Returns whether the player wants to play again.
    private static boolean runAgain() {
        System.out.println("Do you want to run the program again?");
        System.out.println("not 1 | Exit Program");
        System.out.println("1 | Run Program Again");

        return utils.validInt(new Scanner(System.in)) == RUN;
    }
}
