package com.mbronshteyn.pingo20.activity.fragment;


import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.ImageView;

import com.mbronshteyn.pingo20.R;

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
        scaleUi();
    }

    public void setWindowBackground(ImageView windowBackground) {
        this.windowBackground = windowBackground;
    }

    public ImageView getWindowBackground() {
        return windowBackground;
    }

    public void putFinger() {

        windowBackground.setBackground(getResources().getDrawable(R.drawable.finger_animation));
        play = (ImageView) getActivity().findViewById(R.id.play);
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
        },7000);

        new Handler().postDelayed(()->{
            rockplay = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.anim.rockplay);
            rockplay.setTarget(play);
            rockplay.start();
        },totalDuration);
    }

    public void scaleUi() {

        // scale the screen
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        BitmapDrawable bmap = (BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.blue_window, null);
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

    }
}
