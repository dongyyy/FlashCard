package kr.co.bit.osf.flashcard;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.bit.osf.flashcard.common.IntentExtrasName;
import kr.co.bit.osf.flashcard.common.IntentRequestCode;
import kr.co.bit.osf.flashcard.db.CardDTO;
import kr.co.bit.osf.flashcard.db.FlashCardDB;
import kr.co.bit.osf.flashcard.db.StateDTO;
import kr.co.bit.osf.flashcard.debug.Dlog;

public class CardViewActivity extends AppCompatActivity {
    FlashCardDB db = null;
    StateDTO cardState = null;
    List<CardDTO> cardList = null;
    Button List_View_Mode;
    ViewPager pager;
    CardViewPagerAdapter pagerAdapter;

    // view pager item map
    Map<Integer, View> itemViewMap = new HashMap<>();
    int lastPosition = -1;

    // send card
    int sendCardListIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_view);

        // todo: full screen
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

        // set start position by state card id
        int startPosition = 0;
        int stateCardId = cardState.getCardId();
        for (int i = 0; i < cardList.size(); i++) {
            if (stateCardId == cardList.get(i).getId()) {
                startPosition = i;
                break;
            }
        }
        if (startPosition < cardList.size()) {
            pager.setCurrentItem(startPosition);
        }

        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Dlog.i("position:" + position + ", lastPosition=" + lastPosition);
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

            View view = inflater.inflate(R.layout.activity_card_view_pager_child, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.cardViewPagerChildImage);
            TextView textView = (TextView) view.findViewById(R.id.cardViewPagerChildText);

            ValueAnimator flipAnimator = ValueAnimator.ofFloat(0f, 1f);
            flipAnimator.addUpdateListener(new FlipListener(imageView, textView));

            PagerHolder holder = new PagerHolder(list.get(position), position,
                    imageView, textView, flipAnimator);
            // image
            String imagePath = holder.getCard().getImagePath();
            if (holder.card.getType() == FlashCardDB.CardEntry.TYPE_USER) {
                // load image from sd card(glide)
                Glide.with(context).load(imagePath).into(imageView);
            } else {
                // card demo data(glide)
                Glide.with(context).fromResource()
                        .load(Integer.parseInt(imagePath)).into(imageView);
            }
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

        // flip animation
        holder.flip();

        // write holder
        view.setTag(holder);
        Dlog.i("holder:" + holder);
    }

    private void childViewLongClicked(View view) {
        // start card edit activity
        CardDTO sendCard = ((PagerHolder) view.getTag()).getCard();
        sendCardListIndex = ((PagerHolder) view.getTag()).getCardIndex();
        Intent intent = new Intent(this, CardEditActivity.class);
        intent.putExtra(IntentExtrasName.REQUEST_CODE, IntentRequestCode.CARD_EDIT);
        intent.putExtra(IntentExtrasName.SEND_DATA, sendCard);
        startActivityForResult(intent, IntentRequestCode.CARD_EDIT);
        Dlog.i("sendData:" + sendCard);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Dlog.i("requestCode=" + requestCode + ",resultCode=" + resultCode);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case IntentRequestCode.CARD_EDIT:
                    // get result data
                    CardDTO returnCard = data.getParcelableExtra(IntentExtrasName.RETURN_DATA);
                    Dlog.i("returnData:" + returnCard);
                    // refresh returned data
                    cardList.set(sendCardListIndex, returnCard);
                    // refresh view pager
                    pagerAdapter.notifyDataSetChanged();
                    break;
            }
        }
    }

    // inner class for pager adapter item and flip animation
    private class PagerHolder {
        private CardDTO card;
        private boolean isFront;
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
                    "isFront=" + isFront +
                    ", card=" + card +
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
}
