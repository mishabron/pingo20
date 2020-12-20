package com.mbronshteyn.pingo20.events;

public class WinStarsEvent {

    int offsetWinStars;
    int durationWinStars;
    int pingoNumber;

    public WinStarsEvent(int offsetWinStars, int durationWinStars, int pingoNumber) {
        this.offsetWinStars = offsetWinStars;
        this.durationWinStars = durationWinStars;
        this.pingoNumber = pingoNumber;
    }

    public int getOffsetWinStars() {
        return offsetWinStars;
    }

    public void setOffsetWinStars(int offsetWinStars) {
        this.offsetWinStars = offsetWinStars;
    }

    public int getDurationWinStars() {
        return durationWinStars;
    }

    public void setDurationWinStars(int durationWinStars) {
        this.durationWinStars = durationWinStars;
    }

    public int getPingoNumber() {
        return pingoNumber;
    }

    public void setPingoNumber(int pingoNumber) {
        this.pingoNumber = pingoNumber;
    }
}
