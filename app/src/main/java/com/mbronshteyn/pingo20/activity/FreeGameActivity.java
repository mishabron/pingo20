package com.mbronshteyn.pingo20.activity;

import android.app.Activity;
import android.os.Bundle;

import com.mbronshteyn.pingo20.R;

public class FreeGameActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_pingo_window);
    }
}
