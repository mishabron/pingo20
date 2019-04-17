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
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.mbronshteyn.pingo20.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PingoWindow extends Fragment {

    private ImageView windowBackground;
    private AnimatorSet rockplay;
    private ImageView play;

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
    }

    @Override
    public void onStart() {
        super.onStart();
        scaleUi();
    }

    public void putFinger() {

        windowBackground.setBackground(getResources().getDrawable(R.drawable.finger_animation));
        play = (ImageView) getView().findViewById(R.id.play);
        play.setVisibility(View.VISIBLE);

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

        List<Drawable> wheel = new ArrayList<>();
        wheel.add(getResources().getDrawable(R.drawable.zero));
        wheel.add(getResources().getDrawable(R.drawable.one));
        wheel.add(getResources().getDrawable(R.drawable.two));
        wheel.add(getResources().getDrawable(R.drawable.three));
        wheel.add(getResources().getDrawable(R.drawable.four));
        wheel.add(getResources().getDrawable(R.drawable.five));

        ImageView numberPick = (ImageView) getView().findViewById(R.id.numberPick);
        numberPick.setVisibility(View.VISIBLE);

        new Handler().postDelayed(()->{
            for(int i = 0; i < wheel.size()-1 ; i++){

                Drawable currentNumber = wheel.get(i);
                Drawable nextNumber = wheel.get(i + 1);
                flipNumbers(numberPick, currentNumber,nextNumber);

            }},delay);
    }

    private boolean flipNumbers(ImageView numberPick,Drawable currentNumber, Drawable nextNumber){

        final boolean[] result = {false};

        Animation slideToBottom = AnimationUtils.loadAnimation(getActivity(), R.anim.to_bottom);

        numberPick.setImageDrawable(currentNumber);

        slideToBottom.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                Animation slideFromTop = AnimationUtils.loadAnimation(getActivity(), R.anim.from_top);
                slideFromTop.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        result[0] = true;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                numberPick.setImageDrawable(nextNumber);
                numberPick.startAnimation(slideFromTop);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        numberPick.startAnimation(slideToBottom);

        return result[0];
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

        //scale number pick
        ImageView numberPick1 = (ImageView) getView().findViewById(R.id.numberPick);
        ViewGroup.LayoutParams numberPickParams1 = numberPick1.getLayoutParams();
        numberPickParams1.width =(int)(newBmapWidth*0.1354F);
        numberPickParams1.height =(int)(newBmapHeight*0.2599F);

    }

}
