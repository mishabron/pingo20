package com.mbronshteyn.pingo20.events;

import android.support.constraint.ConstraintLayout;

import com.mbronshteyn.pingo20.types.PingoState;

public class NumberSpinEvent {

    int pingoNumber;
    Integer numberGuesed;
    ConstraintLayout pingo;
    PingoState pingoState;

    public NumberSpinEvent(int pingoNumber, Integer numberGuesed, ConstraintLayout pingo) {
        this.pingoNumber = pingoNumber;
        this.numberGuesed = numberGuesed;
        this.pingo = pingo;
    }

    public int getPingoNumber() {
        return pingoNumber;
    }

    public void setPingoNumber(int pingoNumber) {
        this.pingoNumber = pingoNumber;
    }

    public ConstraintLayout getPingo() {
        return pingo;
    }

    public void setPingo(ConstraintLayout pingo) {
        this.pingo = pingo;
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
