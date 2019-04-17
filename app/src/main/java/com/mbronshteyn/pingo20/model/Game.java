package com.mbronshteyn.pingo20.model;

import android.os.Build;

public class Game {

    public static final String GAMEID = "Pingo";

    private int attemptCounter;

    public static  String devicedId = "35"
            + // we make this look like a valid IMEI
            Build.BOARD.length() % 10 + Build.BRAND.length() % 10 + Build.SUPPORTED_ABIS.length % 10 + Build.DEVICE.length() % 10
            + Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 + Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10
            + Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 + Build.TAGS.length() % 10 + Build.TYPE.length() % 10
            + Build.USER.length() % 10; // 13 digits;

    private String cardNumber;

    private static Game game = new Game();

    private Game() {
    }

    public static Game getInstancce(){
        return game;
    }

    public int getAttemptCounter() {
        return attemptCounter;
    }

    public void setAttemptCounter(int attemptCounter) {
        this.attemptCounter = attemptCounter;
    }

    public static String getDevicedId() {
        return devicedId;
    }

    public static String getGAMEID() {
        return GAMEID;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
}
