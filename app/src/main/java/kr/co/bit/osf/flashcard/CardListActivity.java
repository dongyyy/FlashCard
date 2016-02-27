package kr.co.bit.osf.flashcard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    FlashCardDB db = null;
    CardDAO dao = null;
    StateDTO state = null;
    // list
    List<CardDTO> cardList = null;
    HashMap<Integer, Boolean> deleteCardIdMap = null;
    //card list update
    boolean isCardListUpdated = false;
    // grid view
    GridView cardCustomGridView = null;
    CardListAdapter adapter = null;
    // send card
    int sendCardListIndex = 0;
    // menu
    MenuItem cardListMenuAdd = null;
    MenuItem showDeleteCompleteButton = null;
    Menu optionMenuGroup = null;
    boolean deleteMenuClicked = false;
    // dialog
    DialogInterface dialogInterface = null;
    // activity state
    private String activityStateDataName = "activityStateDataName";
    private ActivityState currentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        // db
        db = new FlashCardDB(this);
        state = db.getState();
        if (state.getCardId() > 0) {
            Intent intent = new Intent(this, CardViewActivity.class);
            startActivityForResult(intent, IntentRequestCode.CARD_VIEW);
        }

        //카드 박스 이름 정하기
        try {
            setTitle(db.getBox(state.getBoxId()).getName());
        } catch (Exception e) {
            Dlog.e(e.toString());
        }

        // read card list
        cardList = db.getCardByBoxId(state.getBoxId());
        deleteCardIdMap = new HashMap<>();

        // grid view
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
                View dlg = CardListActivity.this.getLayoutInflater().inflate(R.layout.edit_dialog_title, null);
                Dlog.i("Card Item long click dialog - add View");
                final AlertDialog.Builder builder = new AlertDialog.Builder(CardListActivity.this);
                Dlog.i("Card Item long click dialog - add AlertDialog.Builder");
                TextView dlgTitle = (TextView) dlg.findViewById(R.id.dialogTitleTextView);
                TextView textMenuOne = (TextView) dlg.findViewById(R.id.dialogMenuTextViewOne);
                TextView textMenuTwo = (TextView) dlg.findViewById(R.id.dialogMenuTextViewTwo);
                Dlog.i("Card Item long click dialog - add TextView");
                dlgTitle.setText(R.string.card_view_edit_dialog_title);
                textMenuOne.setText(R.string.card_edit_dialog_menu_edit_text);
                textMenuTwo.setText(R.string.card_edit_dialog_menu_delete_text);

                sendCardListIndex = position;
                //edit card
                textMenuOne.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (cardList.size() >= sendCardListIndex) {
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
                        View dlg2 = CardListActivity.this.getLayoutInflater().inflate(R.layout.edit_dialog_title, null);

                        TextView deleteTitle = (TextView) dlg2.findViewById(R.id.dialogTitleTextView);
                        TextView deleteMessage = (TextView) dlg2.findViewById(R.id.dialogMenuTextViewOne);

                        deleteTitle.setVisibility(View.VISIBLE);
                        deleteMessage.setVisibility(View.VISIBLE);

                        final AlertDialog.Builder delete = new AlertDialog.Builder(CardListActivity.this);

                        deleteTitle.setText(R.string.card_edit_dialog_delete_title);
                        deleteMessage.setText(R.string.card_edit_dialog_delete_message);

                        delete.setView(dlg2);

                        delete.setPositiveButton(R.string.card_edit_dialog_delete_ok_button_text,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (cardList.size() >= sendCardListIndex) {
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
                        delete.setNegativeButton(R.string.card_edit_dialog_delete_cancel_button_text,
                                new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // Canceled.
                                    }
                                });
                        dialogInterface.dismiss();
                        delete.show();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_card_list_menu, menu);
        cardListMenuAdd = menu.findItem(R.id.cardListMenuAdd);
        showDeleteCompleteButton = menu.findItem(R.id.showDeleteCompleteButton);
        optionMenuGroup = menu;

        // restore instance state
        if (deleteMenuClicked) {
            setupDeleteMenuClicked();
        }

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
                setupDeleteMenuClicked();
                return true;
            }
            case R.id.showDeleteCompleteButton: {
                Dlog.i("R.id.showDeleteCompleteButton");
                showDeleteCompleteButton.setVisible(false);
                deleteMenuClicked = false;
                optionMenuGroup.setGroupEnabled(R.id.optionMenuGroup, true); //option menu 활성화
                cardListMenuAdd.setVisible(true);

                if (deleteCardIdMap.size() > 0) {
                    ArrayList<CardDTO> deleteList = new ArrayList<>();
                    for(CardDTO card : cardList) {
                        if (deleteCardIdMap.get(card.getId()) != null) {
                            deleteList.add(card);
                        }
                    }
                    if (deleteList.size() > 0) {
                        Dlog.i("startActivityForResult");
                        Intent intent = new Intent(getApplicationContext(), CardEditActivity.class);
                        intent.putExtra(IntentExtrasName.REQUEST_CODE, IntentRequestCode.CARD_DELETE_LIST);
                        intent.putParcelableArrayListExtra(IntentExtrasName.SEND_DATA, deleteList);
                        startActivityForResult(intent, IntentRequestCode.CARD_DELETE_LIST);
                        Dlog.i("DELETE:startActivityForResult");
                    }
                    deleteCardIdMap.clear();
                }
                adapter.notifyDataSetChanged();
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

    private void setupDeleteMenuClicked() {
        showDeleteCompleteButton.setVisible(true);
        deleteMenuClicked = true;
        optionMenuGroup.setGroupEnabled(R.id.optionMenuGroup, false); //option menu 비활성화
        cardListMenuAdd.setVisible(false);

        adapter.notifyDataSetChanged();
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
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.activity_card_list_item, null);

            final ViewHolder holder = new ViewHolder();
            holder.layout = (LinearLayout) convertView.findViewById(R.id.cardListItemBackground);
            holder.imageView = (ImageView) convertView.findViewById(R.id.cardCustomListImage);
            holder.textView = (TextView) convertView.findViewById(R.id.cardCustomListName);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.cardCustomCheckBox);

            Boolean isChecked = deleteCardIdMap.get(cardList.get(position).getId());
            if (isChecked != null) {
                holder.checkBox.setChecked(true);
            } else {
                holder.checkBox.setChecked(false);
            }

            String cardListName = cardList.get(position).getName();
            holder.textView.setText(cardListName);//이름
            ImageUtil.loadCardImageIntoImageView(CardListActivity.this, cardList.get(position), holder.imageView);
            convertView.setTag(holder);

            Dlog.i("position:" + position + ", box:" + cardList.get(position).getName());

            if (deleteMenuClicked) { //삭제 메뉴 버튼 클릭 되었을 때
                holder.checkBox.setVisibility(View.VISIBLE);
                holder.layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Dlog.i("onItemClicked");
                        if (deleteMenuClicked) { //삭제 메뉴 버튼 클릭 되었을 때
                            Dlog.i("deleteMenuClicked:onItemClicked");

                            int cardId = cardList.get(position).getId();
                            Boolean isChecked = deleteCardIdMap.get(cardId);
                            if (isChecked != null) {
                                holder.checkBox.setChecked(false);
                                deleteCardIdMap.remove(cardId);
                            } else {
                                holder.checkBox.setChecked(true);
                                deleteCardIdMap.put(cardId, true);
                            }
                        }
                        Dlog.i("layout.onClickFinished");
                    }
                });
            } else { //삭제 완료 했을 때
                holder.checkBox.setVisibility(View.INVISIBLE);
                Dlog.i("delete completed");
            }
            return convertView;
        }
    }

    private class ViewHolder {
        public LinearLayout layout;
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
                    try {
                        if (data.getParcelableExtra(IntentExtrasName.RETURN_DATA) != null) {
                            CardDTO cardAdded = data.getParcelableExtra(IntentExtrasName.RETURN_DATA);
                            Dlog.i("addData:" + cardAdded);
                            if (db.getCard(cardAdded.getId()) != null) {
                                Dlog.i("db.getCard(cardAdded.getId()) : " + db.getCard(cardAdded.getId()));
                                cardList.add(cardAdded);
                                refreshCardList();
                                // move last position
                                if (adapter.getCount() > 0) {
                                    cardCustomGridView.smoothScrollToPosition(adapter.getCount() - 1);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Dlog.e(e.toString());
                    }
                    break;
                case IntentRequestCode.CARD_EDIT:
                    try {
                        if (data.getParcelableExtra(IntentExtrasName.RETURN_DATA) != null) {
                            CardDTO editReturnCard = data.getParcelableExtra(IntentExtrasName.RETURN_DATA);
                            Dlog.i("returnData:" + editReturnCard);
                            cardList.set(sendCardListIndex, editReturnCard);
                            refreshCardList();
                        }
                    } catch (Exception e) {
                        Dlog.e(e.toString());
                    }
                    break;
                case IntentRequestCode.CARD_DELETE:
                    cardList.remove(sendCardListIndex);
                    refreshCardList();
                    break;
                case IntentRequestCode.CARD_DELETE_LIST:
                    Dlog.i("delete check");
                    Dlog.i("Delete Box ID " + state.getBoxId());
                    cardList.clear();
                    cardList = db.getCardByBoxId(state.getBoxId());
                    for (int i = 0; i < cardList.size(); i++) {
                        Dlog.i("cardList name" + cardList.get(i));
                    }
                    Dlog.i("Delete Completed");
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
                            cardList = db.getCardByBoxId(state.getBoxId());
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
            return arg0.getName().compareTo(arg1.getName());
        }
    }

    static class NameDescCompare implements Comparator<CardDTO> {
        @Override
        public int compare(CardDTO arg0, CardDTO arg1) {
            return arg1.getName().compareTo(arg0.getName());
        }
    }

    static class NoAscCompare implements Comparator<CardDTO> {
        @Override
        public int compare(CardDTO arg0, CardDTO arg1) {
            return arg0.getId() < arg1.getId() ? -1 : arg0.getId() > arg1.getId() ? 1 : 0;
        }
   }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // restore saved activity state
        try {
            currentState = savedInstanceState.getParcelable(activityStateDataName);
            Dlog.i(currentState.toString());
            // delete menu
            deleteMenuClicked = currentState.isDeleteMenuClicked();
            // delete card id map
            deleteCardIdMap = currentState.getDeleteCardIdMap();
            // refresh grid
            refreshCardList();
            // http://stackoverflow.com/questions/12712050/set-first-visible-item-in-gridview
            int firstVisiblePositoin = currentState.getFirstVisiblePosition();
            Dlog.i("first visible position:" + firstVisiblePositoin);
            cardCustomGridView.smoothScrollToPosition(firstVisiblePositoin);
        } catch (Exception e) {
            Dlog.e(e.toString());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save current state
        if (db != null) {
            db.updateState(state.getBoxId(), 0);
            Dlog.i("state.getBoxId:" + state.getBoxId());
        }
        // save current activity state
        try {
            currentState = new ActivityState(cardCustomGridView.getFirstVisiblePosition(),
                    deleteMenuClicked, deleteCardIdMap);
            outState.putParcelable(activityStateDataName, currentState);
            Dlog.i(currentState.toString());
            // http://stackoverflow.com/questions/7992368/grid-view-get-items-which-are-visible-to-user
            Dlog.i("first visible position:" + currentState.getFirstVisiblePosition());
        } catch (Exception e) {
            Dlog.e(e.toString());
        }
    }

    private class ActivityState implements Parcelable {
        private int firstVisiblePosition;
        private boolean deleteMenuClicked;
        HashMap<Integer, Boolean> deleteCardIdMap;

        public ActivityState(int firstVisiblePosition, boolean deleteMenuClicked,
                             HashMap<Integer, Boolean> deleteCardIdMap) {
            this.firstVisiblePosition = firstVisiblePosition;
            this.deleteMenuClicked = deleteMenuClicked;
            this.deleteCardIdMap = deleteCardIdMap;
        }

        protected ActivityState(Parcel in) {
            firstVisiblePosition = in.readInt();
            deleteMenuClicked = in.readByte() != 0;
            // http://stackoverflow.com/questions/22498746/android-implement-parcelable-object-which-has-hashmap
            final int size = in.readInt();
            if (size > 0) {
                deleteCardIdMap = new HashMap<>();
                for (int i = 0; i < size; i++) {
                    final int key = in.readInt();
                    final boolean value = in.readByte() != 0;
                    deleteCardIdMap.put(key, value);
                }
            }
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(firstVisiblePosition);
            dest.writeByte((byte) (deleteMenuClicked ? 1 : 0));

            if (deleteCardIdMap != null) {
                // http://stackoverflow.com/questions/22498746/android-implement-parcelable-object-which-has-hashmap
                dest.writeInt(deleteCardIdMap.size());
                for (Map.Entry<Integer, Boolean> entry : deleteCardIdMap.entrySet()) {
                    dest.writeInt(entry.getKey());
                    dest.writeByte((byte) (entry.getValue() ? 1 : 0));
                }
            } else {
                dest.writeInt(0);
            }
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

        public int getFirstVisiblePosition() {
            return firstVisiblePosition;
        }

        public void setFirstVisiblePosition(int firstVisiblePosition) {
            this.firstVisiblePosition = firstVisiblePosition;
        }

        public boolean isDeleteMenuClicked() {
            return deleteMenuClicked;
        }

        public void setDeleteMenuClicked(boolean deleteMenuClicked) {
            this.deleteMenuClicked = deleteMenuClicked;
        }

        public HashMap<Integer, Boolean> getDeleteCardIdMap() {
            return deleteCardIdMap;
        }

        public void setDeleteCardIdMap(HashMap<Integer, Boolean> deleteCardIdMap) {
            this.deleteCardIdMap = deleteCardIdMap;
        }

        @Override
        public String toString() {
            return "ActivityState{" +
                    "firstVisiblePosition=" + firstVisiblePosition +
                    ", deleteMenuClicked=" + deleteMenuClicked +
                    ", deleteCardIdMap=" + deleteCardIdMap +
                    '}';
        }
    }
}