package com.mbronshteyn.pingo20.events;

public class WinAnimation {

    private final int pingoNumber;

    public static  enum colorType{GREEN};

    private  colorType color;

    public WinAnimation(int pingoNumber, colorType color) {
        this.pingoNumber = pingoNumber;
        this.color = color;
    }

    public int getPingoNumber() {
        return pingoNumber;
    }

    public colorType getColor() {
        return color;
    }

    public void setColor(colorType color) {
        this.color = color;
    }
}
