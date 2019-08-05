package com.mbronshteyn.pingo20.events;

public class SpinEvent {

    int pingoNumber;

    public SpinEvent(int pingoNumber) {
        this.pingoNumber = pingoNumber;
    }

    public int getPingoNumber() {
        return pingoNumber;
    }
}
