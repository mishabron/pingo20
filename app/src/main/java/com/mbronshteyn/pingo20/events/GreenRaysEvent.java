package com.mbronshteyn.pingo20.events;

public class GreenRaysEvent {

    private final int pingoNumber;

    public GreenRaysEvent(int pingoNumber) {
        this.pingoNumber = pingoNumber;
    }

    public int getPingoNumber() {
        return pingoNumber;
    }
}
