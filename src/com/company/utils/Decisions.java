package com.company.utils;

// Represents game modes.
public enum Decisions {
    PVP(0), PVE1(1), PVE2(2), PVE3(3), EVE1(4), EVE2(5), EVE3(6);

    private int val;

    Decisions(int val) {
        this.val = val;
    }

    // Gets a Decision from its corresponding integer value.
    public static Decisions fromInteger(int x) {
        switch(x) {
            case 0:
                return PVP;
            case 1:
                return PVE1;
            case 2:
                return PVE2;
            case 3:
                return PVE3;
            case 4:
                return EVE1;
            case 5:
                return EVE2;
            case 6:
                return EVE3;
            default:
                return null;
        }
    }

    // Returns true if test is a valid Decision.
    public static boolean contains(Decisions test) {

        for (Decisions decisions : Decisions.values()) {
            if (decisions.equals(test)) {
                return true;
            }
        }

        return false;
    }
}