package com.mbronshteyn.pingo20.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mbronshteyn.gameserver.dto.game.Bonus;
import com.mbronshteyn.gameserver.dto.game.CardDto;
import com.mbronshteyn.gameserver.dto.game.CardHitDto;
import com.mbronshteyn.gameserver.dto.game.HitDto;
import com.mbronshteyn.gameserver.exception.ErrorCode;
import com.mbronshteyn.pingo20.R;
import com.mbronshteyn.pingo20.activity.fragment.PingoProgressBar;
import com.mbronshteyn.pingo20.activity.fragment.PingoWindow;
import com.mbronshteyn.pingo20.events.BonusPinEvent;
import com.mbronshteyn.pingo20.events.FingerTap;
import com.mbronshteyn.pingo20.events.GuessedNumberEvent;
import com.mbronshteyn.pingo20.events.InitBackgroundEvent;
import com.mbronshteyn.pingo20.events.NoGuessedNumberEvent;
import com.mbronshteyn.pingo20.events.NumberRorateEvent;
import com.mbronshteyn.pingo20.events.NumberSpinEndEvent;
import com.mbronshteyn.pingo20.events.NumberSpinEvent;
import com.mbronshteyn.pingo20.events.NumberStopSpinEvent;
import com.mbronshteyn.pingo20.events.PingoEvent;
import com.mbronshteyn.pingo20.events.ScrollEnd;
import com.mbronshteyn.pingo20.events.ScrollStart;
import com.mbronshteyn.pingo20.events.StopPlayer;
import com.mbronshteyn.pingo20.events.WinAnimation;
import com.mbronshteyn.pingo20.events.WinFlashEvent;
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
import java.util.concurrent.atomic.AtomicReference;

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
    private TextView balance;
    private GameActivity context;
    private boolean spinning;
    private ConstraintLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_start);

        root = findViewById(R.id.coordinatorLayoutGame);

        scaleUi();

        ImageView freeGame = (ImageView) findViewById(R.id.free_game);

        if(card.isFreeGame()){
            Game.attemptCounter = 3 - card.getNumberOfHits();
        }
        else {
            Game.attemptCounter = 4 - card.getNumberOfHits();
            freeGame.setVisibility(View.INVISIBLE);
        }

        context = this;

        //ImageView iView = (ImageView) findViewById(R.id.gameBacgroundimageView);
        //Glide.with(this).load(R.drawable.game_background).into(iView);;

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

        ImageView nonTouchShield = (ImageView) findViewById(R.id.nonTouch_shield);
        nonTouchShield.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //do nothing
            }
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if(Game.attemptCounter == 4 ) {
            //display splash
            ImageView overlayBlue = (ImageView) findViewById(R.id.overlay_blue);
            Glide.with(context).clear(overlayBlue);
            Glide.with(this).load(R.drawable.load_screen).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(overlayBlue);
            overlayBlue.setVisibility(View.VISIBLE);
            new Handler().postDelayed(() -> {
                overlayBlue.setVisibility(View.INVISIBLE);
            }, 4000);
            new Handler().postDelayed(() -> { transitionLayout(); }, 4100);
        }else{
            new Handler().postDelayed(() -> { transitionLayout(); }, 500);
        }

    }

    private void transitionLayout(){

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this, R.layout.activity_game);

        ChangeBounds transition = new ChangeBounds();
        transition.setInterpolator(new AnticipateOvershootInterpolator(1.2f));
        transition.setDuration(1000);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {

            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                new Handler().postDelayed(() -> {initState(true); }, 100);
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

        ImageView shield = (ImageView) findViewById(R.id.shield_full);
        shield.setVisibility(View.VISIBLE);
        ImageView nonTouchShield = (ImageView) findViewById(R.id.nonTouch_shield);
        nonTouchShield.setVisibility(View.VISIBLE);

        balance.setTextColor(Color.BLACK);
        balance.setTag(balance.getText());
        new Handler().postDelayed(()->{
                balance.setTextColor(Color.WHITE);
                balance.setText("GOOD LUCK! ");
            },1500);
        new Handler().postDelayed(()->{
                balance.setTextColor(Color.WHITE);
                balance.setText((String)balance.getTag());
            },5000);

        progressBar.startProgress();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(PingoRemoteService.baseUrl)
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
                new Handler().postDelayed(()->{ playSound(R.raw.error_short);},100);
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
            EventBus.getDefault().post(new NumberSpinEvent(activeWindow, loadNumberGuessed(activeWindow)));
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
    public void onFingerTouch(StopPlayer event){
        stopPlaySound(event.getSound());
    }

    @Subscribe
    public void onFingerTap(FingerTap event){
        new Handler().postDelayed(()->{playSound(R.raw.knocking_on_glass);},100);
    }

    @Subscribe
    public void onPingoEventMessage(PingoEvent event) {

        int pingo = event.getPingoNumber();
        removeNumber(closedPingos,pingo);

        if(closedPingos.size() ==0 && !flippedToGo){
            new Handler().postDelayed(()->{playSound(R.raw.short_button_turn);},200);
            flippToGo();
        }
    }

    public void flippToGo() {

        mSetRightOut.setTarget(buttonCounter);
        mSetLeftIn.setTarget(hitButtonGo);
        mSetLeftIn.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}
            @Override
            public void onAnimationEnd(Animator animation) {
                hitButtonGo.setEnabled(true);
            }
            @Override
            public void onAnimationCancel(Animator animation) {}
            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        mSetRightOut.start();
        mSetLeftIn.start();

        flippedToGo = true;
    }

    @Subscribe
    public void noWinNumber(NoGuessedNumberEvent event){
        playSound(R.raw.wrong_number);
        progressBar.startFailure();
        new Handler().postDelayed(()->{
                progressBar.stopFailure();
                EventBus.getDefault().post(new NumberStopSpinEvent(event.getPingoNumber()));
            },3000);
    }

    @Subscribe
    public void winNumber(GuessedNumberEvent event){

        if(pingoIterator.hasNext() || !isWinningCard()) {
            int delay = 0;
            if (Game.guessedCount == 2 && Game.attemptCounter > 0) {
                doHalfWayThere();
                delay = 3000;
            }
            new Handler().postDelayed(()-> {
                playSound(R.raw.right_number);
                //start blinking winning backgound
                EventBus.getDefault().post(new WinAnimation(event.getPingoNumber(),WinAnimation.colorType.GREEN));
            },delay);
        }
        else if(isWinningCard() && Game.attemptCounter != 0 ){
            doWinningFlash();
            EventBus.getDefault().post(new NumberStopSpinEvent(event.getPingoNumber()));
        }
    }

    private void attemptTransition(){

        int slideNo = 0;

        switch(Game.attemptCounter){
            case 3:
                slideNo = R.drawable.to2;
                break;
            case 2:
                slideNo = R.drawable.to3;
                break;
            case 1:
                slideNo = R.drawable.to4;
                break;
            case 0:
                initState(false);
                break;
        }

        if(slideNo != 0 && !card.isFreeGame()) {
            ImageView overlayBlue = (ImageView) findViewById(R.id.overlay_blue);
            Glide.with(context).clear(overlayBlue);
            Glide.with(this).load(slideNo).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(overlayBlue);
            overlayBlue.setVisibility(View.VISIBLE);

            new Handler().postDelayed(() -> {
                overlayBlue.setVisibility(View.INVISIBLE);
                initState(false);
            }, 5000);
        }
        else{
            initState(false);
        }

    }

    private void gotoToBonus() {

        //bonus pingos background
        ImageView iView = (ImageView) findViewById(R.id.gameBacgroundimageView);
        Glide.with(this).load(R.drawable.bonuspin_background).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(iView);
        EventBus.getDefault().post(new BonusPinEvent());

        new Handler().postDelayed(() -> {
            EventBus.getDefault().post(new WinAnimation(1,WinAnimation.colorType.GOLD));
            EventBus.getDefault().post(new WinAnimation(3,WinAnimation.colorType.GOLD));
        }, 1000);

        //rotate pingos
        new Handler().postDelayed(() -> {
            EventBus.getDefault().post(new WinAnimation(2,WinAnimation.colorType.GOLD));
            EventBus.getDefault().post(new WinAnimation(4,WinAnimation.colorType.GOLD));
        }, 2000);

        //move up the game interface
        new Handler().postDelayed(()->{
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(this, R.layout.activity_game_end);

            ChangeBounds transition = new ChangeBounds();
            transition.setInterpolator(new AnticipateOvershootInterpolator(1.2f));
            transition.setDuration(1000);
            TransitionManager.beginDelayedTransition(root, transition);
            constraintSet.applyTo(root);

        },5000);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(getApplicationContext(), BonusGameActivity.class);
            startActivity(intent);
            Activity activity = (Activity) context;
            activity.finish();
        }, 6000);
    }

    private void doWinningFlash(){

        ImageView overlayBlue = (ImageView) findViewById(R.id.overlay_blue);
        Glide.with(context).clear(overlayBlue);
        Glide.with(this).load(R.drawable.overlay_blue3).diskCacheStrategy( DiskCacheStrategy.NONE ).skipMemoryCache( true ).into(overlayBlue);
        overlayBlue.setVisibility(View.VISIBLE);
        ImageView pingoWinner = (ImageView) findViewById(R.id.popup_logo);
        Glide.with(this).load(R.drawable.pingo_winner_logo).diskCacheStrategy( DiskCacheStrategy.NONE ).skipMemoryCache( true ).into(pingoWinner);
        pingoWinner.setVisibility(View.VISIBLE);

        ImageView winStarts = (ImageView) findViewById(R.id.overlay_stars);
        winStarts.setImageDrawable(getResources().getDrawable(R.drawable.win_star_animation, null));
        AnimationDrawable winAnimation = (AnimationDrawable) winStarts.getDrawable();
        winAnimation.start();

        Typeface fontBalance = Typeface.createFromAsset(this.getAssets(), "fonts/showg.ttf");
        TextView winBalance = (TextView) findViewById(R.id.win_amount);
        winBalance.setText("$" +(int) card.getBalance()+" ");
        winBalance.setTypeface(fontBalance,Typeface.BOLD_ITALIC);
        winBalance.setShadowLayer(30, 30, 30, 0xFF303030);
        Animation zoomIntAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in);
        new Handler().postDelayed(()->{winBalance.startAnimation(zoomIntAnimation);},1000);

        new Handler().postDelayed(()->{
            overlayBlue.setVisibility(View.INVISIBLE);
            pingoWinner.setVisibility(View.INVISIBLE);
            Glide.with(context).clear(pingoWinner);
            winBalance.setVisibility(View.INVISIBLE);
            winBalance.clearAnimation();
            winStarts.setVisibility(View.INVISIBLE);
        },7000);

    }

    private void doHalfWayThere() {
        ImageView overlayBlue = (ImageView) findViewById(R.id.overlay_blue);
        Glide.with(this).load(R.drawable.overlay_blue7).diskCacheStrategy( DiskCacheStrategy.NONE ).skipMemoryCache( true ).into(overlayBlue);
        overlayBlue.setVisibility(View.VISIBLE);

        ImageView spiral = (ImageView) findViewById(R.id.spiral);
        Animation zoomRotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_rotate);
        spiral.startAnimation(zoomRotate);

        ImageView halfWay = (ImageView) findViewById(R.id.popup_logo);
        Glide.with(this).load(R.drawable.half_way).diskCacheStrategy( DiskCacheStrategy.NONE ).skipMemoryCache( true ).into(halfWay);
        Animation zoomHalfWay = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in_halfway);
        zoomHalfWay.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                overlayBlue.setVisibility(View.INVISIBLE);
                spiral.clearAnimation();
                halfWay.clearAnimation();
                spiral.setVisibility(View.INVISIBLE);
                Glide.with(context).clear(halfWay);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        halfWay.startAnimation(zoomHalfWay);
    }

    @Subscribe
    public void spinEnd(NumberSpinEndEvent event){

        int duration = 0;
        progressBar.stopProgress();

        //wrong nummber
        if(!event.isGuessed()){
            duration = 3000;
        }
        //right number
        else {
            //half there popup for non free game
            if(!card.isFreeGame() && Game.guessedCount == 2){
                duration = 9000;
            }
            //last guessed number when game is won / not frre game
            else if(!pingoIterator.hasNext() && isWinningCard() && Game.attemptCounter > 0){
                duration = 8000;
            }
            //last guessed number when game is won / frre game
            else if(!pingoIterator.hasNext() && isWinningCard() && Game.attemptCounter == 0){
                duration = 500;
            }
            //flash ray animation for guessed number
            else {
                duration = 6000;
            }
        }

        new Handler().postDelayed(()-> {
            if (pingoIterator.hasNext()) {
                Integer activeWindow = pingoIterator.next();
                progressBar.startProgress();
                EventBus.getDefault().post(new NumberSpinEvent(activeWindow, loadNumberGuessed(activeWindow)));
                playSound(R.raw.button);
            }
            //end of attempt
            else {
                if(isWinningCard()){
                    processWin(event.getPingoNumber());
                }
                else{
                    //remove sheilds
                    ImageView shield = (ImageView) findViewById(R.id.shield_full);
                    shield.setVisibility(View.INVISIBLE);
                    ImageView nonTouchShield = (ImageView) findViewById(R.id.nonTouch_shield);
                    nonTouchShield.setVisibility(View.INVISIBLE);
                    balance.setTextColor(Color.WHITE);
                    balance.setText(getCardReward());
                    EventBus.getDefault().post(new InitBackgroundEvent());
                    if(card.getBonusPin() != null && card.getBonusPin().equals(Bonus.BONUSPIN)){
                        gotoToBonus();
                    }
                    else {
                        attemptTransition();
                    }
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
        },duration);
    }

    private void processWin(int pingoNumber) {

        //remove sheilds
        ImageView shield = (ImageView) findViewById(R.id.shield_full);
        shield.setVisibility(View.INVISIBLE);

        //first pair
        int window1 = pingoNumber;
        int window2 = (window1 + 2) < 5 ? window1 + 2 : window1 + 2 - 4;

        //second pair
        int window3 = (window1 + 1) <= 4 ? window1 + 1 : 1;
        int window4 = (window3 + 2) < 5 ? window3 + 2 : window3 + 2 - 4;

        EventBus.getDefault().post(new WinFlashEvent(window1));
        EventBus.getDefault().post(new WinFlashEvent(window2));
        EventBus.getDefault().post(new NumberRorateEvent(window3));
        EventBus.getDefault().post(new NumberRorateEvent(window4));

        new Handler().postDelayed(() -> {
            EventBus.getDefault().post(new WinFlashEvent(window3));
            EventBus.getDefault().post(new WinFlashEvent(window4));
            EventBus.getDefault().post(new NumberRorateEvent(window1));
            EventBus.getDefault().post(new NumberRorateEvent(window2));
        }, 2000);

        AtomicReference<Intent> intent  = new AtomicReference<>(new Intent());
        new Handler().postDelayed(() -> {
            if(Game.attemptCounter == 0 && isWinningCard() && !card.isFreeGame()){
                intent.set(new Intent(getApplicationContext(), FreeGameActivity.class));
            }
            else{
                intent.set(new Intent(getApplicationContext(), WinEmailActivity.class));
            }

            startActivity(intent.get());
            Activity activity = (Activity) context;
            activity.finish();
        }, 10000);
    }


    private void doWinPinCheck() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(PingoRemoteService.baseUrl)
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

        int newBmapWidth = (int) (bmapWidth * ratioMultiplier);
        int newBmapHeight = (int) (bmapHeight * ratioMultiplier);

        //scale background
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.coordinatorLayoutGame);
        ImageView iView = (ImageView) findViewById(R.id.gameBacgroundimageView);
        ConstraintSet set = new ConstraintSet();
        set.clone(layout);
        set.constrainHeight(iView.getId(), newBmapHeight);
        set.constrainWidth(iView.getId(), newBmapWidth);
        set.applyTo(layout);

        //scale game background
        ImageView mainBackground = (ImageView) findViewById(R.id.mainGameBacgroundimageView);
        ViewGroup.LayoutParams mainBackgroundParams = mainBackground.getLayoutParams();
        mainBackgroundParams.width = newBmapWidth;
        mainBackgroundParams.height = newBmapHeight;

        //scale progress bar
        FrameLayout progressBar = (FrameLayout) findViewById(R.id.gameFragmentProgressBar);
        ViewGroup.LayoutParams progressParams = progressBar.getLayoutParams();
        progressParams.height = (int)(newBmapHeight*0.059F);
        progressParams.width = (int)(newBmapWidth*0.1397F);

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
        float pingoSize = 0.3203F;
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

        //scale shild
        ImageView shield = (ImageView) findViewById(R.id.shield_full);
        ViewGroup.LayoutParams shieldParams = shield.getLayoutParams();
        shieldParams.width = newBmapWidth;
        shieldParams.height = newBmapHeight;

        //scale nonTouchShield
        ImageView nonTouchShield = (ImageView) findViewById(R.id.nonTouch_shield);
        ViewGroup.LayoutParams nonTouchShieldParams = nonTouchShield.getLayoutParams();
        nonTouchShieldParams.width =(int)(newBmapWidth*0.8472F);
        nonTouchShieldParams.height =(int)(newBmapHeight*0.5923F);

        //scale free game
        ImageView freeGame = (ImageView) findViewById(R.id.free_game);
        ViewGroup.LayoutParams freeGameParams = freeGame.getLayoutParams();
        freeGameParams.width =(int)(newBmapWidth*0.1641F);
        freeGameParams.height =(int)(newBmapHeight*0.06099F);

        //scale spiral
        ImageView spiral = (ImageView) findViewById(R.id.spiral);
        ViewGroup.LayoutParams spiralParams = spiral.getLayoutParams();
        //spiralParams.width =(int)(newBmapWidth*0.9174F);
        spiralParams.height =(int)(newBmapHeight*0.9174F);

        //scale half way
        ImageView halfWay = (ImageView) findViewById(R.id.popup_logo);
        ViewGroup.LayoutParams halfWayParams = halfWay.getLayoutParams();
        halfWayParams.width =(int)(newBmapWidth*0.75F);
        halfWayParams.height =(int)(newBmapHeight*0.7722F);

        //scale stars overlay
        ImageView starsWin = (ImageView) findViewById(R.id.overlay_stars);
        ViewGroup.LayoutParams starsWinParams = starsWin.getLayoutParams();
        starsWinParams.width = newBmapWidth;
        starsWinParams.height = newBmapHeight;

        //scale blue overlay
        ImageView overlayBlue = (ImageView) findViewById(R.id.overlay_blue);
        ViewGroup.LayoutParams overlayBlueParams = overlayBlue.getLayoutParams();
        overlayBlueParams.width = newBmapWidth;
        overlayBlueParams.height = newBmapHeight;

    }
}
