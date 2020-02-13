package com.mbronshteyn.pingo20.events;

public class NoGuessedNumberEvent {

    int pingoNumber;

    public NoGuessedNumberEvent(int pingoNumber) {
        this.pingoNumber = pingoNumber;
    }

    public int getPingoNumber() {
        return pingoNumber;
    }

    public void setPingoNumber(int pingoNumber) {
        this.pingoNumber = pingoNumber;
    }
}
