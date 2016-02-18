package kr.co.bit.osf.flashcard;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import kr.co.bit.osf.flashcard.common.ImageUtil;
import kr.co.bit.osf.flashcard.db.CardDTO;
import kr.co.bit.osf.flashcard.db.FlashCardDB;
import kr.co.bit.osf.flashcard.db.StateDTO;
import kr.co.bit.osf.flashcard.flip3d.DisplayNextView;
import kr.co.bit.osf.flashcard.flip3d.Flip3dAnimation;

public class CardViewActivity extends AppCompatActivity {
    final String TAG = "FlashCardCardViewTag";

    FlashCardDB db = null;
    StateDTO cardState = null;
    List<CardDTO> cardList = null;

    ViewPager pager;
    CardViewPagerAdapter pagerAdapter;

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
        Log.i(TAG, "read card state:" + cardState);

        // read card list by state
        cardList = db.getCardByBoxId(cardState.getBoxId());
        Log.i(TAG, "card list:size:" + cardList.size());
        Log.i(TAG, "card list:value:" + cardList);

        // show card list
        // view pager
        pager = (ViewPager) findViewById(R.id.cardViewPager);
        // set pager adapter
        pagerAdapter = new CardViewPagerAdapter(this, cardList);
        pager.setAdapter(pagerAdapter);
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
            Log.i(TAG, "list:size():" + list.size());
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
            Log.i(TAG, "instantiateItem:position:" + position);

            View view = inflater.inflate(R.layout.activity_card_view_pager_child, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.cardViewPagerChildImage);
            TextView textView = (TextView) view.findViewById(R.id.cardViewPagerChildText);

            PagerHolder holder = new PagerHolder(list.get(position), true, imageView, textView);
            // image
            String imagePath = holder.getCard().getImagePath();
            if (holder.card.getType() == FlashCardDB.CardEntry.TYPE_USER) {
                // load image from sd card
                ImageUtil.showImageFileInImageView(imagePath, imageView);
            } else {
                // card demo data
                int imageId = context.getResources().getIdentifier("drawable/" + imagePath, null, context.getPackageName());
                imageView.setImageResource(imageId);
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

            // write holder
            view.setTag(holder);
            container.addView(view);

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            Log.i(TAG, "destroyItem:position:" + position);
            container.removeView((View) object);
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
        Log.i(TAG, "childViewClicked:holder:" + holder);
    }

    // http://www.inter-fuser.com/2009/08/android-animations-3d-flip.html
    private void applyRotation(boolean isFirstImage, float start, float end,
                               ImageView imageView, TextView textView) {
        // Find the center of image
        final float centerX = imageView.getWidth() / 2.0f;
        final float centerY = imageView.getHeight() / 2.0f;

        // Create a new 3D rotation with the supplied parameter
        // The animation listener is used to trigger the next animation
        final Flip3dAnimation rotation = new Flip3dAnimation(start, end, centerX, centerY);
        rotation.setDuration(500);
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
