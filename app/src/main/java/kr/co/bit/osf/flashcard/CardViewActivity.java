package kr.co.bit.osf.flashcard;

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
import android.view.animation.AccelerateInterpolator;
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
import kr.co.bit.osf.flashcard.flip3d.DisplayNextView;
import kr.co.bit.osf.flashcard.flip3d.Flip3dAnimation;

public class CardViewActivity extends AppCompatActivity {
    FlashCardDB db = null;
    StateDTO cardState = null;
    List<CardDTO> cardList = null;

    ViewPager pager;
    CardViewPagerAdapter pagerAdapter;

    // view pager item map
    Map<Integer, View> itemViewMap = new HashMap<>();
    int lastPosition = -1;

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
                    if (!holder.isFront()) {
                        Dlog.i("lastView is back");
                        // show image
                        applyRotation(holder.isFront(), 0, 90,
                                holder.getImageView(), holder.getTextView(), true);
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

            PagerHolder holder = new PagerHolder(list.get(position), true, imageView, textView);
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
            imageView.setVisibility(View.VISIBLE);
            // text
            textView.setText(holder.getCard().getName());
            textView.setVisibility(View.INVISIBLE);

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
        if (holder.isFront()) {
            // show text
            applyRotation(holder.isFront(), 0, -90, holder.getImageView(), holder.getTextView());
        } else {
            // show image
            applyRotation(holder.isFront(), 0, 90, holder.getImageView(), holder.getTextView());
        }

        // change front/back state
        holder.flip();

        // write holder
        view.setTag(holder);
        Dlog.i("holder:" + holder);
    }

    private void childViewLongClicked(View view) {
        // start card edit activity
        CardDTO sendCard = ((PagerHolder) view.getTag()).getCard();
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
                    // todo: refresh data
                    // refresh view pager
                    pagerAdapter.notifyDataSetChanged();
                    break;
            }
        }
    }

    // http://www.inter-fuser.com/2009/08/android-animations-3d-flip.html
    private void applyRotation(boolean isFirstImage, float start, float end,
                               ImageView imageView, TextView textView) {
        applyRotation(isFirstImage, start, end, imageView, textView, false);
    }

    private void applyRotation(boolean isFirstImage, float start, float end,
                               ImageView imageView, TextView textView,
                               boolean isNoAnimation) {
        long duration = 500;
        if (isNoAnimation) duration = 0;

        // Find the center of image
        final float centerX = imageView.getWidth() / 2.0f;
        final float centerY = imageView.getHeight() / 2.0f;

        // Create a new 3D rotation with the supplied parameter
        // The animation listener is used to trigger the next animation
        final Flip3dAnimation rotation = new Flip3dAnimation(start, end, centerX, centerY);
        rotation.setDuration(duration);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());
        rotation.setAnimationListener(new DisplayNextView(isFirstImage, imageView, textView));

        if (isFirstImage) {
            imageView.startAnimation(rotation);
        } else {
            textView.startAnimation(rotation);
        }
    }

    // inner class for pager adapter item and flip animation
    private class PagerHolder {
        private CardDTO card;
        private boolean isFront;
        private ImageView imageView;
        private TextView textView;

        public PagerHolder(CardDTO card, boolean isFront, ImageView imageView, TextView textView) {
            this.isFront = isFront;
            this.imageView = imageView;
            this.textView = textView;
            this.card = card;
        }

        public ImageView getImageView() {
            return imageView;
        }

        public TextView getTextView() {
            return textView;
        }

        public boolean isFront() {
            return isFront;
        }

        public void flip() {
            this.isFront = !this.isFront;
        }

        public CardDTO getCard() {
            return card;
        }

        @Override
        public String toString() {
            return "pagerHolder{" +
                    "isFront=" + isFront +
                    ", card=" + card +
                    '}';
        }
    }
}
