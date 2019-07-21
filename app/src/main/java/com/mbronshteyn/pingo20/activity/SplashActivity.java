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
    private ImageView zsLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        context = this;

        zsLogo = (ImageView) findViewById(R.id.zsLogoSplash);
        zsLogo.setVisibility(View.INVISIBLE);
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
                Thread.sleep(1000);
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        zsLogo.setVisibility(View.VISIBLE);
                        Animation zoomIntAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                        zsLogo.startAnimation(zoomIntAnimation);
                    }
                });
                Thread.sleep(3000);
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        Animation zoomIntAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
                        zsLogo.startAnimation(zoomIntAnimation);
                        zsLogo.setVisibility(View.INVISIBLE);
                    }
                });
                Thread.sleep(2500);
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        zsLogo.setImageResource(R.drawable.pcg_logo2);
                        zsLogo.setVisibility(View.VISIBLE);
                        Animation zoomIntAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                        zsLogo.startAnimation(zoomIntAnimation);
                    }
                });
                Thread.sleep(3500);
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        Animation zoomIntAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
                        zsLogo.startAnimation(zoomIntAnimation);
                        zsLogo.setVisibility(View.INVISIBLE);
                    }
                });
                Thread.sleep(2000);
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
