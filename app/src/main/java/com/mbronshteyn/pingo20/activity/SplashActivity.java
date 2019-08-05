package com.mbronshteyn.pingo20.activity;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
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
}
