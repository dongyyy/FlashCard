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
import kr.co.bit.osf.flashcard.db.StateDTO;
import kr.co.bit.osf.flashcard.debug.Dlog;

public class CardListActivity extends AppCompatActivity {
    // db
    FlashCardDB db;
    CardDAO dao;
    CardDTO dto;
    StateDTO state;
    // list
    List<CardDTO> cardList;
    ArrayList<CardDTO> deleteCardList;
    // grid view
    GridView cardCustomGridView;
    CardListAdapter adapter;
    // send card
    int sendCardListIndex = 0;
    // menu
    MenuItem showDeleteCompleteButton;
    boolean deleteMenuClicked=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        deleteCardList=new ArrayList<CardDTO>();

        // db
        db = new FlashCardDB(this);
        dao = db;
        dto = new CardDTO();//이미지셋팅용 테스트
        state = db.getState();
        // read card list
        cardList = dao.getCardByBoxId(state.getBoxId());

        cardCustomGridView = (GridView)findViewById(R.id.cardCustomGridView);
        adapter = new CardListAdapter(this);
        cardCustomGridView.setAdapter(adapter);

        // activity change CardViewActivity
        cardCustomGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // card view
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
                Intent intent = new Intent(getApplicationContext(), CardEditActivity.class);
                intent.putExtra(IntentExtrasName.REQUEST_CODE, IntentRequestCode.CARD_EDIT);
                intent.putExtra(IntentExtrasName.SEND_DATA, sendCard);
                final boolean DELETE_QUESTION = intent.getBooleanExtra("DELETE_OK", false);//삭제 요청이 오면 삭제
                if (DELETE_QUESTION == true) {
                    Toast.makeText(CardListActivity.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                }
                startActivityForResult(intent, IntentRequestCode.CARD_EDIT);
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_card_list_menu, menu);
        showDeleteCompleteButton =menu.findItem(R.id.showDeleteCompleteButton);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.cardListMenuAdd:{
                /*sendCardListIndex = position;
                CardDTO sendCard = cardList.get(sendCardListIndex);
                Intent intent = new Intent(getApplicationContext(), CardEditActivity.class);
                intent.putExtra(IntentExtrasName.REQUEST_CODE, IntentRequestCode.CARD_EDIT);
                intent.putExtra(IntentExtrasName.SEND_DATA, sendCard);*/
                CardDTO cardToAdd = new CardDTO();
                cardToAdd.setBoxId(state.getBoxId());

                Intent intent = new Intent(getApplicationContext(), CardEditActivity.class);
                intent.putExtra(IntentExtrasName.REQUEST_CODE, IntentRequestCode.CARD_ADD);
                intent.putExtra(IntentExtrasName.SEND_DATA, cardToAdd);

                startActivityForResult(intent, IntentRequestCode.CARD_ADD);
                Dlog.i("ADD:startActivityForResult");
                return true;
            }
            case R.id.cardListMenuDelete:{
                showDeleteCompleteButton.setVisible(true);
                deleteMenuClicked=true;
                adapter.notifyDataSetChanged();
                return true;
            }
            case R.id.showDeleteCompleteButton:{
                Dlog.i("R.id.showDeleteCompleteButton");
                showDeleteCompleteButton.setVisible(false);
                deleteMenuClicked=false;
                adapter.notifyDataSetChanged();

                if(deleteCardList.size()!=0) {
                    Dlog.i("startActivityForResult");
                    Intent intent = new Intent(getApplicationContext(), CardEditActivity.class);
                    intent.putExtra(IntentExtrasName.REQUEST_CODE, IntentRequestCode.CARD_DELETE_LIST);
                    intent.putParcelableArrayListExtra(IntentExtrasName.SEND_DATA, deleteCardList);
                    startActivityForResult(intent, IntentRequestCode.CARD_DELETE_LIST);
                    Dlog.i("DELETE:startActivityForResult");
                    return true;
                }
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
            return cardList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder holder = null;

            holder = new ViewHolder();

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.activity_card_list_item, null);
            holder.imageView=(ImageView)view.findViewById(R.id.cardCustomListImage);
            holder.textView=(TextView)view.findViewById(R.id.cardCustomListName);
            holder.checkBox=(CheckBox)view.findViewById(R.id.cardCustomCheckBox);

            String cardListName = cardList.get(position).getName();

            holder.textView.setText(cardListName);//이름
            ImageUtil.loadCardImageIntoImageView(CardListActivity.this, cardList.get(position), holder.imageView);
            view.setTag(holder);

            Dlog.i("position:" + position + ", box:" + cardList.get(position).getName());
            holder = (ViewHolder) view.getTag();

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
                case IntentRequestCode.CARD_ADD:
                    CardDTO cardAdded = data.getParcelableExtra(IntentExtrasName.RETURN_DATA);
                    Dlog.i("addData:" + cardAdded);
                    cardList.add(cardAdded);
                    cardCustomGridView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    break;
                case IntentRequestCode.CARD_EDIT:
                    // get result data
                    CardDTO returnCard = data.getParcelableExtra(IntentExtrasName.RETURN_DATA);
                    Dlog.i("returnData:" + returnCard);
                    // refresh returned data
                    cardList.set(sendCardListIndex, returnCard);
                    // refresh view pager
                    cardCustomGridView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    break;
                case IntentRequestCode.CARD_DELETE_LIST:
                    Dlog.i("delete check");
                    Dlog.i("Delete Box ID " + state.getBoxId());
                    deleteCardList.clear();
                    cardList.clear();
                    cardList = dao.getCardByBoxId(state.getBoxId());
                    for (int i = 0; i < cardList.size(); i++) {
                        Dlog.i("cardList name" + cardList.get(i));
                    }
                    cardCustomGridView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    }
}