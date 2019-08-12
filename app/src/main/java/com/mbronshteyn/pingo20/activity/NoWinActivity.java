package com.mbronshteyn.pingo20.activity;

import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mbronshteyn.pingo20.R;
import com.mbronshteyn.pingo20.model.Game;

import java.util.ArrayList;

public class NoWinActivity extends PingoActivity {

    private NoWinActivity context;
    private TextView balance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_win);

        context = this;

        //balance
        Typeface fontBalance = Typeface.createFromAsset(this.getAssets(), "fonts/showg.ttf");
        balance = (TextView) findViewById(R.id.balance);
        balance.setTypeface(fontBalance,Typeface.BOLD_ITALIC);
        balance.setText(balance.getText().toString() + (int)card.getBalance()+ " ");

        TextView cardNumber = (TextView) findViewById(R.id.cardNumber);
        String cardId = Game.cardNumber;
        cardNumber.setText(cardNumber.getText()+ cardId.substring(0,4)+" "+cardId.substring(4,8)+" "+cardId.substring(8,12));

        scaleUi();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {

        super.onPostCreate(savedInstanceState);

        ArrayList<Integer> pingoNumbers = new ArrayList<>();
        pingoNumbers.add(R.drawable.zero);
        pingoNumbers.add(R.drawable.one);
        pingoNumbers.add(R.drawable.two);
        pingoNumbers.add(R.drawable.three);
        pingoNumbers.add(R.drawable.four);
        pingoNumbers.add(R.drawable.five);
        pingoNumbers.add(R.drawable.six);
        pingoNumbers.add(R.drawable.seven);
        pingoNumbers.add(R.drawable.eight);
        pingoNumbers.add(R.drawable.nine);

        ImageView number1 = (ImageView) findViewById(R.id.number1);
        ImageView number2 = (ImageView) findViewById(R.id.number2);
        ImageView number3 = (ImageView) findViewById(R.id.number3);
        ImageView number4 = (ImageView) findViewById(R.id.number4);

        loadWinNUmber(number1,pingoNumbers,loadNumberGuessed(1));
        loadWinNUmber(number2,pingoNumbers,loadNumberGuessed(2));
        loadWinNUmber(number3,pingoNumbers,loadNumberGuessed(3));
        loadWinNUmber(number4,pingoNumbers,loadNumberGuessed(4));
    }

    private void loadWinNUmber(ImageView number, ArrayList<Integer> pingoNumbers, Integer loadNumberGuessed) {

        //Glide.with(this).load(pingoNumbers.get(loadNumberGuessed)).diskCacheStrategy( DiskCacheStrategy.NONE ).skipMemoryCache( true ).into(number);

        number.setBackgroundResource(pingoNumbers.get(loadNumberGuessed));
    }

    public void scaleUi() {

        // scale the screen
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.game_background, options);
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

        float nw = 0.1313F;
        float nh = 0.2888F;

        //scale pingo windows
        ImageView number1 = (ImageView) findViewById(R.id.number1);
        ViewGroup.LayoutParams number1Params = number1.getLayoutParams();
        number1Params.width =(int)(newBmapWidth*nw);
        number1Params.height =(int)(newBmapHeight*nh);

        ImageView number2 = (ImageView) findViewById(R.id.number2);
        ViewGroup.LayoutParams number2Params = number1.getLayoutParams();
        number2Params.width =(int)(newBmapWidth*nw);
        number2Params.height =(int)(newBmapHeight*nh);

        ImageView number3 = (ImageView) findViewById(R.id.number3);
        ViewGroup.LayoutParams number3Params = number1.getLayoutParams();
        number3Params.width =(int)(newBmapWidth*nw);
        number3Params.height =(int)(newBmapHeight*nh);

        ImageView number4 = (ImageView) findViewById(R.id.number4);
        ViewGroup.LayoutParams number4Params = number1.getLayoutParams();
        number4Params.width =(int)(newBmapWidth*nw);
        number4Params.height =(int)(newBmapHeight*nh);


    }
}
