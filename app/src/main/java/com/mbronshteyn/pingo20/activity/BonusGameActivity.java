package com.mbronshteyn.pingo20.activity;

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
import com.mbronshteyn.pingo20.model.Game;

import java.util.HashMap;

public class BonusGameActivity extends PingoActivity {

    private HashMap<Object, Object> buttonMap;

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

    }

    private void transitionToPlay() {
        ImageView backgroundView = (ImageView) findViewById(R.id.gameBacgroundimageView);
        Glide.with(this).load(R.drawable.bonuspin_background_play).into(backgroundView);
        ImageView playText = (ImageView) findViewById(R.id.playTextBackground);
        playText.setVisibility(View.INVISIBLE);
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

    }

}
