package com.mbronshteyn.pingo20.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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
import com.mbronshteyn.pingo20.events.NumberSpinEndEvent;
import com.mbronshteyn.pingo20.events.NumberSpinEvent;
import com.mbronshteyn.pingo20.events.PingoEvent;
import com.mbronshteyn.pingo20.events.ScrollEnd;
import com.mbronshteyn.pingo20.events.ScrollStart;
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
import retrofit2.converter.scalars.ScalarsConverterFactory;

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
    List<Integer> closedPingos  = new ArrayList<>();;
    List<Integer> playPingos;
    private boolean flippedToGo;
    private Iterator<Integer> pingoIterator;
    private HashMap<Integer, Integer> buttonMap;
    private int newBmapHeight;
    private int newBmapWidth;
    private TextView balance;
    private GameActivity context;
    private boolean spinning;
    private ImageView whiteHeader;
    private ImageView whiteTopBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        ImageView freeGame = (ImageView) findViewById(R.id.free_game);

        if(card.isFreeGame()){
            Game.attemptCounter = 3 - card.getNumberOfHits();
        }
        else {
            Game.attemptCounter = 4 - card.getNumberOfHits();
            freeGame.setVisibility(View.INVISIBLE);
        }

        context = this;

        ImageView iView = (ImageView) findViewById(R.id.gameBacgroundimageView);
        Glide.with(this).load(R.drawable.game_background).into(iView);
        ImageView header = (ImageView) findViewById(R.id.header);
        Glide.with(this).load(R.drawable.header).into(header);
        whiteHeader = (ImageView) findViewById(R.id.whiteheader);
        Glide.with(this).load(R.drawable.header_white).into(whiteHeader);
        whiteHeader.setVisibility(View.INVISIBLE);
        ImageView topBanner = (ImageView) findViewById(R.id.banner);
        Glide.with(this).load(R.drawable.banner_animation).into(topBanner);
        whiteTopBanner = (ImageView) findViewById(R.id.whitebanner);
        Glide.with(this).load(R.drawable.slogan3d).into(whiteTopBanner);
        whiteTopBanner.setVisibility(View.INVISIBLE);

        //balance
        Typeface fontBalance = Typeface.createFromAsset(this.getAssets(), "fonts/showg.ttf");
        balance = (TextView) findViewById(R.id.balance);
        balance.setTypeface(fontBalance,Typeface.BOLD_ITALIC);
        balance.setText(getCardReward());

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

        if(isWinningCard()){
            buttonCounter.setVisibility(View.INVISIBLE);
            hitButtonGo.setVisibility(View.INVISIBLE);
        }

        TextView cardNumber = (TextView) findViewById(R.id.cardNumber);
        String cardId = Game.getInstancce().getCardNumber();
        cardNumber.setText(cardNumber.getText()+ cardId.substring(0,4)+" "+cardId.substring(4,8)+" "+cardId.substring(8,12));

        ImageView nonTouchShield = (ImageView) findViewById(R.id.shield);
        nonTouchShield.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //do nothing
            }
        });

        scaleUi();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        new Handler().postDelayed(()->{initState(true);},100);
    }

    private void initState(boolean withWin) {

        playPingos = loadPingosInPlay(true);
        List<Integer> winPingos = loadPingosInPlay(false);
        initPingos(playPingos,Game.attemptCounter != 0);
        if(withWin) {
            initPingos(winPingos, false);
        }
        for(Integer playPingo: playPingos){
            closedPingos.add(playPingo);
        }
    }

    private void initPingos(List<Integer> playPingos, boolean canHaveFinger) {
        int i = 0;
        for(Integer pingo: playPingos){

            Bundle pingoBundle = new Bundle();
            Integer guessedNUmber = loadNumberGuessed(pingo);
            pingoBundle.putInt("spinDelay",100 + i*300);
            pingoBundle.putBoolean("hasFinger", i == 0 && canHaveFinger);
            pingoBundle.putSerializable("pingoState", PingoState.ACTIVE);
            pingoBundle.putIntegerArrayList("playedNumbers",loadNumbersPlayed(pingo));
            pingoBundle.putSerializable("guessedNumber",loadNumberGuessed(pingo));
            i++;

            switch(pingo){
                case 1:
                    pingo1.initPingo(pingoBundle);
                    checkEndGame(pingo1);
                    break;
                case 2:
                    pingo2.initPingo(pingoBundle);
                    checkEndGame(pingo2);
                    break;
                case 3:
                    pingo3.initPingo(pingoBundle);
                    checkEndGame(pingo3);
                    break;
                case 4:
                    pingo4.initPingo(pingoBundle);
                    checkEndGame(pingo4);
                    break;
            }
        }
    }

    private void checkEndGame(PingoWindow pingo) {
        if(Game.attemptCounter == 0){
            pingo.disableWindow();
        }
    }

    private List<Integer> loadPingosInPlay(boolean pickNonWin) {

        List<Integer> pingosInPlay = new ArrayList<>();

        for(int i=1; i<=4 ; i++){
            if(loadNumberGuessed(i) == null && pickNonWin){
                pingosInPlay.add(i);
            }
            else if(loadNumberGuessed(i) != null && !pickNonWin){
                pingosInPlay.add(i);
            }
        }
        return pingosInPlay;
    }

    private void doPinCheck() {

        TextView pinInPlay = (TextView) findViewById(R.id.pinInPlay);
        pinInPlay.setText(getString(R.string.pin_in_play)+ pingo1.getCurrentPingo().toString()+
                pingo2.getCurrentPingo().toString()+
                pingo3.getCurrentPingo().toString()+
                pingo4.getCurrentPingo().toString());

        ImageView glow1 = (ImageView) findViewById(R.id.pingo1_glow);
        if(!pingo1.isGuessedNumber()) {
            Glide.with(this).load(R.drawable.blueglow).into(glow1);
        }
        glow1.setVisibility(View.VISIBLE);
        ImageView glow2 = (ImageView) findViewById(R.id.pingo2_glow);
        if(!pingo2.isGuessedNumber()) {
            Glide.with(this).load(R.drawable.blueglow).into(glow2);
        }
        glow2.setVisibility(View.VISIBLE);
        ImageView glow3 = (ImageView) findViewById(R.id.pingo3_glow);
        if(!pingo3.isGuessedNumber()) {
            Glide.with(this).load(R.drawable.blueglow).into(glow3);
        }
        glow3.setVisibility(View.VISIBLE);
        ImageView glow4 = (ImageView) findViewById(R.id.pingo4_glow);
        if(!pingo4.isGuessedNumber()) {
            Glide.with(this).load(R.drawable.blueglow).into(glow4);
        }
        glow4.setVisibility(View.VISIBLE);

        ImageView shield = (ImageView) findViewById(R.id.shield_full);
        shield.setVisibility(View.VISIBLE);
        ImageView nonTouchShield = (ImageView) findViewById(R.id.shield);
        nonTouchShield.setVisibility(View.VISIBLE);
        balance.setTextColor(Color.WHITE);
        whiteHeader.setVisibility(View.VISIBLE);
        whiteTopBanner.setVisibility(View.VISIBLE);

        progressBar.startProgress();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final PingoRemoteService service = retrofit.create(PingoRemoteService.class);

        String card = Game.getInstancce().getCardNumber();
        card = card.replaceAll("[^\\d]", "");
        CardHitDto cardHitDto = new CardHitDto();
        cardHitDto.setCardNumber(Long.parseLong(card));
        cardHitDto.setDeviceId(Game.devicedId);
        cardHitDto.setGame(Game.getGAMEID());
        cardHitDto.setBonusHit(false);

        cardHitDto.setHit1(pingo1.getCurrentPingo());
        cardHitDto.setHit2(pingo2.getCurrentPingo());
        cardHitDto.setHit3(pingo3.getCurrentPingo());
        cardHitDto.setHit4(pingo4.getCurrentPingo());

        Call<CardDto> call = service.hitCard(cardHitDto);
        call.enqueue(new Callback<CardDto>() {
            @Override
            public void onResponse(Call<CardDto> call, Response<CardDto> response) {
                processHitResponse(response);
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

    private void processHitResponse(Response<CardDto> response) {

        Headers headers = response.headers();
        String message = headers.get("message");

        if(StringUtils.isEmpty(headers.get("errorCode"))) {
            card = response.body();
            pingoIterator = playPingos.iterator();
            Integer activeWindow = pingoIterator.next();
            EventBus.getDefault().post(new NumberSpinEvent(activeWindow, loadNumberGuessed(activeWindow), getPingoWindow(activeWindow)));
            Game.attemptCounter--;
            flippToCounter(Game.attemptCounter);
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
    public void onScrollStart(ScrollStart event){
        if(!spinning){
            playSound(R.raw.wheel_spinning);
            spinning = true;
        }
    }

    @Subscribe
    public void onScrollEnd(ScrollEnd event){
        playSound(R.raw.wheel_stop);
        spinning = false;
    }

    @Subscribe
    public void onPingoEventMessage(PingoEvent event) {

        int pingo = event.getPingoNumber();
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
    public void spinEnd(NumberSpinEndEvent event){

        ImageView glow = null;
        ImageView rays = null;
        switch(event.getPingoNumber()){
            case 1:
                glow = (ImageView) findViewById(R.id.pingo1_glow);
                rays = (ImageView) findViewById(R.id.pingo1_rays);
                break;
            case 2:
                glow = (ImageView) findViewById(R.id.pingo2_glow);
                rays = (ImageView) findViewById(R.id.pingo2_rays);
                break;
            case 3:
                glow = (ImageView) findViewById(R.id.pingo3_glow);
                rays = (ImageView) findViewById(R.id.pingo3_rays);
                break;
            case 4:
                glow = (ImageView) findViewById(R.id.pingo4_glow);
                rays = (ImageView) findViewById(R.id.pingo4_rays);
                break;                
        }
        
        progressBar.stopProgress();
        if(event.isGuessed()){
            Glide.with(this).load(R.drawable.greenglow).into(glow);

            rays.setVisibility(View.VISIBLE);
            Animation raysAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rays_animation);
            rays.startAnimation(raysAnim);

            progressBar.startSaccess();
            new Handler().postDelayed(()->{progressBar.stopSuccess();},3000);
        }
        else{
            Glide.with(this).load(R.drawable.orangeglow).into(glow);
            progressBar.startFailure();
            new Handler().postDelayed(()->{progressBar.stopFailure();},3000);
        }

        new Handler().postDelayed(()-> {
            if (pingoIterator.hasNext()) {
                //progressBar.startProgress();
                Integer activeWindow = pingoIterator.next();
                EventBus.getDefault().post(new NumberSpinEvent(activeWindow, loadNumberGuessed(activeWindow), getPingoWindow(activeWindow)));
                playSound(R.raw.button);
            } else {
                //check free game
                if (Game.attemptCounter == 0 && isWinningCard()) {
                    //process free game
                    processFreeGame();
                }
                else{
                    //turn off glows
                    ImageView glow1 = (ImageView) findViewById(R.id.pingo1_glow);
                    glow1.setVisibility(View.INVISIBLE);
                    ImageView glow2 = (ImageView) findViewById(R.id.pingo2_glow);
                    glow2.setVisibility(View.INVISIBLE);
                    ImageView glow3 = (ImageView) findViewById(R.id.pingo3_glow);
                    glow3.setVisibility(View.INVISIBLE);
                    ImageView glow4 = (ImageView) findViewById(R.id.pingo4_glow);
                    glow4.setVisibility(View.INVISIBLE);
                    whiteHeader.setVisibility(View.INVISIBLE);
                    whiteTopBanner.setVisibility(View.INVISIBLE);

                    //remove sheilds
                    ImageView shield = (ImageView) findViewById(R.id.shield_full);
                    shield.setVisibility(View.INVISIBLE);
                    ImageView nonTouchShield = (ImageView) findViewById(R.id.shield);
                    nonTouchShield.setVisibility(View.INVISIBLE);
                    balance.setTextColor(Color.BLACK);
                    balance.setText(getCardReward());
                    initState(false);
                    //check end of game
                    if (Game.attemptCounter == 0) {
                        //show winning pin
                        flippToGo();
                        hitButtonGo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                playSound(R.raw.button);
                                doWinPinCheck();
                                hitButtonGo.setEnabled(false);
                            }
                        });
                    }
                }
            }
        },3100);
    }

    private void processFreeGame() {
        Intent intent = new Intent(getApplicationContext(), FreeGameActivity.class);
        Activity activity = (Activity) context;
        activity.finish();
        startActivity(intent);
    }

    private void doWinPinCheck() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        final PingoRemoteService service = retrofit.create(PingoRemoteService.class);

        Call<String> call = service.getWinningPin(Game.GAMEID,Long.parseLong(Game.cardNumber),Game.devicedId);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                processWinPinResponse(response);
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                playSound(R.raw.error_short);
                progressBar.stopProgress();
                Animation zoomIntAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_out);
                rightSmallBaloon.startAnimation(zoomIntAnimation);
                rightSmallBaloon.setImageResource(R.drawable.try_again_baloon);
                popBaloon(rightSmallBaloon,4000);
            }
        });
    }

    private void processWinPinResponse(Response<String> response) {
        Headers headers = response.headers();
        String message = headers.get("message");

        if(StringUtils.isEmpty(headers.get("errorCode"))) {
            String winPin = response.body();
            flippToCounter(Game.attemptCounter);
            pingo1.showWinPin(Integer.parseInt(winPin.substring(0,1)));
            pingo2.showWinPin(Integer.parseInt(winPin.substring(1,2)));
            pingo3.showWinPin(Integer.parseInt(winPin.substring(2,3)));
            pingo4.showWinPin(Integer.parseInt(winPin.substring(3,4)));
            new Handler().postDelayed(()->{
                Intent intent = new Intent(getApplicationContext(), EndOfGameActivity.class);
                startActivity(intent);
                Activity activity = (Activity) context;
                activity.finish();
            },4000);
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

    private String getCardReward(){

        String reward = "";

        if(card.getBalance() != 0) {
            reward = getString(R.string.card_balance) + " $" +(int) card.getBalance()+ " ";
        }
        else if(Game.attemptCounter == 0){
            reward = "GAME OVER ";
        }
        else{
            reward = "WIN FREE GAME ";
        }

        return reward;
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

        //sacele top white banner
        ImageView whiteTopBanner = (ImageView) findViewById(R.id.whitebanner);
        ViewGroup.LayoutParams whiteTopBannerarams = whiteTopBanner.getLayoutParams();
        whiteTopBannerarams.width =(int)(newBmapWidth*0.7890F);
        whiteTopBannerarams.height =(int)(newBmapHeight*0.06296F);

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
        float pingoSize = 0.3403F;
        ConstraintLayout pingo1 = (ConstraintLayout) findViewById(R.id.pingo1);
        ViewGroup.LayoutParams pingoParams = pingo1.getLayoutParams();
        pingoParams.height = (int)(newBmapHeight*pingoSize);
        pingoParams.width = (int)(newBmapHeight*pingoSize);

        ConstraintLayout pingo2 = (ConstraintLayout) findViewById(R.id.pingo2);
        pingoParams = pingo2.getLayoutParams();
        pingoParams.height = (int)(newBmapHeight*pingoSize);
        pingoParams.width = (int)(newBmapHeight*pingoSize);

        ConstraintLayout pingo3 = (ConstraintLayout) findViewById(R.id.pingo3);
        pingoParams = pingo3.getLayoutParams();
        pingoParams.height = (int)(newBmapHeight*pingoSize);
        pingoParams.width = (int)(newBmapHeight*pingoSize);

        ConstraintLayout pingo4 = (ConstraintLayout) findViewById(R.id.pingo4);
        pingoParams = pingo4.getLayoutParams();
        pingoParams.height = (int)(newBmapHeight*pingoSize);
        pingoParams.width = (int)(newBmapHeight*pingoSize);

        //scale header
        ImageView header = (ImageView) findViewById(R.id.header);
        ViewGroup.LayoutParams headerParams = header.getLayoutParams();
        headerParams.width =(int)(newBmapWidth*0.4225F);
        //headerParams.height =(int)(newBmapHeight*0.2871F);

        //scale white header
        ImageView whiteHeader = (ImageView) findViewById(R.id.whiteheader);
        ViewGroup.LayoutParams whiteHeaderParams = whiteHeader.getLayoutParams();
        whiteHeaderParams.width =(int)(newBmapWidth*0.4225F);
        //headerParams.height =(int)(newBmapHeight*0.2871F);

        //scale shild
        ImageView shield = (ImageView) findViewById(R.id.shield_full);
        ViewGroup.LayoutParams shieldParams = shield.getLayoutParams();
        shieldParams.width = newBmapWidth;
        shieldParams.height = newBmapHeight;

        //scale nonTouchShield
        ImageView nonTouchShield = (ImageView) findViewById(R.id.shield);
        ViewGroup.LayoutParams nonTouchShieldParams = nonTouchShield.getLayoutParams();
        nonTouchShieldParams.width =(int)(newBmapWidth*0.8472F);
        nonTouchShieldParams.height =(int)(newBmapHeight*0.5923F);

        //scale glows
        ImageView glow1 = (ImageView) findViewById(R.id.pingo1_glow);
        ViewGroup.LayoutParams glow1Params = glow1.getLayoutParams();
        glow1Params.width = (int)(newBmapHeight*pingoSize);

        ImageView glow2 = (ImageView) findViewById(R.id.pingo2_glow);
        ViewGroup.LayoutParams glow2Params = glow2.getLayoutParams();
        glow2Params.width = (int)(newBmapHeight*pingoSize);

        ImageView glow3 = (ImageView) findViewById(R.id.pingo3_glow);
        ViewGroup.LayoutParams glow3Params = glow3.getLayoutParams();
        glow3Params.width = (int)(newBmapHeight*pingoSize);

        ImageView glow4 = (ImageView) findViewById(R.id.pingo4_glow);
        ViewGroup.LayoutParams glow4Params = glow4.getLayoutParams();
        glow4Params.width = (int)(newBmapHeight*pingoSize);

        //scale green rays
        ImageView ray1 = (ImageView) findViewById(R.id.pingo1_rays);
        ViewGroup.LayoutParams ray1Params = ray1.getLayoutParams();
        ray1Params.height = (int)(newBmapHeight*pingoSize);
        ray1Params.width = (int)(newBmapHeight*pingoSize);

        ImageView ray2 = (ImageView) findViewById(R.id.pingo2_rays);
        ViewGroup.LayoutParams ray2Params = ray2.getLayoutParams();
        ray2Params.height = (int)(newBmapHeight*pingoSize);
        ray2Params.width = (int)(newBmapHeight*pingoSize);

        ImageView ray3 = (ImageView) findViewById(R.id.pingo3_rays);
        ViewGroup.LayoutParams ray3Params = ray3.getLayoutParams();
        ray3Params.height = (int)(newBmapHeight*pingoSize);
        ray3Params.width = (int)(newBmapHeight*pingoSize);

        ImageView ray4 = (ImageView) findViewById(R.id.pingo4_rays);
        ViewGroup.LayoutParams ray4Params = ray4.getLayoutParams();
        ray4Params.height = (int)(newBmapHeight*pingoSize);
        ray4Params.width = (int)(newBmapHeight*pingoSize);

        //sacele free game
        ImageView freeGame = (ImageView) findViewById(R.id.free_game);
        ViewGroup.LayoutParams freeGameParams = freeGame.getLayoutParams();
        freeGameParams.width =(int)(newBmapWidth*0.1641F);
        freeGameParams.height =(int)(newBmapHeight*0.06099F);
    }
}
