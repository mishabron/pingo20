package com.mbronshteyn.pingo20.activity.fragment;


import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
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

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class PingoWindow extends Fragment {

    private ImageView windowBackground;
    private AnimatorSet rockplay;
    private ImageView play;
    private ViewFlipper numberFlipper;
    private ImageView pingoView;

    public PingoWindow() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pingo_window, container, false);

        windowBackground = (ImageView) view.findViewById(R.id.window_background);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scaleUi();
        numberFlipper = (ViewFlipper) getView().findViewById(R.id.number_flipper);

        numberFlipper.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(numberFlipper.getChildCount() == 11){
                    numberFlipper.removeViewAt(0);
                }
                numberFlipper.showNext();
                return false;
            }
        });

        pingoView = (ImageView)numberFlipper.getChildAt(0);
    }

    public void putFinger() {

        numberFlipper.setVisibility(View.INVISIBLE);

        windowBackground.setBackground(getResources().getDrawable(R.drawable.finger_animation,null));
        play = (ImageView) getView().findViewById(R.id.play);
        play.setVisibility(View.VISIBLE);

        windowBackground.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                numberFlipper.setVisibility(View.VISIBLE);
                windowBackground.setOnTouchListener(null);
                windowBackground.setBackground(getResources().getDrawable(R.drawable.blue_window,null));
                play.setVisibility(View.INVISIBLE);
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

    public void spinWheel(int delay,int duration) {

        new Handler().postDelayed(()->{
            numberFlipper.stopFlipping();
            numberFlipper.setDisplayedChild(0);
        },delay+duration);

        new Handler().postDelayed(()->{
            numberFlipper.startFlipping();
        },delay);
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

        //scale number picks
        ImageView numberPick = (ImageView) getView().findViewById(R.id.numberPick0);
        ViewGroup.LayoutParams numberPickParams = numberPick.getLayoutParams();
        numberPickParams.width =(int)(newBmapWidth*0.1354F);
        numberPickParams.height =(int)(newBmapHeight*0.2599F);

        numberPick = (ImageView) getView().findViewById(R.id.numberPick1);
        numberPickParams = numberPick.getLayoutParams();
        numberPickParams.width =(int)(newBmapWidth*0.1354F);
        numberPickParams.height =(int)(newBmapHeight*0.2599F);

        numberPick = (ImageView) getView().findViewById(R.id.numberPick2);
        numberPickParams = numberPick.getLayoutParams();
        numberPickParams.width =(int)(newBmapWidth*0.1354F);
        numberPickParams.height =(int)(newBmapHeight*0.2599F);

        numberPick = (ImageView) getView().findViewById(R.id.numberPick3);
        numberPickParams = numberPick.getLayoutParams();
        numberPickParams.width =(int)(newBmapWidth*0.1354F);
        numberPickParams.height =(int)(newBmapHeight*0.2599F);

        numberPick = (ImageView) getView().findViewById(R.id.numberPick4);
        numberPickParams = numberPick.getLayoutParams();
        numberPickParams.width =(int)(newBmapWidth*0.1354F);
        numberPickParams.height =(int)(newBmapHeight*0.2599F);

        numberPick = (ImageView) getView().findViewById(R.id.numberPick5);
        numberPickParams = numberPick.getLayoutParams();
        numberPickParams.width =(int)(newBmapWidth*0.1354F);
        numberPickParams.height =(int)(newBmapHeight*0.2599F);

        numberPick = (ImageView) getView().findViewById(R.id.numberPick6);
        numberPickParams = numberPick.getLayoutParams();
        numberPickParams.width =(int)(newBmapWidth*0.1354F);
        numberPickParams.height =(int)(newBmapHeight*0.2599F);

        numberPick = (ImageView) getView().findViewById(R.id.numberPick7);
        numberPickParams = numberPick.getLayoutParams();
        numberPickParams.width =(int)(newBmapWidth*0.1354F);
        numberPickParams.height =(int)(newBmapHeight*0.2599F);

        numberPick = (ImageView) getView().findViewById(R.id.numberPick8);
        numberPickParams = numberPick.getLayoutParams();
        numberPickParams.width =(int)(newBmapWidth*0.1354F);
        numberPickParams.height =(int)(newBmapHeight*0.2599F);

        numberPick = (ImageView) getView().findViewById(R.id.numberPick9);
        numberPickParams = numberPick.getLayoutParams();
        numberPickParams.width =(int)(newBmapWidth*0.1354F);
        numberPickParams.height =(int)(newBmapHeight*0.2599F);

        numberPick = (ImageView) getView().findViewById(R.id.pingo);
        numberPickParams = numberPick.getLayoutParams();
        numberPickParams.width =(int)(newBmapWidth*0.1354F);
        numberPickParams.height =(int)(newBmapHeight*0.2599F);

    }

}
