package com.mbronshteyn.pingo20.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.transition.ChangeBounds;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v7.content.res.AppCompatResources;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
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
import com.mbronshteyn.pingo20.activity.fragment.PingoWindow;
import com.mbronshteyn.pingo20.events.FingerTap;
import com.mbronshteyn.pingo20.events.GuessedNumberEvent;
import com.mbronshteyn.pingo20.events.InitBackgroundEvent;
import com.mbronshteyn.pingo20.events.NoGuessedNumberEvent;
import com.mbronshteyn.pingo20.events.NumberSpinEndEvent;
import com.mbronshteyn.pingo20.events.NumberSpinEvent;
import com.mbronshteyn.pingo20.events.PingoEvent;
import com.mbronshteyn.pingo20.events.ScrollEnd;
import com.mbronshteyn.pingo20.events.ScrollStart;
import com.mbronshteyn.pingo20.events.StopPlayer;
import com.mbronshteyn.pingo20.events.WinAnimation;
import com.mbronshteyn.pingo20.events.WinStarsEvent;
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

    private PingoWindow pingo1;
    private PingoWindow pingo2;
    private PingoWindow pingo3;
    private PingoWindow pingo4;
    private Button hitButtonGo;
    private ImageView buttonCounter;
    private AnimatorSet mSetLeftIn;
    private AnimatorSet mSetRightOut;
    private List<Integer> closedPingos  = new ArrayList<>();;
    private List<Integer> playPingos;
    private boolean flippedToGo;
    private Iterator<Integer> pingoIterator;
    private HashMap<Integer, Integer> buttonMap;
    private TextView balance;
    private GameActivity context;
    private boolean spinning;
    private ConstraintLayout root;
    private AnimatorSet mSetRightOutLeft;
    private AnimatorSet mSetLeftInLeft;
    private int playablePingos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_start);

        scaleUi();

        root = findViewById(R.id.rootCoordinatorLayoutGame);

        if(Game.card.isFreeGame()){
            Game.attemptCounter = 3 - Game.card.getNonBonusHits().size();
            ImageView freeGame = (ImageView) findViewById(R.id.free_game);
            freeGame.setVisibility(View.VISIBLE);
        }
        else if(Game.card.isFreeAttempt() && !luckySeven){
            Game.attemptCounter = 4 - Game.card.getNonBonusHits().size() +1;
        }
        else {
            Game.attemptCounter = 4 - Game.card.getNonBonusHits().size();
        }

        context = this;

        progressCounter = (ImageView) findViewById(R.id.progressCounter);
        dotsProgress = (AnimationDrawable) progressCounter.getDrawable();

        //balance
        Typeface fontBalance = Typeface.createFromAsset(this.getAssets(), "fonts/showg.ttf");
        balance = (TextView) findViewById(R.id.balance);
        balance.setTypeface(fontBalance,Typeface.BOLD_ITALIC);
        balance.setText(getCardReward());

        flippedToGo = false;

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
        mSetRightOutLeft = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.anim.card_flip_left_out);
        mSetLeftInLeft = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.anim.card_flip_left_in);
        
        buttonCounter = (ImageView) findViewById(R.id.hitCounter);
        Glide.with(this).load(buttonMap.get(Game.attemptCounter)).into(buttonCounter);

        hitButtonGo = (Button) findViewById(R.id.actionButtonGo);
        hitButtonGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(R.raw.button);
                stopPplayInBackground();
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

        //menu button
        ImageView menuButton = (ImageView) findViewById(R.id.menuButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(GameActivity.this);
                startActivity(intent, options.toBundle());
            }
        });

        isOKToInit = true;

        ImageView iView = (ImageView) findViewById(R.id.blueGameBacgroundimage);
        Glide.with(this).load(R.drawable.main_background_blue).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(iView);

        ImageView gameBacgroundimageView = (ImageView) findViewById(R.id.gameBacgroundimageView);
        Glide.with(this).load(R.drawable.main_background).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(gameBacgroundimageView);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        clearBonusSplash();
        clearBonus777Splash();

        if(isWinningCard() && Game.attemptCounter != 0 ){
            new Handler().postDelayed(() -> {transitionLayout();}, 7100);
            new Handler().postDelayed(()->{processWin(1);},14000);
        }
        else if(Game.card.isFreeGame() || luckySeven){
            new Handler().postDelayed(() -> {transitionLayout();}, 1000);
        }
        else if(isOKToInit ){
            int slideNo = 0;
            int delay = 0;

            switch (Game.attemptCounter) {
                case 3:
                    slideNo = R.drawable.to2;
                    delay = 8000;
                    break;
                case 2:
                    slideNo = R.drawable.to3;
                    delay = 8000;
                    break;
                case 1:
                    slideNo = R.drawable.to4;
                    delay = 8000;
                    break;
            }

            if (slideNo > 0) {
                ImageView overlayBlue = (ImageView) findViewById(R.id.overlay_blue);
                Glide.with(context).clear(overlayBlue);
                Glide.with(this).load(slideNo).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(overlayBlue);
                overlayBlue.setVisibility(View.VISIBLE);
                new Handler().postDelayed(() -> {
                    playSound(R.raw.comix_page_short);
                }, 700);
                new Handler().postDelayed(() -> {
                    overlayBlue.setVisibility(View.INVISIBLE);
                }, delay);
            }
            new Handler().postDelayed(() -> {
                transitionLayout();
            }, delay + 100);
        }
        else{
            playInBackgroundIfNotPlaying(R.raw.main_long_minus10);
        }
        isOKToInit = false;
        ImageView messageAlert = (ImageView) findViewById(R.id.messageAlert);
        Glide.with(context).clear(messageAlert);
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
                new Handler().postDelayed(()->{playSound(R.raw.screen_down);},500);
            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                new Handler().postDelayed(() -> {initState(); }, 100);
                if(luckySeven){
                    luckySeven = false;
                    Game.attemptCounter = 4 - Game.card.getNonBonusHits().size() +1;
                    new Handler().postDelayed(() -> {flippToCounterLeft();}, 4000);
                }
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

    private void initState() {

        playPingos = loadPingosInPlay(true);
        playablePingos = playPingos.size();
        List<Integer> winPingos = loadPingosInPlay(false);
        initPingos(playPingos,Game.attemptCounter != 0 && !fingerred);
        initPingos(winPingos, false);
        for(Integer playPingo: playPingos){
            closedPingos.add(playPingo);
        }
    }

    private void initPingos(List<Integer> playPingos, boolean canHaveFinger) {

        if (canHaveFinger){
            fingerred = true;
        }

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

    private void doCheckGameOver() {

        //show winning pin
        flippToGo();

        //alert message
        playSound(R.raw.aler_message);
        ImageView messageAlert = (ImageView) findViewById(R.id.messageAlert);
        Glide.with(this).load(R.drawable.alert_open).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(messageAlert);
        Animation fromRight = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_from_right);
        messageAlert.startAnimation(fromRight);

        hitButtonGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(R.raw.button);
                doWinPinCheck();
                hitButtonGo.setEnabled(false);
            }
        });
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

        ImageView nonTouchShield = (ImageView) findViewById(R.id.nonTouch_shield);
        nonTouchShield.setVisibility(View.VISIBLE);
        ImageView pinChekBackground = (ImageView) findViewById(R.id.pinChekBackground);
        pinChekBackground.setVisibility(View.VISIBLE);
        Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        pinChekBackground.startAnimation(fadeIn);

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

        doProgress(true);

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

        cardHitDto.setHit1(pingo1.getCurrentPingo());
        cardHitDto.setHit2(pingo2.getCurrentPingo());
        cardHitDto.setHit3(pingo3.getCurrentPingo());
        cardHitDto.setHit4(pingo4.getCurrentPingo());
        cardHitDto.setBonus(Game.bonusHit);
        cardHitDto.setBonusHit(Game.bonusHit != null);
        //reset bonus
        Game.bonusHit = null;

        Call<CardDto> call = service.hitCard(cardHitDto);
        call.enqueue(new Callback<CardDto>() {
            @Override
            public void onResponse(Call<CardDto> call, Response<CardDto> response) {
                processHitResponse(response);
            }

            @Override
            public void onFailure(Call<CardDto> call, Throwable t) {
                new Handler().postDelayed(()->{ playSound(R.raw.error_short);},100);
                doProgress(false);
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
            Game.card = response.body();
            pingoIterator = playPingos.iterator();
            Integer activeWindow = pingoIterator.next();
            playSpingSound();
            EventBus.getDefault().post(new NumberSpinEvent(activeWindow, loadNumberGuessed(activeWindow)));
            activatePingoCheckWindow(activeWindow,View.VISIBLE);
            Game.attemptCounter--;
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

        List<HitDto> hits = Game.card.getHits();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        stopPlaySound(R.raw.wheel_spinning);
        spinning = false;
        playablePingos--;
        if(playablePingos == 0){
            playInBackgroundIfNotPlaying(R.raw.main_long_minus10);
        }
    }

    @Subscribe
    public void onFingerTouch(StopPlayer event){
        ImageView overlayBlue = (ImageView) findViewById(R.id.overlay_blue);
        overlayBlue.setVisibility(View.INVISIBLE);
        stopPlaySound(event.getSound());
    }

    @Subscribe
    public void onFingerTap(FingerTap event){
        if(event.getTapNumber() == 1) {
            ImageView overlayBlue = (ImageView) findViewById(R.id.overlay_blue);
            Glide.with(context).clear(overlayBlue);
            Glide.with(this).load(R.drawable.instruction_game).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(overlayBlue);
            overlayBlue.setVisibility(View.VISIBLE);
        }
        new Handler().postDelayed(()->{playSound(R.raw.knocking_on_glass);},100);
    }

    @Subscribe
    public void onPingoEventMessage(PingoEvent event) {

        ImageView overlayBlue = (ImageView) findViewById(R.id.overlay_blue);
        overlayBlue.setVisibility(View.INVISIBLE);

        int pingo = event.getPingoNumber();
        removeNumber(pingo);

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
    }

    @Subscribe
    public void winNumber(GuessedNumberEvent event){

        int delay = 0;
        if (Game.guessedCount == 2 && Game.attemptCounter > 0 && !Game.card.isFreeGame()) {
            doHalfWayThere();
            delay = 2000;
        }
        new Handler().postDelayed(()-> {
            //stopPlaySound(R.raw.spin);
            playSound(R.raw.right_number);
            EventBus.getDefault().post(new WinAnimation(event.getPingoNumber(),WinAnimation.colorType.GREEN));
        },delay);
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
                initState();
                break;
        }

        if(slideNo != 0 && !Game.card.isFreeGame()) {
            ImageView overlayBlue = (ImageView) findViewById(R.id.overlay_blue);
            Glide.with(context).clear(overlayBlue);
            Glide.with(this).load(slideNo).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(overlayBlue);
            overlayBlue.setVisibility(View.VISIBLE);
            playSound(R.raw.comix_page_short);
            new Handler().postDelayed(() -> {
                overlayBlue.setVisibility(View.INVISIBLE);
                doProgress(false);
                flippToCounter();
                initState();
            }, 8000);
        }
        else{
            doProgress(false);
            flippToCounter();
            initState();
        }

    }

    private void gotoToBonus() {

        playSound(R.raw.trans_to_777);

        //pop up dark overlay and bonus logo
        ImageView overlayBlue = (ImageView) findViewById(R.id.overlay_blue);
        Glide.with(context).clear(overlayBlue);
        Glide.with(this).load(R.drawable.dark_overlay).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(overlayBlue);
        overlayBlue.setVisibility(View.VISIBLE);

        //popup logo
        ImageView logo = (ImageView) findViewById(R.id.popup_logo1);
        Glide.with(this).load(R.drawable.bonus_logo).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(logo);
        Animation logoPopup = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in_logo);
        logo.startAnimation(logoPopup);

        //spitral rays
        ImageView rays = (ImageView) findViewById(R.id.spiral);
        rays.setVisibility(View.VISIBLE);
        Animation raysSiral = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_rotate);
        rays.startAnimation(raysSiral);

        //drop cherry
        new Handler().postDelayed(()->{
            ImageView cherrys = (ImageView) findViewById(R.id.cherry);
            cherrys.setVisibility(View.VISIBLE);
            Animation cherryDrop = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.from_top);
            cherrys.startAnimation(cherryDrop);
        },3600);

        //transition to bonus activity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(getApplicationContext(), BonusGameActivity.class);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(GameActivity.this);
            startActivity(intent, options.toBundle());
        }, 6100);
    }

    private void doHalfWayThere() {

        ImageView overlayBlue = (ImageView) findViewById(R.id.overlay_blue);
        Glide.with(this).load(R.drawable.overlay_blue).diskCacheStrategy( DiskCacheStrategy.NONE ).skipMemoryCache( true ).into(overlayBlue);
        overlayBlue.setVisibility(View.VISIBLE);

        ImageView halfWay = (ImageView) findViewById(R.id.popup_logo1);
        Glide.with(this).load(R.drawable.half_way).diskCacheStrategy( DiskCacheStrategy.NONE ).skipMemoryCache( true ).into(halfWay);
        halfWay.setVisibility(View.VISIBLE);
        Animation zoomHalfWay = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in_halfway);
        zoomHalfWay.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                overlayBlue.setVisibility(View.INVISIBLE);
                halfWay.clearAnimation();
                Glide.with(context).clear(halfWay);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        halfWay.startAnimation(zoomHalfWay);
        playSound(R.raw.half_way);
    }

    @Subscribe
    public void spinEnd(NumberSpinEndEvent event){

        dotsProgress.stop();
        dotsProgress.setVisible(true,true);

        int duration = 0;

        //wrong nummber
        if(!event.isGuessed()){
            duration = 3000;
        }
        //right number
        else {
            //half there popup for non free game
            if(!Game.card.isFreeGame() && Game.guessedCount == 2 && Game.attemptCounter > 0){
                duration = 9000;
            }
            //last guessed number when game is won / not frre game
            else if(!pingoIterator.hasNext() && isWinningCard()){
                duration = 6000;
            }
            //flash ray animation for guessed number
            else {
                duration = 6000;
            }
        }

        new Handler().postDelayed(()-> {
            activatePingoCheckWindow(event.getPingoNumber(),View.INVISIBLE);
            if (pingoIterator.hasNext()) {
                Integer activeWindow = pingoIterator.next();
                doProgress(true);
                playSpingSound();
                EventBus.getDefault().post(new NumberSpinEvent(activeWindow, loadNumberGuessed(activeWindow)));
                activatePingoCheckWindow(activeWindow,View.VISIBLE);
                playSound(R.raw.button);
            }
            //end of attempt
            else {
                if(isWinningCard()){
                    ImageView pinChekBackground = (ImageView) findViewById(R.id.pinChekBackground);
                    pinChekBackground.setVisibility(View.INVISIBLE);
                    processWin(event.getPingoNumber());
                }
                else{
                    //remove sheilds
                    ImageView nonTouchShield = (ImageView) findViewById(R.id.nonTouch_shield);
                    nonTouchShield.setVisibility(View.INVISIBLE);
                    ImageView pinChekBackground = (ImageView) findViewById(R.id.pinChekBackground);
                    pinChekBackground.setVisibility(View.INVISIBLE);
                    balance.setTextColor(Color.WHITE);
                    balance.setText(getCardReward());
                    EventBus.getDefault().post(new InitBackgroundEvent());
                    if(Game.card.getBonusPin() != null && Game.card.getBonusPin().equals(Bonus.BONUSPIN)){
                        gotoToBonus();
                    }
                    else if(Game.card.getBonusPin() != null && Game.card.getBonusPin().equals(Bonus.SUPERPIN)){
                        gotoToSpinBonus();
                    }
                    else {
                        attemptTransition();
                    }
                    //check end of game
                    if (Game.attemptCounter == 0) {
                        doCheckGameOver();
                    }
                }
            }
        },duration);
    }

    private void gotoToSpinBonus() {

        playSound(R.raw.gotobonus);

        //move up the game interface
        new Handler().postDelayed(()->{
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(this, R.layout.activity_game_end);

            ChangeBounds transition = new ChangeBounds();
            transition.setInterpolator(new AnticipateOvershootInterpolator(1.2f));
            transition.setDuration(1000);
            transition.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(@NonNull Transition transition) {
                    //pop up dark overlay and bonus logo
                    ImageView overlayBlue = (ImageView) findViewById(R.id.overlay_blue);
                    Glide.with(context).clear(overlayBlue);
                    Glide.with(context).load(R.drawable.bonus_spin_mainbackground).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(overlayBlue);
                    overlayBlue.setVisibility(View.VISIBLE);
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
            TransitionManager.beginDelayedTransition(root, transition);
            constraintSet.applyTo(root);

        },100);

        //popup logo 1
        new Handler().postDelayed(()->{
            ImageView logo1 = (ImageView) findViewById(R.id.popup_logo1);
            logo1.setVisibility(View.VISIBLE);
            Glide.with(this).load(R.drawable.bonus_blue).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(logo1);
            Animation logoPopup1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in_bonus);
            logo1.startAnimation(logoPopup1);},600);

        //popup logo 2
        new Handler().postDelayed(()->{
            ImageView logo2 = (ImageView) findViewById(R.id.popup_logo2);
            logo2.setVisibility(View.VISIBLE);
            Glide.with(this).load(R.drawable.bonus_yellow).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(logo2);
            Animation logoPopup2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in_bonus);
            logo2.startAnimation(logoPopup2);
        },750);


        //popup logo 3
        new Handler().postDelayed(()->{
            ImageView logo3 = (ImageView) findViewById(R.id.popup_logo3);
            logo3.setVisibility(View.VISIBLE);
            Glide.with(this).load(R.drawable.bonus_banner).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(logo3);
            Animation logoPopup3 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_rotate_bonus);
            logo3.startAnimation(logoPopup3);
        },850);

        //transition to bonus activity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(getApplicationContext(), BonusSpinActivity.class);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(GameActivity.this);
            startActivity(intent, options.toBundle());
        }, 5500);
    }

    private void clearBonusSplash(){
        ImageView overlayBlue = (ImageView) findViewById(R.id.overlay_blue);
        Glide.with(context).clear(overlayBlue);
        overlayBlue.setVisibility(View.INVISIBLE);

        ImageView logo1 = (ImageView) findViewById(R.id.popup_logo1);
        Glide.with(context).clear(logo1);
        logo1.setVisibility(View.INVISIBLE);

        ImageView logo2 = (ImageView) findViewById(R.id.popup_logo2);
        Glide.with(context).clear(logo2);
        logo2.setVisibility(View.INVISIBLE);

        ImageView logo3 = (ImageView) findViewById(R.id.popup_logo3);
        Glide.with(context).clear(logo3);
        logo3.setVisibility(View.INVISIBLE);
    }

    private void clearBonus777Splash(){

        ImageView overlayBlue = (ImageView) findViewById(R.id.overlay_blue);
        Glide.with(context).clear(overlayBlue);
        overlayBlue.setVisibility(View.INVISIBLE);

        ImageView logo = (ImageView) findViewById(R.id.popup_logo1);
        Glide.with(context).clear(logo);
        logo.setVisibility(View.INVISIBLE);

        ImageView rays = (ImageView) findViewById(R.id.spiral);
        rays.setVisibility(View.INVISIBLE);
        rays.clearAnimation();

        ImageView cherrys = (ImageView) findViewById(R.id.cherry);
        cherrys.setVisibility(View.INVISIBLE);
        cherrys.clearAnimation();
    }

    private void processWin(int pingoNumber) {

        stopPplayInBackground();

        //move up the game interface
        new Handler().postDelayed(()->{
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(this, R.layout.activity_game_end);

            ChangeBounds transition = new ChangeBounds();
            transition.setInterpolator(new AnticipateOvershootInterpolator(1.2f));
            transition.setDuration(1000);
            TransitionManager.beginDelayedTransition(root, transition);
            constraintSet.applyTo(root);

        },100);

        int duration;
        Intent intent  = null;
        if(Game.attemptCounter == 0 && isWinningCard() && !Game.card.isFreeGame()){
            duration = 1200;
            intent = new Intent(getApplicationContext(), FreeGameActivity.class);
        }
        else{
            intent = new Intent(getApplicationContext(), WinEmailActivity.class);
            duration = 10000;
            new Handler().postDelayed(()->{ doWinningFlash();},1000);
        }

        Intent finalIntent = intent;
        new Handler().postDelayed(() -> {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(GameActivity.this);
            startActivity(finalIntent, options.toBundle());
        }, duration);
    }

    private void doWinningFlash(){

        playSound(R.raw.game_win);

        ImageView starts = (ImageView) findViewById(R.id.winGameStars);
        starts.setVisibility(View.VISIBLE);
        AnimationDrawable starsAnimation = (AnimationDrawable) starts.getDrawable();
        starsAnimation.start();

        ImageView pingoWinner = (ImageView) findViewById(R.id.mainLogo);
        pingoWinner.setVisibility(View.VISIBLE);

        TextView winBalance = (TextView) findViewById(R.id.win_amount);
        Typeface fontBalance = Typeface.createFromAsset(this.getAssets(), "fonts/showg.ttf");
        int win = (int) Game.card.getBalance();
        winBalance.setText(String.valueOf(win)+" ");
        winBalance.setTypeface(fontBalance,Typeface.BOLD_ITALIC);
        Animation zoomIntAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.win_zoom_in);
        new Handler().postDelayed(()->{winBalance.startAnimation(zoomIntAnimation);},300);
        new Handler().postDelayed(()->{winBalance.startAnimation(zoomIntAnimation);},6000);

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
                doProgress(false);
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
            flippToCounter();
            pingo1.showWinPin(Integer.parseInt(winPin.substring(0,1)));
            pingo2.showWinPin(Integer.parseInt(winPin.substring(1,2)));
            pingo3.showWinPin(Integer.parseInt(winPin.substring(2,3)));
            pingo4.showWinPin(Integer.parseInt(winPin.substring(3,4)));

            stopPplayInBackground();

            new Handler().postDelayed(()->{
                Intent intent = new Intent(getApplicationContext(), EndOfGameActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
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

    public void flippToCounter() {

        //flip button
        Glide.with(this).load(buttonMap.get(Game.attemptCounter)).into(buttonCounter);
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

    public void flippToCounterLeft() {

        ImageView plusOne = (ImageView) findViewById(R.id.popup_logo1);
        Glide.with(this).load(R.drawable.plus1).diskCacheStrategy( DiskCacheStrategy.NONE ).skipMemoryCache( true ).into(plusOne);
        plusOne.setVisibility(View.VISIBLE);
        Animation zoomPlusOne = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in_fade_plus);
        plusOne.startAnimation(zoomPlusOne);

        playSound(R.raw.up_plus_one2);

        GameActivity app = this;

        //flip button
        Drawable backFlip = AppCompatResources.getDrawable(context, buttonMap.get(Game.attemptCounter));
        hitButtonGo.setBackground(backFlip);
        mSetRightOutLeft.setTarget(buttonCounter);
        mSetLeftInLeft.setTarget(hitButtonGo);
        mSetLeftInLeft.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }
            @Override
            public void onAnimationEnd(Animator animation) {

                plusOne.setVisibility(View.INVISIBLE);
                plusOne.clearAnimation();

                Glide.with(app).load(buttonMap.get(Game.attemptCounter)).into(buttonCounter);

                AnimatorSet fadeOut = (AnimatorSet) AnimatorInflater.loadAnimator(app, R.anim.alpha_out);
                AnimatorSet fadeIn = (AnimatorSet) AnimatorInflater.loadAnimator(app, R.anim.alpha_in);
                fadeOut.setTarget(hitButtonGo);
                fadeIn.setTarget(buttonCounter);
                fadeOut.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Drawable go = AppCompatResources.getDrawable(context, R.drawable.btn_auth);
                        hitButtonGo.setBackground(go);
                    }
                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }
                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
                fadeOut.start();
                fadeIn.start();
            }
            @Override
            public void onAnimationCancel(Animator animation) {
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        mSetRightOutLeft.start();
        mSetLeftInLeft.start();

        flippedToGo = false;
    }

    private void removeNumber(int tagNumberToRemove){
        Iterator<Integer> numbersIter = closedPingos.iterator();
        while (numbersIter.hasNext()){
            if(numbersIter.next().equals(tagNumberToRemove)){
                numbersIter.remove();              // it will remove element from collection
            }
        }
    }

    private void activatePingoCheckWindow(int pingoNumber, int visibility){

        //pingo check window
        ImageView pingoCheck = null;
        switch(pingoNumber){
            case 1:
                pingoCheck = (ImageView) findViewById(R.id.pingoCheck1);
                break;
            case 2:
                pingoCheck = (ImageView) findViewById(R.id.pingoCheck2);
                break;
            case 3:
                pingoCheck = (ImageView) findViewById(R.id.pingoCheck3);
                break;
            case 4:
                pingoCheck = (ImageView) findViewById(R.id.pingoCheck4);
                break;
        }
        pingoCheck.setVisibility(visibility);
    }

    @Subscribe
    public void flashWinStars(WinStarsEvent event){

        ImageView winFlash = null;
        switch(event.getPingoNumber()){
            case 1:
                winFlash = (ImageView) findViewById(R.id.winStars1);
                break;
            case 2:
                winFlash = (ImageView) findViewById(R.id.winStars2);
                break;
            case 3:
                winFlash = (ImageView) findViewById(R.id.winStars3);
                break;
            case 4:
                winFlash = (ImageView) findViewById(R.id.winStars4);
                break;
        }

        Animation zoomIntAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.star_zoom);;
        ImageView finalWinFlash = winFlash;
        new Handler().postDelayed(()->{ finalWinFlash.startAnimation(zoomIntAnimation);},event.getOffsetWinStars());
        new Handler().postDelayed(()->{ finalWinFlash.clearAnimation();},event.getDurationWinStars());
    }

    @Subscribe
    public void onStaertNumberCheck(NumberSpinEvent event){
        //generate alert on last window
        if(!pingoIterator.hasNext()){
            int alert = 0;
            if(Game.card.getBonusPin() != null && Game.card.getBonusPin().equals(Bonus.BONUSPIN)){
                alert = R.drawable.alert_777;
            }
            else if(Game.card.getBonusPin() != null && Game.card.getBonusPin().equals(Bonus.SUPERPIN)){
                alert = R.drawable.alert_bonus;
            }
            if(alert != 0) {
                playSound(R.raw.aler_message);
                ImageView messageAlert = (ImageView) findViewById(R.id.messageAlert);
                Glide.with(this).load(alert).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(messageAlert);
                Animation fromRight = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_from_right);
                messageAlert.startAnimation(fromRight);
            }
        }
    }

    public void playSpingSound(){
        new Handler().postDelayed(()->{playSound(R.raw.spin);},600);
    }

    public void scaleUi() {

        // scale the screen
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.main_background_blue, options);
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
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.rootCoordinatorLayoutGame);
        ImageView iView = (ImageView) findViewById(R.id.blueGameBacgroundimage);
        ConstraintSet set = new ConstraintSet();
        set.clone(layout);
        set.constrainHeight(iView.getId(), newBmapHeight);
        set.constrainWidth(iView.getId(), newBmapWidth);
        set.applyTo(layout);

        //scale main background
        ImageView gameBacgroundimageView = (ImageView) findViewById(R.id.gameBacgroundimageView);
        ViewGroup.LayoutParams gameBacgroundimageViewParams = gameBacgroundimageView.getLayoutParams();
        gameBacgroundimageViewParams.width = newBmapWidth;
        gameBacgroundimageViewParams.height = newBmapHeight;

        //scale game background
        ImageView mainBackground = (ImageView) findViewById(R.id.mainGameBacgroundimageView);
        ViewGroup.LayoutParams mainBackgroundParams = mainBackground.getLayoutParams();
        mainBackgroundParams.width = newBmapWidth;
        mainBackgroundParams.height = newBmapHeight;

        //scale action18  button
        ImageView actionButton18 = (ImageView) findViewById(R.id.hitCounter);
        int buttonHeight = (int) (newBmapHeight * 0.3149F);
        int buttonWidth = (int) (newBmapWidth * 0.1777F);
        ViewGroup.LayoutParams buttonParams18 = actionButton18.getLayoutParams();
        buttonParams18.height = buttonHeight;
        buttonParams18.width = buttonWidth;


        //scale dots progress
        ImageView dotsProgress = (ImageView) findViewById(R.id.progressCounter);
        ViewGroup.LayoutParams dotsProgressParams18 = dotsProgress.getLayoutParams();
        dotsProgressParams18.height = buttonHeight;
        dotsProgressParams18.width = buttonWidth;

        //scale actionGo  button
        Button actionButtonGo = (Button) findViewById(R.id.actionButtonGo);
        ViewGroup.LayoutParams buttonParamsGo = actionButtonGo.getLayoutParams();
        buttonParamsGo.height = buttonHeight;
        buttonParamsGo.width = buttonWidth;

        //scale pingo windows
        float pingoHeight = 0.3618F;
        float pingoWidth = 0.3428F;

        ConstraintLayout pingo1 = (ConstraintLayout) findViewById(R.id.pingo1);
        ViewGroup.LayoutParams pingoParams = pingo1.getLayoutParams();
        pingoParams.height = (int)(newBmapHeight*pingoHeight);
        pingoParams.width = (int)(newBmapHeight*pingoWidth);

        ConstraintLayout pingo2 = (ConstraintLayout) findViewById(R.id.pingo2);
        pingoParams = pingo2.getLayoutParams();
        pingoParams.height = (int)(newBmapHeight*pingoHeight);
        pingoParams.width = (int)(newBmapHeight*pingoWidth);

        ConstraintLayout pingo3 = (ConstraintLayout) findViewById(R.id.pingo3);
        pingoParams = pingo3.getLayoutParams();
        pingoParams.height = (int)(newBmapHeight*pingoHeight);
        pingoParams.width = (int)(newBmapHeight*pingoWidth);

        ConstraintLayout pingo4 = (ConstraintLayout) findViewById(R.id.pingo4);
        pingoParams = pingo4.getLayoutParams();
        pingoParams.height = (int)(newBmapHeight*pingoHeight);
        pingoParams.width = (int)(newBmapHeight*pingoWidth);

        //scale nonTouchShield
        ImageView nonTouchShield = (ImageView) findViewById(R.id.nonTouch_shield);
        ViewGroup.LayoutParams nonTouchShieldParams = nonTouchShield.getLayoutParams();
        nonTouchShieldParams.width =(int)(newBmapWidth*1F);
        nonTouchShieldParams.height =(int)(newBmapHeight*1F);

        //scale pinChekBackground
        ImageView pinChekBackground = (ImageView) findViewById(R.id.pinChekBackground);
        ViewGroup.LayoutParams pinChekBackgroundParams = pinChekBackground.getLayoutParams();
        pinChekBackgroundParams.width = newBmapWidth;
        pinChekBackgroundParams.height = newBmapHeight;

        //scale free game
        ImageView freeGame = (ImageView) findViewById(R.id.free_game);
        ViewGroup.LayoutParams freeGameParams = freeGame.getLayoutParams();
        freeGameParams.width =(int)(newBmapWidth*0.1641F);
        freeGameParams.height =(int)(newBmapHeight*0.06099F);

        //scale half way
        ImageView halfWay = (ImageView) findViewById(R.id.popup_logo1);
        ViewGroup.LayoutParams halfWayParams = halfWay.getLayoutParams();
        halfWayParams.width =(int)(newBmapWidth*0.75F);
        halfWayParams.height =(int)(newBmapHeight*0.7722F);

        //scale half way
        ImageView halfWay2 = (ImageView) findViewById(R.id.popup_logo2);
        ViewGroup.LayoutParams halfWay2Params = halfWay2.getLayoutParams();
        halfWay2Params.width =(int)(newBmapWidth*0.75F);
        halfWay2Params.height =(int)(newBmapHeight*0.7722F);

        //scale half way
        ImageView halfWay3 = (ImageView) findViewById(R.id.popup_logo3);
        ViewGroup.LayoutParams halfWay3arams = halfWay3.getLayoutParams();
        halfWay3arams.width =(int)(newBmapWidth*0.75F);
        halfWay3arams.height =(int)(newBmapHeight*0.7722F);

        //scale blue overlay
        ImageView overlayBlue = (ImageView) findViewById(R.id.overlay_blue);
        ViewGroup.LayoutParams overlayBlueParams = overlayBlue.getLayoutParams();
        overlayBlueParams.width = newBmapWidth;
        overlayBlueParams.height = newBmapHeight;

        //scale banner
        ImageView mainBanner = (ImageView) findViewById(R.id.mainBanner);
        ViewGroup.LayoutParams mainBannerParams = mainBanner.getLayoutParams();
        mainBannerParams.width = newBmapWidth;
        mainBannerParams.height = newBmapHeight;

        //scale rays
        ImageView rays = (ImageView) findViewById(R.id.spiral);
        ViewGroup.LayoutParams raysParams = rays.getLayoutParams();
        raysParams.width =(int)(newBmapWidth*0.5020F);
        raysParams.height =(int)(newBmapHeight*0.8896F);

        //scale rays
        ImageView cherrys = (ImageView) findViewById(R.id.cherry);
        ViewGroup.LayoutParams cherrysParams = cherrys.getLayoutParams();
        cherrysParams.width =(int)(newBmapWidth*0.2751);
        cherrysParams.height =(int)(newBmapHeight*0.4003F);

        //main logo
        ImageView mainLogo = (ImageView) findViewById(R.id.mainLogo);
        ViewGroup.LayoutParams mainLogoParams = mainLogo.getLayoutParams();
        mainLogoParams.width = newBmapWidth;
        mainLogoParams.height = newBmapHeight;

        //sping check1
        ImageView pingoCheck1 = (ImageView) findViewById(R.id.pingoCheck1);
        ViewGroup.LayoutParams mpingoCheck1Params = pingoCheck1.getLayoutParams();
        mpingoCheck1Params.width = (int)(newBmapHeight*pingoWidth*1.05);
        mpingoCheck1Params.height = (int)(newBmapHeight*pingoHeight*1.03);
        //sping check2
        ImageView pingoCheck2 = (ImageView) findViewById(R.id.pingoCheck2);
        ViewGroup.LayoutParams mpingoCheck2Params = pingoCheck2.getLayoutParams();
        mpingoCheck2Params.width = mpingoCheck1Params.width;
        mpingoCheck2Params.height = mpingoCheck1Params.height;
        //sping check3
        ImageView pingoCheck3 = (ImageView) findViewById(R.id.pingoCheck3);
        ViewGroup.LayoutParams mpingoCheck3Params = pingoCheck3.getLayoutParams();
        mpingoCheck3Params.width = mpingoCheck1Params.width;
        mpingoCheck3Params.height = mpingoCheck1Params.height;
        //sping check4
        ImageView pingoCheck4 = (ImageView) findViewById(R.id.pingoCheck4);
        ViewGroup.LayoutParams mpingoCheck4Params = pingoCheck4.getLayoutParams();
        mpingoCheck4Params.width = mpingoCheck1Params.width;
        mpingoCheck4Params.height = mpingoCheck1Params.height;

        //scale win stars1
        ImageView winStars1 = (ImageView) findViewById(R.id.winStars1);
        ViewGroup.LayoutParams winStars1Params = winStars1.getLayoutParams();
        winStars1Params.width =(int)(newBmapWidth*0.3710F);
        winStars1Params.height =(int)(newBmapHeight*0.6805F);
        //scale win stars2
        ImageView winStars2 = (ImageView) findViewById(R.id.winStars2);
        ViewGroup.LayoutParams winStars2Params = winStars2.getLayoutParams();
        winStars2Params.width = winStars1Params.width;
        winStars2Params.height = winStars1Params.height;
        //scale win stars3
        ImageView winStars3 = (ImageView) findViewById(R.id.winStars3);
        ViewGroup.LayoutParams winStars3Params = winStars3.getLayoutParams();
        winStars3Params.width = winStars1Params.width;
        winStars3Params.height = winStars1Params.height;
        //scale win stars3
        ImageView winStars4 = (ImageView) findViewById(R.id.winStars4);
        ViewGroup.LayoutParams winStars4Params = winStars4.getLayoutParams();
        winStars4Params.width = winStars1Params.width;
        winStars4Params.height = winStars1Params.height;

        //menu button
        ImageView menuButton = (ImageView) findViewById(R.id.menuButton);
        ViewGroup.LayoutParams menuButtonParams = menuButton.getLayoutParams();
        menuButtonParams.width = (int)(newBmapWidth*0.07802F);
        menuButtonParams.height = (int)(newBmapHeight*0.08238F);

        //scale stars
        ImageView winGameStars = (ImageView) findViewById(R.id.winGameStars);
        ViewGroup.LayoutParams winGameStarsParams = winGameStars.getLayoutParams();
        winGameStarsParams.width =(int)(newBmapWidth);
        winGameStarsParams.height =(int)(newBmapHeight);

        //scale messageAlert
        ImageView messageAlert = (ImageView) findViewById(R.id.messageAlert);
        ViewGroup.LayoutParams messageAlertParams = messageAlert.getLayoutParams();
        messageAlertParams.width = (int)(newBmapWidth*0.2409F);
        messageAlertParams.height = (int)(newBmapHeight*0.0960F);
    }
}
