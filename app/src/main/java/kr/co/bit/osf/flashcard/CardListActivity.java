package kr.co.bit.osf.flashcard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import kr.co.bit.osf.flashcard.common.ImageUtil;
import kr.co.bit.osf.flashcard.common.IntentExtrasName;
import kr.co.bit.osf.flashcard.common.IntentRequestCode;
import kr.co.bit.osf.flashcard.common.IntentReturnCode;
import kr.co.bit.osf.flashcard.db.CardDAO;
import kr.co.bit.osf.flashcard.db.CardDTO;
import kr.co.bit.osf.flashcard.db.FlashCardDB;
import kr.co.bit.osf.flashcard.db.StateDTO;
import kr.co.bit.osf.flashcard.debug.Dlog;

public class CardListActivity extends AppCompatActivity {
    // db
    FlashCardDB db=null;
    CardDAO dao=null;
    CardDTO dto=null;
    StateDTO state=null;
    // list
    List<CardDTO> cardList=null;
    ArrayList<CardDTO> deleteCardList=null;
    HashMap<Integer, Boolean> hashMap=null;
    //card list update
    boolean isCardListUpdated = false;
    // grid view
    GridView cardCustomGridView=null;
    CardListAdapter adapter=null;
    // send card
    int sendCardListIndex = 0;
    // menu
    MenuItem cardListMenuAdd=null;
    MenuItem showDeleteCompleteButton=null;
    Menu optionMenuGroup=null;
    boolean deleteMenuClicked = false;
    // dialog
    DialogInterface dialogInterface=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        // db
        db = new FlashCardDB(this);
        dao = db;
        dto = new CardDTO(); //이미지셋팅용 테스트
        state = db.getState();

        if (state.getCardId() > 0) {
            Intent intent = new Intent(this, CardViewActivity.class);
            startActivityForResult(intent, IntentRequestCode.CARD_VIEW);
        }

        setTitle(db.getBox(state.getBoxId()).getName()); //카드 박스 이름 정하기

        deleteCardList = new ArrayList<CardDTO>();
        // read card list
        cardList = dao.getCardByBoxId(state.getBoxId());
        hashMap=new HashMap<Integer, Boolean>();

        for(int i=0; i<cardList.size(); i++){
            hashMap.put(cardList.get(i).getSeq(),false);
        }

