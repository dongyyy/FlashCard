package kr.co.bit.osf.flashcard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
    ArrayList<CardDTO> deleteCardList;
    MenuItem cardListMenuCompleted;
    FlashCardDB db;
    CardDAO dao;
    CardDTO dto;
    Integer lastNumber = 0;
    CardListAdapter adapter;
    // send card
    int sendCardListIndex = 0;
    boolean deleteMenuClicked=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        cardList = new ArrayList<>();
        deleteCardList=new ArrayList<CardDTO>();

        db = new FlashCardDB(this);
        dao = db;
        dto = new CardDTO();//이미지셋팅용 테스트
        Intent getItem = getIntent();
        Integer BoxId = getItem.getIntExtra("BoxId",0);

        cardList = dao.getCardByBoxId(BoxId);

        GridView cardCustomGridView = (GridView)findViewById(R.id.cardCustomGridView);
        adapter = new CardListAdapter(this);
        cardCustomGridView.setAdapter(adapter);
        // activity change CardViewActivity
        cardCustomGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int boxId = cardList.get(position).getBoxId();
                int cardId = cardList.get(position).getId();
                db.updateState(boxId, cardId);
                Intent intent = new Intent(getApplicationContext(), CardViewActivity.class);
                startActivity(intent);
            }
        });
        //편집
        cardCustomGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // start card edit activity
                sendCardListIndex = position;
                CardDTO sendCard = cardList.get(sendCardListIndex);
                Intent intent = new Intent(CardListActivity.this, CardEditActivity.class);
                intent.putExtra(IntentExtrasName.REQUEST_CODE, IntentRequestCode.CARD_EDIT);
                intent.putExtra(IntentExtrasName.SEND_DATA, sendCard);
                final boolean DELETE_QUESTION = intent.getBooleanExtra("DELETE_OK", false);//삭제 요청이 오면 삭제
                if (DELETE_QUESTION == true) {
                    Toast.makeText(CardListActivity.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    refreshCard(cardList);
                }
                startActivityForResult(intent, IntentRequestCode.CARD_EDIT);
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.custom_card_list_menu, menu);
        cardListMenuCompleted=menu.findItem(R.id.cardListMenuCompleted);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.cardListMenuDelete:{
                cardListMenuCompleted.setVisible(true);
                deleteMenuClicked=true;
                adapter.notifyDataSetChanged();
                return true;
            }
            case R.id.cardListMenuCompleted:{
                cardListMenuCompleted.setVisible(false);
                deleteMenuClicked=false;
                adapter.notifyDataSetChanged();
                Intent intent=new Intent(getApplicationContext(),CardEditActivity.class);
                intent.putExtra(IntentExtrasName.REQUEST_CODE,IntentRequestCode.CARD_DELETE_LIST);
                intent.putParcelableArrayListExtra(IntentExtrasName.SEND_DATA, deleteCardList);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
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
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder holder = null;

            if(view == null){
                holder = new ViewHolder();

                LayoutInflater inflater =(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.custom_card_list,null);
                holder.imageView=(ImageView)view.findViewById(R.id.cardCustomListImage);
                holder.textView=(TextView)view.findViewById(R.id.cardCustomListName);
                holder.checkBox=(CheckBox)view.findViewById(R.id.cardCustomCheckBox);

                String Card_List_Number = "BoxID." + cardList.get(position).getBoxId() + " CardID." + cardList.get(position).getId();
                String cardListName = cardList.get(position).getName();

                holder.textView.setText(cardListName);//이름
                ImageUtil.loadCardImageIntoImageView(CardListActivity.this, cardList.get(position), holder.imageView);
                lastNumber = cardList.get(position).getId()+1;//임시 아이디,seq
                view.setTag(holder);
            } else{
                holder=(ViewHolder)view.getTag();
                if(deleteMenuClicked) { //삭제 메뉴 버튼 클릭 되었을 때
                    holder.checkBox.setVisibility(View.VISIBLE);
                    holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            if (isChecked) { //체크를 할 때
                                deleteCardList.add(cardList.get(position));
                                for (int i = 0; i < deleteCardList.size(); i++)
                                    Log.d("isChecked", deleteCardList.get(i).getName());
                            } else { //체크가 해제될 때
                                deleteCardList.remove(cardList.get(position));
                            }
                        }
                    });
                } else { //삭제 완료 했을 때
                    holder.checkBox.setVisibility(View.INVISIBLE);
                }
            }

            return view;
        }
    }

    private class ViewHolder{
        public ImageView imageView;
        public TextView textView;
        public CheckBox checkBox;
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
