package com.mbronshteyn.pingo20.events;

public class NumberSpinEvent {

    int pingoNumber;
    boolean numberGuesed;

    public NumberSpinEvent(int pingoNumber, boolean numberGuesed) {
        this.pingoNumber = pingoNumber;
        this.numberGuesed = numberGuesed;
    }

    public int getPingoNumber() {
        return pingoNumber;
    }

    public void setPingoNumber(int pingoNumber) {
        this.pingoNumber = pingoNumber;
    }
}
