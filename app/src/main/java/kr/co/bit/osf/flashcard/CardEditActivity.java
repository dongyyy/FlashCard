package kr.co.bit.osf.flashcard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import kr.co.bit.osf.flashcard.common.IntentExtrasName;
import kr.co.bit.osf.flashcard.common.IntentRequestCode;
import kr.co.bit.osf.flashcard.db.CardDTO;
import kr.co.bit.osf.flashcard.db.FlashCardDB;
import kr.co.bit.osf.flashcard.debug.Dlog;

public class CardEditActivity extends AppCompatActivity {
    private int intentRequestCode = 0;
    private int intentResultCode = RESULT_CANCELED;
    private CardDTO card = null;
    private TextView textView = null;
    private ImageView imageView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_edit);

        Dlog.i("started");
        // get intent data
        Intent intent = getIntent();
        intentRequestCode = intent.getIntExtra(IntentExtrasName.REQUEST_CODE, 0);
        Dlog.i("intentRequestCode:" + intentRequestCode);
        switch (intentRequestCode) {
            case IntentRequestCode.CARD_EDIT:
                card = intent.getParcelableExtra(IntentExtrasName.SEND_DATA);
                break;
        }
        if (card == null) {
            Dlog.i("getExtras:sendData:no data");
            finish();
            return ;
        }

        // todo: process requested task
        Dlog.i("getExtras:sendData:" + card);
        intentResultCode = RESULT_OK;
        textView = (TextView)findViewById(R.id.cardEditTextView);
        imageView = (ImageView)findViewById(R.id.cardEditImageView);

        //show card
        if( intentRequestCode == IntentRequestCode.CARD_EDIT){
            String imagePath = card.getImagePath();
            if (card.getType() == FlashCardDB.CardEntry.TYPE_USER) {
                // load image from sd card(glide)
                Glide.with(getApplicationContext()).load(imagePath).into(imageView);
            } else {
                // card demo data(glide)
                Glide.with(getApplicationContext()).fromResource()
                        .load(Integer.parseInt(imagePath)).into(imageView);
            }
        }
        textView.setText(card.getName());
    }

    @Override
    public void finish() {
        // return data
        if (intentResultCode == RESULT_OK) {
            Intent data = new Intent();
            data.putExtra(IntentExtrasName.RETURN_DATA, card);
            Dlog.i("putExtra:returnData:" + card);
            setResult(intentResultCode, data);
        } else {
            setResult(intentResultCode);
        }
        Dlog.i("setResult:" + intentResultCode);
        super.finish();
    }
}
