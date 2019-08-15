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
        setContentView(R.layout.activity_nowin);

        context = this;

        //balance
        Typeface fontBalance = Typeface.createFromAsset(this.getAssets(), "fonts/showg.ttf");
        balance = (TextView) findViewById(R.id.balance);
        balance.setTypeface(fontBalance,Typeface.BOLD_ITALIC);
        balance.setText(balance.getText().toString() +  " ");

        TextView cardNumber = (TextView) findViewById(R.id.cardNumber);
        String cardId = Game.cardNumber;
        cardNumber.setText(cardNumber.getText()+ cardId.substring(0,4)+" "+cardId.substring(4,8)+" "+cardId.substring(8,12));

    }

}
