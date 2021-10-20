package com.mbronshteyn.pingo20.activity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mbronshteyn.pingo20.R;
import com.mbronshteyn.pingo20.model.Game;

public class EndOfGameActivity extends PingoActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_of_game);

        EndOfGameActivity context = this;

        ImageView yesButton = (ImageView) findViewById(R.id.yesAgain);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("playAgain",true);
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        ImageView noButton = (ImageView) findViewById(R.id.noAgain);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Game.exit = true;
                Bundle bundle = new Bundle();
                bundle.putBoolean("playAgain",false);
                Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        });

        scaleUi();
    }

    public void scaleUi() {

        // scale the screen
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.play_again, options);
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
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.endGame);
        ImageView iView = (ImageView) findViewById(R.id.endOfGameBackgroundImage);
        ConstraintSet set = new ConstraintSet();
        set.clone(layout);
        set.constrainHeight(iView.getId(), newBmapHeight);
        set.constrainWidth(iView.getId(), newBmapWidth);
        set.applyTo(layout);

        //yes no buttons
        ImageView yesButton = (ImageView) findViewById(R.id.yesAgain);
        ViewGroup.LayoutParams yesButtonParams = yesButton.getLayoutParams();
        yesButtonParams.width =(int)(newBmapWidth*0.06224F);
        yesButtonParams.height =(int)(newBmapHeight*0.1103F);

        ImageView noButton = (ImageView) findViewById(R.id.noAgain);
        ViewGroup.LayoutParams noButtonParams = noButton.getLayoutParams();
        noButtonParams.width =(int)(newBmapWidth*0.06224F);
        noButtonParams.height =(int)(newBmapHeight*0.1103F);
    }

}