        cardCustomGridView = (GridView) findViewById(R.id.cardCustomGridView);
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
                startActivityForResult(intent, IntentRequestCode.CARD_VIEW);
                Dlog.i("startActivityForResult");
            }
        });
        //편집
        cardCustomGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                // start card edit activity
                View dlg = CardListActivity.this.getLayoutInflater().inflate(R.layout.dialog_title, null);
                Dlog.i("Card Item long click dialog - add View");
                final AlertDialog.Builder builder = new AlertDialog.Builder(CardListActivity.this);
                Dlog.i("Card Item long click dialog - add AlertDialog.Builder");
                TextView dlgTitle = (TextView) dlg.findViewById(R.id.dialogTitleTextView);
                TextView textMenuOne = (TextView) dlg.findViewById(R.id.dialogMenuTextViewOne);
                TextView textMenuTwo = (TextView) dlg.findViewById(R.id.dialogMenuTextViewTwo);
                Dlog.i("Card Item long click dialog - add TextView");
                dlgTitle.setText(R.string.card_view_edit_dialog_title);
                textMenuOne.setText(R.string.card_view_edit_dialog_edit_button_text);
                textMenuTwo.setText(R.string.card_view_edit_dialog_delete_button_text);

                sendCardListIndex = position;
                //edit card
                textMenuOne.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(cardList.size() >= sendCardListIndex) {
                            CardDTO sendCard = cardList.get(sendCardListIndex);
                            if (sendCard != null) {
                                Intent intent = new Intent(getApplicationContext(), CardEditActivity.class);
                                intent.putExtra(IntentExtrasName.REQUEST_CODE, IntentRequestCode.CARD_EDIT);
                                intent.putExtra(IntentExtrasName.SEND_DATA, sendCard);
                                startActivityForResult(intent, IntentRequestCode.CARD_EDIT);
                                dialogInterface.dismiss();
                            }
                        }
                    }
                });
                //delete card
                textMenuTwo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(cardList.size() >= sendCardListIndex) {
                            CardDTO sendCard = cardList.get(sendCardListIndex);
                            if (sendCard != null) {
                                Intent intent = new Intent(getApplicationContext(), CardEditActivity.class);
                                intent.putExtra(IntentExtrasName.REQUEST_CODE, IntentRequestCode.CARD_DELETE);
                                intent.putExtra(IntentExtrasName.SEND_DATA, sendCard);
                                startActivityForResult(intent, IntentRequestCode.CARD_DELETE);
                                dialogInterface.dismiss();
                            }
                        }
                    }
                });

                Dlog.i("Card Item long click dialog - add setText");
                dlgTitle.setVisibility(View.VISIBLE);
                textMenuOne.setVisibility(View.VISIBLE);
                textMenuTwo.setVisibility(View.VISIBLE);
                Dlog.i("Card Item long click dialog - add Visible");
                builder.setView(dlg);
                Dlog.i("Card Item long click dialog - set View");
                dialogInterface = builder.show();

                Dlog.i("Card Item long click dialog - builder.show");
                return true;
            }
        });
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Dlog.i("state.getBoxId():" + state.getBoxId());
        // save current state
        if (db != null) {
            Dlog.i("onSaveInstanceState:cardId:" + state.getBoxId());
            db.updateState(state.getBoxId(), 0);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_card_list_menu, menu);
        cardListMenuAdd = menu.findItem(R.id.cardListMenuAdd);
        showDeleteCompleteButton = menu.findItem(R.id.showDeleteCompleteButton);
        optionMenuGroup = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.cardListMenuAdd: {
                CardDTO cardToAdd = new CardDTO();
                cardToAdd.setBoxId(state.getBoxId());

                Intent intent = new Intent(getApplicationContext(), CardEditActivity.class);
                intent.putExtra(IntentExtrasName.REQUEST_CODE, IntentRequestCode.CARD_ADD);
                intent.putExtra(IntentExtrasName.SEND_DATA, cardToAdd);
                startActivityForResult(intent, IntentRequestCode.CARD_ADD);
                Dlog.i("ADD:startActivityForResult");
                return true;
            }
            case R.id.cardListMenuDelete: {
                showDeleteCompleteButton.setVisible(true);
                deleteMenuClicked = true;
                optionMenuGroup.setGroupEnabled(R.id.optionMenuGroup, false); //option menu 비활성화
                cardListMenuAdd.setVisible(false);

                adapter.notifyDataSetChanged();
                return true;
            }
            case R.id.showDeleteCompleteButton: {
                Dlog.i("R.id.showDeleteCompleteButton");
                showDeleteCompleteButton.setVisible(false);
                deleteMenuClicked = false;
                optionMenuGroup.setGroupEnabled(R.id.optionMenuGroup, true); //option menu 활성화
                cardListMenuAdd.setVisible(true);
                adapter.notifyDataSetChanged();

                if (deleteCardList.size() != 0) {
                    Dlog.i("startActivityForResult");
                    Intent intent = new Intent(getApplicationContext(), CardEditActivity.class);
                    intent.putExtra(IntentExtrasName.REQUEST_CODE, IntentRequestCode.CARD_DELETE_LIST);
                    intent.putParcelableArrayListExtra(IntentExtrasName.SEND_DATA, deleteCardList);
                    startActivityForResult(intent, IntentRequestCode.CARD_DELETE_LIST);
                    Dlog.i("DELETE:startActivityForResult");
                    return true;
                }
                break;
            }
            case R.id.cardListMenuShuffle: {
                Collections.shuffle(cardList);
                db.updateCardSeq(cardList);
                refreshCardList();
                break;
            }
            case R.id.cardListMenuAscSort: {
                Collections.sort(cardList, new NameAscCompare());
                db.updateCardSeq(cardList);
                refreshCardList();
                break;
            }
            case R.id.cardListMenuDescSort: {
                Collections.sort(cardList, new NameDescCompare());
                db.updateCardSeq(cardList);
                refreshCardList();
                break;
            }
            case R.id.cardListMenuInitial: {
                Collections.sort(cardList, new NoAscCompare());
                db.updateCardSeq(cardList);
                refreshCardList();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public class CardListAdapter extends BaseAdapter {
        Context context;

        public CardListAdapter(Context c) {
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
            ViewHolder holder = new ViewHolder();

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.activity_card_list_item, null);

            holder.imageView = (ImageView) convertView.findViewById(R.id.cardCustomListImage);
            holder.textView = (TextView) convertView.findViewById(R.id.cardCustomListName);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.cardCustomCheckBox);

            if(hashMap.get(cardList.get(position).getSeq())){
                holder.checkBox.setChecked(true);
            }

            String cardListName = cardList.get(position).getName();
            holder.textView.setText(cardListName);//이름
            ImageUtil.loadCardImageIntoImageView(CardListActivity.this, cardList.get(position), holder.imageView);
            convertView.setTag(holder);

            Dlog.i("position:" + position + ", box:" + cardList.get(position).getName());
            holder = (ViewHolder) convertView.getTag();

            if (deleteMenuClicked) { //삭제 메뉴 버튼 클릭 되었을 때
                holder.checkBox.setVisibility(View.VISIBLE);
                holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) { //체크를 할 때
                            deleteCardList.add(cardList.get(position));
                            hashMap.put(cardList.get(position).getSeq(),true);
                        } else { //체크가 해제될 때
                            deleteCardList.remove(cardList.get(position));
                            hashMap.put(cardList.get(position).getSeq(),false);
                        }
                    }
                });
            } else { //삭제 완료 했을 때
                holder.checkBox.setVisibility(View.INVISIBLE);
            }
            return convertView;
        }
    }

    private class ViewHolder {
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
                    try{
                        if(data.getParcelableExtra(IntentExtrasName.RETURN_DATA)!=null) {
                            CardDTO cardAdded = data.getParcelableExtra(IntentExtrasName.RETURN_DATA);
                            Dlog.i("addData:"+cardAdded);
                            if(db.getCard(cardAdded.getId()) != null) {
                                Dlog.i("db.getCard(cardAdded.getId()) : " + db.getCard(cardAdded.getId()));
                                cardList.add(cardAdded);
                                refreshCardList();
                            }
                        }
                    } catch (Exception e){
                        e.toString();
                    }
                    break;
                case IntentRequestCode.CARD_EDIT:
                    try{
                        if(data.getParcelableExtra(IntentExtrasName.RETURN_DATA)!=null){
                            CardDTO editReturnCard = data.getParcelableExtra(IntentExtrasName.RETURN_DATA);
                            Dlog.i("returnData:" + editReturnCard);
                            cardList.set(sendCardListIndex, editReturnCard);
                            refreshCardList();
                        }
                    } catch (Exception e){
                        e.toString();
                    }
                    break;
                case IntentRequestCode.CARD_DELETE:
                    cardList.remove(sendCardListIndex);
                    refreshCardList();
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
                    refreshCardList();
                    break;
                case IntentRequestCode.CARD_VIEW:
                    int returnCode = data.getIntExtra(IntentExtrasName.RETURN_CODE, 0);
                    Dlog.i("" + returnCode);
                    switch (returnCode) {
                        case IntentReturnCode.NONE: {
                            break;
                        }
                        case IntentReturnCode.CARD_LIST_REFRESH: {
                            cardList.clear();
                            cardList = dao.getCardByBoxId(state.getBoxId());
                            refreshCardList();
                            break;
                        }
                        case IntentReturnCode.BOX_LIST_REFRESH: {
                            break;
                        }
                    }
                    break;
            }
        }
    }

    public void refreshCardList() {
        hashMap.clear();
        for(int i=0; i<cardList.size(); i++){
            hashMap.put(cardList.get(i).getSeq(),false);
        }
        isCardListUpdated = true;
        cardCustomGridView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void finish() {
        //is update?
        int returnCode = (isCardListUpdated ? IntentReturnCode.BOX_LIST_REFRESH : IntentReturnCode.NONE);
        Dlog.i("isCardListUpdated:" + isCardListUpdated);
        Dlog.i(returnCode + "");
        //return data
        int intentResultCode = RESULT_OK;
        Intent data = new Intent();
        data.putExtra(IntentExtrasName.RETURN_CODE, returnCode);
        setResult(intentResultCode, data);
        super.finish();
    }

    static class NameAscCompare implements Comparator<CardDTO> {

        @Override
        public int compare(CardDTO arg0, CardDTO arg1) {
            // TODO Auto-generated method stub
            return arg0.getName().compareTo(arg1.getName());
        }

    }

    static class NameDescCompare implements Comparator<CardDTO> {

        @Override
        public int compare(CardDTO arg0, CardDTO arg1) {
            // TODO Auto-generated method stub
            return arg1.getName().compareTo(arg0.getName());
        }

    }

    static class NoAscCompare implements Comparator<CardDTO> {

        @Override
        public int compare(CardDTO arg0, CardDTO arg1) {
            // TODO Auto-generated method stub
            return arg0.getId() < arg1.getId() ? -1 : arg0.getId() > arg1.getId() ? 1 : 0;
        }

    }

}