package com.mbronshteyn.pingo20.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mbronshteyn.gameserver.dto.game.WinnerEmailDto;
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

public class WinEmailActivity extends PingoActivity {

    private WinEmailActivity context;
    private TextView balance;
    private Retrofit retrofit;
    private EditText email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_winemail);

        retrofit = new Retrofit.Builder()
                .baseUrl(PingoRemoteService.baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        context = this;

        Button goButton = (Button) findViewById(R.id.goButton);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendEmail();
            }
        });

        email = (EditText) findViewById(R.id.email);
        EditText emailConfirm = (EditText) findViewById(R.id.email_confirm);

        scaleUi();

    }

    private void sendEmail() {

        final PingoRemoteService service = retrofit.create(PingoRemoteService.class);

        WinnerEmailDto winnerEmailDto = new WinnerEmailDto();
        winnerEmailDto.setCardNumber(card.getCardNumber());
        winnerEmailDto.setDeviceId(Game.devicedId);
        winnerEmailDto.setGame(Game.GAMEID);
        winnerEmailDto.setEmail(email.getText().toString());

        Call<Void> call = service.saveEmail(winnerEmailDto);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                processResponse(response);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                playSound(R.raw.error_short);
            }
        });
    }

    private void processResponse(Response<Void> response) {

        Headers headers = response.headers();
        if(StringUtils.isEmpty(headers.get("errorCode"))) {
            Intent intent = new Intent(getApplicationContext(), EndOfGameActivity.class);
            startActivity(intent);
            Activity activity = (Activity) context;
            activity.finish();
        }else{

        }

    }

    public void scaleUi(){

        // scale the screen
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.winner_mail, options);
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

        //scale action18  button
        Button actionButtonGo = (Button) findViewById(R.id.goButton);
        int buttonSizeGo = (int) (newBmapHeight * 0.3406F);
        ViewGroup.LayoutParams buttonParamsGo = actionButtonGo.getLayoutParams();
        buttonParamsGo.height = buttonSizeGo;
        buttonParamsGo.width = buttonSizeGo;
    }

}
