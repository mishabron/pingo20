package com.mbronshteyn.pingo20.activity;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
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
import android.widget.TextView;

import com.mbronshteyn.gameserver.dto.game.Bonus;
import com.mbronshteyn.gameserver.dto.game.CardDto;
import com.mbronshteyn.gameserver.dto.game.CardHitDto;
import com.mbronshteyn.gameserver.dto.game.HitDto;
import com.mbronshteyn.gameserver.exception.ErrorCode;
import com.mbronshteyn.pingo20.R;
import com.mbronshteyn.pingo20.activity.fragment.BonusSpinWondow;
import com.mbronshteyn.pingo20.events.NumberSpinEvent;
import com.mbronshteyn.pingo20.events.ScrollEnd;
import com.mbronshteyn.pingo20.events.SelecForSpinEvent;
import com.mbronshteyn.pingo20.events.SpinResultEvent;
import com.mbronshteyn.pingo20.events.SpinScrollEnd;
import com.mbronshteyn.pingo20.model.Game;
import com.mbronshteyn.pingo20.network.PingoRemoteService;
import com.mbronshteyn.pingo20.types.PingoState;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BonusSpinActivity extends PingoActivity{

    private ConstraintLayout root;
    private BonusSpinWondow pingo1;
    private BonusSpinWondow pingo2;
    private BonusSpinWondow pingo3;
    private BonusSpinWondow pingo4;
    private Button fingerButton;
    private ArrayList<Integer> acctivePingos = new ArrayList<>();
    private ArrayList<Integer> pingosInPlay = new ArrayList<>();
    private FingerTimer fingerTimer;
    private TextView balance;
    private List<Integer> spinSounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bonus_spin_start);

        root = findViewById(R.id.bonusLayoutSpin);

        pingo1 = (BonusSpinWondow) getSupportFragmentManager().findFragmentById(R.id.bonusSpinPingo1);
        pingo2 = (BonusSpinWondow) getSupportFragmentManager().findFragmentById(R.id.bonusSpinPingo2);
        pingo3 = (BonusSpinWondow) getSupportFragmentManager().findFragmentById(R.id.bonusSpinPingo3);
        pingo4 = (BonusSpinWondow) getSupportFragmentManager().findFragmentById(R.id.bonusSpinPingo4);

        fingerButton = (Button) findViewById(R.id.bonusButtonGo);
        fingerButton.setEnabled(false);

        fingerTimer = new BonusSpinActivity.FingerTimer(1000,100);

        //balance
        Typeface fontBalance = Typeface.createFromAsset(this.getAssets(), "fonts/showg.ttf");
        balance = (TextView) findViewById(R.id.spinBalance);
        balance.setTypeface(fontBalance,Typeface.BOLD_ITALIC);
        balance.setText(getCardReward());

        spinSounds = new ArrayList(Arrays.asList(R.raw.bonusspin_1,R.raw.bonusspin_2,R.raw.bonusspin_3,R.raw.bonusspin_4));

        scaleUi();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        new Handler().postDelayed(() -> { transitionLayout(); }, 500);
        Game.bonusHit = Bonus.SUPERPIN;
        new Handler().postDelayed(()->{ playInBackground(R.raw.bonusspin_background);},0);
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

    private void transitionLayout() {

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this, R.layout.activity_bonus_spin);

        ChangeBounds transition = new ChangeBounds();
        transition.setInterpolator(new AnticipateOvershootInterpolator(1.2f));
        transition.setDuration(1000);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {
                searchLights();
            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {

                ImageView spinBannerw = (ImageView) findViewById(R.id.spinBannerw);
                Animation zoomIntAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in);
                spinBannerw.startAnimation(zoomIntAnimation);

                new Handler().postDelayed(()->{transitionToPlay();},500);
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

    private void searchLights() {
        ImageView lights = (ImageView) findViewById(R.id.bonusSpinLights);
        lights.setImageDrawable(getResources().getDrawable(R.drawable.lights_animation,null));
        lights.setVisibility(View.VISIBLE);
        AnimationDrawable winAnimation = (AnimationDrawable) lights.getDrawable();
        winAnimation.start();
    }

    private void transitionToPlay() {

        playSound(R.raw.wheel_spinning);

        initPingos(pingo1);
        initPingos(pingo2);
        initPingos(pingo3);
        initPingos(pingo4);

        hitCard();
    }

    private void initPingos(BonusSpinWondow pingo){

        Bundle pingoBundle = new Bundle();
        pingoBundle.putSerializable("pingoState", PingoState.ACTIVE);
        pingoBundle.putIntegerArrayList("playedNumbers",loadNumbersPlayed(pingo.getPingoNumber()));
        pingoBundle.putSerializable("guessedNumber",loadNumberGuessed(pingo.getPingoNumber()));

        boolean guessed = loadNumberGuessed(pingo.getPingoNumber()) != null;
        pingoBundle.putBoolean("guessed", guessed);
        if(!guessed){
            acctivePingos.add(pingo.getPingoNumber());
            pingosInPlay.add(pingo.getPingoNumber());
        }

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
            if(playedNumber != null && playedNumber <100) {
                numbersPlayed.add(playedNumber);
            }
        }
        return numbersPlayed;
    }

    @Subscribe
    public void onInitScrollEnd(ScrollEnd event){

        acctivePingos.remove(Integer.valueOf(event.getPingoNumber()));
        playSound(R.raw.wheel_stop);

        //all pingos stoped srolling
        if (acctivePingos.isEmpty()){

            //start finger timer
            fingerButton.setEnabled(true);
            fingerTimer.start();

            //put yellow frame on first window
            EventBus.getDefault().post(new SelecForSpinEvent(pingosInPlay.get(0)));
            playSound(R.raw.button);

            //attach new finger button listiner
            fingerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fingerTimer.cancel();
                    spinPingos(pingosInPlay.get(0),spinSounds.get(0));
                    pingosInPlay.remove(0);
                    spinSounds.remove(0);
                }
            });
        }
    }

    @Subscribe
    public void onSpinScrollEnd(SpinScrollEnd event){
        if(!pingosInPlay.isEmpty()) {

            //start finger timer
            fingerButton.setEnabled(true);
            fingerTimer.start();

            EventBus.getDefault().post(new SelecForSpinEvent(pingosInPlay.get(0)));
            playSound(R.raw.button);
        }
    }

    @Subscribe
    public void onSpinResultEvent(SpinResultEvent event){

        int sound = 0;;

        switch(event.getResult()){
            case RIGHT:
                searchLights();
                sound = R.raw.right_number_winner;
                break;
            case WRONG:
                sound = R.raw.wrong_try_again;
                break;
            case TOKEN:
                sound = R.raw.token_try_again;
                break;
        }
        int finalSound = sound;
        new Handler().postDelayed(()->{playSound(finalSound);},1000);
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

    public void spinPingos(Integer pingoWindow, Integer spinSound){

        fingerButton.setEnabled(false);
        playSound(spinSound);
        switch (pingoWindow){
            case 1:
                pingo1.spinPingo(loadNumberGuessed(1));
                break;
            case 2:
                pingo2.spinPingo(loadNumberGuessed(2));
                break;
            case 3:
                pingo3.spinPingo(loadNumberGuessed(3));
                break;
            case 4:
                pingo4.spinPingo(loadNumberGuessed(4));
                break;
        }
    }

    private void hitCard(){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(PingoRemoteService.baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        final PingoRemoteService service = retrofit.create(PingoRemoteService.class);

        String card = Game.cardNumber;
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

        //scale spin banner
        ImageView spinBannerw = (ImageView) findViewById(R.id.spinBannerw);
        ViewGroup.LayoutParams spinBannerwParams = spinBannerw.getLayoutParams();
        spinBannerwParams.width = newBmapWidth;
        spinBannerwParams.height = newBmapHeight;

        //scale pingo windows
        float pingoWidth = 0.2008F;
        float pingoHeight = 0.3558F;
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
        int fingerButtonHeight = (int) (newBmapHeight * 0.4039F);
        int fingerButtonWidt = (int) (newBmapWidth * 0.2400F);
        ViewGroup.LayoutParams buttonParamsFinger = fingerButton.getLayoutParams();
        buttonParamsFinger.height = fingerButtonHeight;
        buttonParamsFinger.width = fingerButtonWidt;

        //scale lighta
        ImageView lights = (ImageView) findViewById(R.id.bonusSpinLights);
        ViewGroup.LayoutParams lightsParams = lights.getLayoutParams();
        lightsParams.width = newBmapWidth;
        lightsParams.height = newBmapHeight;
    }
}
