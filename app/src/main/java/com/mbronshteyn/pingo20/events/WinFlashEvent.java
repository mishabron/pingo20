package com.mbronshteyn.pingo20.events;

public class WinFlashEvent {

    private final int window;

    public WinFlashEvent(int window) {
        this.window = window;
    }

    public int getWindow() {
        return window;
    }
}
