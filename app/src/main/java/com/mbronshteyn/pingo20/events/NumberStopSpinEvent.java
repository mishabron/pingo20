package com.mbronshteyn.pingo20.events;

public class NumberStopSpinEvent {

    int pingoNumber;

    public NumberStopSpinEvent(int pingoNumber) {
        this.pingoNumber = pingoNumber;
    }

    public int getPingoNumber() {
        return pingoNumber;
    }

    public void setPingoNumber(int pingoNumber) {
        this.pingoNumber = pingoNumber;
    }
}
