package com.mbronshteyn.pingo20.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

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
    private Retrofit retrofit;
    private EditText email;
    private Button goButton;
    private AnimatorSet mSetRightOut;
    private ImageView buttonBlank;
    private AnimatorSet mSetLeftIn;
    private EditText emailConfirm;
    private ImageView messageBaloon;

    @Override
    @SuppressLint("ResourceType")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_winemail);

        retrofit = new Retrofit.Builder()
                .baseUrl(PingoRemoteService.baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        context = this;

        
        
        goButton = (Button) findViewById(R.id.goButton);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!email.getText().toString().equals(emailConfirm.getText().toString())){
                    flipToBlank();
                    messageBaloon.setImageResource(R.drawable.bubble_email_error);
                }
                else {
                    playSound(R.raw.button);
                    messageBaloon.setImageResource(R.drawable.bubble_email_standby);
                    goButton.setEnabled(false);
                    doProgress(true);
                    new Handler().postDelayed(()->{sendEmail();},3500);
                }
            }
        });

        buttonBlank = (ImageView) findViewById(R.id.buttonBlank);

        goButton.setEnabled(false);

        int distance = 8000;
        float scale = getResources().getDisplayMetrics().density * distance;
        buttonBlank.setCameraDistance(scale);
        goButton.setCameraDistance(scale);

        mSetRightOut = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.anim.out_animation);
        mSetLeftIn = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.anim.in_animation);
        
        
        
        email = (EditText) findViewById(R.id.email);
        emailConfirm = (EditText) findViewById(R.id.email_confirm);
        email.setCursorVisible(false);
        emailConfirm.setCursorVisible(false);
        emailConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(email.getText().toString().equals(emailConfirm.getText().toString())){
                    flipToGo();
                }
            }
        });

        Typeface font = Typeface.createFromAsset(this.getAssets(), "fonts/badabb.ttf");
        email.setTypeface(font,Typeface.BOLD_ITALIC);
        emailConfirm.setTypeface(font,Typeface.BOLD_ITALIC);

        progressCounter = (ImageView) findViewById(R.id.progressCounter);
        dotsProgress = (AnimationDrawable) progressCounter.getDrawable();

        messageBaloon = (ImageView) findViewById(R.id.emailMessageBaloon);

        scaleUi();

    }

    public void flipToGo() {

        //close keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

        new Handler().postDelayed(()->{playSound(R.raw.short_button_turn);},200);

        mSetRightOut.setTarget(buttonBlank);
        mSetLeftIn.setTarget(goButton);
        mSetLeftIn.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                goButton.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        mSetRightOut.start();
        mSetLeftIn.start();
    }

    public void flipToBlank(){

        new Handler().postDelayed(()->{playSound(R.raw.short_button_turn);},200);
        messageBaloon.setImageResource(R.drawable.blank_baloon);

        //flip button
        mSetRightOut.setTarget(goButton);
        mSetLeftIn.setTarget(buttonBlank);
        mSetLeftIn.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                goButton.setEnabled(false);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        mSetRightOut.start();
        mSetLeftIn.start();
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
                doProgress(false);
                processResponse(response);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                doProgress(false);
                goButton.setEnabled(true);
                messageBaloon.setImageResource(R.drawable.bubble_email_error);
                playSound(R.raw.error_short);
            }
        });
    }

    private void processResponse(Response<Void> response) {

        Headers headers = response.headers();
        if(StringUtils.isEmpty(headers.get("errorCode"))) {
            messageBaloon.setImageResource(R.drawable.bubble_email_succsess);
            playSound(R.raw.email_sent);
            new Handler().postDelayed(()->{
                Intent intent = new Intent(getApplicationContext(), EndOfGameActivity.class);
                startActivity(intent);
                Activity activity = (Activity) context;
                activity.finish();
            },3500);
        }else{
            messageBaloon.setImageResource(R.drawable.bubble_email_error);
            goButton.setEnabled(true);
            playSound(R.raw.error_short);
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

        //scale background
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.emailScreen);
        ImageView iView = (ImageView) findViewById(R.id.emailBackgroundImage);
        ConstraintSet set = new ConstraintSet();
        set.clone(layout);
        set.constrainHeight(iView.getId(), newBmapHeight);
        set.constrainWidth(iView.getId(), newBmapWidth);
        set.applyTo(layout);

        //scale actionGo  button
        int buttonHeight = (int) (newBmapHeight * 0.3149F);
        int buttonWidth = (int) (newBmapWidth * 0.1777F);
        Button actionButtonGo = (Button) findViewById(R.id.goButton);
        ViewGroup.LayoutParams buttonParamsGo = actionButtonGo.getLayoutParams();
        buttonParamsGo.height = buttonHeight;
        buttonParamsGo.width = buttonWidth;

        //scale dots progress
        ImageView dotsProgress = (ImageView) findViewById(R.id.progressCounter);
        ViewGroup.LayoutParams dotsProgressParams18 = dotsProgress.getLayoutParams();
        dotsProgressParams18.height = buttonHeight;
        dotsProgressParams18.width = buttonWidth;

        ImageView buttonBlank = (ImageView) findViewById(R.id.buttonBlank);
        ViewGroup.LayoutParams buttonBlankParams = buttonBlank.getLayoutParams();
        buttonBlankParams.height = buttonHeight;
        buttonBlankParams.width = buttonWidth;

        //sacele message baloon
        ImageView messageBaloon = (ImageView) findViewById(R.id.emailMessageBaloon);
        ViewGroup.LayoutParams messageBaloonParams = messageBaloon.getLayoutParams();
        messageBaloonParams.width =(int)(newBmapWidth*0.3544F);
        messageBaloonParams.height =(int)(newBmapHeight*0.2775F);

    }

}
