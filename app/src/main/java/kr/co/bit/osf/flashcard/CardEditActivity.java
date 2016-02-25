package kr.co.bit.osf.flashcard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

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
    private TextView cardEditTextView = null;
    // default_camera_image, gallery
    private File photoFile = null;
    private String photoFilePath = null;
    private DialogInterface dialogInterface;
    // delete card
    List<CardDTO> cardList = null;

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
            case IntentRequestCode.CARD_ADD:
                Dlog.i("intentRequestCode in case:" + intentRequestCode);
                card = intent.getParcelableExtra(IntentExtrasName.SEND_DATA);
                card.setType(FlashCardDB.CardEntry.TYPE_USER);
            case IntentRequestCode.CARD_EDIT:
            case IntentRequestCode.CARD_DELETE:
                Dlog.i("intentRequestCode in case:" + intentRequestCode);
                card = intent.getParcelableExtra(IntentExtrasName.SEND_DATA);
                if (card == null) {
                    Dlog.i("getParcelableExtra: sendData:no data");
                    finish();
                    return;
                }
                break;
            //delete cardList
            case IntentRequestCode.CARD_DELETE_LIST:
                Dlog.i("intentRequestCode in case:" + intentRequestCode);
                cardList = intent.getParcelableArrayListExtra(IntentExtrasName.SEND_DATA);
                Dlog.i("CARD_DELETE_LIST:cardList:size():" + cardList.size());
                if (cardList == null) {
                    Dlog.i("getParcelableArrayListExtra:sendData: no data");
                    finish();
                    return;
                }
                break;
        }

        // todo: process requested task
        Dlog.i("getExtras:sendData:" + card);
        intentResultCode = RESULT_OK;
        if (intentRequestCode == IntentRequestCode.CARD_DELETE) {
            Dlog.i("delete data:" + card);
            // delete card
            FlashCardDB db = new FlashCardDB(CardEditActivity.this);
            if (db.deleteCard(card.getId()) == false) {
                intentResultCode = RESULT_CANCELED;
                Dlog.i("delete error:" + card);
            }
            finish();
            return;
        }
        //delete cardList
        if (intentRequestCode == IntentRequestCode.CARD_DELETE_LIST) {
            Dlog.i("delete data:" + cardList);
            //delete cardList
            if (cardList.size() > 0) {
                FlashCardDB db = new FlashCardDB(CardEditActivity.this);
                Dlog.i("delete data:size():" + cardList.size());
                if (db.deleteCard(cardList) == false) {
                    intentResultCode = RESULT_CANCELED;
                    Dlog.i("delete error:" + card);
                }
                finish();
                return;
            }
        }

        imageView = (ImageView) findViewById(R.id.cardEditImageView);
        cardEditTextView = (TextView) findViewById(R.id.cardEditTextView);

        //show card
        if (intentRequestCode == IntentRequestCode.CARD_EDIT) {
            ImageUtil.loadCardImageIntoImageView(this, card, imageView);
        }
        if (card.getName() != null) {
            cardEditTextView.setText(card.getName());
        }

        //imageView - card Image
        (findViewById(R.id.frameLayout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageClicked();
            }
        });

        //textView - card Name
        (findViewById(R.id.cardEditTextView)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textClicked();
            }
        });

        //yes, no Button
        (findViewById(R.id.cardEditYesButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dlog.i("yes button:check:card:" + card);
                boolean isOk = false;
                switch (intentRequestCode) {
                    case IntentRequestCode.CARD_ADD:
                    case IntentRequestCode.CARD_EDIT:
                        Dlog.i("yes button:check:intentRequestCode" + intentRequestCode);
                        isOk = updatedCardIsOk(true);
                        break;
                }

                if (isOk) {
                    Dlog.i("yes button:check:ok:" + card);
                    FlashCardDB db = new FlashCardDB(getApplicationContext());
                    card.setName(cardEditTextView.getText().toString().trim());
                    if (intentRequestCode == IntentRequestCode.CARD_ADD) {
                        Dlog.i("yes button:check:ok:add" + card);
                        db.addCard(card);
                    } else {
                        Dlog.i("yes button:check:ok:update" + card);
                        db.updateCard(card);
                    }
                    finish();
                } else {
                    Dlog.i("yes button:check:fail:" + card);
                }
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

    private void imageClicked() {
        Dlog.i("CardEditActivity: imageClicked");
        View dlg = CardEditActivity.this.getLayoutInflater().inflate(R.layout.dialog_title, null);
        // get user action from dialog
        final CharSequence[] items = {
                getString(R.string.card_edit_image_dialog_camera_button_text),
                getString(R.string.card_edit_image_dialog_gallery_button_text),
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(CardEditActivity.this);
        TextView editTitle = (TextView)dlg.findViewById(R.id.dialogTitleTextView);
        editTitle.setText(R.string.card_edit_image_dialog_title);
        editTitle.setVisibility(View.VISIBLE);

        TextView editMenuOne = (TextView)dlg.findViewById(R.id.dialogMenuTextViewOne);
        editMenuOne.setText(R.string.card_edit_image_dialog_camera_button_text);
        editMenuOne.setVisibility(View.VISIBLE);
        editMenuOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dlog.i("cardEdit:photoCaptureButton clicked");
                //capture card
                photoFile = ImageUtil.getOutputMediaFile(ImageUtil.MEDIA_TYPE_IMAGE);
                Dlog.i("cardEdit:photoCaptureButton clicked");
                photoFilePath = photoFile.getAbsolutePath();

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, IntentRequestCode.CAPTURE_IMAGE);
                }
                dialogInterface.dismiss();
            }
        });

        TextView editmenuTwo = (TextView)dlg.findViewById(R.id.dialogMenuTextViewTwo);
        editmenuTwo.setText(R.string.card_edit_image_dialog_gallery_button_text);
        editmenuTwo.setVisibility(View.VISIBLE);
        editmenuTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dlog.i("cardEdit:galleryButton clicked");
                // select card in gallery
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(
                        Intent.createChooser(intent, "Select Picture"),
                        IntentRequestCode.SELECT_PICTURE);
                dialogInterface.dismiss();
            }

        });
        builder.setView(dlg);
        dialogInterface = builder.show();
    }

    private void textClicked() {

        Dlog.i("CardEditActivity: textClicked");

        AlertDialog.Builder alert = new AlertDialog.Builder(CardEditActivity.this);
        Dlog.i("CardEditActivity: textClicked:AlertDialog.Builder");
        View dlg = CardEditActivity.this.getLayoutInflater().inflate(R.layout.dialog_title, null);

        TextView editTitle = (TextView)dlg.findViewById(R.id.dialogTitleTextView);
        editTitle.setText(R.string.card_edit_text_dialog_title_text);
        editTitle.setVisibility(View.VISIBLE);

        TextView editMenuOne = (TextView)dlg.findViewById(R.id.dialogMenuTextViewOne);
        editMenuOne.setText(R.string.card_edit_text_dialog_message_text);
        editMenuOne.setVisibility(View.VISIBLE);

        final EditText editText = (EditText)dlg.findViewById(R.id.dialogMenuEditTextOne);
        editText.setVisibility(View.VISIBLE);
        Dlog.i("CardEditActivity: textClicked:AlertDialog.Builder:setView");
        if (card.getName() != null) {
            editText.setText(card.getName());
            editText.setSelection(card.getName().length());
        } else {
            editText.setText("");
        }
        Dlog.i("CardEditActivity: textClicked:AlertDialog.Builder:setText");
        alert.setPositiveButton(R.string.card_edit_text_dialog_ok_button_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String name = editText.getText().toString();
                cardEditTextView.setText(name);
                card.setName(name);
            }
        });

        alert.setNegativeButton(R.string.card_edit_text_dialog_cancel_button_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });


