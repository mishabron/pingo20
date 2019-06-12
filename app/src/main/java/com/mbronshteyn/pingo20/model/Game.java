package com.mbronshteyn.pingo20.model;

import android.os.Build;

public class Game {

    public static final String GAMEID = "Pingo";

    public static int attemptCounter = 4;

    public static  String devicedId = "35"
            + // we make this look like a valid IMEI
            Build.BOARD.length() % 10 + Build.BRAND.length() % 10 + Build.SUPPORTED_ABIS.length % 10 + Build.DEVICE.length() % 10
            + Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 + Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10
            + Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 + Build.TAGS.length() % 10 + Build.TYPE.length() % 10
            + Build.USER.length() % 10; // 13 digits;

    public static String cardNumber;

    private static Game game = new Game();

    private Game() {
    }

    public static Game getInstancce(){
        return game;
    }

    public static String getDevicedId() {
        return devicedId;
    }

    public static String getGAMEID() {
        return GAMEID;
    }

    public static String getCardNumber() {
        return cardNumber;
    }

}
