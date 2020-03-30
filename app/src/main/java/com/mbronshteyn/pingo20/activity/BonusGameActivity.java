package com.mbronshteyn.pingo20.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.mbronshteyn.pingo20.R;

import java.util.HashMap;

public class BonusGameActivity extends PingoActivity {

    private HashMap<Object, Integer> buttonMap;
    private int attemptCounter =5;
    private AnimatorSet mSetRightOut;
    private AnimatorSet mSetLeftIn;
    private Button buttonCounter1;
    private Button buttonCounter2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bonus_game);

        //setup ui
        ImageView backgroundView = (ImageView) findViewById(R.id.gameBacgroundimageView);
        Glide.with(this).load(R.drawable.bonuspin_background).into(backgroundView);
        backgroundView = (ImageView) findViewById(R.id.bubbleBackground);
        Glide.with(this).load(R.drawable.bubble_background).into(backgroundView);
        backgroundView = (ImageView) findViewById(R.id.logoBackground);
        Glide.with(this).load(R.drawable.bonus_logo_background).into(backgroundView);
        backgroundView = (ImageView) findViewById(R.id.playTextBackground);
        Glide.with(this).load(R.drawable.play_text_background).into(backgroundView);
        scaleUi();

        Button fingerButton = (Button) findViewById(R.id.bonusButtonGo);
        fingerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transitionToPlay();
                fingerButton.setEnabled(false);
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

    }

    private void transitionToPlay() {
        ImageView backgroundView = (ImageView) findViewById(R.id.gameBacgroundimageView);
        Glide.with(this).load(R.drawable.bonuspin_background_play).into(backgroundView);
        ImageView playText = (ImageView) findViewById(R.id.playTextBackground);
        playText.setVisibility(View.INVISIBLE);

        buttonCounter1.setVisibility(View.VISIBLE);
        buttonCounter2.setVisibility(View.VISIBLE);

        buttonCounter1.setBackground(getResources().getDrawable(buttonMap.get(attemptCounter),this.getTheme()));

        buttonCounter1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(R.raw.button);
                if(attemptCounter >0) {
                    buttonCounter1.setEnabled(false);
                    buttonCounter2.setEnabled(true);
                    flippToCounter(attemptCounter--,buttonCounter1,buttonCounter2);
                }
            }
        });

        buttonCounter2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(R.raw.button);
                if(attemptCounter >0) {
                    buttonCounter2.setEnabled(false);
                    buttonCounter1.setEnabled(true);
                    flippToCounter(attemptCounter--,buttonCounter2,buttonCounter1);
                }
            }
        });
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
        ImageView iView = (ImageView) findViewById(R.id.gameBacgroundimageView);
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
        int buttonSizeGo = (int) (newBmapHeight * 0.2406F);
        ViewGroup.LayoutParams buttonParamsCounter1 = counterButton1.getLayoutParams();
        buttonParamsCounter1.height = buttonSizeGo;
        buttonParamsCounter1.width = buttonSizeGo;

        //scale counter  button2
        Button counterButton2 = (Button) findViewById(R.id.counterButton2);
        ViewGroup.LayoutParams buttonParamsCounter2 = counterButton2.getLayoutParams();
        buttonParamsCounter2.height = buttonSizeGo;
        buttonParamsCounter2.width = buttonSizeGo;

    }

}
