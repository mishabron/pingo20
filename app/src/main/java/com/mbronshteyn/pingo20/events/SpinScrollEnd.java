package com.mbronshteyn.pingo20.events;

public class SpinScrollEnd {

    int pingoNumber;

    public SpinScrollEnd(int pingoNumber) {
        this.pingoNumber = pingoNumber;
    }

    public int getPingoNumber() {
        return pingoNumber;
    }

    public void setPingoNumber(int pingoNumber) {
        this.pingoNumber = pingoNumber;
    }
}
