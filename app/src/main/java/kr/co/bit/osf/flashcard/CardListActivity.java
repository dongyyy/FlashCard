package kr.co.bit.osf.flashcard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kr.co.bit.osf.flashcard.common.ImageUtil;
import kr.co.bit.osf.flashcard.common.IntentExtrasName;
import kr.co.bit.osf.flashcard.common.IntentRequestCode;
import kr.co.bit.osf.flashcard.db.CardDAO;
import kr.co.bit.osf.flashcard.db.CardDTO;
import kr.co.bit.osf.flashcard.db.FlashCardDB;
import kr.co.bit.osf.flashcard.debug.Dlog;

public class CardListActivity extends AppCompatActivity {

    List<CardDTO> cardList;
    FlashCardDB db;
    CardDAO dao;
    CardDTO dto;
    Integer lastNumber = 0;
    CardListAdapter adapter;

    // send card
    int sendCardListIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        cardList = new ArrayList<>();
        db = new FlashCardDB(this);
        dao = db;
        dto = new CardDTO();//이미지셋팅용 테스트
        Intent getItem = getIntent();
        Integer BoxId = getItem.getIntExtra("BoxId",0);

        cardList = dao.getCardByBoxId(BoxId);

        GridView Card_Custom_Grid_View = (GridView)findViewById(R.id.Card_Custom_List_View);
        adapter = new CardListAdapter(this);
        Card_Custom_Grid_View.setAdapter(adapter);
        // activity change CardViewActivity
        Card_Custom_Grid_View.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int boxId = cardList.get(position).getBoxId();
                int cardId = cardList.get(position).getId();
                db.updateState(boxId, cardId);
                Intent intent = new Intent(getApplicationContext(),CardViewActivity.class);
                startActivity(intent);
            }
        });
        //편집
        Card_Custom_Grid_View.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // start card edit activity
                sendCardListIndex = position;
                CardDTO sendCard = cardList.get(sendCardListIndex);
                Intent intent = new Intent(CardListActivity.this, CardEditActivity.class);
                intent.putExtra(IntentExtrasName.REQUEST_CODE, IntentRequestCode.CARD_EDIT);
                intent.putExtra(IntentExtrasName.SEND_DATA, sendCard);
               final boolean DELETE_QUESTION = intent.getBooleanExtra("DELETE_OK",false);//삭제 요청이 오면 삭제
                if(DELETE_QUESTION == true){
                    Toast.makeText(CardListActivity.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    refreshCard(cardList);
                }
                startActivityForResult(intent, IntentRequestCode.CARD_EDIT);
                return true;
            }
        });
    }

    public class CardListAdapter extends BaseAdapter{
        Context context;
        public CardListAdapter(Context c){
            context = c;
        }

        @Override
        public int getCount() {
            return cardList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if(view == null){
                LayoutInflater inflater =(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.custom_card_list,null);
            }

            String Card_List_Number = "BoxID." + cardList.get(position).getBoxId() + " CardID." + cardList.get(position).getId();
            String Card_List_Name = cardList.get(position).getName();
            ((TextView)view.findViewById(R.id.Card_Custom_List_Name)).setText(Card_List_Name);//이름
            ImageUtil.loadCardImageIntoImageView(CardListActivity.this, cardList.get(position), ((ImageView) view.findViewById(R.id.Card_Custom_List_Image)));
            lastNumber = cardList.get(position).getId()+1;//임시 아이디,seq

            return view;
        }
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
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    }
    public void refreshCard(List<CardDTO> list){//새로고침 함수
        cardList = list;
        adapter.notifyDataSetChanged();
    }

}


/*
// read state from db
db = new FlashCardDB(this);
        stateDao = db;
        cardState = stateDao.getState();
        Dlog.i("read card state:" + cardState);

        // state.cardId > 0 : start card view activity
        if (cardState.getCardId() > 0) {
        Intent intent = new Intent(this, CardViewActivity.class);
        startActivity(intent);
        }*/
