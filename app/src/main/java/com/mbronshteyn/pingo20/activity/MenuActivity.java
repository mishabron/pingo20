package com.mbronshteyn.pingo20.activity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.mbronshteyn.gameserver.dto.game.AuthinticateDto;
import com.mbronshteyn.gameserver.dto.game.CardDto;
import com.mbronshteyn.gameserver.dto.game.HistoryDto;
import com.mbronshteyn.pingo20.R;
import com.mbronshteyn.pingo20.activity.fragment.BonusesFragment;
import com.mbronshteyn.pingo20.activity.fragment.HistoryFragment;
import com.mbronshteyn.pingo20.activity.fragment.RulesFragment;
import com.mbronshteyn.pingo20.model.Game;
import com.mbronshteyn.pingo20.network.PingoRemoteService;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MenuActivity extends PingoActivity {

    private RulesFragment rulesFragment;
    private BonusesFragment bonusesFragment;
    private HistoryFragment historyFragment;
    private Retrofit retrofit;
    private HistoryDto hits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        double payouts[] = {card.getPayout1(),card.getPayout2(),card.getPayout3()};
        rulesFragment = RulesFragment.newInstance(payouts);
        bonusesFragment = BonusesFragment.newInstance(null,null);
        historyFragment = new HistoryFragment();

        //close button
        ImageView menuButton = (ImageView) findViewById(R.id.closeButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isOKToInit = false;
                finish();
            }
        });

        Button rulesButton = (Button) findViewById(R.id.rulesButton);
        rulesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.menuFragment,rulesFragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        Button bonusesButton = (Button) findViewById(R.id.bonusesButton);
        bonusesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.menuFragment,bonusesFragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        Button historyButton = (Button) findViewById(R.id.historyButton);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle = new Bundle();
                bundle.putSerializable("hits", hits);
                historyFragment.setArguments(bundle);

                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.menuFragment,historyFragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        scaleUi();

        retrofit = new Retrofit.Builder()
                .baseUrl(PingoRemoteService.baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        AuthinticateDto dto = new AuthinticateDto();
        ;
        dto.setCardNumber(card.getCardNumber());
        dto.setDeviceId(Game.devicedId);
        dto.setGame(Game.getGAMEID());
        
        new Handler().postDelayed(()->{
            final PingoRemoteService service = retrofit.create(PingoRemoteService.class);

            Call<HistoryDto> call = service.getHistory(dto);
            call.enqueue(new Callback<HistoryDto>() {
                @Override
                public void onResponse(Call<HistoryDto> call, Response<HistoryDto> response) {
                    processResponse(response);
                }

                @Override
                public void onFailure(Call<HistoryDto> call, Throwable t) {
                    playSound(R.raw.error_short);
                }
            });

        },5000);
    }

    private void processResponse(Response<HistoryDto> response) {

        Headers headers = response.headers();
        if(StringUtils.isEmpty(headers.get("errorCode"))) {
            hits = response.body();
        }else{

        }
    }

    @Override
    public void onBackPressed() {
        isOKToInit = false;
        finish();
    }

    public void scaleUi() {

        // scale the screen
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.menu_background, options);
        float bmapHeight = options.outHeight;
        float bmapWidth = options.outWidth;

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
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.coordinatorLayoutMenu);
        ImageView iView = (ImageView) findViewById(R.id.menuBacgroundimageView);
        ConstraintSet set = new ConstraintSet();
        set.clone(layout);
        set.constrainHeight(iView.getId(), newBmapHeight);
        set.constrainWidth(iView.getId(), newBmapWidth);
        set.applyTo(layout);

    }
}
