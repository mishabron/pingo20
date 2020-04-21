package com.mbronshteyn.pingo20.activity.fragment;


import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mbronshteyn.pingo20.R;
import com.mbronshteyn.pingo20.events.BlinkEvent;
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
import com.mbronshteyn.pingo20.events.SpinEvent;
import com.mbronshteyn.pingo20.events.StopPlayer;
import com.mbronshteyn.pingo20.events.WinAnimation;
import com.mbronshteyn.pingo20.events.WinFlashEvent;
import com.mbronshteyn.pingo20.model.Game;
import com.mbronshteyn.pingo20.types.PingoState;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.AbstractWheelAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class PingoWindow extends Fragment {

    private ImageView windowBackground;
    private ImageView play;
    private WheelView wheel;
    private boolean starting;
    private ImageView touchBackground;
    private List<ImageView> numbers;
    private int pingoNumber;
    private boolean hasFinger;
    private int newBmapWidth;
    private int newBmapHeight;
    private ImageView fishka;
    private int[] greenFishkas = new int[10];
    private Integer currentPingo;
    private FingerTimer fingerTimer;
    private PingoState pingoState;
    private Integer guessedNumber;
    private int[] redFishkas = new int[10];;
    private View mainView;
    private float zOrder;
    private boolean zoomedIn;

    public PingoWindow() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_pingo_window, container, false);

        scaleUi(mainView);

        windowBackground = (ImageView) mainView.findViewById(R.id.window_background);
        Glide.with(this).load(R.drawable.blue_window).diskCacheStrategy( DiskCacheStrategy.NONE ).skipMemoryCache( true ).into(windowBackground);
        zOrder = mainView.getZ();

        fishka = (ImageView) mainView.findViewById(R.id.fishka);

        fingerTimer = new FingerTimer(2000,100);

        touchBackground = (ImageView) mainView.findViewById(R.id.touchBackground);
        touchBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wheel.setInterpolator(null);
                wheel.scroll(-1 , 600);
                fingerTimer.cancel();
                if(fishka.getVisibility() == View.VISIBLE) {
                    fishka.setVisibility(View.INVISIBLE);
                }
            }
        });

        greenFishkas[0] = R.drawable.green0;
        greenFishkas[1] = R.drawable.green1;
        greenFishkas[2] = R.drawable.green2;
        greenFishkas[3] = R.drawable.green3;
        greenFishkas[4] = R.drawable.green4;
        greenFishkas[5] = R.drawable.green5;
        greenFishkas[6] = R.drawable.green6;
        greenFishkas[7] = R.drawable.green7;
        greenFishkas[8] = R.drawable.green8;
        greenFishkas[9] = R.drawable.green9;

        redFishkas[0] = R.drawable.red0;
        redFishkas[1] = R.drawable.red1;
        redFishkas[2] = R.drawable.red2;
        redFishkas[3] = R.drawable.red3;
        redFishkas[4] = R.drawable.red4;
        redFishkas[5] = R.drawable.red5;
        redFishkas[6] = R.drawable.red6;
        redFishkas[7] = R.drawable.red7;
        redFishkas[8] = R.drawable.red8;
        redFishkas[9] = R.drawable.red9;

        return mainView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PingoParameters);
        pingoNumber = a.getInt(R.styleable.PingoParameters_pingoNumber,1);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        numbers = loadPingoNumbers();

        wheel = getView().findViewById(R.id.slot);

        wheel.setViewAdapter(new SlotMachineAdapter());
        wheel.setCurrentItem(0,true);

        wheel.addChangingListener(changedListener);
        wheel.addScrollingListener(scrolledListener);
        wheel.setCyclic(true);
        wheel.setEnabled(false);
        wheel.setDrawShadows(false);
        wheel.setVisibility(View.INVISIBLE);
    }

    private List<ImageView> loadPingoNumbers() {

        ArrayList<ImageView> pingoNumbers = new ArrayList<>();
        pingoNumbers.add(loadNumber(0,R.drawable.zero,(int)(newBmapWidth*0.1313F), (int)(newBmapHeight*0.2888F)));
        pingoNumbers.add(loadNumber(9,R.drawable.nine,(int)(newBmapWidth*0.1313F), (int)(newBmapHeight*0.2888F)));
        pingoNumbers.add(loadNumber(8,R.drawable.eight,(int)(newBmapWidth*0.1313F), (int)(newBmapHeight*0.2888F)));
        pingoNumbers.add(loadNumber(7,R.drawable.seven,(int)(newBmapWidth*0.1313F), (int)(newBmapHeight*0.2888F)));
        pingoNumbers.add(loadNumber(6,R.drawable.six,(int)(newBmapWidth*0.1313F), (int)(newBmapHeight*0.2888F)));
        pingoNumbers.add(loadNumber(5,R.drawable.five,(int)(newBmapWidth*0.1313F), (int)(newBmapHeight*0.2888F)));
        pingoNumbers.add(loadNumber(4,R.drawable.four,(int)(newBmapWidth*0.1313F), (int)(newBmapHeight*0.2888F)));
        pingoNumbers.add(loadNumber(3,R.drawable.three,(int)(newBmapWidth*0.1313F), (int)(newBmapHeight*0.2888F)));
        pingoNumbers.add(loadNumber(2,R.drawable.two, (int)(newBmapWidth*0.1313F), (int)(newBmapHeight*0.2888F)));
        pingoNumbers.add(loadNumber(1,R.drawable.one,(int)(newBmapWidth*0.1313F), (int)(newBmapHeight*0.2888F)));
        pingoNumbers.add(loadNumber(10,R.drawable.pingo,(int)(newBmapWidth*0.1604F),(int)(newBmapWidth*0.1604F)));

        return pingoNumbers;
    }

    public void putFinger() {

        wheel.setVisibility(View.INVISIBLE);
        touchBackground.setVisibility(View.INVISIBLE);

        windowBackground.setImageDrawable(getResources().getDrawable(R.drawable.finger_animation,null));
        play = (ImageView) getView().findViewById(R.id.play);
        play.setVisibility(View.VISIBLE);

        windowBackground.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                windowBackground.setOnTouchListener(null);
                Glide.with(getActivity()).load(R.drawable.blue_window).diskCacheStrategy( DiskCacheStrategy.NONE )
                        .skipMemoryCache( true ).into(windowBackground);
                play.setVisibility(View.INVISIBLE);
                wheel.setVisibility(View.VISIBLE);
                touchBackground.setVisibility(View.VISIBLE);
                touchBackground.setEnabled(true);
                EventBus.getDefault().post(new StopPlayer(R.raw.knocking_on_glass));
                return false;
            }
        });

        AnimationDrawable fingerAnimation = (AnimationDrawable) windowBackground.getDrawable();
        long totalDuration = 0;
        for(int i = 0; i< fingerAnimation.getNumberOfFrames();i++){
            totalDuration += fingerAnimation.getDuration(i);
        }

        touchBackground.setEnabled(false);
        new Handler().postDelayed(()->{touchBackground.setEnabled(true);},totalDuration);

        //first tap
        EventBus.getDefault().post(new FingerTap());
        fingerAnimation.start();
        new Handler().postDelayed(()->{
            if(play.getVisibility() == View.VISIBLE) {
                AnimatorSet rockplay = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.anim.rockplay);
                rockplay.setTarget(play);
                rockplay.setStartDelay(500);
                rockplay.start();
            }
        },fingerAnimation.getDuration(0));

        //second tap
        new Handler().postDelayed(()->{
            if(play.getVisibility() == View.VISIBLE) {
                EventBus.getDefault().post(new FingerTap());
                AnimatorSet rockplay = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.anim.rockplay);
                rockplay.setTarget(play);
                rockplay.setStartDelay(500);
                rockplay.start();
            }
        },2500+fingerAnimation.getDuration(0));
    }

    public void initPingo(Bundle pingoBundle) {

        touchBackground.setEnabled(true);
        starting = true;
        numbers = loadPingoNumbers();

        hasFinger = pingoBundle.getBoolean("hasFinger");
        pingoState = (PingoState)pingoBundle.getSerializable("pingoState");
        guessedNumber = (Integer)pingoBundle.getSerializable("guessedNumber");
        int spinDelay = pingoBundle.getInt("spinDelay");
        ArrayList<Integer> playedNumbers = pingoBundle.getIntegerArrayList("playedNumbers");

        if(guessedNumber != null){
            hasFinger =false;
            pingoState = PingoState.WIN;
        }
        else{
            fishka.setVisibility(View.INVISIBLE);
            for(Integer playedNumber: playedNumbers){
                removeNumber(numbers,playedNumber);
            }
            //Glide.with(this).load(R.drawable.blue_window).diskCacheStrategy( DiskCacheStrategy.NONE ).skipMemoryCache( true ).into(windowBackground);
            windowBackground.setImageDrawable(getResources().getDrawable(R.drawable.blue_window,null));
            wheel.setCurrentItem(0);
        }
        wheel.setInterpolator(new AccelerateDecelerateInterpolator());
        wheel.scroll(-(numbers.size()*2+1), 3000+spinDelay);
    }

    // Wheel scrolled listener
    OnWheelScrollListener scrolledListener = new OnWheelScrollListener() {
        @Override
        public void onScrollingStarted(WheelView wheel) {
            new Handler().postDelayed(()->{wheel.setVisibility(View.VISIBLE);},350);
            if(starting) {
                touchBackground.setEnabled(false);
                new Handler().postDelayed(() -> {
                    EventBus.getDefault().post(new ScrollStart(pingoNumber));
                }, 300);
            }
        }
        @Override
        public void onScrollingFinished(WheelView wheel) {
            int currentNumber = wheel.getCurrentItem();
            currentPingo = (Integer) wheel.getViewAdapter().getItem(currentNumber, null, null).getId();
            // init scroll
            if (starting){
                touchBackground.setEnabled(true);
                //winning window
                if(pingoState.equals(PingoState.WIN)){
                    currentPingo = guessedNumber;
                    wheel.setCurrentItem(getNumberIndex(currentPingo),true);
                    Glide.with(getActivity()).load(R.drawable.green_window).diskCacheStrategy( DiskCacheStrategy.NONE )
                            .skipMemoryCache( true ).into(windowBackground);
                    disableWindow();
                    fishka.setImageResource(greenFishkas[currentPingo]);
                    fishka.setVisibility(View.VISIBLE);
                }
                else{
                    //if first try and last window then initiate blink sequence
                    if (pingoNumber == 4 && Game.attemptCounter ==4){
                        EventBus.getDefault().post(new BlinkEvent(1));
                    }
                    else if (hasFinger){
                        fingerTimer.start();
                    }
                }
                starting = false;
            }
            //touch scroll
            else {
                fishka.setImageResource(greenFishkas[currentPingo]);
                rockFishka(fishka);
                if(!pingoState.equals(PingoState.GAMEOVER)) {
                    EventBus.getDefault().post(new PingoEvent(pingoNumber, currentPingo));
                }
            }
            EventBus.getDefault().post(new ScrollEnd(pingoNumber));
        }
    };

    private void rockFishka(ImageView fishka) {
        AnimatorSet rockplay = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.anim.fishka_rockplay);
        fishka.setVisibility(View.VISIBLE);
        rockplay.setTarget(fishka);
        rockplay.start();
    }

    public void disableWindow() {
        touchBackground.setOnClickListener(null);
        pingoState = PingoState.INACTIVE;
    }

    // Wheel changed listener
    private OnWheelChangedListener changedListener = new OnWheelChangedListener() {
        @Override
        public void onChanged(WheelView wheel, int oldValue, int newValue) {
            if(!starting){
                removeNumber(numbers, 10);
            }
        }
    };

    /**
     * Slot machine adapter
     */
    private class SlotMachineAdapter extends AbstractWheelAdapter {
        /**
         * Constructor
         */
        public SlotMachineAdapter() {
        }

        @Override
        public int getItemsCount() {
            return numbers.size();
        }

        @Override
        public View getItem(int index, View cachedView, ViewGroup parent) {
            ImageView img = numbers.get(index);
            return img;
        }
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
            putFinger();
        }
    }

    private ImageView loadNumber(int tagNumber, int image, int width, int height){

        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.gravity = Gravity.CENTER;
        ImageView imageNumber = new ImageView(getActivity());
        imageNumber.setId(tagNumber);
        imageNumber.setLayoutParams(params);

        Glide.with(this).load(image).diskCacheStrategy( DiskCacheStrategy.NONE )
                .skipMemoryCache( true ).into(imageNumber);

        return imageNumber;
    }

    private void removeNumber(List<ImageView> numbers, int tagNumberToRemove){
        Iterator<ImageView> numbersIter = numbers.iterator();
        while (numbersIter.hasNext()){
            if(numbersIter.next().getId() == tagNumberToRemove){
                numbersIter.remove();              // it will remove element from collection
            }
        }
    }

    private int getNumberIndex(int pinNUmber){
        int index = 0;
        int i = 0;
        for (ImageView numberView: numbers){
            if (numberView.getId() == pinNUmber){
                index = i;
                break;
            }
            i++;
        }
        return index;
    }

    public int getPingoNumber() {
        return pingoNumber;
    }

    public Integer getCurrentPingo() {
        return currentPingo;
    }

    @Subscribe
    public void onBlinkEvent(BlinkEvent event){
        if(event.getPingoNumber() != pingoNumber){
            Glide.with(this).load(R.drawable.blue_window).diskCacheStrategy( DiskCacheStrategy.NONE )
                    .skipMemoryCache( true ).into(windowBackground);
            //if all windows blinked
            if(event.getPingoNumber() > 4){
                if (hasFinger){
                    fingerTimer.start();
                }
            }
        }
        else{
            Glide.with(this).load(R.drawable.blue_window_blink).diskCacheStrategy( DiskCacheStrategy.NONE )
                    .skipMemoryCache( true ).into(windowBackground);
            new Handler().postDelayed(()->{
                EventBus.getDefault().post(new BlinkEvent(pingoNumber+1));
            },100);
        }
    }

    @Subscribe
    public void onSpinEvent(SpinEvent event){
        if(event.getPingoNumber() != pingoNumber){
            touchBackground.setEnabled(true);
        }
    }

    @Subscribe
    public void onInitTouchBackground(InitBackgroundEvent event){
        touchBackground.setEnabled(true);
    }

    @Subscribe
    public void onBonusPin(BonusPinEvent event){

        touchBackground.setEnabled(false);
        windowBackground.setImageDrawable(getResources().getDrawable(R.drawable.yellow_regular_bright,null));
    }

    @Subscribe
    public void flashWin(WinFlashEvent event){
        touchBackground.setEnabled(true);
        if (event.getWindow() == pingoNumber) {
            windowBackground.setImageDrawable(getResources().getDrawable(R.drawable.win_animation_endgame,null));
            AnimationDrawable winAnimation = (AnimationDrawable) windowBackground.getDrawable();
            long totalDuration = 0;
            for(int i = 0; i< winAnimation.getNumberOfFrames();i++){
                totalDuration += winAnimation.getDuration(i);
            }
            winAnimation.start();
        }
    }

    @Subscribe
    public void numberRorate(NumberRorateEvent event){
        if (event.getWindow() == pingoNumber){
            ObjectAnimator animation = ObjectAnimator.ofFloat(wheel,"rotationY", 0,360);
            animation.setDuration(1000);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            animation.start();
            new Handler().postDelayed(()->{animation.start();},4000);
        }
    }

    @Subscribe
    public void spin(NumberSpinEvent event){

        if (event.getPingoNumber() == pingoNumber) {

            ViewGroup.LayoutParams pingoParams = mainView.getLayoutParams();
            pingoParams.height = (int)(pingoParams.height * 1.06);
            pingoParams.width = (int)(pingoParams.width * 1.06);
            mainView.setZ(zOrder +1);
            zoomedIn = true;

            guessedNumber = event.getNumberGuesed();
            touchBackground.setEnabled(false);
            EventBus.getDefault().post(new SpinEvent(pingoNumber));

            ImageView spin = (ImageView) getView().findViewById(R.id.spin);
            spin.setVisibility(View.VISIBLE);
            wheel.setVisibility(View.INVISIBLE);

            int currentNumber = wheel.getCurrentItem();
            ImageView viw = numbers.get(currentNumber);
            spin.setImageDrawable(viw.getDrawable());

            //spin cycle
            windowBackground.setImageDrawable(getResources().getDrawable(R.drawable.spin_animation,null));
            AnimationDrawable spinAnimation = (AnimationDrawable) windowBackground.getDrawable();
            new Handler().postDelayed(()->{
                spinAnimation.start();
            },445);

            //stop spin
            new Handler().postDelayed(() -> {
                spinAnimation.stop();
                if(guessedNumber != null){
                    Game.guessedCount++;
                    pingoState = PingoState.WIN;
                    touchBackground.setOnClickListener(null);
                    EventBus.getDefault().post(new GuessedNumberEvent(pingoNumber));
                }
                else{
                    Glide.with(this).load(R.drawable.red_window).diskCacheStrategy( DiskCacheStrategy.NONE )
                            .skipMemoryCache( true ).into(windowBackground);
                    fishka.setImageResource(redFishkas[ numbers.get(currentNumber).getId()]);
                    EventBus.getDefault().post(new NoGuessedNumberEvent(pingoNumber));
                }
            }, 5450);

            //restore window state
            new Handler().postDelayed(() -> {
                spin.setVisibility(View.INVISIBLE);
                wheel.setVisibility(View.VISIBLE);
                spin.setBackground(null);

                EventBus.getDefault().post(new NumberSpinEndEvent(pingoNumber,guessedNumber != null));

            }, 5500);
        }
    }

    @Subscribe
    public void onStopSpin(NumberStopSpinEvent event){

        if(zoomedIn){
            mainView.setZ(zOrder);
            ViewGroup.LayoutParams pingoParams = mainView.getLayoutParams();
            pingoParams.height = (int)(pingoParams.height / 1.06);
            pingoParams.width = (int)(pingoParams.width / 1.06);

            zoomedIn = false;
        }
    }

    public boolean isGuessedNumber() {
        return pingoState.equals(PingoState.WIN);
    }

    public void showWinPin(int pinNumber){
        pingoState = PingoState.GAMEOVER;
        wheel.setInterpolator(new AccelerateDecelerateInterpolator());
        wheel.setCurrentItem(getNumberIndex(pinNumber),true);
    }

    @Subscribe
    public void doWinAnimation(WinAnimation event){

        if(event.getPingoNumber() == pingoNumber) {

            ObjectAnimator animation = ObjectAnimator.ofFloat(wheel,"rotationY", 0,360);
            animation.setDuration(1000);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            new Handler().postDelayed(()->{animation.start();},1000);
            new Handler().postDelayed(()->{animation.start();},4000);

            int background;
            if(event.getColor().equals(WinAnimation.colorType.GREEN)){
                background = R.drawable.win_animation;
            }
            else{
                background = R.drawable.bonuspin_animation;
            }
            windowBackground.setImageDrawable(getResources().getDrawable(background, null));
            AnimationDrawable winAnimation = (AnimationDrawable) windowBackground.getDrawable();
            long totalDuration = 0;
            for (int i = 0; i < winAnimation.getNumberOfFrames(); i++) {
                totalDuration += winAnimation.getDuration(i);
            }

            //blink
            winAnimation.start();

            new Handler().postDelayed(()->{EventBus.getDefault().post(new NumberStopSpinEvent(pingoNumber));},totalDuration);

        }
    }

    private void scaleUi(View view) {

        // scale the screen
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
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

        //scale play
        ImageView playIcon = (ImageView) view.findViewById(R.id.play);
        ViewGroup.LayoutParams playParams = playIcon.getLayoutParams();
        playParams.width =(int)(newBmapWidth*0.1041F);
        playParams.height =(int)(newBmapHeight*0.07428F);

        //scale fishka
        ImageView fishkaIcon = (ImageView) view.findViewById(R.id.fishka);
        ViewGroup.LayoutParams fishkaParams = fishkaIcon.getLayoutParams();
        fishkaParams.width =(int)(newBmapWidth*0.1199F);
        fishkaParams.height =(int)(newBmapHeight*0.0835F);

        //scale spin
        ImageView spin = (ImageView) view.findViewById(R.id.spin);
        ViewGroup.LayoutParams spinParams = spin.getLayoutParams();
        spinParams.width =(int)(newBmapWidth*0.1513F*1.18);
        spinParams.height =(int)(newBmapHeight*0.2588F*1.18);

        //scale window
        float pingoSize = 0.3203F;
        ImageView window = (ImageView) mainView.findViewById(R.id.window_background);
        ViewGroup.LayoutParams pingoParams = window.getLayoutParams();
        pingoParams.height = (int)(newBmapHeight*pingoSize);
        pingoParams.width = (int)(newBmapHeight*pingoSize);
    }
}
