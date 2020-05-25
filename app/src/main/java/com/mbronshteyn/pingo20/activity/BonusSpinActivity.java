package com.mbronshteyn.pingo20.activity;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.transition.ChangeBounds;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;

import com.mbronshteyn.gameserver.dto.game.HitDto;
import com.mbronshteyn.pingo20.R;
import com.mbronshteyn.pingo20.activity.fragment.BonusSpinWondow;
import com.mbronshteyn.pingo20.types.PingoState;

import java.util.ArrayList;
import java.util.List;

public class BonusSpinActivity extends PingoActivity{

    private ConstraintLayout root;
    private BonusSpinWondow pingo1;
    private BonusSpinWondow pingo2;
    private BonusSpinWondow pingo3;
    private BonusSpinWondow pingo4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bonus_spin_start);

        root = findViewById(R.id.bonusLayoutSpin);

        pingo1 = (BonusSpinWondow) getSupportFragmentManager().findFragmentById(R.id.bonusSpinPingo1);
        pingo2 = (BonusSpinWondow) getSupportFragmentManager().findFragmentById(R.id.bonusSpinPingo2);
        pingo3 = (BonusSpinWondow) getSupportFragmentManager().findFragmentById(R.id.bonusSpinPingo3);
        pingo4 = (BonusSpinWondow) getSupportFragmentManager().findFragmentById(R.id.bonusSpinPingo4);

        scaleUi();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        new Handler().postDelayed(() -> { transitionLayout(); }, 500);
    }

    private void transitionLayout() {

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this, R.layout.activity_bonus_spin);

        ChangeBounds transition = new ChangeBounds();
        transition.setInterpolator(new AnticipateOvershootInterpolator(1.2f));
        transition.setDuration(1000);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {

            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                new Handler().postDelayed(()->{transitionToPlay();},1000);
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

        TransitionManager.beginDelayedTransition(root, transition);
        constraintSet.applyTo(root);
    }

    private void transitionToPlay() {
        initPingos(pingo1);
        initPingos(pingo2);
        initPingos(pingo3);
        initPingos(pingo4);
    }

    private void initPingos(BonusSpinWondow pingo){

        Bundle pingoBundle = new Bundle();
        pingoBundle.putSerializable("pingoState", PingoState.ACTIVE);
        pingoBundle.putIntegerArrayList("playedNumbers",loadNumbersPlayed(pingo.getPingoNumber()));
        pingo.initPingo(pingoBundle);
    }

    private ArrayList<Integer> loadNumbersPlayed(int pingoNumber) {

        ArrayList<Integer>  numbersPlayed = new ArrayList<>();

        List<HitDto> hits = card.getHits();
        for(HitDto hit :hits){
            Integer playedNumber = null;
            switch(pingoNumber){
                case 1:
                    playedNumber = hit.getNumber_1().getNumber();
                    break;
                case 2:
                    playedNumber = hit.getNumber_2().getNumber();
                    break;
                case 3:
                    playedNumber = hit.getNumber_3().getNumber();
                    break;
                case 4:
                    playedNumber = hit.getNumber_4().getNumber();
                    break;
            }
            if(playedNumber != null) {
                numbersPlayed.add(playedNumber);
            }
        }
        return numbersPlayed;
    }

    private void scaleUi() {

        // scale the screen
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.bonus_spin_mainbackground, options);
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
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.bonusLayoutSpin);
        ImageView iView = (ImageView) findViewById(R.id.bonusSpinBacgroundimageView);
        ConstraintSet set = new ConstraintSet();
        set.clone(layout);
        set.constrainHeight(iView.getId(), newBmapHeight);
        set.constrainWidth(iView.getId(), newBmapWidth);
        set.applyTo(layout);

        //scale bubble overlay
        ImageView overlayBubble = (ImageView) findViewById(R.id.mainBonusSpinBacgroundimageView);
        ViewGroup.LayoutParams overlayBubbleParams = overlayBubble.getLayoutParams();
        overlayBubbleParams.width = newBmapWidth;
        overlayBubbleParams.height = newBmapHeight;

        //scale pingo windows
        float pingoWidth = 0.1987F;
        float pingoHeight = 0.3277F;
        ConstraintLayout pingo1 = (ConstraintLayout) findViewById(R.id.bonusSpinPingo1);
        ViewGroup.LayoutParams pingoParams1 = pingo1.getLayoutParams();
        pingoParams1.height = (int)(newBmapHeight*pingoHeight);
        pingoParams1.width = (int)(newBmapWidth*pingoWidth);

        ConstraintLayout pingo2 = (ConstraintLayout) findViewById(R.id.bonusSpinPingo2);
        ViewGroup.LayoutParams pingoParams2 = pingo2.getLayoutParams();
        pingoParams2.height = (int)(newBmapHeight*pingoHeight);
        pingoParams2.width = (int)(newBmapWidth*pingoWidth);

        ConstraintLayout pingo3 = (ConstraintLayout) findViewById(R.id.bonusSpinPingo3);
        ViewGroup.LayoutParams pingoParams3 = pingo3.getLayoutParams();
        pingoParams3.height = (int)(newBmapHeight*pingoHeight);
        pingoParams3.width = (int)(newBmapWidth*pingoWidth);

        ConstraintLayout pingo4 = (ConstraintLayout) findViewById(R.id.bonusSpinPingo4);
        ViewGroup.LayoutParams pingoParams4 = pingo4.getLayoutParams();
        pingoParams4.height = (int)(newBmapHeight*pingoHeight);
        pingoParams4.width = (int)(newBmapWidth*pingoWidth);

        //scale finger  button
        Button fingerButton = (Button) findViewById(R.id.bonusButtonGo);
        int fingerButtonHeight = (int) (newBmapHeight * 0.4059F);
        int fingerButtonWidt = (int) (newBmapWidth * 0.2500F);
        ViewGroup.LayoutParams buttonParamsFinger = fingerButton.getLayoutParams();
        buttonParamsFinger.height = fingerButtonHeight;
        buttonParamsFinger.width = fingerButtonWidt;
    }
}
