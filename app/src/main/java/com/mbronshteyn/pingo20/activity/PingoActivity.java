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


public class PingoActivity extends AppCompatActivity {

    public static  String devicedId = "35"
            + // we make this look like a valid IMEI
            Build.BOARD.length() % 10 + Build.BRAND.length() % 10 + Build.SUPPORTED_ABIS.length % 10 + Build.DEVICE.length() % 10
            + Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 + Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10
            + Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 + Build.TAGS.length() % 10 + Build.TYPE.length() % 10
            + Build.USER.length() % 10; // 13 digits;

    public static final String GAMEID = "Pingo";

    protected static MediaPlayer mediaPlayer;

    protected CardDto card;

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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation zoomIntAnimationOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_out);
                ballon.startAnimation(zoomIntAnimationOut);
            }
        }, duration);
    }
}
