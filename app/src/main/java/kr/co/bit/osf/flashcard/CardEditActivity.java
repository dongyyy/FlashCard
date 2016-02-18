package kr.co.bit.osf.flashcard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import kr.co.bit.osf.flashcard.common.IntentExtrasName;
import kr.co.bit.osf.flashcard.common.IntentRequestCode;
import kr.co.bit.osf.flashcard.db.CardDTO;

public class CardEditActivity extends AppCompatActivity {
    final String TAG = "FlashCardCardEditTag";

    private int intentRequestCode = 0;
    private int intentResultCode = RESULT_CANCELED;
    private CardDTO card = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_edit);

        Log.i(TAG, "card edit acvitty started");
        // get intent data
        Intent intent = getIntent();
        intentRequestCode = intent.getIntExtra(IntentExtrasName.REQUEST_CODE, 0);
        Log.i(TAG, "intentRequestCode:" + intentRequestCode);
        switch (intentRequestCode) {
            case IntentRequestCode.CARD_EDIT:
                card = intent.getParcelableExtra(IntentExtrasName.SEND_DATA);
                break;
        }
        if (card == null) {
            Log.i(TAG, "getExtras:sendData:no data");
            finish();
            return ;
        }

        // todo: process requested task
        Log.i(TAG, "getExtras:sendData:" + card);
        card.setName("is updated");
        intentResultCode = RESULT_OK;
    }

    @Override
    public void finish() {
        // return data
        if (intentResultCode == RESULT_OK) {
            Intent data = new Intent();
            data.putExtra(IntentExtrasName.RETURN_DATA, card);
            Log.i(TAG, "putExtra:returnData:" + card);
            setResult(intentResultCode, data);
        } else {
            setResult(intentResultCode);
        }
        Log.i(TAG, "setResult:" + intentResultCode);
        super.finish();
    }
}
