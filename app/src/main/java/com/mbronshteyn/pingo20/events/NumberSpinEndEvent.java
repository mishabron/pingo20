package com.mbronshteyn.pingo20.events;

public class NumberSpinEndEvent {

    int pingoNumber;

    public NumberSpinEndEvent(int pingoNumber) {
        this.pingoNumber = pingoNumber;
    }

    public int getPingoNumber() {
        return pingoNumber;
    }

    public void setPingoNumber(int pingoNumber) {
        this.pingoNumber = pingoNumber;
    }
}
