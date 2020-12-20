package com.mbronshteyn.pingo20.events;

import com.mbronshteyn.gameserver.exception.ErrorCode;

public class CardAuthinticatedEvent {

    private final ErrorCode errorCode;

    public CardAuthinticatedEvent(ErrorCode errorCode) {
        this.errorCode =  errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
