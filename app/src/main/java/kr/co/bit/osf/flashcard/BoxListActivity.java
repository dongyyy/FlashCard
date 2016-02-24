package kr.co.bit.osf.flashcard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import kr.co.bit.osf.flashcard.common.ImageUtil;
import kr.co.bit.osf.flashcard.db.BoxDTO;
import kr.co.bit.osf.flashcard.db.CardDTO;
import kr.co.bit.osf.flashcard.db.FlashCardDB;
import kr.co.bit.osf.flashcard.db.StateDTO;
import kr.co.bit.osf.flashcard.debug.Dlog;

public class BoxListActivity extends AppCompatActivity {
    // db
    private FlashCardDB db = null;
    private List<BoxDTO> boxList;
    // grid view
    private GridView gridView;
    private BoxListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_list);
        Dlog.i("");

        // read state from db
        db = new FlashCardDB(this);
        StateDTO cardState = db.getState();
        Dlog.i("read card state:" + cardState);

        // state.cardId > 0 : start card view activity
        if (cardState.getBoxId() > 0) {
            Intent intent = new Intent(this, CardListActivity.class);
            startActivity(intent);
        }
        if (cardState.getBoxId() == -1) {
            Intent intent = new Intent(this, SplashActivity.class);
            startActivity(intent);
        }


        // read box list
        db = new FlashCardDB(this);
        boxList = db.getBoxAll();
        Dlog.i("getBoxAll:size():" + boxList.size());

        // list view
        gridView = (GridView) findViewById(R.id.boxListGridView);
        adapter = new BoxListAdapter(this, boxList);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Dlog.i("setOnItemClickListener:position:" + position);
                if (!(db.updateState(boxList.get(position).getId(), 0))) {
                    Toast.makeText(getApplicationContext(), "state 전송 실패", Toast.LENGTH_SHORT).show();
                }
                Intent intent = new Intent(getApplicationContext(), CardListActivity.class);
                startActivity(intent);
                Dlog.i("box:" + boxList.get(position));
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                // get user action from dialog

                View dlg = BoxListActivity.this.getLayoutInflater().inflate(R.layout.dialog_title, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(BoxListActivity.this);
                TextView textView1 = (TextView) dlg.findViewById(R.id.dialogMenuTextViewOne);
                TextView textView2 = (TextView) dlg.findViewById(R.id.dialogMenuTextViewTwo);

                textView1.setText("바꿀래요?");
                textView1.setVisibility(View.VISIBLE);
                textView2.setText("지울래요");
                textView2.setVisibility(View.VISIBLE);
                //update box dialog
                textView1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Dlog.i("dialog:edit box");
                        final EditText inputText = new EditText(BoxListActivity.this);
                        inputText.setText(boxList.get(position).getName());
                        AlertDialog.Builder input = new AlertDialog.Builder(BoxListActivity.this);
                        input.setTitle(R.string.box_list_edit_dialog_edit_dialog_title_text);
                        input.setMessage(R.string.box_list_edit_dialog_edit_dialog_message_text);
                        input.setView(inputText);

                        input.setPositiveButton(R.string.box_list_edit_dialog_edit_dialog_ok_button_text,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // set new box name
                                        String newBoxName = inputText.getText().toString();
                                        boxList.get(position).setName(newBoxName);
                                        db.updateBox(boxList.get(position));
                                        adapter.notifyDataSetChanged();
                                        Dlog.i("new box name:" + newBoxName);
                                    }
                                });
                        input.show();
                    }
                });
                // delete box dialog
                textView2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Dlog.i("dialog:delete box");
                        final AlertDialog.Builder delete = new AlertDialog.Builder(BoxListActivity.this);
                        delete.setTitle(R.string.box_list_edit_dialog_delete_dialog_title_text);
                        delete.setMessage(R.string.box_list_edit_dialog_delete_dialog_message_text);
                        delete.setPositiveButton(R.string.box_list_edit_dialog_delete_dialog_ok_button_text,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // delete box
                                        int deleteBoxId = boxList.get(position).getId();
                                        Dlog.i("delete box name:" + boxList.get(position).getName());
                                        if (db.deleteBox(deleteBoxId)) {
                                            db.deleteCardByBoxId(deleteBoxId);
                                            boxList.remove(position);
                                            Dlog.i("delete box id:" + deleteBoxId);
                                            adapter.notifyDataSetChanged();
                                            //임시 에러 방지
                                            finish();
                                            Intent intent = new Intent(getApplicationContext(),BoxListActivity.class);
                                            startActivity(intent);
                                        }
                                    }
                                });
                        delete.setNegativeButton(R.string.box_list_edit_dialog_delete_dialog_cancel_button_text,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // Canceled.
                                    }
                                });
                        delete.show();

                    }
                });
                builder.setView(dlg);
                builder.show();

                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Dlog.i("onCreateOptionMenu : " + "OK");
        getMenuInflater().inflate(R.menu.activity_box_list_menu, menu);
        MenuItem showDeleteCompleteButton = menu.findItem(R.id.showDeleteCompleteButton);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Integer id = item.getItemId();

        switch (id){
            case R.id.box_list_box_create:
                Dlog.i("dialog:add box");
                // add box
                // set an EditText view to get user input
                View dlg = BoxListActivity.this.getLayoutInflater().inflate(R.layout.dialog_title, null);
                final EditText inputText = (EditText)dlg.findViewById(R.id.dialogMenuEditTextOne);
                TextView titleTextView = (TextView)dlg.findViewById(R.id.dialogTitleTextView);
                TextView textView = (TextView)dlg.findViewById(R.id.dialogMenuTextViewOne);
                Dlog.i("add R.id.dialog Item");
                titleTextView.setText(R.string.dialog_box_create);
                textView.setText(R.string.box_list_add_dialog_message_text);
                Dlog.i("dialog Item setText");
                inputText.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                Dlog.i("dialog Item setVisible");
                AlertDialog.Builder input = new AlertDialog.Builder(BoxListActivity.this);
                Dlog.i("dialog create");
                input.setView(dlg);
                input.setPositiveButton(R.string.box_list_add_dialog_ok_button_text,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // add box
                                String addBoxName = inputText.getText().toString();
                                Dlog.i("add box name:" + addBoxName);
                                BoxDTO box = db.addBox(addBoxName);
                                if (box != null) {
                                    // refresh list
                                    Dlog.i("refresh list:size():before:" + boxList.size());
                                    boxList.add(box);
                                    Dlog.i("refresh list:size():after:" + boxList.size());
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        });
                input.setNegativeButton(R.string.box_list_add_dialog_cancel_button_text,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Canceled.
                            }
                        });
                input.show();
                break;
