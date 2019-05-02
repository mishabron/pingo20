package com.mbronshteyn.pingo20.activity.fragment;


import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mbronshteyn.pingo20.R;
import com.mbronshteyn.pingo20.events.PingoEvent;

import org.greenrobot.eventbus.EventBus;

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
    private AnimatorSet rockplay;
    private ImageView play;
    private WheelView wheel;
    private boolean starting = true;
    private ImageView touchBackground;
    private List<ImageView> numbers;
    private int currentNumber;
    private int pingoNumber;
    private boolean hasFinger;
    private int newBmapWidth;
    private int newBmapHeight;
    private FishkaTimer fishkaTimer;
    private ImageView fishka;
    private int[] fishkas = new int[10];
    private Integer currentPingo;

    public PingoWindow() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pingo_window, container, false);

        windowBackground = (ImageView) view.findViewById(R.id.window_background);

        fishkaTimer = new FishkaTimer(3000,1000);
        fishka = (ImageView) view.findViewById(R.id.fishka);

        touchBackground = (ImageView) view.findViewById(R.id.touchBackground);
        touchBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wheel.scroll(-1 , 600);
                fishkaTimer.cancel();
                fishkaTimer.start();
                if(fishka.getVisibility() == View.VISIBLE) {
                    Animation zoomIntAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.short_fade_out);
                    fishka.startAnimation(zoomIntAnimation);
                    fishka.setVisibility(View.INVISIBLE);
                }
                removeNumber(numbers,10);
                //EventBus.getDefault().post(new PingoEvent());
            }
        });

        numbers = new ArrayList<>();

        fishkas[0] = R.drawable.green0;
        fishkas[1] = R.drawable.green1;
        fishkas[2] = R.drawable.green2;
        fishkas[3] = R.drawable.green3;
        fishkas[4] = R.drawable.green4;
        fishkas[5] = R.drawable.green5;
        fishkas[6] = R.drawable.green6;
        fishkas[7] = R.drawable.green7;
        fishkas[8] = R.drawable.green8;
        fishkas[9] = R.drawable.green9;

        scaleUi(view);

        return view;
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PingoParameters);
        pingoNumber = a.getInt(R.styleable.PingoParameters_pingoNumber,1);
        hasFinger = a.getBoolean(R.styleable.PingoParameters_hasFinger,false);
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

        windowBackground.setBackground(getResources().getDrawable(R.drawable.finger_animation,null));
        play = (ImageView) getView().findViewById(R.id.play);
        play.setVisibility(View.VISIBLE);

        windowBackground.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                windowBackground.setOnTouchListener(null);
                windowBackground.setBackground(getResources().getDrawable(R.drawable.blue_window,null));
                play.setVisibility(View.INVISIBLE);
                wheel.setVisibility(View.VISIBLE);
                touchBackground.setVisibility(View.VISIBLE);
                return false;
            }
        });

        AnimationDrawable fingerAnimation = (AnimationDrawable) windowBackground.getBackground();
        long totalDuration = 0;
        for(int i = 0; i< fingerAnimation.getNumberOfFrames();i++){
            totalDuration += fingerAnimation.getDuration(i);
        }
        fingerAnimation.start();

        new Handler().postDelayed(()->{
            rockplay = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.anim.rockplay);
            rockplay.setTarget(play);
            rockplay.start();
        },6000);

        new Handler().postDelayed(()->{
            rockplay = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.anim.rockplay);
            rockplay.setTarget(play);
            rockplay.start();
        },totalDuration);
    }

    public void spinWheel(int delay) {
        new Handler().postDelayed(()->{wheel.scroll(-100 , 2000);},delay);
    }

    // Wheel scrolled listener
    OnWheelScrollListener scrolledListener = new OnWheelScrollListener() {
        @Override
        public void onScrollingStarted(WheelView wheel) {

        }
        @Override
        public void onScrollingFinished(WheelView wheel) {
            currentNumber = wheel.getCurrentItem();
            currentPingo = (Integer) wheel.getViewAdapter().getItem(currentNumber, null, null).getTag();
            if (starting){
                if (hasFinger){
                    new Handler().postDelayed(()->{putFinger();},3000);
                }
                starting = false;
            }
        }
    };

    // Wheel changed listener
    private OnWheelChangedListener changedListener = new OnWheelChangedListener() {
        @Override
        public void onChanged(WheelView wheel, int oldValue, int newValue) {

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

    private class FishkaTimer extends CountDownTimer {

        public FishkaTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            fishka.setImageResource(fishkas[currentPingo]);
            fishka.setVisibility(View.VISIBLE);
            Animation zoomIntAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.short_fade_in);
            fishka.startAnimation(zoomIntAnimation);
        }
    }

    private ImageView loadNumber(int number, int image, Context context, int width, int height){

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), image);

        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.gravity = Gravity.CENTER;
        ImageView imageNumber = new ImageView(context);
        imageNumber.setImageBitmap(bitmap);
        imageNumber.setLayoutParams(params);
        imageNumber.setTag(number);

        return imageNumber;
    }

    private void removeNumber(List<ImageView> numbers, int numberToRemove){
        Iterator<ImageView> numbersIter =numbers.iterator();
        while (numbersIter.hasNext()){
            if(numbersIter.next().getTag().equals(numberToRemove)){
                numbersIter.remove();              // it will remove element from collection
            }
        }
    }

    public int getCurrentNumber() {
        return currentNumber;
    }

    private void scaleUi(View view) {

        // scale the screen
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        BitmapDrawable bmap = (BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.game_background, null);
        float bmapWidth = bmap.getBitmap().getWidth();
        float bmapHeight = bmap.getBitmap().getHeight();

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

    }

}
