package com.mbronshteyn.pingo20.activity.fragment;


import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.mbronshteyn.pingo20.R;
import com.mbronshteyn.pingo20.events.NumberSpinEndEvent;
import com.mbronshteyn.pingo20.events.NumberSpinEvent;
import com.mbronshteyn.pingo20.events.PingoEvent;
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
    private int[] readFishkas = new int[10];;

    public PingoWindow() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pingo_window, container, false);
        windowBackground = (ImageView) view.findViewById(R.id.window_background);

        fishka = (ImageView) view.findViewById(R.id.fishka);

        fingerTimer = new FingerTimer(700,100);

        touchBackground = (ImageView) view.findViewById(R.id.touchBackground);
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

        readFishkas[0] = R.drawable.red0;
        readFishkas[1] = R.drawable.red1;
        readFishkas[2] = R.drawable.red2;
        readFishkas[3] = R.drawable.red3;
        readFishkas[4] = R.drawable.red4;
        readFishkas[5] = R.drawable.red4;
        readFishkas[6] = R.drawable.red6;
        readFishkas[7] = R.drawable.red7;
        readFishkas[8] = R.drawable.red8;
        readFishkas[9] = R.drawable.red9;

        scaleUi(view);
        Glide.with(this).load(R.drawable.blue_window).into(windowBackground);

        return view;
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
                Glide.with(getActivity()).load(R.drawable.blue_window).into(windowBackground);
                play.setVisibility(View.INVISIBLE);
                wheel.setVisibility(View.VISIBLE);
                touchBackground.setVisibility(View.VISIBLE);
                return false;
            }
        });

        AnimationDrawable fingerAnimation = (AnimationDrawable) windowBackground.getDrawable();
        long totalDuration = 0;
        for(int i = 0; i< fingerAnimation.getNumberOfFrames();i++){
            totalDuration += fingerAnimation.getDuration(i);
        }

        //first tap
        fingerAnimation.start();
        new Handler().postDelayed(()->{
            AnimatorSet rockplay = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.anim.rockplay);
            rockplay.setTarget(play);
            rockplay.start();
        },fingerAnimation.getDuration(0));

        //second tap
        new Handler().postDelayed(()-> {
            fingerAnimation.stop();
            fingerAnimation.start();
        },totalDuration+ 2000);
        new Handler().postDelayed(()->{
            AnimatorSet rockplay = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.anim.rockplay);
            rockplay.setTarget(play);
            rockplay.start();
        },totalDuration+2000+fingerAnimation.getDuration(0));
    }

    public void initPingo(Bundle pingoBundle) {

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
            wheel.setCurrentItem(0);
            Glide.with(this).load(R.drawable.blue_window).into(windowBackground);
        }

        wheel.setInterpolator(new AccelerateDecelerateInterpolator());
        wheel.scroll(-(numbers.size()*2+1), 3000+spinDelay);
    }

    // Wheel scrolled listener
    OnWheelScrollListener scrolledListener = new OnWheelScrollListener() {
        @Override
        public void onScrollingStarted(WheelView wheel) {

        }
        @Override
        public void onScrollingFinished(WheelView wheel) {
            int currentNumber = wheel.getCurrentItem();
            currentPingo = (Integer) wheel.getViewAdapter().getItem(currentNumber, null, null).getId();
            if (starting){

                if(pingoState.equals(PingoState.WIN)){
                    currentPingo = guessedNumber;
                    wheel.setCurrentItem(getNumberIndex(currentPingo),true);
                    Glide.with(getActivity()).load(R.drawable.green_window).into(windowBackground);
                    disableWindow();
                    fishka.setImageResource(greenFishkas[currentPingo]);
                    fishka.setVisibility(View.VISIBLE);
                }
                else{
                    if (hasFinger){
                        fingerTimer.start();
                    }
                }
                starting = false;
            }
            else{
                fishka.setImageResource(greenFishkas[currentPingo]);
                fishka.setVisibility(View.VISIBLE);
                EventBus.getDefault().post(new PingoEvent(pingoNumber,currentPingo));
            }
        }
    };

    public void disableWindow() {
        touchBackground.setOnClickListener(null);
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

    public void setEnabled(boolean enabled) {

    }

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

        Glide.with(this).load(image).into(imageNumber);

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
    public void spin(NumberSpinEvent event){

        if (event.getPingoNumber() == pingoNumber) {

            guessedNumber = event.getNumberGuesed();

            double zoomScale = 1.08;
            touchBackground.setEnabled(false);

            ViewGroup.LayoutParams pingoParams = event.getPingo().getLayoutParams();
            int originalSize = pingoParams.height;
            //pingoParams.height = (int)(pingoParams.height * zoomScale);
            //pingoParams.width = (int)(pingoParams.width * zoomScale);
            //float zOrder = event.getPingo().getZ();
            //event.getPingo().setZ(20);

            ImageView spin = (ImageView) getView().findViewById(R.id.spin);
            spin.setVisibility(View.VISIBLE);
            wheel.setVisibility(View.INVISIBLE);
            ViewGroup.LayoutParams spinParams = spin.getLayoutParams();
            int spinWidth = spinParams.width;
            int spinHeight = spinParams.height;

            int currentNumber = wheel.getCurrentItem();
            ImageView viw = numbers.get(currentNumber);
            spin.setImageDrawable(viw.getDrawable());

/*            //start initial move
            RotateAnimation rotateLeft = new RotateAnimation(10, -25, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateLeft.setDuration(500);
            rotateLeft.setInterpolator(new AnticipateOvershootInterpolator());
            spin.startAnimation(rotateLeft);*/

            //spin cycle
            new Handler().postDelayed(()->{

                //Glide.with(getActivity()).load(R.drawable.spin_cycle).into(spin);
                //spinParams.width = (int)(pingoParams.width * zoomScale);
                //spinParams.height = (int)(pingoParams.height * zoomScale);

                RotateAnimation rotateSpin = new RotateAnimation(20, -360 * 25, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotateSpin.setDuration(7000);
                rotateSpin.setInterpolator(new AccelerateDecelerateInterpolator());
                spin.startAnimation(rotateSpin);
            },450);

            //stop spin
            new Handler().postDelayed(() -> {

                //spinParams.width = spinWidth;
                //spinParams.height = spinHeight;

                if(guessedNumber != null){
                    pingoState = PingoState.WIN;
                    Glide.with(this).load(R.drawable.green_window).into(windowBackground);
                    touchBackground.setOnClickListener(null);
                }
                else{
                    Glide.with(this).load(R.drawable.red_window).into(windowBackground);
                    fishka.setImageResource(readFishkas[ numbers.get(currentNumber).getId()]);
                }

/*                spin.setImageDrawable(viw.getDrawable());
                RotateAnimation rotateEnd = new RotateAnimation(-30, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotateEnd.setDuration(500);
                spin.startAnimation(rotateEnd);*/

            }, 7400);

            //restore window state
            new Handler().postDelayed(() -> {
                touchBackground.setEnabled(true);
                //event.getPingo().setZ(zOrder);
                spin.setVisibility(View.INVISIBLE);
                wheel.setVisibility(View.VISIBLE);
                spin.setBackground(null);
                //pingoParams.height = originalSize;
                //pingoParams.width = originalSize;

                EventBus.getDefault().post(new NumberSpinEndEvent(pingoNumber,guessedNumber != null));

            }, 7900);
        }
    }

    public boolean isGuessedNumber() {
        return pingoState.equals(PingoState.WIN);
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
    }
}
