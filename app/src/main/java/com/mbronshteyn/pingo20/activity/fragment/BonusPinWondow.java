package com.mbronshteyn.pingo20.activity.fragment;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mbronshteyn.pingo20.R;
import com.mbronshteyn.pingo20.events.LuckySevenEvent;
import com.mbronshteyn.pingo20.events.NumberStopSpinEvent;
import com.mbronshteyn.pingo20.events.ScrollEnd;
import com.mbronshteyn.pingo20.events.SpinEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.AbstractWheelAdapter;

public class BonusPinWondow  extends Fragment {

    private WheelView wheel;
    private View mainView;
    private List<ImageView> numbers;
    private int newBmapWidth;
    private int newBmapHeight;
    private Integer currentPingo;
    private int pingoNumber;
    private ArrayList<ImageView> pingoNumbers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_bonuspin_window, container, false);

        scaleUi(mainView);
        
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

    @Subscribe
    public void onSpinEvent(SpinEvent event){

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

        wheel = getView().findViewById(R.id.bonusSlot);

        wheel.setViewAdapter(new BonusPinWondow.SlotMachineAdapter());
        wheel.setCurrentItem(0,true);

        wheel.addScrollingListener(scrolledListener);
        wheel.setCyclic(true);
        wheel.setEnabled(false);
        wheel.setDrawShadows(false);
        wheel.setCurrentItem(11);
        wheel.setVisibility(View.INVISIBLE);
    }

    public void initPingo(){
        wheel.setInterpolator(new AccelerateDecelerateInterpolator());
        wheel.scroll(-(numbers.size()*2), 3000+pingoNumber*200);
    }

    public void spinPingo(){
        Random rand = new Random();
        new Handler().postDelayed(()->{
            wheel.setInterpolator(new AccelerateDecelerateInterpolator());
            wheel.scroll(-(numbers.size()*pingoNumber*1+(rand.nextInt(10+pingoNumber))), 2000+(pingoNumber*1000));
        },0);
    }

    private List<ImageView> loadPingoNumbers() {

        pingoNumbers = new ArrayList<>();

        switch (pingoNumber){
            case 1:
                pingoNumbers.add(loadNumber(0,R.drawable.bonus_7,(int)(newBmapWidth*0.158F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(1,R.drawable.bonuspin_chip,(int)(newBmapWidth*0.1650F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(2,R.drawable.bonus_7,(int)(newBmapWidth*0.158F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(3,R.drawable.bonuspin_chip,(int)(newBmapWidth*0.1650F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(4,R.drawable.bonus_7,(int)(newBmapWidth*0.158F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(5,R.drawable.bonuspin_chip,(int)(newBmapWidth*0.1650F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(6,R.drawable.bonus_7,(int)(newBmapWidth*0.158F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(7,R.drawable.bonuspin_chip,(int)(newBmapWidth*0.1650F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(8,R.drawable.bonus_7,(int)(newBmapWidth*0.158F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(9,R.drawable.bonuspin_chip,(int)(newBmapWidth*0.1650F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(10,R.drawable.bonus_7,(int)(newBmapWidth*0.158F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(11,R.drawable.bonuspin_chip,(int)(newBmapWidth*0.1650F), (int)(newBmapHeight*0.2888F)));
                break;

            case 2:
                pingoNumbers.add(loadNumber(0,R.drawable.bonus_7,(int)(newBmapWidth*0.158F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(1,R.drawable.bonuspin_chip,(int)(newBmapWidth*0.1650F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(2,R.drawable.bonuspin_chip,(int)(newBmapWidth*0.1650F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(3,R.drawable.bonus_7,(int)(newBmapWidth*0.158F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(4,R.drawable.bonuspin_chip,(int)(newBmapWidth*0.1650F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(5,R.drawable.bonuspin_chip,(int)(newBmapWidth*0.1650F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(6,R.drawable.bonus_7,(int)(newBmapWidth*0.158F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(7,R.drawable.bonuspin_chip,(int)(newBmapWidth*0.1650F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(8,R.drawable.bonuspin_chip,(int)(newBmapWidth*0.1650F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(9,R.drawable.bonus_7,(int)(newBmapWidth*0.158F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(10,R.drawable.bonuspin_chip,(int)(newBmapWidth*0.1650F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(11,R.drawable.bonuspin_chip,(int)(newBmapWidth*0.1650F), (int)(newBmapHeight*0.2888F)));
                break;

            case 3:
                pingoNumbers.add(loadNumber(0,R.drawable.bonus_7,(int)(newBmapWidth*0.158F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(1,R.drawable.bonuspin_chip,(int)(newBmapWidth*0.1650F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(2,R.drawable.bonuspin_chip,(int)(newBmapWidth*0.1650F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(3,R.drawable.bonuspin_chip,(int)(newBmapWidth*0.1650F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(4,R.drawable.bonus_7,(int)(newBmapWidth*0.158F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(5,R.drawable.bonuspin_chip,(int)(newBmapWidth*0.1650F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(6,R.drawable.bonuspin_chip,(int)(newBmapWidth*0.1650F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(7,R.drawable.bonuspin_chip,(int)(newBmapWidth*0.1650F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(8,R.drawable.bonus_7,(int)(newBmapWidth*0.158F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(9,R.drawable.bonuspin_chip,(int)(newBmapWidth*0.1650F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(10,R.drawable.bonuspin_chip,(int)(newBmapWidth*0.1650F), (int)(newBmapHeight*0.2888F)));
                pingoNumbers.add(loadNumber(11,R.drawable.bonuspin_chip,(int)(newBmapWidth*0.1650F), (int)(newBmapHeight*0.2888F)));
                break;
        }
        return pingoNumbers;
    }

    private ImageView loadNumber(int tagNumber, int image, int width, int height){

        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.gravity = Gravity.CENTER;
        ImageView imageNumber = new ImageView(getActivity());
        imageNumber.setId(tagNumber);
        imageNumber.setLayoutParams(params);

        Glide.with(this).load(image).diskCacheStrategy( DiskCacheStrategy.NONE )
                .skipMemoryCache( true ).into(imageNumber);
        imageNumber.setTag(image);

        return imageNumber;
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

    // Wheel scrolled listener
    OnWheelScrollListener scrolledListener = new OnWheelScrollListener() {
        @Override
        public void onScrollingStarted(WheelView wheel) {
            new Handler().postDelayed(()->{wheel.setVisibility(View.VISIBLE);},350);
        }
        @Override
        public void onScrollingFinished(WheelView wheel) {
            int currentNumber = wheel.getCurrentItem();
            currentPingo = (Integer) wheel.getViewAdapter().getItem(currentNumber, null, null).getId();
            EventBus.getDefault().post(new ScrollEnd(pingoNumber));

            //animate lucky seven
            ImageView window = pingoNumbers.get(currentPingo);
            if((int)window.getTag() == R.drawable.bonus_7){
                ImageView spin = (ImageView) mainView.findViewById(R.id.seven);
                spin.setVisibility(View.VISIBLE);
                AnimationDrawable winAnimation = (AnimationDrawable) spin.getDrawable();
                long totalDuration = 0;
                for (int i = 0; i < winAnimation.getNumberOfFrames(); i++) {
                    totalDuration += winAnimation.getDuration(i);
                }
                //blink
                winAnimation.start();
                new Handler().postDelayed(()->{ spin.setVisibility(View.INVISIBLE); },totalDuration);
                winAnimation.start();
                EventBus.getDefault().post(new LuckySevenEvent(pingoNumber));
            }
        }
    };

    private void scaleUi(View mainView) {

        // scale the screen
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.bonuspin_background, options);
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

        //scale spin
        ImageView spin = (ImageView) mainView.findViewById(R.id.seven);
        ViewGroup.LayoutParams spinParams = spin.getLayoutParams();
        spinParams.width =(int)(newBmapWidth*0.158F);
        spinParams.height =(int)(newBmapHeight*0.2888F);
    }
}
