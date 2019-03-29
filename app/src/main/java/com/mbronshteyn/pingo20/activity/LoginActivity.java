package com.mbronshteyn.pingo20.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.res.ResourcesCompat;
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
    private PingoProgressBar progressBar;
    private ImageView leftLargeBaloon;
    private ImageView rightSmallBaloon;
    private Button authButtonGo;
    private AnimatorSet mSetRightOut;
    private AnimatorSet mSetLeftIn;

    public interface AnimatorCallback {
        public void onAnimationEnd();

        public void onAnimationStart();
    }

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        scaleUi();

        authButton18 = (ImageView) findViewById(R.id.actionButton18);

        authButtonGo = (Button) findViewById(R.id.actionButtonGo);
        authButtonGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSound(R.raw.button);
                EventBus.getDefault().post(new ActionButtonEvent());
            }
        });
        authButtonGo.setEnabled(false);

        int distance = 8000;
        float scale = getResources().getDisplayMetrics().density * distance;
        authButton18.setCameraDistance(scale);
        authButtonGo.setCameraDistance(scale);

        mSetRightOut = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.anim.out_animation);
        mSetLeftIn = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.anim.in_animation);

        Typeface font = Typeface.createFromAsset(this.getAssets(), "fonts/comic.ttf");
        cardNumberInput = (EditText) findViewById(R.id.cardId);
        cardNumberInput.setTypeface(font,Typeface.BOLD_ITALIC);

        leftLargeBaloon = (ImageView) findViewById(R.id.leftLargeBaloon);
        leftLargeBaloon.setVisibility(View.INVISIBLE);
        rightSmallBaloon = (ImageView) findViewById(R.id.rightSmallBaloon);
        rightSmallBaloon.setVisibility(View.INVISIBLE);

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
                } else {
                    //authButtonGo.setEnabled(false);
                    //authButton18.setBackgroundResource(R.drawable.button18_reverse);
                }
            }
        });

        progressBar = (PingoProgressBar) getSupportFragmentManager().findFragmentById(R.id.fragmentProgressBar);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                leftLargeBaloon.setVisibility(View.VISIBLE);
                Animation zoomIntAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in);
                leftLargeBaloon.startAnimation(zoomIntAnimation);
            }
        }, 3000);
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

        //start progress
        progressBar.startProgress();

        //pop baloons
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                rightSmallBaloon.setImageResource(R.drawable.standby_blueright);
                rightSmallBaloon.setVisibility(View.VISIBLE);
                Animation zoomIntAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in);
                rightSmallBaloon.startAnimation(zoomIntAnimation);

                Animation zoomIntAnimationOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_out);
                leftLargeBaloon.startAnimation(zoomIntAnimationOut);
            }
        }, 1500);
    }

    @Subscribe
    public void onActionButtonEventAuthinticate(ActionButtonEvent event){

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://ec2-18-219-33-9.us-east-2.compute.amazonaws.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        final AuthinticateDto dto = new AuthinticateDto();

        String card = cardNumberInput.getText().toString();
        card = card.replaceAll("[^\\d]", "");
        dto.setCardNumber(Long.parseLong(card));

        dto.setDeviceId(devicedId);
        dto.setGame(GAMEID);

        final PingoRemoteService service = retrofit.create(PingoRemoteService.class);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Call<CardDto> call = service.authinticate(dto);
                call.enqueue(new Callback<CardDto>() {
                    @Override
                    public void onResponse(Call<CardDto> call, Response<CardDto> response) {
                        progressBar.stopProgress();
                        Animation zoomIntAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_out);
                        rightSmallBaloon.startAnimation(zoomIntAnimation);
                        processResponse(response);
                    }

                    @Override
                    public void onFailure(Call<CardDto> call, Throwable t) {
                        cardNumberInput.setEnabled(true);
                        playSound(R.raw.error_short);
                        progressBar.stopProgress();
                        Animation zoomIntAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_out);
                        rightSmallBaloon.startAnimation(zoomIntAnimation);
                        rightSmallBaloon.setImageResource(R.drawable.try_again_baloon);
                        popBaloon(rightSmallBaloon,4000);
                    }
                });
            }
        }, 6000);
    }

    private void processResponse(Response<CardDto> response) {

        Headers headers = response.headers();
        String message = headers.get("message");

        if(StringUtils.isEmpty(headers.get("errorCode"))) {
            card = response.body();
            EventBus.getDefault().post(new CardAuthinticatedEvent());
        }else{
            playSound(R.raw.error_short);
            cardNumberInput.setEnabled(true);
            ErrorCode errorCode = ErrorCode.valueOf(headers.get("errorCode"));
            switch(errorCode){
                case INVALID:
                    leftLargeBaloon.setImageResource(R.drawable.fake_card_baloon);
                    popBaloon(leftLargeBaloon,4000);
                    break;
                case NOTACTIVE:
                    leftLargeBaloon.setImageResource(R.drawable.not_active);
                    popBaloon(leftLargeBaloon,4000);
                    break;
                case INUSE:
                    leftLargeBaloon.setImageResource(R.drawable.another_device);
                    popBaloon(leftLargeBaloon,4000);
                    break;
                case PLAYED:
                    leftLargeBaloon.setImageResource(R.drawable.used_card);
                    popBaloon(leftLargeBaloon,4000);
                    break;
                case SERVERERROR:
                    rightSmallBaloon.setImageResource(R.drawable.error_blue_right);
                    popBaloon(leftLargeBaloon,4000);
                    break;
            }
        }
    }

    @Subscribe
    public void onCardAuthinticatedEvent(CardAuthinticatedEvent event){

        final ImageView topBanner = (ImageView) findViewById(R.id.banner);
        topBanner.setImageResource(R.drawable.slogan_wht_blur);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                topBanner.setImageResource(R.drawable.slogan_black);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        topBanner.setImageResource(R.drawable.slogan_wht_blur);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                topBanner.setImageResource(R.drawable.slogan_black);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        topBanner.setImageResource(R.drawable.slogan3d);
                                    }
                                }, 300);
                            }
                        }, 300);
                    }
                }, 300);
            }
        },1300);

        rightSmallBaloon.setImageResource(R.drawable.success);
        popBaloon(rightSmallBaloon,4000);
        progressBar.startSaccess();
    }

    public void scaleUi(){

        // scale the screen
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        BitmapDrawable bmap = (BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.login_background, null);
        float bmapWidth = bmap.getBitmap().getWidth();
        float bmapHeight = bmap.getBitmap().getHeight();

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
        int cardIdTextWidth = (int) (newBmapHeight * 0.8787F);
        ViewGroup.LayoutParams cardIdTextParams = cardIdText.getLayoutParams();
        cardIdTextParams.height = cardIdTextHeight;
        cardIdTextParams.width = cardIdTextWidth;

        //scale progress bar
        FrameLayout progressBar = (FrameLayout) findViewById(R.id.fragmentProgressBar);
        ViewGroup.LayoutParams progressParams = progressBar.getLayoutParams();
        progressParams.height = (int)(newBmapHeight*0.059F);
        progressParams.width = (int)(newBmapWidth*0.1397F);

        //scale action18  button
        ImageView actionButton18 = (ImageView) findViewById(R.id.actionButton18);
        int buttonSize18 = (int) (newBmapHeight * 0.2406F);
        ViewGroup.LayoutParams buttonParams18 = actionButton18.getLayoutParams();
        buttonParams18.height = buttonSize18;
        buttonParams18.width = buttonSize18;

        //scale actionGo  button
        Button actionButtonGo = (Button) findViewById(R.id.actionButtonGo);
        int buttonSizeGo = (int) (newBmapHeight * 0.2406F);
        ViewGroup.LayoutParams buttonParamsGo = actionButtonGo.getLayoutParams();
        buttonParamsGo.height = buttonSizeGo;
        buttonParamsGo.width = buttonSizeGo;

        //sacele top banner
        ImageView topBanner = (ImageView) findViewById(R.id.banner);
        ViewGroup.LayoutParams bannerParams = topBanner.getLayoutParams();
        bannerParams.width =(int)(newBmapWidth*0.7890F);

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

