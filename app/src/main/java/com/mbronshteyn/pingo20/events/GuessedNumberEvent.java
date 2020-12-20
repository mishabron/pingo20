package com.mbronshteyn.pingo20.events;

public class GuessedNumberEvent {

    private final int pingoNumber;

    public GuessedNumberEvent(int pingoNumber) {
        this.pingoNumber = pingoNumber;
    }

    public int getPingoNumber() {
        return pingoNumber;
    }
}
