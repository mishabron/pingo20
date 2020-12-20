package com.mbronshteyn.pingo20.events;

public class NumberRorateEvent {
    private final int window;

    public NumberRorateEvent(int window) {
        this.window = window;
    }

    public int getWindow() {
        return window;
    }
}
