package com.mbronshteyn.pingo20.events;

public class LuckySevenEvent {

    int pingoNumber;

    public LuckySevenEvent(int pingoNumber) {
        this.pingoNumber = pingoNumber;
    }

    public int getPingoNumber() {
        return pingoNumber;
    }

    public void setPingoNumber(int pingoNumber) {
        this.pingoNumber = pingoNumber;
    }
}
