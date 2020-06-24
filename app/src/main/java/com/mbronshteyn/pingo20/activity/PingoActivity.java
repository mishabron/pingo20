package com.mbronshteyn.pingo20.activity;

import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.mbronshteyn.gameserver.dto.game.CardDto;
import com.mbronshteyn.gameserver.dto.game.HitDto;
import com.mbronshteyn.pingo20.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PingoActivity extends AppCompatActivity {

    protected MediaPlayer mediaPlayer1;
    protected MediaPlayer mediaPlayer2;
    protected static CardDto card;
    protected ImageView rightSmallBaloon;
    protected SoundPool soundPool;
    protected static Map<Integer,Integer> soundMap = new HashMap<>();
    protected static Map<Integer,Integer> soundsInPlayMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        soundPool = new SoundPool.Builder().setMaxStreams(5).build();

        //load all sounds
        int soindId = soundPool.load(this, R.raw.button, 1);
        soundMap.put(R.raw.button,soindId);
        soindId = soundPool.load(this, R.raw.error_short, 1);
        soundMap.put(R.raw.error_short,soindId);
        soindId = soundPool.load(this, R.raw.knocking_on_glass, 1);
        soundMap.put(R.raw.knocking_on_glass,soindId);
        soindId = soundPool.load(this, R.raw.right_number, 1);
        soundMap.put(R.raw.right_number,soindId);
        soindId = soundPool.load(this, R.raw.short_button_turn, 1);
        soundMap.put(R.raw.short_button_turn,soindId);
        soindId = soundPool.load(this, R.raw.wheel_spinning, 1);
        soundMap.put(R.raw.wheel_spinning,soindId);
        soindId = soundPool.load(this, R.raw.wheel_stop, 1);
        soundMap.put(R.raw.wheel_stop,soindId);
        soindId = soundPool.load(this, R.raw.wrong_number, 1);
        soundMap.put(R.raw.wrong_number,soindId);
        soindId = soundPool.load(this, R.raw.luckyseven, 1);
        soundMap.put(R.raw.luckyseven,soindId);
        soindId = soundPool.load(this, R.raw.bonus_nowin, 1);
        soundMap.put(R.raw.bonus_nowin,soindId);
        soindId = soundPool.load(this, R.raw.bonus_background, 1);
        soundMap.put(R.raw.bonus_background,soindId);
        soindId = soundPool.load(this, R.raw.jackpot, 1);
        soundMap.put(R.raw.jackpot,soindId);
        soindId = soundPool.load(this, R.raw.bonusspin_1, 1);
        soundMap.put(R.raw.bonusspin_1,soindId);
    }

    @Override
    protected void onStop() {
        super.onStop();
        soundPool.release();
        soundPool = null;

        if(mediaPlayer1 != null) {
            mediaPlayer1.release();
            mediaPlayer1 = null;
        }
        if(mediaPlayer2 != null) {
            mediaPlayer2.release();
            mediaPlayer2 = null;
        }
    }

    protected void stopPlaySound(int sound) {

        Integer soundId = soundsInPlayMap.get(sound);

        if (soundId != null){
            soundPool.stop(soundId);
        }
    }

    protected void playSound(int sound) {

        Integer soundId = soundMap.get(sound);
        int soundPlaying = soundPool.play(soundId, 1, 1, 0, 0, 1);
        soundsInPlayMap.put(sound,soundPlaying);
    }

    protected void playSoundLoop(int sound) {

        Integer soundId = soundMap.get(sound);
        int soundPlaying = soundPool.play(soundId, 1, 1, 0, -1, 1);
        soundsInPlayMap.put(sound,soundPlaying);
    }

    protected void playInBackground(int sound){

        final AssetFileDescriptor afd = getResources().openRawResourceFd(sound);
        mediaPlayer1 = MediaPlayer.create(this,sound);
        mediaPlayer2 = MediaPlayer.create(this,sound);

        mediaPlayer1.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer2.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mediaPlayer1.start();
        mediaPlayer1.setNextMediaPlayer(mediaPlayer2);

        mediaPlayer1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.reset();
                try {
                    mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    mediaPlayer.prepare();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mediaPlayer2.setNextMediaPlayer(mediaPlayer1);
            }
        });

        mediaPlayer2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.reset();
                try {
                    mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    mediaPlayer.prepare();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mediaPlayer1.setNextMediaPlayer(mediaPlayer2);
            }
        });
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
    
    public int getGuessedCount(){

        int goessedCount = 0;

        for(HitDto hit :card.getHits()){
            if(hit.getNumber_1().isGuessed() || hit.getNumber_2().isGuessed() || hit.getNumber_3().isGuessed() || hit.getNumber_4().isGuessed()){
                goessedCount++;
            }

        }
        return goessedCount;
    }
    
}
