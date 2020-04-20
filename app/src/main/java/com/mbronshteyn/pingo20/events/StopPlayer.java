package com.mbronshteyn.pingo20.events;

public class StopPlayer {

    int sound;

    public StopPlayer(int sound) {
        this.sound = sound;
    }

    public int getSound() {
        return sound;
    }

    public void setSound(int sound) {
        this.sound = sound;
    }
}
