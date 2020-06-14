package com.mbronshteyn.pingo20.activity.fragment;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mbronshteyn.pingo20.R;
import com.mbronshteyn.pingo20.events.ScrollEnd;
import com.mbronshteyn.pingo20.events.SelecForSpinEvent;
import com.mbronshteyn.pingo20.events.SpinEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.AbstractWheelAdapter;

public class BonusSpinWondow extends Fragment {

    private WheelView wheel;
    private View mainView;
    private List<ImageView> numbers;
    private int newBmapWidth;
    private int newBmapHeight;
    private Integer currentPingo;
    private float pingoHeight;
    private float pingoWidth;
    private boolean guessed;
    private int slotNumber;
    private int spinCount;
    private float numberHeight;
    private float numberWidth;

    public int getPingoNumber() {
        return pingoNumber;
    }

    private int pingoNumber;
    private ArrayList<ImageView> pingoNumbers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_bonusspin_window, container, false);

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

    @Subscribe
    public void onSelecForSpinEvent(SelecForSpinEvent event){
        ImageView yellowBackground = (ImageView) mainView.findViewById(R.id.yellowBonusSpinBackground);
        if(event.getPingo() == pingoNumber){
            yellowBackground.setVisibility(View.VISIBLE);
        }
        else{
            yellowBackground.setVisibility(View.INVISIBLE);
        }
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

        wheel = getView().findViewById(R.id.bonusSpinSlot);

        wheel.setViewAdapter(new BonusSpinWondow.SlotMachineAdapter());
        wheel.setCurrentItem(0,true);

        wheel.addScrollingListener(scrolledListener);
        wheel.setCyclic(true);
        wheel.setEnabled(false);
        wheel.setDrawShadows(false);
        wheel.setCurrentItem(1);
        wheel.setVisibility(View.INVISIBLE);
    }

    public void initPingo(Bundle pingoBundle){

        guessed = pingoBundle.getBoolean("guessed");

        if(!guessed) {
            wheel.setVisibility(View.VISIBLE);
            ArrayList<Integer> playedNumbers = pingoBundle.getIntegerArrayList("playedNumbers");
            for (Integer playedNumber : playedNumbers) {
                removeNumber(numbers, playedNumber);
                //remove corresponding pingo
                removeNumber(numbers, playedNumber+100);
            }
            Random r = new Random();
            slotNumber = Math.abs(r.nextInt()) % pingoNumbers.size();
            currentPingo = pingoNumbers.get(slotNumber).getId();

            wheel.setCurrentItem(1);
            wheel.setInterpolator(new AccelerateDecelerateInterpolator());
            new Handler().postDelayed(()->{wheel.scroll(-(numbers.size() * 2), 3000 + pingoNumber * 200);},1000);
        }
        else{
            currentPingo = (Integer)pingoBundle.getSerializable("guessedNumber");
            wheel.setVisibility(View.INVISIBLE);
            ImageView spin = (ImageView) mainView.findViewById(R.id.nonSpin);
            spin.setVisibility(View.VISIBLE);
        }
    }

    public void spinPingo(){

        if(!guessed) {
            wheel.addChangingListener(changedListener);
            Random rand = new Random();
            new Handler().postDelayed(() -> {
                wheel.setInterpolator(new AccelerateInterpolator());
                wheel.scroll(-(numbers.size()*8 + (rand.nextInt(10))), 8000);
            }, 0);
        }
    }

    private List<ImageView> loadPingoNumbers() {

        pingoNumbers = new ArrayList<>();

        pingoNumbers.add(loadNumber(0,R.drawable.zero,(int)(newBmapWidth*numberWidth), (int)(newBmapHeight*numberHeight)));
        pingoNumbers.add(loadNumber(100,R.drawable.pingo_spin,(int)(newBmapWidth*pingoWidth), (int)(newBmapHeight*pingoHeight)));
        pingoNumbers.add(loadNumber(1,R.drawable.one,(int)(newBmapWidth*numberWidth), (int)(newBmapHeight*numberHeight)));
        pingoNumbers.add(loadNumber(101,R.drawable.pingo_spin,(int)(newBmapWidth*pingoWidth), (int)(newBmapHeight*pingoHeight)));
        pingoNumbers.add(loadNumber(2,R.drawable.two,(int)(newBmapWidth*numberWidth), (int)(newBmapHeight*numberHeight)));
        pingoNumbers.add(loadNumber(102,R.drawable.pingo_spin,(int)(newBmapWidth*pingoWidth), (int)(newBmapHeight*pingoHeight)));
        pingoNumbers.add(loadNumber(3,R.drawable.three,(int)(newBmapWidth*numberWidth), (int)(newBmapHeight*numberHeight)));
        pingoNumbers.add(loadNumber(103,R.drawable.pingo_spin,(int)(newBmapWidth*pingoWidth), (int)(newBmapHeight*pingoHeight)));
        pingoNumbers.add(loadNumber(4,R.drawable.four,(int)(newBmapWidth*numberWidth), (int)(newBmapHeight*numberHeight)));
        pingoNumbers.add(loadNumber(104,R.drawable.pingo_spin,(int)(newBmapWidth*pingoWidth), (int)(newBmapHeight*pingoHeight)));
        pingoNumbers.add(loadNumber(5,R.drawable.five,(int)(newBmapWidth*numberWidth), (int)(newBmapHeight*numberHeight)));
        pingoNumbers.add(loadNumber(105,R.drawable.pingo_spin,(int)(newBmapWidth*pingoWidth), (int)(newBmapHeight*pingoHeight)));
        pingoNumbers.add(loadNumber(6,R.drawable.six,(int)(newBmapWidth*numberWidth), (int)(newBmapHeight*numberHeight)));
        pingoNumbers.add(loadNumber(106,R.drawable.pingo_spin,(int)(newBmapWidth*pingoWidth), (int)(newBmapHeight*pingoHeight)));
        pingoNumbers.add(loadNumber(7,R.drawable.seven,(int)(newBmapWidth*numberWidth), (int)(newBmapHeight*numberHeight)));
        pingoNumbers.add(loadNumber(107,R.drawable.pingo_spin,(int)(newBmapWidth*pingoWidth), (int)(newBmapHeight*pingoHeight)));
        pingoNumbers.add(loadNumber(8,R.drawable.eight,(int)(newBmapWidth*numberWidth), (int)(newBmapHeight*numberHeight)));
        pingoNumbers.add(loadNumber(108,R.drawable.pingo_spin,(int)(newBmapWidth*pingoWidth), (int)(newBmapHeight*pingoHeight)));
        pingoNumbers.add(loadNumber(9,R.drawable.nine,(int)(newBmapWidth*numberWidth), (int)(newBmapHeight*numberHeight)));
        pingoNumbers.add(loadNumber(109,R.drawable.pingo_spin,(int)(newBmapWidth*pingoWidth), (int)(newBmapHeight*pingoHeight)));;

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

        return imageNumber;
    }

    public Integer getCurrentPingo() {
        return currentPingo;
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
            ImageView blueBackground = (ImageView) getView().findViewById(R.id.blueSpinBackground);
            blueBackground.setVisibility(View.INVISIBLE);
        }
        @Override
        public void onScrollingFinished(WheelView wheel) {
            ImageView blueBackground = (ImageView) getView().findViewById(R.id.blueSpinBackground);
            blueBackground.setVisibility(View.VISIBLE);
            EventBus.getDefault().post(new ScrollEnd(pingoNumber));
        }
    };

    OnWheelChangedListener changedListener = new OnWheelChangedListener() {
        @Override
        public void onChanged(WheelView wheel, int oldValue, int newValue) {
            if (newValue == slotNumber && spinCount > 5) {
                wheel.stopScrolling();
            }
            else if (newValue == slotNumber){
                spinCount++;
            }
        }
    };

    private void removeNumber(List<ImageView> numbers, int tagNumberToRemove){
        Iterator<ImageView> numbersIter = numbers.iterator();
        while (numbersIter.hasNext()){
            if(numbersIter.next().getId() == tagNumberToRemove){
                numbersIter.remove();              // it will remove element from collection
            }
        }
    }

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

        pingoHeight = 0.2846F;
        pingoWidth = 0.1907F;
        numberHeight = pingoHeight;
        numberWidth = pingoWidth;

        //scale spin
        ImageView spin = (ImageView) mainView.findViewById(R.id.nonSpin);
        ViewGroup.LayoutParams spinParams = spin.getLayoutParams();
        spinParams.width =(int)(newBmapWidth*pingoWidth);
        spinParams.height =(int)(newBmapHeight*pingoHeight);

    }
}
