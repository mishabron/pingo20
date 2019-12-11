package com.mbronshteyn.pingo20.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.mbronshteyn.gameserver.dto.game.AuthinticateDto;
import com.mbronshteyn.gameserver.dto.game.CardDto;
import com.mbronshteyn.gameserver.exception.ErrorCode;
import com.mbronshteyn.pingo20.R;
import com.mbronshteyn.pingo20.activity.fragment.PingoProgressBar;
import com.mbronshteyn.pingo20.events.ActionButtonEvent;
import com.mbronshteyn.pingo20.events.CardAuthinticatedEvent;
import com.mbronshteyn.pingo20.events.CardIdEnterEvent;
import com.mbronshteyn.pingo20.model.Game;
import com.mbronshteyn.pingo20.network.PingoRemoteService;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends PingoActivity {

    private EditText cardNumberInput;
    private ImageView authButton18;
    private final String ROTATE_VERTICAL = "rotationY";
    private ImageView leftLargeBaloon;
    private Button authButtonGo;
    private AnimatorSet mSetRightOut;
    private AnimatorSet mSetLeftIn;
    private LoginActivity context;
    private ImageView rightErrorBaloon;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = this;
        
        scaleUi();

        authButton18 = (ImageView) findViewById(R.id.hitCounter);

        authButtonGo = (Button) findViewById(R.id.actionButtonGo);
        authButtonGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new ActionButtonEvent());
                authButtonGo.setEnabled(false);
            }
        });
        authButtonGo.setEnabled(false);

        int distance = 8000;
        float scale = getResources().getDisplayMetrics().density * distance;
        authButton18.setCameraDistance(scale);
        authButtonGo.setCameraDistance(scale);

        mSetRightOut = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.anim.out_animation);
        mSetLeftIn = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.anim.in_animation);

        Typeface font = Typeface.createFromAsset(this.getAssets(), "fonts/badabb.ttf");
        cardNumberInput = (EditText) findViewById(R.id.cardId);
        cardNumberInput.setTypeface(font,Typeface.BOLD_ITALIC);

        leftLargeBaloon = (ImageView) findViewById(R.id.leftLargeBaloon);
        leftLargeBaloon.setVisibility(View.INVISIBLE);

        rightErrorBaloon = (ImageView) findViewById(R.id.rightErrorBaloon);
        rightErrorBaloon.setVisibility(View.INVISIBLE);

        cardNumberInput.addTextChangedListener(new TextWatcher() {

            final char space = ' ';

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cardNumberInput.setCursorVisible(false);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                // Remove spacing char
                if (s.length() > 0 && (s.length() % 5) == 0) {
                    final char c = s.charAt(s.length() - 1);
                    if (space == c) {
                        s.delete(s.length() - 1, s.length());
                    }
                }
                // Insert char where needed.
                if (s.length() > 0 && (s.length() % 5) == 0) {
                    char c = s.charAt(s.length() - 1);
                    // Only if its a digit where there should be a space we
                    // insert a space
                    if (Character.isDigit(c) && TextUtils.split(s.toString(), String.valueOf(space)).length <= 3) {
                        s.insert(s.length() - 1, String.valueOf(space));
                    }
                }

                if (s.length() == 14) {
                    EventBus.getDefault().post(new CardIdEnterEvent());
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onCardIdEnterEvent(CardIdEnterEvent event) {

        //close keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

        mSetRightOut.setTarget(authButton18);
        mSetLeftIn.setTarget(authButtonGo);
        mSetLeftIn.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                authButtonGo.setEnabled(true);
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

    @Subscribe
    public void onActionButtonEventUi(ActionButtonEvent event){

        playSound(R.raw.button);
        rightErrorBaloon.setVisibility(View.INVISIBLE);

        //disable input
        cardNumberInput.setEnabled(false);

        //flip button
        mSetRightOut.setTarget(authButtonGo);
        mSetLeftIn.setTarget(authButton18);
        mSetLeftIn.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                authButtonGo.setEnabled(false);
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

    @Subscribe
    public void onActionButtonEventAuthinticate(ActionButtonEvent event){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(PingoRemoteService.baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        final AuthinticateDto dto = new AuthinticateDto();

        String card = cardNumberInput.getText().toString();
        card = card.replaceAll("[^\\d]", "");
        dto.setCardNumber(Long.parseLong(card));

        Game.cardNumber = card;

        dto.setDeviceId(Game.devicedId);
        dto.setGame(Game.getGAMEID());

        final PingoRemoteService service = retrofit.create(PingoRemoteService.class);

        new Handler().postDelayed(()-> {
                Call<CardDto> call = service.authinticate(dto);
                call.enqueue(new Callback<CardDto>() {
                    @Override
                    public void onResponse(Call<CardDto> call, Response<CardDto> response) { ;
                        processResponse(response);
                    }

                    @Override
                    public void onFailure(Call<CardDto> call, Throwable t) {
                        cardNumberInput.setEnabled(true);
                        playSound(R.raw.error_short);
                    }
                });
        }, 0);
    }

    private void processResponse(Response<CardDto> response) {

        Headers headers = response.headers();
        String message = headers.get("message");
        ErrorCode errorCode = !StringUtils.isEmpty(headers.get("errorCode")) ? ErrorCode.valueOf(headers.get("errorCode")) : null;
;
        if(errorCode == null) {
            this.card = response.body();
            Game.guessedCount = getGuessedCount();
            EventBus.getDefault().post(new CardAuthinticatedEvent(null));
        }else{
            playSound(R.raw.error_short);
            cardNumberInput.setEnabled(true);
            switch(errorCode){
                case INVALID:
                    rightErrorBaloon.setVisibility(View.VISIBLE);
                    cardNumberInput.setText("");
                    break;
                case NOTACTIVE:
                    leftLargeBaloon.setImageResource(R.drawable.not_active);
                    popBaloon(leftLargeBaloon,4000);
                    break;
                case PLAYED:
                    EventBus.getDefault().post(new CardAuthinticatedEvent(errorCode));
                    break;
                case INUSE:
                    leftLargeBaloon.setImageResource(R.drawable.another_device);
                    popBaloon(leftLargeBaloon,4000);
                    break;
                case SERVERERROR:
                    rightSmallBaloon.setImageResource(R.drawable.error_blue_right);
                    popBaloon(rightSmallBaloon,4000);
                    break;
            }
        }
    }

    @Subscribe
    public void onCardAuthinticatedEvent(CardAuthinticatedEvent event){

        //go to game screen
        new Handler().postDelayed(()-> {
            Intent intent;
            if (event.getErrorCode() != null && event.getErrorCode().equals(ErrorCode.PLAYED)) {
                intent = new Intent(getApplicationContext(), NoWinActivity.class);
            } else if (isWinningCard()) {
                intent = new Intent(getApplicationContext(), WinActivity.class);
            } else {
                intent = new Intent(getApplicationContext(), GameActivity.class);
            }

            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            Activity activity = (Activity) context;
            activity.finish();
            Runtime.getRuntime().gc();
        }, 500);
    }

    public void scaleUi(){

        // scale the screen
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.login_newbacground, options);
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
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.coordinatorLayoutAuth);
        ImageView iView = (ImageView) findViewById(R.id.loginBacgroundimageView);
        ConstraintSet set = new ConstraintSet();
        set.clone(layout);
        set.constrainHeight(iView.getId(),newBmapHeight);
        set.constrainWidth(iView.getId(),newBmapWidth);
        set.centerVertically(R.id.loginBacgroundimageView, 0);
        set.centerHorizontally(R.id.loginBacgroundimageView, 0);
        set.applyTo(layout);

        //scale tex field
        EditText cardIdText = (EditText) findViewById(R.id.cardId);
        int cardIdTextHeight = (int) (newBmapHeight * 0.1085F);
        int cardIdTextWidth = (int) (newBmapHeight * 0.9787F);
        ViewGroup.LayoutParams cardIdTextParams = cardIdText.getLayoutParams();
        cardIdTextParams.height = cardIdTextHeight;
        cardIdTextParams.width = cardIdTextWidth;

        //scale action18  button
        ImageView actionButton18 = (ImageView) findViewById(R.id.hitCounter);
        int buttonSize18 = (int) (newBmapHeight * 0.3286F);
        ViewGroup.LayoutParams buttonParams18 = actionButton18.getLayoutParams();
        buttonParams18.height = buttonSize18;
        buttonParams18.width = buttonSize18;

        //scale actionGo  button
        Button actionButtonGo = (Button) findViewById(R.id.actionButtonGo);
        int buttonSizeGo = (int) (newBmapHeight * 0.3286F);
        ViewGroup.LayoutParams buttonParamsGo = actionButtonGo.getLayoutParams();
        buttonParamsGo.height = buttonSizeGo;
        buttonParamsGo.width = buttonSizeGo;

        //sacele hey baloon
        ImageView heyBaloon = (ImageView) findViewById(R.id.leftLargeBaloon);
        ViewGroup.LayoutParams heyBaloonParams = heyBaloon.getLayoutParams();
        heyBaloonParams.width =(int)(newBmapWidth*0.3507F);

        //sacele stand by baloon
        ImageView stanByBaloon = (ImageView) findViewById(R.id.rightSmallBaloon);
        ViewGroup.LayoutParams stanByBaloonParams = stanByBaloon.getLayoutParams();
        stanByBaloonParams.width =(int)(newBmapWidth*0.2795F);


    }
}

