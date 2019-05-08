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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mbronshteyn.pingo20.R;
import com.mbronshteyn.pingo20.activity.fragment.PingoProgressBar;
import com.mbronshteyn.pingo20.activity.fragment.PingoWindow;
import com.mbronshteyn.pingo20.events.PingoEvent;
import com.mbronshteyn.pingo20.model.Game;
import com.mbronshteyn.pingo20.types.PingoState;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class GameActivity extends PingoActivity {

    private PingoProgressBar progressBar;
    private PingoWindow pingo1;
    private PingoWindow pingo2;
    private PingoWindow pingo3;
    private PingoWindow pingo4;
    private Button hitButtonGo;
    private ImageView buttonCounter;
    private AnimatorSet mSetLeftIn;
    private AnimatorSet mSetRightOut;
    private int counter;
    List<Integer> closedPingos;
    private boolean flippedToGo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        scaleUi();

        counter = 4;
        flippedToGo = false;
        
        progressBar = (PingoProgressBar) getSupportFragmentManager().findFragmentById(R.id.gameFragmentProgressBar);

        pingo1 = (PingoWindow) getSupportFragmentManager().findFragmentById(R.id.pingo1);
        pingo2 = (PingoWindow) getSupportFragmentManager().findFragmentById(R.id.pingo2);
        pingo3 = (PingoWindow) getSupportFragmentManager().findFragmentById(R.id.pingo3);
        pingo4 = (PingoWindow) getSupportFragmentManager().findFragmentById(R.id.pingo4);

        mSetRightOut = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.anim.out_animation);
        mSetLeftIn = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.anim.in_animation);
        
        buttonCounter = (ImageView) findViewById(R.id.hitCounter);

        hitButtonGo = (Button) findViewById(R.id.actionButtonGo);
        hitButtonGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(R.raw.button);
            }
        });
        hitButtonGo.setEnabled(false);

        int distance = 8000;
        float scale = getResources().getDisplayMetrics().density * distance;
        buttonCounter.setCameraDistance(scale);
        hitButtonGo.setCameraDistance(scale);

        TextView cardNumber = (TextView) findViewById(R.id.cardNumber);
        String cardId = Game.getInstancce().getCardNumber();
        cardNumber.setText(cardNumber.getText()+ cardId.substring(0,4)+" "+cardId.substring(4,8)+" "+cardId.substring(8,12));

        closedPingos = new ArrayList<>();
        closedPingos.add(1);
        closedPingos.add(2);
        closedPingos.add(3);
        closedPingos.add(4);

        //pingo 1
        Bundle pingoBundle1 = new Bundle();
        pingoBundle1.putInt("spinDelay",100);
        pingoBundle1.putBoolean("hasFibger",true);
        pingoBundle1.putSerializable("pingoState", PingoState.ACTIVE);
        pingoBundle1.putIntegerArrayList("playedNumbers",new ArrayList<>(Arrays.asList()));

        //pingo 2
        Bundle pingoBundle2 = new Bundle();
        pingoBundle2.putInt("spinDelay",500);
        pingoBundle2.putBoolean("hasFibger",false);
        pingoBundle2.putSerializable("pingoState", PingoState.ACTIVE);
        pingoBundle2.putIntegerArrayList("playedNumbers",new ArrayList<>(Arrays.asList()));

        //pingo 3
        Bundle pingoBundle3 = new Bundle();
        pingoBundle3.putInt("spinDelay",900);
        pingoBundle3.putBoolean("hasFibger",false);
        pingoBundle3.putSerializable("pingoState", PingoState.ACTIVE);
        pingoBundle3.putIntegerArrayList("playedNumbers",new ArrayList<>(Arrays.asList()));

        //pingo 4
        Bundle pingoBundle4 = new Bundle();
        pingoBundle4.putInt("spinDelay",1300);
        pingoBundle4.putBoolean("hasFibger",false);
        pingoBundle4.putSerializable("pingoState", PingoState.ACTIVE);
        pingoBundle4.putIntegerArrayList("playedNumbers",new ArrayList<>(Arrays.asList()));


        pingo1.initPingo(pingoBundle1);
        pingo2.initPingo(pingoBundle2);
        pingo3.initPingo(pingoBundle3);
        pingo4.initPingo(pingoBundle4);
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
    public void onPingoEventMessage(PingoEvent event) {
        int pingo = event.getPingoNumber();
        int numberSelect = event.getCurrentNumber();

        removeNumber(closedPingos,pingo);
        if(closedPingos.size() ==0 && !flippedToGo){
            flippToGo(counter);
        }
    }

    public void flippToGo(int counter) {

        mSetRightOut.setTarget(buttonCounter);
        mSetLeftIn.setTarget(hitButtonGo);
        mSetLeftIn.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                hitButtonGo.setEnabled(true);
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

        flippedToGo = true;
    }

    private void removeNumber(List<Integer> closedPingos, int tagNumberToRemove){
        Iterator<Integer> numbersIter =closedPingos.iterator();
        while (numbersIter.hasNext()){
            if(numbersIter.next().equals(tagNumberToRemove)){
                numbersIter.remove();              // it will remove element from collection
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
        BitmapFactory.decodeResource(getResources(), R.drawable.game_background, options);
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
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.coordinatorLayoutGame);
        ImageView iView = (ImageView) findViewById(R.id.gameBacgroundimageView);
        ConstraintSet set = new ConstraintSet();
        set.clone(layout);
        set.constrainHeight(iView.getId(), newBmapHeight);
        set.constrainWidth(iView.getId(), newBmapWidth);
        set.centerVertically(R.id.gameBacgroundimageView, 0);
        set.centerHorizontally(R.id.gameBacgroundimageView, 0);
        set.applyTo(layout);

        //scale progress bar
        FrameLayout progressBar = (FrameLayout) findViewById(R.id.gameFragmentProgressBar);
        ViewGroup.LayoutParams progressParams = progressBar.getLayoutParams();
        progressParams.height = (int)(newBmapHeight*0.059F);
        progressParams.width = (int)(newBmapWidth*0.1397F);

        //sacele top banner
        ImageView topBanner = (ImageView) findViewById(R.id.banner);
        ViewGroup.LayoutParams bannerParams = topBanner.getLayoutParams();
        bannerParams.width =(int)(newBmapWidth*0.7890F);
        bannerParams.height =(int)(newBmapHeight*0.06296F);

        //scale action18  button
        ImageView actionButton18 = (ImageView) findViewById(R.id.hitCounter);
        int buttonSize18 = (int) (newBmapHeight * 0.2406F);
        ViewGroup.LayoutParams buttonParams18 = actionButton18.getLayoutParams();
        buttonParams18.height = buttonSize18;
        buttonParams18.width = buttonSize18;

        //scale actionGo  button
        Button actionButtonGo = (Button) findViewById(R.id.actionButtonGo);
        int buttonSizeGo = (int) (newBmapHeight * 0.2406F);
        ViewGroup.LayoutParams buttonParamsGo = actionButtonGo.getLayoutParams();
        buttonParamsGo.height = buttonSizeGo;
        buttonParamsGo.width = buttonSizeGo;

        //scale pingo windows
        ConstraintLayout pingo1 = (ConstraintLayout) findViewById(R.id.pingo1);
        ViewGroup.LayoutParams pingoParams = pingo1.getLayoutParams();
        pingoParams.height = (int)(newBmapHeight*0.3203F);
        pingoParams.width = (int)(newBmapHeight*0.3203F);

        ConstraintLayout pingo2 = (ConstraintLayout) findViewById(R.id.pingo2);
        pingoParams = pingo2.getLayoutParams();
        pingoParams.height = (int)(newBmapHeight*0.3203F);
        pingoParams.width = (int)(newBmapHeight*0.3203F);

        ConstraintLayout pingo3 = (ConstraintLayout) findViewById(R.id.pingo3);
        pingoParams = pingo3.getLayoutParams();
        pingoParams.height = (int)(newBmapHeight*0.3203F);
        pingoParams.width = (int)(newBmapHeight*0.3203F);

        ConstraintLayout pingo4 = (ConstraintLayout) findViewById(R.id.pingo4);
        pingoParams = pingo4.getLayoutParams();
        pingoParams.height = (int)(newBmapHeight*0.3203F);
        pingoParams.width = (int)(newBmapHeight*0.3203F);

        //scale header
        ImageView header = (ImageView) findViewById(R.id.header);
        ViewGroup.LayoutParams headerParams = header.getLayoutParams();
        headerParams.width =(int)(newBmapWidth*0.4066F);
        headerParams.height =(int)(newBmapHeight*0.2939F);

    }
}
