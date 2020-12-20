package com.mbronshteyn.pingo20.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.mbronshteyn.pingo20.R;

public class EndOfGameActivity extends Activity {

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
                intent.putExtras(bundle);
                startActivity(intent);
                Activity activity = (Activity) context;
                activity.finish();
            }
        });

        ImageView noButton = (ImageView) findViewById(R.id.noAgain);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory( Intent.CATEGORY_HOME );
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
            }
        });

    }


}
