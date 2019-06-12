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
import com.mbronshteyn.pingo20.R;

import java.io.IOException;


public class PingoActivity extends AppCompatActivity {

    protected static MediaPlayer mediaPlayer;
    protected static String baseUrl = "http://ec2-18-219-33-9.us-east-2.compute.amazonaws.com";
    protected static CardDto card;
    protected ImageView rightSmallBaloon;

    protected void playSound(int sound) {

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
}
