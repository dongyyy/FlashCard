package kr.co.bit.osf.flashcard;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import kr.co.bit.osf.flashcard.db.FlashCardDB;
import kr.co.bit.osf.flashcard.db.StateDAO;
import kr.co.bit.osf.flashcard.db.StateDTO;


public class SplashActivity extends AppCompatActivity {
    FlashCardDB db = null;
    StateDAO stateDao = null;
    StateDTO state = null;
    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }
}
