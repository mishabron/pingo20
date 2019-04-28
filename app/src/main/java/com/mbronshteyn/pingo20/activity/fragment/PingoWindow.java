package com.mbronshteyn.pingo20.activity.fragment;


import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.mbronshteyn.pingo20.R;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelClickedListener;
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

    public PingoWindow() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pingo_window, container, false);

        windowBackground = (ImageView) view.findViewById(R.id.window_background);

        touchBackground = (ImageView) view.findViewById(R.id.touchBackground);
        touchBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wheel.scroll(-1 , 600);
                wheel.getCurrentItem();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        wheel = getView().findViewById(R.id.slot);

        wheel.setViewAdapter(new SlotMachineAdapter(getActivity()));
        wheel.setCurrentItem(0,true);

        wheel.addChangingListener(changedListener);
        wheel.addScrollingListener(scrolledListener);
        wheel.setCyclic(true);
        wheel.setEnabled(false);
        wheel.setDrawShadows(false);

        scaleUi();
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
                wheel.scroll(-1 , 600);
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
            if (starting){
                //wheel.setCurrentItem(0,true);
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
        // Image size
        final int IMAGE_WIDTH = 320;
        final int IMAGE_HEIGHT = 320;

        // Slot machine symbols
        private final int items[] = new int[] {
                R.drawable.zero,
                R.drawable.nine,
                R.drawable.eight,
                R.drawable.seven,
                R.drawable.six,
                R.drawable.five,
                R.drawable.four,
                R.drawable.three,
                R.drawable.two,
                R.drawable.one,
                R.drawable.pingo
        };

        // Cached images
        private List<SoftReference<Bitmap>> images;

        // Layout inflater
        private Context context;

        /**
         * Constructor
         */
        public SlotMachineAdapter(Context context) {
            this.context = context;
            images = new ArrayList<SoftReference<Bitmap>>(items.length);
            for (int id : items) {
                images.add(new SoftReference<Bitmap>(loadImage(id)));
            }
        }

        /**
         * Loads image from resources
         */
        private Bitmap loadImage(int id) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id);
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, IMAGE_WIDTH, IMAGE_HEIGHT, true);
            bitmap.recycle();
            return scaled;
        }

        @Override
        public int getItemsCount() {
            return items.length;
        }

        // Layout params for image view
        final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(IMAGE_WIDTH, IMAGE_HEIGHT);

        @Override
        public View getItem(int index, View cachedView, ViewGroup parent) {
            ImageView img;
            if (cachedView != null) {
                img = (ImageView) cachedView;
            } else {
                img = new ImageView(context);
            }
            img.setLayoutParams(params);
            SoftReference<Bitmap> bitmapRef = images.get(index);
            Bitmap bitmap = bitmapRef.get();
            if (bitmap == null) {
                bitmap = loadImage(items[index]);
                images.set(index, new SoftReference<Bitmap>(bitmap));
            }
            img.setImageBitmap(bitmap);

            return img;
        }
    }

    private void scaleUi() {

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

        int newBmapWidth = (int) (bmapWidth * ratioMultiplier);
        int newBmapHeight = (int) (bmapHeight * ratioMultiplier);

        //scale play
        ImageView playIcon = (ImageView) getView().findViewById(R.id.play);
        ViewGroup.LayoutParams playParams = playIcon.getLayoutParams();
        playParams.width =(int)(newBmapWidth*0.1041F);
        playParams.height =(int)(newBmapHeight*0.07428F);

    }

}
