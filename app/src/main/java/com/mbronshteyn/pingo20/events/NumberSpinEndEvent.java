package com.mbronshteyn.pingo20.events;

public class NumberSpinEndEvent {

    int pingoNumber;
    boolean guessed;

    public NumberSpinEndEvent(int pingoNumber, boolean guessed) {
        this.pingoNumber = pingoNumber;
        this.guessed = guessed;
    }

    public int getPingoNumber() {
        return pingoNumber;
    }

    public void setPingoNumber(int pingoNumber) {
        this.pingoNumber = pingoNumber;
    }

    public boolean isGuessed() {
        return guessed;
    }

    public void setGuessed(boolean guessed) {
        this.guessed = guessed;
    }
}
