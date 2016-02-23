package kr.co.bit.osf.flashcard;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import kr.co.bit.osf.flashcard.db.FlashCardDB;
import kr.co.bit.osf.flashcard.db.StateDAO;
import kr.co.bit.osf.flashcard.db.StateDTO;
import kr.co.bit.osf.flashcard.debug.Dlog;

public class SplashActivity extends AppCompatActivity {
    FlashCardDB db = null;
    StateDAO stateDao = null;
    StateDTO state = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Dlog.i("");

        (findViewById(R.id.splashTextView)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Dlog.i("");
        // save current state
        FlashCardDB db = new FlashCardDB(this);
        if (db != null) {
            db.updateState(0 ,0);
        }
    }
}
