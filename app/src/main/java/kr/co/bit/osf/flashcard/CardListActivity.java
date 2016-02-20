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

import java.util.ArrayList;
import java.util.List;

import kr.co.bit.osf.flashcard.db.CardDAO;
import kr.co.bit.osf.flashcard.db.CardDTO;
import kr.co.bit.osf.flashcard.db.FlashCardDB;

public class CardListActivity extends AppCompatActivity {

    List<CardDTO> CardList;
    FlashCardDB db;
    CardDAO dao;
    CardDTO dto;
    Integer LastNumber = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);
        CardList = new ArrayList<>();
        db = new FlashCardDB(this);

        dao = db;

        dto = new CardDTO();//이미지셋팅용 테스트
        Intent getItem = getIntent();
        Integer BoxId = getItem.getIntExtra("BoxId",0);

        CardList = dao.getCardByBoxId(BoxId);

        GridView Card_Custom_Grid_View = (GridView)findViewById(R.id.Card_Custom_List_View);

        CardListAdapter adapter = new CardListAdapter(this);

        Card_Custom_Grid_View.setAdapter(adapter);


        //todo: activity change CardViewActivity
        //카드리스트 이동
        Card_Custom_Grid_View.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getApplicationContext(),CardViewActivity.class);
                startActivity(intent);

            }
        });

        //편집
        //todo: activity change CardEditActivity
        Card_Custom_Grid_View.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
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
            return CardList.size();
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



            String Card_List_Number = "BoxID." + CardList.get(position).getBoxId() + " CardID." + CardList.get(position).getId();
            String Card_List_Name = CardList.get(position).getName();

            ((TextView)view.findViewById(R.id.Card_Custom_List_Number)).setText(Card_List_Number);//숫자
            ((TextView)view.findViewById(R.id.Card_Custom_List_Name)).setText(Card_List_Name);//이름
            //ImageUtil.showImageFileInImageView(Card_List_Image, Card_Custom_List_Image);
            String Card_List_Image = CardList.get(position).getImagePath();
            Integer Card_List_Image_Path = view.getResources().getIdentifier("drawable/" + Card_List_Image, null, CardListActivity.this.getPackageName());
            ((ImageView)view.findViewById(R.id.Card_Custom_List_Image)).setImageResource(Card_List_Image_Path);//이미지 셋팅
            LastNumber = CardList.get(position).getId()+1;//임시 아이디,seq
            return view;
        }
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
