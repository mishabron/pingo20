package com.mbronshteyn.pingo20.activity;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.mbronshteyn.gameserver.dto.game.AuthinticateDto;
import com.mbronshteyn.gameserver.dto.game.CardDto;
import com.mbronshteyn.pingo20.R;
import com.mbronshteyn.pingo20.model.Game;
import com.mbronshteyn.pingo20.network.PingoRemoteService;

import org.apache.commons.lang3.StringUtils;

import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FreeGameActivity extends PingoActivity{

    private Application context;
    private Retrofit retrofit;
    private AuthinticateDto dto;
    private FreeGameActivity activity;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_free_game);
        
        context = getApplication();
        activity = this;

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        dto = new AuthinticateDto();;
        dto.setCardNumber(card.getCardNumber());
        dto.setDeviceId(Game.devicedId);
        dto.setGame(Game.getGAMEID());
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setContentView(R.layout.activity_free_game);

        ImageView freeGameBaloon = (ImageView) findViewById(R.id.freegame_baloon);
        //Glide.with(this).load(R.drawable.freegame_animation).diskCacheStrategy( DiskCacheStrategy.NONE ).skipMemoryCache( true ).into(freeGameBaloon);
        freeGameBaloon.setImageDrawable(getResources().getDrawable(R.drawable.freegame_animation,null));
        ImageView exl = (ImageView) findViewById(R.id.excl);

        //animation delay
        new Handler().postDelayed(()->{
            ImageView glass = (ImageView) findViewById(R.id.glass);
            glass.setVisibility(View.VISIBLE);
            Animation glassAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_inglass);
            glass.startAnimation(glassAnimation);

            freeGameBaloon.setVisibility(View.VISIBLE);
            Animation freeGameBaloonAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in);
            freeGameBaloonAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {
                    glass.setVisibility(View.INVISIBLE);
                    AnimationDrawable freeGameAnimation = (AnimationDrawable) freeGameBaloon.getDrawable();
                    freeGameAnimation.start();
                    long totalDuration = 0;
                    for(int i = 0; i< freeGameAnimation.getNumberOfFrames();i++){
                        totalDuration += freeGameAnimation.getDuration(i);
                    }
                    freeGameAnimation.start();
                    new Handler().postDelayed(()->{
                        exl.setVisibility(View.VISIBLE);
                        freeGameBaloon.setImageDrawable(getResources().getDrawable(R.drawable.freegame_baloon_noexlm,null));
                        RotateAnimation rotateSpin = new RotateAnimation(0, -360*3, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        rotateSpin.setDuration(1000);
                        exl.startAnimation(rotateSpin);
                    },totalDuration);
                }
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            freeGameBaloon.startAnimation(freeGameBaloonAnimation);
        },300);

        new Handler().postDelayed(()->{
            final PingoRemoteService service = retrofit.create(PingoRemoteService.class);

            Call<CardDto> call = service.authinticate(dto);
            call.enqueue(new Callback<CardDto>() {
                @Override
                public void onResponse(Call<CardDto> call, Response<CardDto> response) {
                    processResponse(response);
                }

                @Override
                public void onFailure(Call<CardDto> call, Throwable t) {
                    playSound(R.raw.error_short);
                }
            });
            
        },5000);
    }

    private void processResponse(Response<CardDto> response) {

        Headers headers = response.headers();
        if(StringUtils.isEmpty(headers.get("errorCode"))) {
            ImageView glass = (ImageView) findViewById(R.id.glass);
            glass.clearAnimation();
            glass.setVisibility(View.VISIBLE);
            Animation glassAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_inglass);
            glassAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {
                    card = response.body();
                    Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    activity.finish();
                    Runtime.getRuntime().gc();
                }
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            glass.startAnimation(glassAnimation);
        }else{

        }
    }
}
