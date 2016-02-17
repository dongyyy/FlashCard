package kr.co.bit.osf.flashcard;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import kr.co.bit.osf.flashcard.db.FlashCardDB;
import kr.co.bit.osf.flashcard.db.StateDAO;
import kr.co.bit.osf.flashcard.db.StateDTO;

public class CardListActivity extends AppCompatActivity {
    final String TAG = "FlashCardCardListTag";

    FlashCardDB db = null;
    StateDAO stateDao = null;
    StateDTO cardState = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        // read state from db
        db = new FlashCardDB(this);
        stateDao = db;
        cardState = stateDao.getState();
        Log.i(TAG, "read card state:" + cardState);

        // state.cardId > 0 : start card view activity
        if (cardState.getCardId() > 0) {
            Intent intent = new Intent(this, CardViewActivity.class);
            startActivity(intent);
        }
    }
}
