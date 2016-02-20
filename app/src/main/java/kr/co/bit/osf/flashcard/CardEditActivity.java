package kr.co.bit.osf.flashcard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

import kr.co.bit.osf.flashcard.common.ImageUtil;
import kr.co.bit.osf.flashcard.common.IntentExtrasName;
import kr.co.bit.osf.flashcard.common.IntentRequestCode;
import kr.co.bit.osf.flashcard.db.CardDTO;
import kr.co.bit.osf.flashcard.db.FlashCardDB;
import kr.co.bit.osf.flashcard.debug.Dlog;

public class CardEditActivity extends AppCompatActivity {
    // dto
    private CardDTO card = null;
    // intent
    private int intentRequestCode = 0;
    private int intentResultCode = RESULT_CANCELED;
    // view
    private ImageView imageView = null;
    private EditText editText = null;
    // camera, gallery
    private File photoFile = null;
    private String photoFilePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_edit);

        Dlog.i("started");

        // todo: full screen
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.hide();

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
            return;
        }

        // todo: process requested task
        Dlog.i("getExtras:sendData:" + card);
        intentResultCode = RESULT_OK;
        editText = (EditText) findViewById(R.id.cardEditText);
        imageView = (ImageView) findViewById(R.id.cardEditImageView);

        //show card
        if(intentRequestCode == IntentRequestCode.CARD_EDIT){
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
        editText.setText(card.getName());
        editText.setSelection(editText.length()); //커서를 맨 뒤로 이동

        //camera Button
        (findViewById(R.id.cardEditCameraButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoFile = ImageUtil.getOutputMediaFile(ImageUtil.MEDIA_TYPE_IMAGE);
                photoFilePath = photoFile.getAbsolutePath();

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, IntentRequestCode.CAPTURE_IMAGECAPTURE_IMAGE);
                }
            }
        });

        //gallery Button
        (findViewById(R.id.cardEditGalleryButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dlog.i("cardViewGalleryButton clicked");
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(
                        Intent.createChooser(intent, "Select Picture"),
                        IntentRequestCode.SELECT_PICTURE);
            }
        });

        //yes, no Button
        (findViewById(R.id.cardEditYesButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlashCardDB db = new FlashCardDB(getApplicationContext());
                card.setName(editText.getText().toString().trim());
                db.updateCard(card);
                finish();
            }
        });

        (findViewById(R.id.cardEditNoButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentResultCode = RESULT_CANCELED;
                finish();
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        Dlog.i("requestCode=" + requestCode + ",resultCode=" + resultCode);
        if(resultCode == RESULT_OK){
            switch(requestCode) {
                case IntentRequestCode.CAPTURE_IMAGECAPTURE_IMAGE:
                case IntentRequestCode.SELECT_PICTURE:
                    if (requestCode== IntentRequestCode.SELECT_PICTURE) {
                        photoFilePath = ImageUtil.getImagePathFromIntentData(this, data);
                    }
                    Glide.with(getApplicationContext()).load(photoFilePath).into(imageView);
                    card.setImagePath(photoFilePath);
                    card.setType(FlashCardDB.CardEntry.TYPE_USER);
                    Dlog.i("photoFilePath:" + card.getImagePath());
                    break;
              }
        }
    }

    @Override
    public void finish() {
        // return data
        if (intentResultCode == RESULT_OK) {
            Intent data = new Intent();
            data.putExtra(IntentExtrasName.RETURN_DATA, card);
            setResult(intentResultCode, data);
        } else {
            setResult(intentResultCode);
        }
        Dlog.i("setResult:" + intentResultCode);
        super.finish();
    }
}
