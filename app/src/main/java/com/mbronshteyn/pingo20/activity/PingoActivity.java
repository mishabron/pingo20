package com.mbronshteyn.pingo20.activity;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.mbronshteyn.gameserver.dto.game.CardDto;
import com.mbronshteyn.gameserver.dto.game.HitDto;
import com.mbronshteyn.pingo20.R;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


public class PingoActivity extends AppCompatActivity {

    protected static MediaPlayer mediaPlayer;
    protected static String baseUrl = "http://ec2-18-219-33-9.us-east-2.compute.amazonaws.com";
    protected static CardDto card;
    protected ImageView rightSmallBaloon;

    protected void stopPlaySound() {

        try {
            try {
                if (mediaPlayer != null && (mediaPlayer.isPlaying() || mediaPlayer.isLooping())) {

                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
            } catch (Exception e) {

            }
        } catch (IllegalStateException e) {

        }
    }

    protected void playSound(int sound) {

        stopPlaySound();

        mediaPlayer = MediaPlayer.create(getApplicationContext(), sound);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
        });
        mediaPlayer.start();
    }

    protected void popBaloon(final ImageView ballon, int duration){

        ballon.setVisibility(View.VISIBLE);
        Animation zoomIntAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in);
        ballon.startAnimation(zoomIntAnimation);

        new Handler().postDelayed(()-> {
                Animation zoomIntAnimationOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_out);
                ballon.startAnimation(zoomIntAnimationOut);
                ballon.setVisibility(View.INVISIBLE);
        }, duration);
    }

    protected void popBaloon(final ImageView ballon){

        ballon.setVisibility(View.VISIBLE);
        Animation zoomIntAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in);
        ballon.startAnimation(zoomIntAnimation);
    }

    protected boolean isWinningCard(){
         boolean winning = false;

        List<HitDto> hits = card.getHits();

        boolean win1 = false;
        boolean win2 = false;
        boolean win3 = false;
        boolean win4 = false;

        for(HitDto hit: hits){
            win1 = !win1 ? hit.getNumber_1().isGuessed() : true;
            win2 = !win2 ? hit.getNumber_2().isGuessed() : true;
            win3 = !win3 ? hit.getNumber_3().isGuessed() : true;
            win4 = !win4 ? hit.getNumber_4().isGuessed() : true;
        }

        if(win1 && win2 && win3 && win4){
            winning = true;
        }

        return winning;
    }

    public Integer loadNumberGuessed(int pingoNumber) {

        Integer guessed = null;
        List<HitDto> hits = card.getHits();
        for(HitDto hit :hits){
            if(guessed != null){
                break;
            }
            switch(pingoNumber){
                case 1:
                    if(hit.getNumber_1().isGuessed()){
                        guessed = hit.getNumber_1().getNumber();
                    };
                    break;
                case 2:
                    if(hit.getNumber_2().isGuessed()){
                        guessed = hit.getNumber_2().getNumber();
                    };
                    break;
                case 3:
                    if(hit.getNumber_3().isGuessed()){
                        guessed = hit.getNumber_3().getNumber();
                    };
                    break;
                case 4:
                    if(hit.getNumber_4().isGuessed()){
                        guessed = hit.getNumber_4().getNumber();
                    };
                    break;
            }
        }
        return guessed;
    }
}
