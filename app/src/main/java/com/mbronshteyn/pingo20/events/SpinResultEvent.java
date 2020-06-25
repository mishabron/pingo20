package com.mbronshteyn.pingo20.events;

import com.mbronshteyn.pingo20.model.BonusSpinResult;

public class SpinResultEvent {

    private BonusSpinResult result;

    public SpinResultEvent(BonusSpinResult result) {
        this.result = result;
    }

    public BonusSpinResult getResult() {
        return result;
    }

    public void setResult(BonusSpinResult result) {
        this.result = result;
    }
}
