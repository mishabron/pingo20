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
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.mbronshteyn.pingo20.R;
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
    private boolean starting = true;
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
                wheel.scroll(-1 , 600);
                fingerTimer.cancel();
                if(fishka.getVisibility() == View.VISIBLE) {
                    //Animation zoomIntAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.zoom_out);
                    //fishka.startAnimation(zoomIntAnimation);
                    fishka.setVisibility(View.INVISIBLE);
                }
            }
        });

        numbers = new ArrayList<>();

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

        numbers.add(loadNumber(0,R.drawable.zero,getActivity(), (int)(newBmapWidth*0.1313F), (int)(newBmapHeight*0.2888F)));
        numbers.add(loadNumber(9,R.drawable.nine,getActivity(), (int)(newBmapWidth*0.1313F), (int)(newBmapHeight*0.2888F)));
        numbers.add(loadNumber(8,R.drawable.eight,getActivity(), (int)(newBmapWidth*0.1313F), (int)(newBmapHeight*0.2888F)));
        numbers.add(loadNumber(7,R.drawable.seven,getActivity(), (int)(newBmapWidth*0.1313F), (int)(newBmapHeight*0.2888F)));
        numbers.add(loadNumber(6,R.drawable.six,getActivity(), (int)(newBmapWidth*0.1313F), (int)(newBmapHeight*0.2888F)));
        numbers.add(loadNumber(5,R.drawable.five,getActivity(), (int)(newBmapWidth*0.1313F), (int)(newBmapHeight*0.2888F)));
        numbers.add(loadNumber(4,R.drawable.four,getActivity(), (int)(newBmapWidth*0.1313F), (int)(newBmapHeight*0.2888F)));
        numbers.add(loadNumber(3,R.drawable.three,getActivity(),(int)(newBmapWidth*0.1313F), (int)(newBmapHeight*0.2888F)));
        numbers.add(loadNumber(2,R.drawable.two,getActivity(), (int)(newBmapWidth*0.1313F), (int)(newBmapHeight*0.2888F)));
        numbers.add(loadNumber(1,R.drawable.one,getActivity(), (int)(newBmapWidth*0.1313F), (int)(newBmapHeight*0.2888F)));
        numbers.add(loadNumber(10,R.drawable.pingo,getActivity(),(int)(newBmapWidth*0.1604F),(int)(newBmapWidth*0.1604F)));

        wheel = getView().findViewById(R.id.slot);

        wheel.setViewAdapter(new SlotMachineAdapter());
        wheel.setCurrentItem(0,true);

        wheel.addChangingListener(changedListener);
        wheel.addScrollingListener(scrolledListener);
        wheel.setCyclic(true);
        wheel.setEnabled(false);
        wheel.setDrawShadows(false);
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
        hasFinger = pingoBundle.getBoolean("hasFibger");
        pingoState = (PingoState)pingoBundle.getSerializable("pingoState");

        ArrayList<Integer> playedNumbers = pingoBundle.getIntegerArrayList("playedNumbers");
        for(Integer playedNumber: playedNumbers){
            removeNumber(numbers,playedNumber);
        }

        new Handler().postDelayed(()->{wheel.scroll(-(numbers.size()*2+1), 4000);},pingoBundle.getInt("spinDelay"));
    }

    // Wheel scrolled listener
    OnWheelScrollListener scrolledListener = new OnWheelScrollListener() {
        @Override
        public void onScrollingStarted(WheelView wheel) {

        }
        @Override
        public void onScrollingFinished(WheelView wheel) {
            int currentNumber = wheel.getCurrentItem();
            currentPingo = (Integer) wheel.getViewAdapter().getItem(currentNumber, null, null).getTag();
            if (starting){
                if (hasFinger){
                    fingerTimer.start();
                }
                starting = false;
            }
            else{
                fishka.setImageResource(greenFishkas[currentPingo]);
                fishka.setVisibility(View.VISIBLE);
                EventBus.getDefault().post(new PingoEvent(pingoNumber,currentPingo));
                removeNumber(numbers,10);
            }
        }
    };

    // Wheel changed listener
    private OnWheelChangedListener changedListener = new OnWheelChangedListener() {
        @Override
        public void onChanged(WheelView wheel, int oldValue, int newValue) {
            if (!starting) {
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

    private ImageView loadNumber(int tagNumber, int image, Context context, int width, int height){

        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.gravity = Gravity.CENTER;
        ImageView imageNumber = new ImageView(context);
        imageNumber.setId(tagNumber);
        imageNumber.setLayoutParams(params);

        Glide.with(this).load(image).into(imageNumber);

        imageNumber.setTag(tagNumber);

        return imageNumber;
    }

    private void removeNumber(List<ImageView> numbers, int tagNumberToRemove){
        Iterator<ImageView> numbersIter =numbers.iterator();
        while (numbersIter.hasNext()){
            if(numbersIter.next().getTag().equals(tagNumberToRemove)){
                numbersIter.remove();              // it will remove element from collection
            }
        }
    }

    public int getPingoNumber() {
        return pingoNumber;
    }

    public Integer getCurrentPingo() {
        return currentPingo;
    }

    @Subscribe
    public void onPingoEventMessage(PingoEvent event) {

    }

    public void spin(){

        ImageView spin = (ImageView) getView().findViewById(R.id.spin);
        spin.setVisibility(View.VISIBLE);
        wheel.setVisibility(View.INVISIBLE);
        spin.setBackground(getResources().getDrawable(R.drawable.spin_animation1,null));
        AnimationDrawable spinAnimation = (AnimationDrawable) spin.getBackground();
        spinAnimation.start();

        new Handler().postDelayed(()->{
            spin.setBackground(getResources().getDrawable(R.drawable.spin09,null));
            RotateAnimation rotate = new RotateAnimation(360*30, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(7500);
            rotate.setInterpolator(new AccelerateDecelerateInterpolator());
            spin.startAnimation(rotate);
        },570);

        new Handler().postDelayed(()->{
            spin.setBackground(getResources().getDrawable(R.drawable.spin_animation3,null));
            ((AnimationDrawable) spin.getBackground()).start();
        },7500);

        new Handler().postDelayed(()->{
            spin.setVisibility(View.INVISIBLE);
            wheel.setVisibility(View.VISIBLE);
            spin.setBackground(null);
        },8000);
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
        spinParams.width =(int)(newBmapWidth*0.1513F);
        spinParams.height =(int)(newBmapHeight*0.2588F);

    }

}