/*
        alert.setTitle(R.string.card_edit_text_dialog_title_text);
        alert.setMessage(R.string.card_edit_text_dialog_message_text);
        Dlog.i("CardEditActivity: textClicked:AlertDialog.Builder:setTitle");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setGravity(Gravity.CENTER);
        alert.setView(input);
        input.setGravity(Gravity.CENTER);
        Dlog.i("CardEditActivity: textClicked:AlertDialog.Builder:setView");
        if (card.getName() != null) {
            input.setText(card.getName());
            input.setSelection(card.getName().length());
        } else {
            input.setText("");
        }
        Dlog.i("CardEditActivity: textClicked:AlertDialog.Builder:setText");
        alert.setPositiveButton(R.string.card_edit_text_dialog_ok_button_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String name = input.getText().toString();
                cardEditTextView.setText(name);
                card.setName(name);
            }
        });

        alert.setNegativeButton(R.string.card_edit_text_dialog_cancel_button_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
*/
        alert.setView(dlg);
        dialogInterface = alert.show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Dlog.i("requestCode=" + requestCode + ",resultCode=" + resultCode);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case IntentRequestCode.CAPTURE_IMAGE:
                case IntentRequestCode.SELECT_PICTURE:
                    if (requestCode == IntentRequestCode.SELECT_PICTURE) {
                        photoFilePath = ImageUtil.getImagePathFromIntentData(this, data);
                    }
                    card.setImagePath(photoFilePath);
                    card.setType(FlashCardDB.CardEntry.TYPE_USER);
                    Dlog.i("photoFilePath:" + photoFilePath);
                    Dlog.i("photoFilePath:" + card);
                    ImageUtil.loadCardImageIntoImageView(this, card, imageView);
                    Dlog.i("photoFilePath:" + card.getImagePath());
                    Dlog.i("photoFilePath:" + card.getImagePath());
                    break;
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void finish() {
        // return data
        if (intentResultCode == RESULT_OK) {
            Intent data = new Intent();
            switch (intentRequestCode) {
                case IntentRequestCode.CARD_ADD:
                case IntentRequestCode.CARD_EDIT:
                case IntentRequestCode.CARD_DELETE:
                    if (updatedCardIsOk() == true) {
                        data.putExtra(IntentExtrasName.RETURN_DATA, card);
                    } else {
                        Dlog.i("updatedCardIsOk() == false");
                        intentResultCode = RESULT_CANCELED;
                    }
                    break;
                case IntentRequestCode.CARD_DELETE_LIST:
                    break;
            }
            setResult(intentResultCode, data);
        } else {
            setResult(intentResultCode);
        }
        Dlog.i("setResult:" + intentResultCode);
        super.finish();
    }

    public boolean updatedCardIsOk(boolean printMassege) {
        // check card content
        boolean isOk = (card != null);
        String errorMessage = "";

        // check image
        if (isOk) {
            try {
                if (card.getType() == FlashCardDB.CardEntry.TYPE_DEMO) {
                    isOk = (card.getImageName().length() > 0);
                    Dlog.i("getImageName().length():check:" + card.getImageName().length());
                } else {
                    isOk = (card.getImagePath().length() > 0);
                    Dlog.i("getImagePath().length():check:" + card.getImagePath().length());
                }
            } catch (Exception e) {
                isOk = false;
                Dlog.i("getImagePath():error:");
            }
            if (isOk == false) {
                errorMessage = getResources().getString(R.string.card_edit_error_message_no_image);
            }
        }


            // check text
            if (isOk) {
                try {
                    isOk = (card.getName().length() > 0);
                    Dlog.i("getName().length():check:" + card.getName().length());
                } catch (Exception e) {
                    isOk = false;
                    Dlog.i("getName():error:");
                }
                if (isOk == false) {
                    errorMessage = getResources().getString(R.string.card_edit_error_message_no_text);
                }
            }

        if(printMassege == true) {
            if (isOk == false) {
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        }

        return isOk;
    }


    public boolean updatedCardIsOk() {
        return updatedCardIsOk(false);
    }
}
