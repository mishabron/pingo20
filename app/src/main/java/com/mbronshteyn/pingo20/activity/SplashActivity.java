package com.mbronshteyn.pingo20.activity;

import android.app.Application;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.mbronshteyn.pingo20.R;

public class SplashActivity extends Activity {

    private SplashActivity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        scaleUi();

        context = this;
    }

    @Override
    protected void onResume() {

        super.onResume();

        View rootView = getWindow().getDecorView().getRootView();

        //start logo show
        Thread logoShow = new Thread(new LogoSwitcher(rootView));
        logoShow.start();
    }

    private class LogoSwitcher implements Runnable {

        View view;

        public LogoSwitcher(View view) {
            this.view = view;
        }

        @Override
        public void run() {

            try {
                Thread.sleep(5000);
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.putExtras(new Bundle());
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                Activity activity = (Activity) context;
                activity.finish();

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
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
        BitmapFactory.decodeResource(getResources(), R.drawable.login_newbacground, options);
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
        set.centerVertically(R.id.loginBacgroundimageView, 0);
        set.centerHorizontally(R.id.loginBacgroundimageView, 0);
        set.applyTo(layout);

    }
}
