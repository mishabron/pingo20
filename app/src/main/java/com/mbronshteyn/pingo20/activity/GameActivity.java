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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
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
import com.mbronshteyn.pingo20.events.NumberStopSpinEvent;
import com.mbronshteyn.pingo20.events.PingoEvent;
import com.mbronshteyn.pingo20.events.ScrollEnd;
import com.mbronshteyn.pingo20.events.ScrollStart;
import com.mbronshteyn.pingo20.events.StopPlayer;
import com.mbronshteyn.pingo20.events.WinAnimation;
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
    private AnimationDrawable dotsProgress;
    private ImageView progressCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_start);

        root = findViewById(R.id.coordinatorLayoutGame);

        scaleUi();

        ImageView freeGame = (ImageView) findViewById(R.id.free_game);

        if(card.isFreeGame()){
            Game.attemptCounter = 3 - card.getNonBonusHits().size();
        }
        else {
            Game.attemptCounter = 4 - card.getNonBonusHits().size();
            freeGame.setVisibility(View.INVISIBLE);
        }

        //if bonus hit increase counter
        if(Game.bonusHit != null && Game.bonusHit.equals(Bonus.BONUSPIN)){
            Game.attemptCounter ++;
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

        progressCounter = (ImageView) findViewById(R.id.progressCounter);
        dotsProgress = (AnimationDrawable) progressCounter.getDrawable();

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

        if(isWinningCard() && Game.attemptCounter != 0 ){
            new Handler().postDelayed(() -> {transitionLayout();}, 7100);
            new Handler().postDelayed(()->{processWin(1);},14000);
        }
        else {
            int slideNo = 0;
            int delay = 0;

            switch (Game.attemptCounter) {
                case 3:
                    slideNo = R.drawable.to2;
                    delay = 5000;
                    break;
                case 2:
                    slideNo = R.drawable.to3;
                    delay = 5000;
                    break;
                case 1:
                    slideNo = R.drawable.to4;
                    delay = 5000;
                    break;
            }

            if (slideNo > 0 && !card.isFreeGame()) {
                ImageView overlayBlue = (ImageView) findViewById(R.id.overlay_blue);
                Glide.with(context).clear(overlayBlue);
                Glide.with(this).load(slideNo).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(overlayBlue);
                overlayBlue.setVisibility(View.VISIBLE);
                new Handler().postDelayed(() -> {
                    overlayBlue.setVisibility(View.INVISIBLE);
                }, delay);
            }
            new Handler().postDelayed(() -> {
                transitionLayout();
            }, delay + 100);
        }
    }

    private void transitionLayout(){

        ImageView banner = (ImageView) findViewById(R.id.mainBanner);
        Animation zoomBanner = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in_banner);
        banner.startAnimation(zoomBanner);

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

    private void doCheckGameOver() {

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
        new Handler().postDelayed(()->{
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
            startActivity(intent);
            Activity activity = (Activity) context;
            activity.finish();
        }, 6100);
    }

    private void doHalfWayThere() {

        ImageView overlayBlue = (ImageView) findViewById(R.id.overlay_blue);
        Glide.with(this).load(R.drawable.overlay_blue7).diskCacheStrategy( DiskCacheStrategy.NONE ).skipMemoryCache( true ).into(overlayBlue);
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
    }

    @Subscribe
    public void spinEnd(NumberSpinEndEvent event){

        int duration = 0;
        doProgress(false);

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
            else if(!pingoIterator.hasNext() && isWinningCard()){
                duration = 700;
            }
            //flash ray animation for guessed number
            else {
                duration = 6000;
            }
        }

        new Handler().postDelayed(()-> {
            if (pingoIterator.hasNext()) {
                Integer activeWindow = pingoIterator.next();
                doProgress(true);
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
                    ImageView nonTouchShield = (ImageView) findViewById(R.id.nonTouch_shield);
                    nonTouchShield.setVisibility(View.INVISIBLE);
                    balance.setTextColor(Color.WHITE);
                    balance.setText(getCardReward());
                    EventBus.getDefault().post(new InitBackgroundEvent());
                    if(card.getBonusPin() != null && card.getBonusPin().equals(Bonus.BONUSPIN)){
                        gotoToBonus();
                    }
                    else if(card.getBonusPin() != null && card.getBonusPin().equals(Bonus.SUPERPIN)){
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

        //pop up dark overlay and bonus logo
        ImageView overlayBlue = (ImageView) findViewById(R.id.overlay_blue);
        Glide.with(context).clear(overlayBlue);
        Glide.with(this).load(R.drawable.dark_overlay_bonus).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(overlayBlue);
        overlayBlue.setVisibility(View.VISIBLE);

        //popup logo 1
        new Handler().postDelayed(()->{
            ImageView logo1 = (ImageView) findViewById(R.id.popup_logo1);
            logo1.setVisibility(View.VISIBLE);
            Glide.with(this).load(R.drawable.bonus_blue).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(logo1);
            Animation logoPopup1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in_bonus);
            logo1.startAnimation(logoPopup1);},200);

        //popup logo 2
        new Handler().postDelayed(()->{
            ImageView logo2 = (ImageView) findViewById(R.id.popup_logo2);
            logo2.setVisibility(View.VISIBLE);
            Glide.with(this).load(R.drawable.bonus_yellow).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(logo2);
            Animation logoPopup2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in_bonus);
            logo2.startAnimation(logoPopup2);
        },450);


        //popup logo 3
        new Handler().postDelayed(()->{
            ImageView logo3 = (ImageView) findViewById(R.id.popup_logo3);
            logo3.setVisibility(View.VISIBLE);
            Glide.with(this).load(R.drawable.bonus_banner).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(logo3);
            Animation logoPopup3 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in_banner);
            logo3.startAnimation(logoPopup3);
        },700);

        //transition to bonus activity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(getApplicationContext(), BonusSpinActivity.class);
            startActivity(intent);
            Activity activity = (Activity) context;
            activity.finish();
        }, 3000);
    }

    private void processWin(int pingoNumber) {

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
        AtomicReference<Intent> intent  = new AtomicReference<>(new Intent());
        if(Game.attemptCounter == 0 && isWinningCard() && !card.isFreeGame()){
            duration = 1200;
            intent.set(new Intent(getApplicationContext(), FreeGameActivity.class));
        }
        else{
            intent.set(new Intent(getApplicationContext(), WinEmailActivity.class));
            duration = 7000;
            doWinningFlash();
        }

        new Handler().postDelayed(() -> {
            startActivity(intent.get());
            Activity activity = (Activity) context;
            activity.finish();
        }, duration);
    }

    private void doWinningFlash(){

        ImageView overlayBlue = (ImageView) findViewById(R.id.overlay_blue);
        Glide.with(context).clear(overlayBlue);
        Glide.with(this).load(R.drawable.overlay_blue3).diskCacheStrategy( DiskCacheStrategy.NONE ).skipMemoryCache( true ).into(overlayBlue);
        overlayBlue.setVisibility(View.VISIBLE);

        ImageView pingoWinner = (ImageView) findViewById(R.id.mainLogo);
        ImageView logo4 = (ImageView) findViewById(R.id.popup_logo4);
        TextView winBalance = (TextView) findViewById(R.id.win_amount);

        new Handler().postDelayed(()->{
            pingoWinner.setVisibility(View.VISIBLE);
        },100);

        //popup logo 2
        new Handler().postDelayed(()->{
            logo4.setVisibility(View.VISIBLE);
        },200);


        Spannable wordtoSpan = new SpannableString("$" +(int) card.getBalance()+"  ");
        wordtoSpan.setSpan(new ForegroundColorSpan(Color.parseColor("#28b5ed")), 1, wordtoSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        wordtoSpan.setSpan(new ForegroundColorSpan(Color.WHITE), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        Typeface fontBalance = Typeface.createFromAsset(this.getAssets(), "fonts/showg.ttf");
        winBalance.setText(wordtoSpan);
        winBalance.setTypeface(fontBalance,Typeface.BOLD_ITALIC);
        winBalance.setShadowLayer(30, 20, 20, Color.BLACK);
        Animation zoomIntAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in);
        new Handler().postDelayed(()->{winBalance.startAnimation(zoomIntAnimation);},300);

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

        //scale dots progress
        ImageView dotsProgress = (ImageView) findViewById(R.id.progressCounter);
        int dotsProgressSize18 = (int) (newBmapHeight * 0.2606F);
        ViewGroup.LayoutParams dotsProgressParams18 = dotsProgress.getLayoutParams();
        dotsProgressParams18.height = dotsProgressSize18;
        dotsProgressParams18.width = dotsProgressSize18;


        //scale action18  button
        ImageView actionButton18 = (ImageView) findViewById(R.id.hitCounter);
        int buttonSize18 = (int) (newBmapHeight * 0.2606F);
        ViewGroup.LayoutParams buttonParams18 = actionButton18.getLayoutParams();
        buttonParams18.height = buttonSize18;
        buttonParams18.width = buttonSize18;

        //scale actionGo  button
        Button actionButtonGo = (Button) findViewById(R.id.actionButtonGo);
        int buttonSizeGo = (int) (newBmapHeight * 0.2606F);
        ViewGroup.LayoutParams buttonParamsGo = actionButtonGo.getLayoutParams();
        buttonParamsGo.height = buttonSizeGo;
        buttonParamsGo.width = buttonSizeGo;

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
        nonTouchShieldParams.width =(int)(newBmapWidth*0.8472F);
        nonTouchShieldParams.height =(int)(newBmapHeight*0.5923F);

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

        //scale logo4
        ImageView logo4 = (ImageView) findViewById(R.id.popup_logo4);
        ViewGroup.LayoutParams logo4Params = logo4.getLayoutParams();
        logo4Params.width =(int)(newBmapWidth*0.5971F);
        logo4Params.height =(int)(newBmapHeight*0.4759F);

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

    }

    private void doProgress(boolean startProgress){

        if(startProgress){
            progressCounter.setVisibility(View.VISIBLE);
            dotsProgress.start();
        }else{
            progressCounter.setVisibility(View.INVISIBLE);
            dotsProgress.stop();
        }

    }
}
