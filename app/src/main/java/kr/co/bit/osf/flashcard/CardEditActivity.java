package kr.co.bit.osf.flashcard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
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
    private ImageView cardEditImageView = null;
    private TextView cardEditTextView = null;
    // image
    private File photoFile = null;
    private String photoFilePath = null;
    // delete card
    private List<CardDTO> cardList = null;
    // activity state
    private String activityStateDataName = "activityStateDataName";
    private ActivityState currentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_edit);
        Dlog.i("");

        // full screen
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.hide();

        // event listener
        // card image - image view
        cardEditImageView = (ImageView) findViewById(R.id.cardEditImageView);
        (findViewById(R.id.cardEditImageFrameLayout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardImageClicked();
            }
        });
        // card name - text view
        cardEditTextView = (TextView) findViewById(R.id.cardEditTextView);
        cardEditTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardTextClicked();
            }
        });
        // yes button
        (findViewById(R.id.cardEditButtonYes)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                yesButtonClicked();
            }
        });
        // no button
        (findViewById(R.id.cardEditNoButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentResultCode = RESULT_CANCELED;
                finish();
            }
        });

        //
        setupActivity();

        // first ?
        try {
            Dlog.i("not first");
            // restore saved activity state
            //currentState = savedInstanceState.getParcelable(activityStateDataName);
            Dlog.i(currentState.toString());
        } catch (Exception e) {
            Dlog.i("first");
            currentState = new ActivityState(intentRequestCode, intentResultCode, photoFilePath, card);
        }
        Dlog.i(currentState.toString());
    }

    private void yesButtonClicked() {
        Dlog.i("card:" + card);
        boolean isOk = false;
        switch (intentRequestCode) {
            case IntentRequestCode.CARD_ADD:
            case IntentRequestCode.CARD_EDIT:
                Dlog.i("intentRequestCode" + intentRequestCode);
                isOk = isValidCard(true);
                break;
        }

        if (isOk) {
            Dlog.i("check:ok:" + card);
            FlashCardDB db = new FlashCardDB(getApplicationContext());
            card.setName(cardEditTextView.getText().toString().trim());
            if (intentRequestCode == IntentRequestCode.CARD_ADD) {
                Dlog.i("check:ok:add:" + card);
                db.addCard(card);
            } else {
                Dlog.i("check:ok:update:" + card);
                db.updateCard(card);
            }
            finish();
        } else {
            Dlog.i("check:fail:" + card);
        }
    }

    private void setupActivity() {
        // get intent data
        Intent intent = getIntent();
        intentRequestCode = intent.getIntExtra(IntentExtrasName.REQUEST_CODE, 0);
        Dlog.i("intentRequestCode:" + intentRequestCode);

        switch (intentRequestCode) {
            case IntentRequestCode.CARD_ADD:
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

        // show card
        if (intentRequestCode == IntentRequestCode.CARD_EDIT) {
            ImageUtil.loadCardImageIntoImageView(this, card, cardEditImageView);
        }
        try {
            cardEditTextView.setText(card.getName());
        } catch (Exception e) {
            Dlog.i(e.toString());
        }

    }

    private void cardImageClicked() {
        Dlog.i("");

        // get user action from dialog
        final CharSequence[] items = {
                getString(R.string.card_edit_image_dialog_camera_button_text),
                getString(R.string.card_edit_image_dialog_gallery_button_text),
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(CardEditActivity.this);
        builder.setTitle(getString(R.string.card_edit_image_dialog_title));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String itemName = items[which].toString();
                Dlog.i("dialog:which:" + which + ", itemName:" + itemName);

                if (itemName.equals(getString(R.string.card_edit_image_dialog_camera_button_text))) {
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
                } else if (itemName.equals(getString(R.string.card_edit_image_dialog_gallery_button_text))) {
                    Dlog.i("cardEdit:galleryButton clicked");
                    // select card in gallery
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select Picture"),
                            IntentRequestCode.SELECT_PICTURE);

                  /*  } else if (itemName.equals(getString(R.string.card_edit_image_dialog_cancel_button_text))) {
                        Dlog.i("dialog:cancelled");
                        //dialog.dismiss();*/
                }
            }
        });
        builder.show();
    }

    private void cardTextClicked() {
        Dlog.i("");

        AlertDialog.Builder alert = new AlertDialog.Builder(CardEditActivity.this);
        alert.setTitle(R.string.card_edit_text_dialog_title_text);
        alert.setMessage(R.string.card_edit_text_dialog_message_text);

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setGravity(Gravity.CENTER);
        alert.setView(input);
        input.setGravity(Gravity.CENTER);
        Dlog.i("AlertDialog.Builder:setView");
        if (card.getName() != null) {
            input.setText(card.getName());
            input.setSelection(card.getName().length());
        } else {
            input.setText("");
        }
        Dlog.i("AlertDialog.Builder:setText");
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

        alert.show();
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
                    ImageUtil.loadCardImageIntoImageView(this, card, cardEditImageView);
                    Dlog.i("photoFilePath:" + card.getImagePath());
                    Dlog.i("photoFilePath:" + card.getImagePath());
                    break;
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // restore saved activity state
        currentState = savedInstanceState.getParcelable(activityStateDataName);
        Dlog.i(currentState.toString());
        // camera, gallery
        photoFilePath = currentState.getPhotoFilePath();
        // image, text
        try {
            ImageUtil.loadCardImageIntoImageView(this, currentState.getCard(), cardEditImageView);
            cardEditTextView.setText(currentState.card.getName());
        } catch (Exception e) {
            Dlog.e(e.toString());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // camera, gallery
        currentState.setPhotoFilePath(photoFilePath);
        // card
        currentState.setCard(card);
        // save current activity state
        outState.putParcelable(activityStateDataName, currentState);
        Dlog.i(currentState.toString());
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
                    if (isValidCard() == true) {
                        data.putExtra(IntentExtrasName.RETURN_DATA, card);
                    } else {
                        Dlog.i("isValidCard() == false");
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

    public boolean isValidCard(boolean isPrintMessage) {
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

        if(isPrintMessage == true) {
            if (isOk == false) {
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        }

        return isOk;
    }

    private boolean isValidCard() {
        return isValidCard(false);
    }

    private class ActivityState implements Parcelable {
        // intent
        private int intentRequestCode;
        private int intentResultCode;
        // camera, gallery
        private String photoFilePath;
        // card
        private CardDTO card;

        public ActivityState(int intentRequestCode, int intentResultCode,
                             String photoFilePath, CardDTO card) {
            this.intentRequestCode = intentRequestCode;
            this.intentResultCode = intentResultCode;
            this.photoFilePath = photoFilePath;
            this.card = card;
        }

        protected ActivityState(Parcel in) {
            intentRequestCode = in.readInt();
            intentResultCode = in.readInt();
            photoFilePath = in.readString();
            card = in.readParcelable(CardDTO.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(intentRequestCode);
            dest.writeInt(intentResultCode);
            dest.writeString(photoFilePath);
            dest.writeParcelable(card, flags);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public final Creator<ActivityState> CREATOR = new Creator<ActivityState>() {
            @Override
            public ActivityState createFromParcel(Parcel in) {
                return new ActivityState(in);
            }

            @Override
            public ActivityState[] newArray(int size) {
                return new ActivityState[size];
            }
        };

        public int getIntentRequestCode() {
            return intentRequestCode;
        }

        public void setIntentRequestCode(int intentRequestCode) {
            this.intentRequestCode = intentRequestCode;
        }

        public int getIntentResultCode() {
            return intentResultCode;
        }

        public void setIntentResultCode(int intentResultCode) {
            this.intentResultCode = intentResultCode;
        }

        public String getPhotoFilePath() {
            return photoFilePath;
        }

        public void setPhotoFilePath(String photoFilePath) {
            this.photoFilePath = photoFilePath;
        }

        public CardDTO getCard() {
            return card;
        }

        public void setCard(CardDTO card) {
            this.card = card;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ActivityState that = (ActivityState) o;

            if (intentRequestCode != that.intentRequestCode) return false;
            if (intentResultCode != that.intentResultCode) return false;
            if (!photoFilePath.equals(that.photoFilePath)) return false;
            return card.equals(that.card);

        }

        @Override
        public int hashCode() {
            int result = intentRequestCode;
            result = 31 * result + intentResultCode;
            result = 31 * result + photoFilePath.hashCode();
            result = 31 * result + card.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "ActivityState{" +
                    "intentRequestCode=" + intentRequestCode +
                    ", intentResultCode=" + intentResultCode +
                    ", photoFilePath='" + photoFilePath + '\'' +
                    ", card=" + card +
                    '}';
        }
    }
}
