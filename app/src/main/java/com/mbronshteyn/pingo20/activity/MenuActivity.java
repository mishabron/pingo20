package com.mbronshteyn.pingo20.activity;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.mbronshteyn.gameserver.dto.game.AuthinticateDto;
import com.mbronshteyn.pingo20.R;
import com.mbronshteyn.pingo20.activity.fragment.BonusesFragment;
import com.mbronshteyn.pingo20.activity.fragment.ContactFragment;
import com.mbronshteyn.pingo20.activity.fragment.RulesFragment;
import com.mbronshteyn.pingo20.model.Game;

public class MenuActivity extends PingoActivity {

    private RulesFragment rulesFragment;
    private BonusesFragment bonusesFragment;
    private ContactFragment contactFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        double payouts[] = {card.getPayout1(),card.getPayout2(),card.getPayout3()};
        rulesFragment = RulesFragment.newInstance(payouts);
        bonusesFragment = BonusesFragment.newInstance(null,null);
        contactFragment = ContactFragment.newInstance(null,null);

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

        Button contactButton = (Button) findViewById(R.id.contactButton);
        contactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle = new Bundle();
                contactFragment.setArguments(bundle);

                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.menuFragment,contactFragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        scaleUi();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        AuthinticateDto dto = new AuthinticateDto();
        ;
        dto.setCardNumber(card.getCardNumber());
        dto.setDeviceId(Game.devicedId);
        dto.setGame(Game.getGAMEID());
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
