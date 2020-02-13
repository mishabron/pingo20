package com.mbronshteyn.pingo20.events;

import android.support.constraint.ConstraintLayout;

import com.mbronshteyn.pingo20.types.PingoState;

public class NumberSpinEvent {

    int pingoNumber;
    Integer numberGuesed;
    PingoState pingoState;

    public NumberSpinEvent(int pingoNumber, Integer numberGuesed) {
        this.pingoNumber = pingoNumber;
        this.numberGuesed = numberGuesed;
    }

    public int getPingoNumber() {
        return pingoNumber;
    }

    public void setPingoNumber(int pingoNumber) {
        this.pingoNumber = pingoNumber;
    }

    public PingoState getPingoState() {
        return pingoState;
    }

    public void setPingoState(PingoState pingoState) {
        this.pingoState = pingoState;
    }

    public Integer getNumberGuesed() {
        return numberGuesed;
    }

    public void setNumberGuesed(Integer numberGuesed) {
        this.numberGuesed = numberGuesed;
    }
}
