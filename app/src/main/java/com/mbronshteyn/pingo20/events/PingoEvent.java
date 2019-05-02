package com.mbronshteyn.pingo20.events;

public class PingoEvent {

    int pingoNumber;
    int currentNumber;

    public PingoEvent() {
    }

    public PingoEvent(int pingoNumber, int currentNumber) {
        this.pingoNumber = pingoNumber;
        this.currentNumber = currentNumber;
    }

    public int getPingoNumber() {
        return pingoNumber;
    }

    public void setPingoNumber(int pingoNumber) {
        this.pingoNumber = pingoNumber;
    }

    public int getCurrentNumber() {
        return currentNumber;
    }

    public void setCurrentNumber(int currentNumber) {
        this.currentNumber = currentNumber;
    }
}
