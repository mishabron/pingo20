package com.mbronshteyn.pingo20.events;

public class ScrollStart {

    int pingoNumber;

    public ScrollStart(int pingoNumber) {
        this.pingoNumber = pingoNumber;
    }

    public int getPingoNumber() {
        return pingoNumber;
    }
}
