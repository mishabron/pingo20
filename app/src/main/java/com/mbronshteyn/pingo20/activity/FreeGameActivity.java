package com.mbronshteyn.pingo20.activity;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mbronshteyn.pingo20.R;

public class FreeGameActivity extends PingoActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_free_game);

        ImageView freeGameValoon = (ImageView) findViewById(R.id.freegame_baloon);

        //pop up baloon after first delay
        new Handler().postDelayed(()->{
            popBaloon(freeGameValoon);
        },1500);

        //start baloon animation after second delay
        new Handler().postDelayed(()->{
            freeGameValoon.setImageDrawable(getResources().getDrawable(R.drawable.freegame_animation,null));
            AnimationDrawable freeGameAnimation = (AnimationDrawable) freeGameValoon.getDrawable();
            long totalDuration = 0;
            for(int i = 0; i< freeGameAnimation.getNumberOfFrames();i++){
                totalDuration += freeGameAnimation.getDuration(i);
            }
            freeGameAnimation.start();
            new Handler().postDelayed(()->{
                Glide.with(this).load(R.drawable.freegame_baloon_noexlm).diskCacheStrategy( DiskCacheStrategy.NONE )
                        .skipMemoryCache( true ).into(freeGameValoon);
            },totalDuration);
        },1750);

    }
}
