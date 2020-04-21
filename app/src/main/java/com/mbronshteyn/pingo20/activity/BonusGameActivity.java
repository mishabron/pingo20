package com.mbronshteyn.pingo20.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.transition.ChangeBounds;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.mbronshteyn.pingo20.R;
import com.mbronshteyn.pingo20.activity.fragment.BonusPinWondow;
import com.mbronshteyn.pingo20.activity.fragment.PingoWindow;
import com.mbronshteyn.pingo20.events.LuckySevenEvent;
import com.mbronshteyn.pingo20.events.ScrollEnd;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class BonusGameActivity extends PingoActivity {

    private HashMap<Object, Integer> buttonMap;
    private int attemptCounter =5;
    private AnimatorSet mSetRightOut;
    private AnimatorSet mSetLeftIn;
    private Button buttonCounter1;
    private Button buttonCounter2;
    private BonusPinWondow pingo1;
    private BonusPinWondow pingo2;
    private BonusPinWondow pingo3;
    private Button fingerButton;
    private boolean heads;
    private char[] luckyState =  {'0','0','0'};
    private FingerTimer fingerTimer;
    private ConstraintLayout bonusRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bonus_game_start);

        bonusRoot = findViewById(R.id.bonusLayoutGame);

        //setup ui
        ImageView backgroundView = (ImageView) findViewById(R.id.bubbleBackground);
        Glide.with(this).load(R.drawable.bubble_background).into(backgroundView);
        backgroundView = (ImageView) findViewById(R.id.logoBackground);
        Glide.with(this).load(R.drawable.bonus_logo_background).into(backgroundView);
        backgroundView = (ImageView) findViewById(R.id.playTextBackground);
        Glide.with(this).load(R.drawable.play_text_background).into(backgroundView);

        fingerButton = (Button) findViewById(R.id.bonusButtonGo);
        fingerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fingerTimer.cancel();
                transitionToPlay();
            }
        });

        buttonMap = new HashMap<>();
        buttonMap.put(0,R.drawable.button0);
        buttonMap.put(1,R.drawable.button1);
        buttonMap.put(2,R.drawable.button2);
        buttonMap.put(3,R.drawable.button3);
        buttonMap.put(4,R.drawable.button4);
        buttonMap.put(5,R.drawable.button5);

        mSetRightOut = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.anim.out_animation);
        mSetLeftIn = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.anim.in_animation);

        buttonCounter1 = (Button) findViewById(R.id.counterButton1);
        buttonCounter2 = (Button) findViewById(R.id.counterButton2);

        int distance = 8000;
        float scale = getResources().getDisplayMetrics().density * distance;
        buttonCounter1.setCameraDistance(scale);
        buttonCounter2.setCameraDistance(scale);

        pingo1 = (BonusPinWondow) getSupportFragmentManager().findFragmentById(R.id.bonusPingo1);
        pingo2 = (BonusPinWondow) getSupportFragmentManager().findFragmentById(R.id.bonusPingo2);
        pingo3 = (BonusPinWondow) getSupportFragmentManager().findFragmentById(R.id.bonusPingo3);

        heads = false;

        fingerTimer = new FingerTimer(2000,100);
        fingerTimer.start();

        scaleUi();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        new Handler().postDelayed(() -> { transitionLayout(); }, 500);
    }

    private void transitionLayout(){

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this, R.layout.activity_bonus_game);

        ChangeBounds transition = new ChangeBounds();
        transition.setInterpolator(new AnticipateOvershootInterpolator(1.2f));
        transition.setDuration(1000);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {

            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
            }

            @Override
            public void onTransitionCancel(@NonNull Transition transition) {

            }

            @Override
            public void onTransitionPause(@NonNull Transition transition) {

            }

            @Override
            public void onTransitionResume(@NonNull Transition transition) {

            }
        });

        TransitionManager.beginDelayedTransition(bonusRoot, transition);
        constraintSet.applyTo(bonusRoot);
    }
    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onScrollEnd(ScrollEnd event){
        playSound(R.raw.wheel_stop);
        if (event.getPingoNumber() ==3 ){
            stopPlaySound(R.raw.wheel_spinning);
            fingerButton.setEnabled(true);
        }
        if(attemptCounter == 0 && event.getPingoNumber() == 3){
            gotoNoWin();
        }
    }

    @Subscribe
    public void onLuckySeven(LuckySevenEvent event){
        luckyState[event.getPingoNumber() - 1] = '1';
        int state = Integer.parseInt(String.valueOf(luckyState),2);
        if(state == 4 || state == 6 || state == 7) {
            playSound(R.raw.luckyseven);
        }

        if (state == 7){
            gotoToWin();
        }
    }

    private void gotoToWin() {
        fingerButton.setEnabled(false);
    }

    private void transitionToPlay() {

        ImageView playText = (ImageView) findViewById(R.id.playTextBackground);
        Animation transAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        transAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                playText.setVisibility(View.INVISIBLE);
                playSound(R.raw.wheel_spinning);
                fingerButton.setEnabled(false);
                pingo1.initPingo();
                pingo2.initPingo();
                pingo3.initPingo();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        playText.startAnimation(transAnimation);

        buttonCounter1.setVisibility(View.VISIBLE);
        buttonCounter2.setVisibility(View.VISIBLE);
        buttonCounter1.setBackground(getResources().getDrawable(buttonMap.get(attemptCounter),this.getTheme()));

        fingerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(attemptCounter >0) {
                    spinPingos();
                    playSound(R.raw.short_button_turn);
                    attemptCounter--;
                    if(attemptCounter == 0){
                        fingerButton.setOnClickListener(null);
                    }
                    if(!heads) {
                        flippToCounter(attemptCounter, buttonCounter1, buttonCounter2);
                        heads = true;
                    }
                    else {
                        flippToCounter(attemptCounter, buttonCounter2, buttonCounter1);
                        heads = false;
                    }
                }
            }
        });
    }

    private void gotoNoWin() {

        fingerButton.setEnabled(false);

        new Handler().postDelayed(()->{
            Intent intent = new Intent(getApplicationContext(), GameActivity.class);
            startActivity(intent);
            Activity activity = (Activity) BonusGameActivity.this;
            activity.finish();
            Runtime.getRuntime().gc();
        },3000);
    }

    public void spinPingos(){
        luckyState =  new char[]{'0','0','0'};
        fingerButton.setEnabled(false);
        playSound(R.raw.wheel_spinning);
        pingo1.spinPingo();
        pingo2.spinPingo();
        pingo3.spinPingo();
    }

    public void flippToCounter(int counter, Button from, Button to) {

        //flip button
        mSetRightOut.setTarget(from);
        to.setBackground(getResources().getDrawable(buttonMap.get(attemptCounter),this.getTheme()));
        mSetLeftIn.setTarget(to);
        mSetLeftIn.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        mSetRightOut.start();
        mSetLeftIn.start();
    }

    private class FingerTimer extends CountDownTimer {
        public FingerTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }
        @Override
        public void onTick(long l) {
        }

        @Override
        public void onFinish() {
            fingerButton.setPressed(true);
            new Handler().postDelayed(()->{fingerButton.setPressed(false);},100);
            new Handler().postDelayed(()->{fingerButton.setPressed(true);},200);
            new Handler().postDelayed(()->{fingerButton.setPressed(false);},300);
            new Handler().postDelayed(()->{fingerButton.setPressed(true);},400);
            new Handler().postDelayed(()->{fingerButton.setPressed(false);},500);
            playSound(R.raw.knocking_on_glass);
        }
    }

    private void scaleUi() {

        // scale the screen
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.bonuspin_background, options);
        float bmapHeight = options.outHeight;
        float bmapWidth  = options.outWidth;

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
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.bonusLayoutGame);
        ImageView iView = (ImageView) findViewById(R.id.bonusGameBacgroundimageView);
        ConstraintSet set = new ConstraintSet();
        set.clone(layout);
        set.constrainHeight(iView.getId(), newBmapHeight);
        set.constrainWidth(iView.getId(), newBmapWidth);
        set.applyTo(layout);

        //scale bubble overlay
        ImageView overlayBubble = (ImageView) findViewById(R.id.bubbleBackground);
        ViewGroup.LayoutParams overlayBubbleParams = overlayBubble.getLayoutParams();
        overlayBubbleParams.width = newBmapWidth;
        overlayBubbleParams.height = newBmapHeight;

        //scale logo overlay
        ImageView overlayLogo = (ImageView) findViewById(R.id.logoBackground);
        ViewGroup.LayoutParams overlayLogoParams = overlayLogo.getLayoutParams();
        overlayLogoParams.width = newBmapWidth;
        overlayLogoParams.height = newBmapHeight;

        //scale play text overlay
        ImageView playText = (ImageView) findViewById(R.id.playTextBackground);
        ViewGroup.LayoutParams playTextParams = playText.getLayoutParams();
        playTextParams.width = newBmapWidth;
        playTextParams.height = newBmapHeight;

        //scale finger  button
        Button fingerButton = (Button) findViewById(R.id.bonusButtonGo);
        int fingerButtonHeight = (int) (newBmapHeight * 0.3327F);
        int fingerButtonWidt = (int) (newBmapWidth * 0.2530F);
        ViewGroup.LayoutParams buttonParamsFinger = fingerButton.getLayoutParams();
        buttonParamsFinger.height = fingerButtonHeight;
        buttonParamsFinger.width = fingerButtonWidt;

        //scale counter  button1
        Button counterButton1 = (Button) findViewById(R.id.counterButton1);
        int buttonSizeGo = (int) (newBmapHeight * 0.2006F);
        ViewGroup.LayoutParams buttonParamsCounter1 = counterButton1.getLayoutParams();
        buttonParamsCounter1.height = buttonSizeGo;
        buttonParamsCounter1.width = buttonSizeGo;

        //scale counter  button2
        Button counterButton2 = (Button) findViewById(R.id.counterButton2);
        ViewGroup.LayoutParams buttonParamsCounter2 = counterButton2.getLayoutParams();
        buttonParamsCounter2.height = buttonSizeGo;
        buttonParamsCounter2.width = buttonSizeGo;

        //scale pingo windows
        float pingoSize = 0.2803F;
        ConstraintLayout pingo1 = (ConstraintLayout) findViewById(R.id.bonusPingo1);
        ViewGroup.LayoutParams pingoParams1 = pingo1.getLayoutParams();
        pingoParams1.height = (int)(newBmapHeight*pingoSize);
        pingoParams1.width = (int)(newBmapHeight*pingoSize);

        ConstraintLayout pingo2 = (ConstraintLayout) findViewById(R.id.bonusPingo2);
        ViewGroup.LayoutParams pingoParams2 = pingo2.getLayoutParams();
        pingoParams2.height = (int)(newBmapHeight*pingoSize);
        pingoParams2.width = (int)(newBmapHeight*pingoSize);

        ConstraintLayout pingo3 = (ConstraintLayout) findViewById(R.id.bonusPingo3);
        ViewGroup.LayoutParams pingoParams3 = pingo3.getLayoutParams();
        pingoParams3.height = (int)(newBmapHeight*pingoSize);
        pingoParams3.width = (int)(newBmapHeight*pingoSize);
    }

}
