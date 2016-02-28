package kr.co.bit.osf.flashcard;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kr.co.bit.osf.flashcard.common.ImageUtil;
import kr.co.bit.osf.flashcard.common.IntentExtrasName;
import kr.co.bit.osf.flashcard.common.IntentRequestCode;
import kr.co.bit.osf.flashcard.common.IntentReturnCode;
import kr.co.bit.osf.flashcard.db.CardDTO;
import kr.co.bit.osf.flashcard.db.FlashCardDB;
import kr.co.bit.osf.flashcard.db.StateDTO;
import kr.co.bit.osf.flashcard.debug.Dlog;

public class CardViewActivity extends AppCompatActivity {
    // db
    FlashCardDB db = null;
    StateDTO cardState = null;
    List<CardDTO> cardList = null;

    // tts -- urstory@gmail.com
    TextToSpeech tts;

    // view pager
    ViewPager pager;
    CardViewPagerAdapter pagerAdapter;

    // view pager item map
    Map<Integer, View> itemViewMap = new HashMap<>();
    int currentPosition = 0;
    int lastPosition = -1;

    // send card
    int sendCardListIndex = 0;

    // card updated
    boolean isCardUpdated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_view);

        // tts
        ttsLoad();

        // full screen
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.hide();

        // read state from db
        db = new FlashCardDB(this);
        cardState = db.getState();
        Dlog.i("read card state:" + cardState);

        // read card list by state
        cardList = db.getCardByBoxId(cardState.getBoxId());
        Dlog.i("card list:size:" + cardList.size());
        if (cardList.size() == 0) {
            finish();
            return ;
        }
        Dlog.i("card list:value:" + cardList);

        // show card list
        // view pager
        pager = (ViewPager) findViewById(R.id.cardViewPager);
        // set pager adapter
        pagerAdapter = new CardViewPagerAdapter(this, cardList);
        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(1);

        // set current position by state card id
        int stateCardId = cardState.getCardId();
        for (int i = 0; i < cardList.size(); i++) {
            if (stateCardId == cardList.get(i).getId()) {
                currentPosition = i;
                break;
            }
        }
        if (currentPosition < cardList.size()) {
            pager.setCurrentItem(currentPosition);
            Dlog.i("setCurrentItem:currentPosition:" + currentPosition);
        }

        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // current position
                currentPosition = position;
                Dlog.i("currentPosition:" + position + ", lastPosition=" + lastPosition);
                // view pager item map
                View lastView = itemViewMap.get(lastPosition);
                if (lastView != null) {
                    Dlog.i("lastView is not null");
                    PagerHolder holder = (PagerHolder) lastView.getTag();
                    if (holder.isFlipped()) {
                        holder.flip();
                    }
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    lastPosition = pager.getCurrentItem();
                }
            }
        });
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Dlog.i("");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Dlog.i("currentPosition:" + currentPosition);
        // save current state
        if (db != null) {
            int cardId = cardList.get(currentPosition).getId();
            Dlog.i("onSaveInstanceState:cardId:" + cardId);
            db.updateState(cardState.getBoxId(), cardId);
        }
    }

    // pager adapter
    private class CardViewPagerAdapter extends PagerAdapter {
        private Context context = null;
        private LayoutInflater inflater;
        List<CardDTO> list = null;

        public CardViewPagerAdapter(Context context, List<CardDTO> list) {
            super();
            this.context = context;
            this.list = list;
            inflater = LayoutInflater.from(context);
            Dlog.i("list:size():" + list.size());
        }

        @Override
        public int getCount() {
            if (list != null) {
                return list.size();
            } else {
                return 0;
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Dlog.i("position:" + position);

            View view = inflater.inflate(R.layout.activity_card_view_pager_item, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.cardViewPagerItemImage);
            TextView textView = (TextView) view.findViewById(R.id.cardViewPagerItemText);

            ValueAnimator flipAnimator = ValueAnimator.ofFloat(0f, 1f);
            flipAnimator.addUpdateListener(new FlipListener(imageView, textView));

            PagerHolder holder = new PagerHolder(list.get(position), position,
                    imageView, textView, flipAnimator);
            // image
            ImageUtil.loadCardImageIntoImageView(CardViewActivity.this, holder.getCard(), imageView);
            // text
            textView.setText(holder.getCard().getName());

            // set click event
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    childViewClicked(v);
                }
            });

            // set long click listener
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    childViewLongClicked(v);
                    return true;
                }
            });

            // write holder
            view.setTag(holder);
            container.addView(view);

            // view pager item map
            itemViewMap.put(position, view);

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            Dlog.i("position:" + position);
            container.removeView((View) object);
        }

        @Override
        public int getItemPosition(Object object) {
            // refresh view pager
            // http://stackoverflow.com/questions/10611018/how-to-update-viewpager-content
            return POSITION_NONE;
        }
    }

    private void childViewClicked(View view) {
        // read holder
        PagerHolder holder = (PagerHolder)view.getTag();

        // tts
        if (!holder.isFlipped()) {
            try {
                String word = holder.getTextView().getText().toString();
                // http://stackoverflow.com/a/29777304
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ttsGreater21(word);
                } else {
                    ttsUnder20(word);
                }
                Dlog.i("tts:" + word);
            } catch (Exception e) {
                Dlog.i(e.toString());
            }
        }

        // flip animation
        holder.flip();

        // write holder
        view.setTag(holder);
        Dlog.i("holder:" + holder);
    }

    private void childViewLongClicked(View view) {
        Dlog.i("");
        // edit card list index
        sendCardListIndex = ((PagerHolder) view.getTag()).getCardIndex();
        Dlog.i("sendCardListIndex:" + sendCardListIndex);
        // get user action from dialog
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.edit_dialog_title, null);
        final AlertDialog dialog = dialogBuilder.setView(dialogView).create();
        // dialog title
        TextView titleTextView = (TextView) dialogView.findViewById(R.id.dialogTitleTextView);
        titleTextView.setText(getString(R.string.card_edit_dialog_menu_title));
        // edit dialog text view
        TextView dialogEditTextView = (TextView) dialogView.findViewById(R.id.dialogMenuTextViewOne);
        dialogEditTextView.setText(getString(R.string.card_edit_dialog_menu_edit_text));
        dialogEditTextView.setVisibility(View.VISIBLE);
        dialogEditTextView.setTag(dialog);
        dialogEditTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogEditTextViewClicked();
                // http://stackoverflow.com/questions/5713312/closing-a-custom-alert-dialog-on-button-click
                ((AlertDialog) v.getTag()).cancel();
                Dlog.i("dialog:edit card:clicked");
            }
        });
        // delete dialog text view
        TextView dialogDeleteTextView = (TextView) dialogView.findViewById(R.id.dialogMenuTextViewTwo);
        dialogDeleteTextView.setText(getString(R.string.card_edit_dialog_menu_delete_text));
        dialogDeleteTextView.setVisibility(View.VISIBLE);
        dialogDeleteTextView.setTag(dialog);
        dialogDeleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogDeleteTextViewClicked();
                // http://stackoverflow.com/questions/5713312/closing-a-custom-alert-dialog-on-button-click
                ((AlertDialog) v.getTag()).cancel();
                Dlog.i("dialog:delete card:clicked");
            }
        });
        dialog.show();
    }

    private void dialogEditTextViewClicked() {
        startEditAcitivity(IntentRequestCode.CARD_EDIT);
    }

    private void dialogDeleteTextViewClicked() {
        // get user action from dialog
        // dialog view
        View dialogView = getLayoutInflater().inflate(R.layout.edit_dialog_title, null);
        TextView titleTextView = (TextView) dialogView.findViewById(R.id.dialogTitleTextView);
        titleTextView.setText(getString(R.string.card_edit_dialog_delete_title));
        // delete confirm message
        TextView dialogDeleteConfirmMessage = (TextView) dialogView.findViewById(R.id.dialogMenuTextViewOne);
        dialogDeleteConfirmMessage.setText(getString(R.string.card_edit_dialog_delete_message));
        dialogDeleteConfirmMessage.setVisibility(View.VISIBLE);
        // delete dialog
        final AlertDialog.Builder confirmDialog = new AlertDialog.Builder(this);
        confirmDialog.setView(dialogView);
        // positive button
        confirmDialog.setPositiveButton(R.string.box_edit_dialog_menu_delete_text,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startEditAcitivity(IntentRequestCode.CARD_DELETE);
                    }
                });
        // negative button
        confirmDialog.setNegativeButton(R.string.box_edit_dialog_delete_cancel_button_text,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });
        confirmDialog.show();
    }

    private void startEditAcitivity(int requestCode) {
        // edit card, delete card
        switch (requestCode) {
            case IntentRequestCode.CARD_EDIT:
            case IntentRequestCode.CARD_DELETE:
                // card
                CardDTO sendCard = cardList.get(sendCardListIndex);
                Dlog.i("sendCard:" + sendCard);
                // start card edit activity
                if (sendCard != null) {
                    Intent intent = new Intent(CardViewActivity.this, CardEditActivity.class);
                    intent.putExtra(IntentExtrasName.REQUEST_CODE, requestCode);
                    intent.putExtra(IntentExtrasName.SEND_DATA, sendCard);
                    startActivityForResult(intent, requestCode);
                    Dlog.i("sendData:" + sendCard);
                }
                Dlog.i("sendCardListIndex:" + sendCardListIndex);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Dlog.i("requestCode=" + requestCode + ",resultCode=" + resultCode);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case IntentRequestCode.CARD_EDIT:
                    // card is updated
                    isCardUpdated = true;
                    // get result data
                    CardDTO returnCard = data.getParcelableExtra(IntentExtrasName.RETURN_DATA);
                    Dlog.i("returnData:" + returnCard);
                    // refresh returned data
                    if (returnCard != null) {
                        cardList.set(sendCardListIndex, returnCard);
                        // refresh view pager
                        pagerAdapter.notifyDataSetChanged();
                    }
                    break;
                case IntentRequestCode.CARD_DELETE:
                    // card is updated
                    isCardUpdated = true;
                    // delete returned data
                    Dlog.i("sendCardListIndex:" + sendCardListIndex);
                    itemViewMap.remove(sendCardListIndex);
                    if (sendCardListIndex >= 0 && sendCardListIndex < cardList.size()) {
                        cardList.remove(sendCardListIndex);
                    }
                    // refresh view pager
                    pagerAdapter.notifyDataSetChanged();
                    if (cardList.size() > 0) {
                        currentPosition = sendCardListIndex - 1;
                        if (currentPosition < 0 || currentPosition >= cardList.size()) {
                            currentPosition = 0;
                        }
                        pager.setCurrentItem(currentPosition);
                        Dlog.i("currentPosition:" + currentPosition);
                    } else {
                        finish();
                        return ;
                    }
                    break;
            }
        }
    }

    @Override
    public void finish() {
        // is updated?
        int returnCode = (isCardUpdated ? IntentReturnCode.CARD_LIST_REFRESH : IntentReturnCode.NONE);
        Dlog.i("isCardUpdated:" + isCardUpdated);
        // return data
        int intentResultCode = RESULT_OK;
        Intent data = new Intent();
        data.putExtra(IntentExtrasName.RETURN_CODE, returnCode);
        setResult(intentResultCode, data);
        Dlog.i("setResult:" + intentResultCode + ", returnCode:" + returnCode);
        super.finish();
    }

    // inner class for pager adapter item and flip animation
    private class PagerHolder {
        private CardDTO card;
        private ImageView imageView;
        private TextView textView;
        private int cardIndex;
        ValueAnimator flipAnimator;

        public PagerHolder(CardDTO card, int index,
                           ImageView imageView, TextView textView, ValueAnimator flipAnimator) {
            this.card = card;
            this.cardIndex = index;
            this.imageView = imageView;
            this.textView = textView;
            this.flipAnimator = flipAnimator;
        }

        public ImageView getImageView() {
            return imageView;
        }

        public TextView getTextView() {
            return textView;
        }

        public void flip() {
            toggleFlip();
        }

        private void toggleFlip() {
            if(isFlipped()){
                flipAnimator.reverse();
            } else {
                flipAnimator.start();
            }
        }

        public boolean isFlipped() {
            return flipAnimator.getAnimatedFraction() == 1;
        }

        public CardDTO getCard() {
            return card;
        }

        public int getCardIndex() {
            return cardIndex;
        }

        @Override
        public String toString() {
            return "pagerHolder{" +
                    "card=" + card +
                    ", cardIndex=" + cardIndex +
                    '}';
        }
    }

    // flip animation
    // http://stackoverflow.com/questions/7785649/creating-a-3d-flip-animation-in-android-using-xml
    private class FlipListener implements ValueAnimator.AnimatorUpdateListener {
        private final View mFrontView;
        private final View mBackView;
        private boolean mFlipped;

        public FlipListener(final View front, final View back) {
            this.mFrontView = front;
            this.mBackView = back;
            this.mBackView.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationUpdate(final ValueAnimator animation) {
            final float value = animation.getAnimatedFraction();
            final float scaleValue = 0.625f + (1.5f * (value - 0.5f) * (value - 0.5f));

            if(value <= 0.5f){
                this.mFrontView.setRotationY(180 * value);
                this.mFrontView.setScaleX(scaleValue);
                this.mFrontView.setScaleY(scaleValue);
                if(mFlipped){
                    setStateFlipped(false);
                }
            } else {
                this.mBackView.setRotationY(-180 * (1f- value));
                this.mBackView.setScaleX(scaleValue);
                this.mBackView.setScaleY(scaleValue);

                if(!mFlipped){
                    setStateFlipped(true);
                }
            }
        }

        private void setStateFlipped(boolean flipped) {
            mFlipped = flipped;
            this.mFrontView.setVisibility(flipped ? View.GONE : View.VISIBLE);
            this.mBackView.setVisibility(flipped ? View.VISIBLE : View.GONE);
        }
    }

    // tts
    @SuppressWarnings("deprecation")
    private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    // tts
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsGreater21(String text) {
        String utteranceId=this.hashCode() + "";
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    // tts
    public void onPause() {
        super.onPause();
        ttsUnload();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ttsLoad();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ttsUnload();
    }

    private void ttsLoad() {
        try {
            if (tts == null) {
                tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if (status != TextToSpeech.ERROR) {
                            tts.setLanguage(Locale.US);
                        }
                    }
                });
            }
        } catch (Exception e) {
            Dlog.e(e.toString());
        }
    }

    private void ttsUnload() {
        try {
            if (tts != null) {
                tts.stop();
                tts.shutdown();
                tts = null;
            }
        } catch (Exception e) {
            Dlog.e(e.toString());
        }
    }
}
