package kr.co.bit.osf.flashcard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import kr.co.bit.osf.flashcard.db.FlashCardDB;
import kr.co.bit.osf.flashcard.db.StateDAO;
import kr.co.bit.osf.flashcard.db.StateDTO;

public class MainActivity extends AppCompatActivity {
    final String TAG = "FlashCardMainTag";

    FlashCardDB db = null;
    StateDAO stateDao = null;
    StateDTO state = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // db initialize
        db = new FlashCardDB(this);
        stateDao = db;
        state = stateDao.getState();
        if (state == null) {
            Log.i(TAG, "db initialize");
            db.initialize();
            state = stateDao.getState();
            Log.i(TAG, "state:" + state);
        } else {
            Log.i(TAG, "db already initialized");
        }

        // start box list activity
        Intent intent = new Intent(this, BoxListActivity.class);
        startActivity(intent);
    }
}
