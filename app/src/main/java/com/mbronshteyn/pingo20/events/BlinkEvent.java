package com.mbronshteyn.pingo20.events;

public class BlinkEvent {

    private final int pingoNumber;

    public BlinkEvent(int pingoNumber) {
        this.pingoNumber = pingoNumber;
    }

    public int getPingoNumber() {
        return pingoNumber;
    }
}
