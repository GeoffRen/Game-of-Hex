package com.company.utils;

// Represents the game order.
public enum GoingFirst {
    FIRST(0), SECOND(1);

    private int val;

    GoingFirst(int val) {
        this.val = val;
    }

    // Gets a GoingFirst from its corresponding integer value.
    public static GoingFirst fromInteger(int x) {
        switch(x) {
            case 0:
                return FIRST;
            case 1:
                return SECOND;
            default:
                return null;
        }
    }

    // Returns true if test is a valid GoingFirst.
    public static boolean contains(GoingFirst test) {

        for (GoingFirst goingFirst : GoingFirst.values()) {
            if (goingFirst.equals(test)) {
                return true;
            }
        }

        return false;
    }
}