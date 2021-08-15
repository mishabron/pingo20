package com.mbronshteyn.pingo20.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
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

    private Retrofit retrofit;
    private AuthinticateDto dto;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_game);

        retrofit = new Retrofit.Builder()
                .baseUrl(PingoRemoteService.baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        dto = new AuthinticateDto();;
        dto.setCardNumber(card.getCardNumber());
        dto.setDeviceId(Game.devicedId);
        dto.setGame(Game.getGAMEID());

        scaleUi();
    }

    @Override
    protected void onResume() {
        super.onResume();

        ImageView starts = (ImageView) findViewById(R.id.freeGameStars);
        AnimationDrawable starsAnimation = (AnimationDrawable) starts.getDrawable();
        starsAnimation.start();

        ImageView exl = (ImageView) findViewById(R.id.excl);

        new Handler().postDelayed(()->{
            ImageView glass = (ImageView) findViewById(R.id.glass);
            glass.setVisibility(View.VISIBLE);
            Animation glassAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_inglass);
            glass.startAnimation(glassAnimation);
        },500);

        new Handler().postDelayed(()->{ playSound(R.raw.free_game);},1000);

        new Handler().postDelayed(()->{

            ImageView logo3 = (ImageView) findViewById(R.id.freegame_baloon);
            logo3.setVisibility(View.VISIBLE);
            Animation logoPopup3 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_rotate_bonus);
            logoPopup3.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    exl.setVisibility(View.VISIBLE);
                    RotateAnimation rotateSpin = new RotateAnimation(0, -360*3, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    rotateSpin.setDuration(1000);
                    exl.startAnimation(rotateSpin);
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            logo3.startAnimation(logoPopup3);
        },250);

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
        },6000);
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
                    Activity activity = (Activity) FreeGameActivity.this;
                    activity.finish();
                    Runtime.getRuntime().gc();
                }
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            glass.startAnimation(glassAnimation);
        }
    }

    public void scaleUi() {

        // scale the screen
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.free_game_background, options);
        float bmapHeight = options.outHeight;
        float bmapWidth  = options.outWidth;

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

        //scale background
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.freeGameLayout);
        ImageView iView = (ImageView) findViewById(R.id.freeGameBacgroundimage);
        ConstraintSet set = new ConstraintSet();
        set.clone(layout);
        set.constrainHeight(iView.getId(), newBmapHeight);
        set.constrainWidth(iView.getId(), newBmapWidth);
        set.applyTo(layout);

        //scale popup
        ImageView freeGame = (ImageView) findViewById(R.id.freegame_baloon);
        ViewGroup.LayoutParams freeGameParams = freeGame.getLayoutParams();
        freeGameParams.width =(int)(newBmapWidth*0.7875F);
        freeGameParams.height =(int)(newBmapHeight*0.3413F);

        //scale stars
        ImageView freeGameStars = (ImageView) findViewById(R.id.freeGameStars);
        ViewGroup.LayoutParams freeGameStarsParams = freeGameStars.getLayoutParams();
        freeGameStarsParams.width =(int)(newBmapWidth);
        freeGameStarsParams.height =(int)(newBmapHeight);
    }
}
