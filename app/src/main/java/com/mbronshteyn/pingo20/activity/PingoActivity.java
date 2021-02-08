package com.mbronshteyn.pingo20.activity;

import android.content.res.AssetFileDescriptor;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.mbronshteyn.gameserver.dto.game.CardDto;
import com.mbronshteyn.gameserver.dto.game.HitDto;
import com.mbronshteyn.pingo20.R;
import com.mbronshteyn.pingo20.model.Game;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PingoActivity extends AppCompatActivity {

    public MediaPlayer mediaPlayer1 = new MediaPlayer();;
    public MediaPlayer mediaPlayer2 = new MediaPlayer();
    protected static CardDto card;
    protected ImageView rightSmallBaloon;
    protected SoundPool soundPool;
    public static Map<Integer,Integer> soundMap = new HashMap<>();
    public static Map<Integer,Integer> soundsInPlayMap = new HashMap<>();
    public ImageView progressCounter;
    public AnimationDrawable dotsProgress;
    private boolean playingBackground;
    public static float volume =1;
    protected static boolean isOKToInit = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        soundPool = new SoundPool.Builder().setMaxStreams(5).build();

        //load all sounds
        int soundId = soundPool.load(this, R.raw.button, 1);
        soundMap.put(R.raw.button,soundId);
        soundId = soundPool.load(this, R.raw.pop_login, 1);
        soundMap.put(R.raw.pop_login,soundId);
        soundId = soundPool.load(this, R.raw.screen_down, 1);
        soundMap.put(R.raw.screen_down,soundId);
        soundId = soundPool.load(this, R.raw.error_short, 1);
        soundMap.put(R.raw.error_short,soundId);
        soundId = soundPool.load(this, R.raw.comix_page_short, 1);
        soundMap.put(R.raw.comix_page_short,soundId);
        soundId = soundPool.load(this, R.raw.knocking_on_glass, 1);
        soundMap.put(R.raw.knocking_on_glass,soundId);
        soundId = soundPool.load(this, R.raw.right_number, 1);
        soundMap.put(R.raw.right_number,soundId);
        soundId = soundPool.load(this, R.raw.short_button_turn, 1);
        soundMap.put(R.raw.short_button_turn,soundId);
        soundId = soundPool.load(this, R.raw.wheel_spinning, 1);
        soundMap.put(R.raw.wheel_spinning,soundId);
        soundId = soundPool.load(this, R.raw.wheel_stop, 1);
        soundMap.put(R.raw.wheel_stop,soundId);
        soundId = soundPool.load(this, R.raw.wrong_number, 1);
        soundMap.put(R.raw.wrong_number,soundId);
        soundId = soundPool.load(this, R.raw.luckyseven, 1);
        soundMap.put(R.raw.luckyseven,soundId);
        soundId = soundPool.load(this, R.raw.bonus_nowin, 1);
        soundMap.put(R.raw.bonus_nowin,soundId);
        soundId = soundPool.load(this, R.raw.jackpot, 1);
        soundMap.put(R.raw.jackpot,soundId);
        soundId = soundPool.load(this, R.raw.trans_to_777, 1);
        soundMap.put(R.raw.trans_to_777,soundId);
        soundId = soundPool.load(this, R.raw.bonusspin_1, 1);
        soundMap.put(R.raw.bonusspin_1,soundId);
        soundId = soundPool.load(this, R.raw.bonusspin_2, 1);
        soundMap.put(R.raw.bonusspin_2,soundId);
        soundId = soundPool.load(this, R.raw.bonusspin_3, 1);
        soundMap.put(R.raw.bonusspin_3,soundId);
        soundId = soundPool.load(this, R.raw.bonusspin_4, 1);
        soundMap.put(R.raw.bonusspin_4,soundId);
        soundId = soundPool.load(this, R.raw.right_number_winner, 1);
        soundMap.put(R.raw.right_number_winner,soundId);
        soundId = soundPool.load(this, R.raw.wrong_try_again, 1);
        soundMap.put(R.raw.wrong_try_again,soundId);
        soundId = soundPool.load(this, R.raw.token_try_again, 1);
        soundMap.put(R.raw.token_try_again,soundId);
        soundId = soundPool.load(this, R.raw.wrong_last, 1);
        soundMap.put(R.raw.wrong_last,soundId);
        soundId = soundPool.load(this, R.raw.token_last, 1);
        soundMap.put(R.raw.token_last,soundId);
        soundId = soundPool.load(this, R.raw.spin, 1);
        soundMap.put(R.raw.spin,soundId);
        soundId = soundPool.load(this, R.raw.half_way, 1);
        soundMap.put(R.raw.half_way,soundId);
        soundId = soundPool.load(this, R.raw.atm, 1);
        soundMap.put(R.raw.atm,soundId);
        soundId = soundPool.load(this, R.raw.card_check_win3, 1);
        soundMap.put(R.raw.card_check_win3,soundId);
        soundId = soundPool.load(this, R.raw.gotobonus, 1);
        soundMap.put(R.raw.gotobonus,soundId);

    }

    @Override
    public void onBackPressed() {
        isOKToInit = false;
        stopPplayInBackground();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPplayInBackground();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        int soundPlaying = soundPool.play(soundId, volume, volume, 0, 0, 1);
        soundsInPlayMap.put(sound,soundPlaying);
    }

    protected void playInBackgroundIfNotPlaying(int sound){
        if(!playingBackground){
            playInBackground(sound);
        }
    }

    protected void playInBackground(int sound){

        final AssetFileDescriptor afd = getResources().openRawResourceFd(sound);
        mediaPlayer1 = MediaPlayer.create(this,sound);
        mediaPlayer2 = MediaPlayer.create(this,sound);
        mediaPlayer1.setVolume(volume,volume);
        mediaPlayer2.setVolume(volume,volume);

        mediaPlayer1.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer2.setAudioStreamType(AudioManager.STREAM_MUSIC);

        playingBackground = true;

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

    protected void stopPplayInBackground(){

        playingBackground = false;

        if(mediaPlayer1 != null && mediaPlayer1.isPlaying()){
            mediaPlayer1.stop();
        }
        if(mediaPlayer2 != null && mediaPlayer2.isPlaying()){
            mediaPlayer2.stop();
        }
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

    public String getCardReward(){

        String reward = "";

        if(card.getBalance() != 0) {
            reward = getString(R.string.card_balance) + " $" +(int) card.getBalance()+ " ";
        }
        else if(Game.attemptCounter == 0){
            reward = "GAME OVER ";
        }
        else{
            reward = "WIN FREE GAME ";
        }

        return reward;
    }

    public void doProgress(boolean startProgress){
        if(startProgress){
            progressCounter.setVisibility(View.VISIBLE);
            dotsProgress.start();
        }else{
            progressCounter.setVisibility(View.INVISIBLE);
            dotsProgress.stop();
        }
    }

    public void muteAudio(){
        volume  = 0;
        Collection<Integer> soundsInPlay = soundsInPlayMap.values();
        for(Integer sound: soundsInPlay){
            soundPool.stop(sound);
        }
        Collection<Integer> sounds = soundMap.values();
        for(Integer sound: sounds){
            soundPool.setVolume(sound,volume,volume);
        }
        mediaPlayer1.stop();
        mediaPlayer2.stop();
        mediaPlayer1.setVolume(volume,volume);
        mediaPlayer2.setVolume(volume,volume);
    }

    public void unMuteAudio(){
        volume = 1;
        Collection<Integer> sounds = soundMap.values();
        for(Integer sound: sounds) {
            soundPool.setVolume(sound, volume, volume);
        }
        mediaPlayer1.setVolume(volume,volume);
        mediaPlayer2.setVolume(volume,volume);
    }
    
}
