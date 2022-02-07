package com.mbronshteyn.pingo20.events;

public class FingerTap {
    int tapNumber;

    public FingerTap(int tapNumber) {
        this.tapNumber = tapNumber;
    }

    public int getTapNumber() {
        return tapNumber;
    }

    public void setTapNumber(int tapNumber) {
        this.tapNumber = tapNumber;
    }
}
