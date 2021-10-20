package com.mbronshteyn.pingo20.activity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.mbronshteyn.pingo20.R;
import com.mbronshteyn.pingo20.model.Game;

public class SplashActivity extends PingoActivity {

    private SplashActivity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        scaleUi();
    }

    @Override
    protected void onResume() {

        super.onResume();

        if(Game.exit){
            exitApp();
            finish();
            System.exit(0);
        }
        else {
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.putExtras(new Bundle());
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(SplashActivity.this);
                startActivity(intent, options.toBundle());
            }, 5000);
        }
    }

    public void scaleUi() {

        // scale the screen
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.splash, options);
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
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.splash);
        ImageView iView = (ImageView) findViewById(R.id.splashBacgroundimageView);
        ConstraintSet set = new ConstraintSet();
        set.clone(layout);
        set.constrainHeight(iView.getId(), newBmapHeight);
        set.constrainWidth(iView.getId(), newBmapWidth);
        set.centerVertically(R.id.loginMainBackground, 0);
        set.centerHorizontally(R.id.loginMainBackground, 0);
        set.applyTo(layout);

    }
}
