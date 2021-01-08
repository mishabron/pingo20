package com.mbronshteyn.pingo20.activity;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.mbronshteyn.pingo20.R;
import com.mbronshteyn.pingo20.activity.fragment.BonusesFragment;
import com.mbronshteyn.pingo20.activity.fragment.HistoryFragment;
import com.mbronshteyn.pingo20.activity.fragment.RulesFragment;

public class MenuActivity extends PingoActivity {

    private RulesFragment rulesFragment;
    private BonusesFragment bonusesFragment;
    private HistoryFragment historyFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        rulesFragment = new RulesFragment();
        bonusesFragment = new BonusesFragment();
        historyFragment = new HistoryFragment();

        //close button
        ImageView menuButton = (ImageView) findViewById(R.id.closeButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isOKToInit = false;
                finish();
            }
        });

        Button bonusesButton = (Button) findViewById(R.id.bonusesButton);
        bonusesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.menuFragment,bonusesFragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        Button rulesButton = (Button) findViewById(R.id.rulesButton);
        rulesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.menuFragment,rulesFragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        Button historyButton = (Button) findViewById(R.id.historyButton);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.menuFragment,historyFragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });


        scaleUi();
    }

    @Override
    public void onBackPressed() {
        isOKToInit = false;
        finish();
    }

    public void scaleUi() {

        // scale the screen
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.menu_background, options);
        float bmapHeight = options.outHeight;
        float bmapWidth = options.outWidth;

        float wRatio = width / bmapWidth;
        float hRatio = height / bmapHeight;

        float ratioMultiplier = wRatio;
        // Untested conditional though I expect this might work for landscape
        // mode
        if (hRatio < wRatio) {
            ratioMultiplier = hRatio;
        }

        int newBmapWidth = (int) (bmapWidth * ratioMultiplier);
        int newBmapHeight = (int) (bmapHeight * ratioMultiplier);

        //scale background
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.coordinatorLayoutMenu);
        ImageView iView = (ImageView) findViewById(R.id.menuBacgroundimageView);
        ConstraintSet set = new ConstraintSet();
        set.clone(layout);
        set.constrainHeight(iView.getId(), newBmapHeight);
        set.constrainWidth(iView.getId(), newBmapWidth);
        set.applyTo(layout);

    }
}
