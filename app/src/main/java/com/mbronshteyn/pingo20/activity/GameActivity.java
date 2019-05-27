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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mbronshteyn.gameserver.dto.game.CardDto;
import com.mbronshteyn.gameserver.dto.game.CardHitDto;
import com.mbronshteyn.gameserver.dto.game.HitDto;
import com.mbronshteyn.gameserver.exception.ErrorCode;
import com.mbronshteyn.pingo20.R;
import com.mbronshteyn.pingo20.activity.fragment.PingoProgressBar;
import com.mbronshteyn.pingo20.activity.fragment.PingoWindow;
import com.mbronshteyn.pingo20.events.CardAuthinticatedEvent;
import com.mbronshteyn.pingo20.events.NumberSpinEndEvent;
import com.mbronshteyn.pingo20.events.NumberSpinEvent;
import com.mbronshteyn.pingo20.events.PingoEvent;
import com.mbronshteyn.pingo20.model.Game;
import com.mbronshteyn.pingo20.network.PingoRemoteService;
import com.mbronshteyn.pingo20.types.PingoState;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
    List<Integer> closedPingos;
    List<Integer> playPingos;
    private boolean flippedToGo;
    private Iterator<Integer> pingoIterator;
    private HashMap<Integer, Integer> buttonMap;
    private int newBmapHeight;
    private int newBmapWidth;
    private TextView cardNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        scaleUi();

        ImageView iView = (ImageView) findViewById(R.id.gameBacgroundimageView);
        Glide.with(this).load(R.drawable.game_background).into(iView);
        ImageView header = (ImageView) findViewById(R.id.header);
        Glide.with(this).load(R.drawable.header).into(header);
        ImageView topBanner = (ImageView) findViewById(R.id.banner);
        Glide.with(this).load(R.drawable.banner_animation).into(topBanner);

        flippedToGo = false;
        
        progressBar = (PingoProgressBar) getSupportFragmentManager().findFragmentById(R.id.gameFragmentProgressBar);

        pingo1 = (PingoWindow) getSupportFragmentManager().findFragmentById(R.id.pingo1);
        pingo2 = (PingoWindow) getSupportFragmentManager().findFragmentById(R.id.pingo2);
        pingo3 = (PingoWindow) getSupportFragmentManager().findFragmentById(R.id.pingo3);
        pingo4 = (PingoWindow) getSupportFragmentManager().findFragmentById(R.id.pingo4);

        buttonMap = new HashMap<>();
        buttonMap.put(0,R.drawable.button0);
        buttonMap.put(1,R.drawable.button1);
        buttonMap.put(2,R.drawable.button2);
        buttonMap.put(3,R.drawable.button3);
        buttonMap.put(4,R.drawable.button4);
        buttonMap.put(5,R.drawable.button5);

        mSetRightOut = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.anim.out_animation);
        mSetLeftIn = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.anim.in_animation);
        
        buttonCounter = (ImageView) findViewById(R.id.hitCounter);
        Glide.with(this).load(buttonMap.get(Game.attemptCounter)).into(buttonCounter);

        hitButtonGo = (Button) findViewById(R.id.actionButtonGo);
        hitButtonGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(R.raw.button);
                doPinCheck();
                hitButtonGo.setEnabled(false);
            }
        });
        hitButtonGo.setEnabled(false);

        int distance = 8000;
        float scale = getResources().getDisplayMetrics().density * distance;
        buttonCounter.setCameraDistance(scale);
        hitButtonGo.setCameraDistance(scale);

        cardNumber = (TextView) findViewById(R.id.cardNumber);
        String cardId = Game.getInstancce().getCardNumber();
        cardNumber.setText(cardNumber.getText()+ cardId.substring(0,4)+" "+cardId.substring(4,8)+" "+cardId.substring(8,12));

        closedPingos = new ArrayList<>();
        closedPingos.add(1);
        closedPingos.add(2);
        closedPingos.add(3);
        closedPingos.add(4);

        playPingos = new ArrayList<>();
        playPingos.add(1);
        playPingos.add(2);
        playPingos.add(3);
        playPingos.add(4);

        //pingo 1
        Bundle pingoBundle1 = new Bundle();
        pingoBundle1.putInt("spinDelay",300);
        pingoBundle1.putBoolean("hasFibger",true);
        pingoBundle1.putSerializable("pingoState", PingoState.ACTIVE);
        pingoBundle1.putIntegerArrayList("playedNumbers",loadNumbersPlayed(1));
        pingoBundle1.putBoolean("guessedNumber",loadGuessed(1));

        //pingo 2
        Bundle pingoBundle2 = new Bundle();
        pingoBundle2.putInt("spinDelay",700);
        pingoBundle2.putBoolean("hasFibger",false);
        pingoBundle2.putSerializable("pingoState", PingoState.ACTIVE);
        pingoBundle2.putIntegerArrayList("playedNumbers",loadNumbersPlayed(2));
        pingoBundle2.putBoolean("guessedNumber",loadGuessed(2));

        //pingo 3
        Bundle pingoBundle3 = new Bundle();
        pingoBundle3.putInt("spinDelay",1100);
        pingoBundle3.putBoolean("hasFibger",false);
        pingoBundle3.putSerializable("pingoState", PingoState.ACTIVE);
        pingoBundle3.putIntegerArrayList("playedNumbers",loadNumbersPlayed(3));
        pingoBundle3.putBoolean("guessedNumber",loadGuessed(3));

        //pingo 4
        Bundle pingoBundle4 = new Bundle();
        pingoBundle4.putInt("spinDelay",1500);
        pingoBundle4.putBoolean("hasFibger",false);
        pingoBundle4.putSerializable("pingoState", PingoState.ACTIVE);
        pingoBundle4.putIntegerArrayList("playedNumbers",loadNumbersPlayed(4));
        pingoBundle4.putBoolean("guessedNumber",loadGuessed(4));


        pingo1.initPingo(pingoBundle1);
        pingo2.initPingo(pingoBundle2);
        pingo3.initPingo(pingoBundle3);
        pingo4.initPingo(pingoBundle4);
    }

    private void doPinCheck() {

        progressBar.startProgress();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final PingoRemoteService service = retrofit.create(PingoRemoteService.class);

        String card = cardNumber.getText().toString();
        card = card.replaceAll("[^\\d]", "");
        CardHitDto cardHitDto = new CardHitDto();
        cardHitDto.setCardNumber(Long.parseLong(card));
        cardHitDto.setDeviceId(Game.devicedId);
        cardHitDto.setGame(Game.getGAMEID());
        cardHitDto.setBonusHit(false);
        if(!pingo1.isGuessedNumber()) {
            cardHitDto.setHit1(pingo1.getCurrentPingo());
        }
        if(!pingo2.isGuessedNumber()) {
            cardHitDto.setHit2(pingo2.getCurrentPingo());
        }
        if(!pingo3.isGuessedNumber()) {
            cardHitDto.setHit3(pingo3.getCurrentPingo());
        }
        if(!pingo4.isGuessedNumber()) {
            cardHitDto.setHit4(pingo4.getCurrentPingo());
        }

        Call<CardDto> call = service.hitCard(cardHitDto);
        call.enqueue(new Callback<CardDto>() {
            @Override
            public void onResponse(Call<CardDto> call, Response<CardDto> response) {
                processResponse(response);
            }

            @Override
            public void onFailure(Call<CardDto> call, Throwable t) {
                playSound(R.raw.error_short);
                progressBar.stopProgress();
                Animation zoomIntAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_out);
                rightSmallBaloon.startAnimation(zoomIntAnimation);
                rightSmallBaloon.setImageResource(R.drawable.try_again_baloon);
                popBaloon(rightSmallBaloon,4000);
            }
        });
    }

    private void processResponse(Response<CardDto> response) {

        Headers headers = response.headers();
        String message = headers.get("message");

        if(StringUtils.isEmpty(headers.get("errorCode"))) {
            card = response.body();
            pingoIterator = playPingos.iterator();
            Integer activeWindow = pingoIterator.next();
            EventBus.getDefault().post(new NumberSpinEvent(activeWindow, loadGuessed(activeWindow), getPingoWindow(activeWindow)));
        }else{
            playSound(R.raw.error_short);
            ErrorCode errorCode = ErrorCode.valueOf(headers.get("errorCode"));
            switch(errorCode){
                case SERVERERROR:
                    rightSmallBaloon.setImageResource(R.drawable.error_blue_right);
                    popBaloon(rightSmallBaloon,4000);
                    break;
            }
        }
    }

    private boolean loadGuessed(int pingoNumber) {

        boolean guessed = false;
        List<HitDto> hits = card.getHits();
        for(HitDto hit :hits){
            if(guessed){
                break;
            }
            switch(pingoNumber){
                case 1:
                    guessed = hit.getNumber_1().isGuessed();
                    break;
                case 2:
                    guessed = hit.getNumber_1().isGuessed();
                    break;
                case 3:
                    guessed = hit.getNumber_1().isGuessed();
                    break;
                case 4:
                    guessed = hit.getNumber_1().isGuessed();
                    break;
            }
        }
        return guessed;
    }

    private ArrayList<Integer> loadNumbersPlayed(int pingoNUmber) {

        ArrayList<Integer>  numbersPlayed = new ArrayList<>();

        List<HitDto> hits = card.getHits();
        for(HitDto hit :hits){
            Integer playedNumber = null;
            switch(pingoNUmber){
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
            flippToGo();
        }
    }

    public void flippToGo() {

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

    @Subscribe
    public void spingEnd(NumberSpinEndEvent event){
        if(pingoIterator.hasNext()) {
            Integer activeWindow = pingoIterator.next();
            EventBus.getDefault().post(new NumberSpinEvent(activeWindow, false, getPingoWindow(activeWindow)));
        }
        else{
            Game.attemptCounter--;
            closedPingos.add(1);
            closedPingos.add(2);
            closedPingos.add(3);
            closedPingos.add(4);
            flippToCounter(Game.attemptCounter);
            progressBar.stopProgress();
        }
    }

    public void flippToCounter(int counter) {

        //flip button
        Glide.with(this).load(buttonMap.get(counter)).into(buttonCounter);
        mSetRightOut.setTarget(hitButtonGo);
        mSetLeftIn.setTarget(buttonCounter);
        mSetLeftIn.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                hitButtonGo.setEnabled(false);
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

        flippedToGo = false;
    }

    private void removeNumber(List<Integer> pingos, int tagNumberToRemove){
        Iterator<Integer> numbersIter =pingos.iterator();
        while (numbersIter.hasNext()){
            if(numbersIter.next().equals(tagNumberToRemove)){
                numbersIter.remove();              // it will remove element from collection
            }
        }
    }

    private ConstraintLayout getPingoWindow(int pingoNumber){

        ConstraintLayout pingoWindow = null;

        switch(pingoNumber){
            case 1:
                pingoWindow = (ConstraintLayout) findViewById(R.id.pingo1);
                break;
            case 2:
                pingoWindow = (ConstraintLayout) findViewById(R.id.pingo2);
                break;
            case 3:
                pingoWindow = (ConstraintLayout) findViewById(R.id.pingo3);
                break;
            case 4:
                pingoWindow = (ConstraintLayout) findViewById(R.id.pingo4);
                break;
        }

        return pingoWindow;
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

        newBmapWidth = (int) (bmapWidth * ratioMultiplier);
        newBmapHeight = (int) (bmapHeight * ratioMultiplier);

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
