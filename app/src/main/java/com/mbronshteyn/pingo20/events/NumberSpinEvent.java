package com.mbronshteyn.pingo20.events;

import android.support.constraint.ConstraintLayout;

public class NumberSpinEvent {

    int pingoNumber;
    boolean numberGuesed;
    ConstraintLayout pingo;

    public NumberSpinEvent(int pingoNumber, boolean numberGuesed, ConstraintLayout pingo) {
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

    public boolean isNumberGuesed() {
        return numberGuesed;
    }

    public void setNumberGuesed(boolean numberGuesed) {
        this.numberGuesed = numberGuesed;
    }

    public ConstraintLayout getPingo() {
        return pingo;
    }

    public void setPingo(ConstraintLayout pingo) {
        this.pingo = pingo;
    }
}