//            case R.id.box_list_menu_list_One:
//                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Dlog.i("");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Dlog.i("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Dlog.i("");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Dlog.i("");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Dlog.i("");
        // save current state
        if (db != null) {
            db.updateState(0, 0);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Dlog.i("");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Dlog.i("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Dlog.i("");
    }

    public class BoxListAdapter extends BaseAdapter {
        private Context context;
        private List<BoxDTO> list;

        public BoxListAdapter() {
        }

        public BoxListAdapter(Context c, List<BoxDTO> list) {
            context = c;
            this.list = list;
            Dlog.i("adapter:list:size():" + list.size());
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
        public Object getItem(int position) {
            Dlog.i("position:" + position);
            return position;
        }


        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (view == null) {
                Dlog.i("view == null");
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.activity_box_list_item, null);
            }
            Dlog.i("box position:" + position + ", box:" + list.get(position));
            // image
            ImageView imageView = (ImageView) view.findViewById(R.id.boxListViewItemImage);
            CardDTO topCard = db.getTopCardByBoxId(list.get(position).getId());
            if (topCard != null) {
                Dlog.i("topCard:" + topCard);
                ImageUtil.loadCardImageIntoImageView(BoxListActivity.this, topCard, imageView);
            } else {
                Dlog.i("default_no_image");
                imageView.setImageResource(R.drawable.default_no_image);
            }
            // text
            TextView nameTextView = (TextView) view.findViewById(R.id.boxListViewItemText);
            nameTextView.setText(list.get(position).getName());
            // card count
            TextView countTextView = (TextView) view.findViewById(R.id.boxListViewItemCount);
            countTextView.setText("" + db.getCardCountByBoxId(list.get(position).getId()));

            return view;
        }
    }
}
