package kr.co.bit.osf.flashcard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import java.util.List;

import kr.co.bit.osf.flashcard.db.BoxDTO;
import kr.co.bit.osf.flashcard.db.FlashCardDB;
import kr.co.bit.osf.flashcard.debug.Dlog;

public class BoxListActivity extends AppCompatActivity {
    // db
    private FlashCardDB db = null;
    private BoxDTO boxDTO;
    private List<BoxDTO> boxList;
    // grid view
    private GridView gridView;
    private BoxListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_list);

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
                Intent intent = new Intent(getApplicationContext(), CardListActivity.class);
                Integer BoxId = boxList.get(position).getId();
                intent.putExtra("BoxId", BoxId);//박스번호를 카드리스트에 전송
                startActivity(intent);
                Dlog.i("box:" + boxList.get(position));
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                // get user action from dialog
                final CharSequence[] items = {
                        getString(R.string.box_list_edit_dialog_edit_button_text),
                        getString(R.string.box_list_edit_dialog_delete_button_text),
                        getString(R.string.box_list_edit_dialog_cancel_button_text)
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(BoxListActivity.this);
                builder.setTitle(getString(R.string.box_list_edit_dialog_title));
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String itemName = items[which].toString();
                        Dlog.i("dialog:which:" + which + ", itemName:" + itemName);

                        if (itemName.equals(getString(R.string.box_list_edit_dialog_edit_button_text))) {
                            Dlog.i("dialog:edit box");
                            // edit box dialog
                            // set an EditText view to get user input
                            final EditText inputText = new EditText(BoxListActivity.this);
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
                                            adapter.notifyDataSetChanged();
                                            Dlog.i("new box name:" + newBoxName);
                                        }
                                    });
                            input.setNegativeButton(R.string.box_list_edit_dialog_edit_dialog_cancel_button_text,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            // Canceled.
                                        }
                                    });
                            input.show();
                        } else if (itemName.equals(getString(R.string.box_list_edit_dialog_delete_button_text))) {
                            Dlog.i("dialog:delete box");
                            // delete box dialog
                            AlertDialog.Builder delete = new AlertDialog.Builder(BoxListActivity.this);
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
                        } else if (itemName.equals(getString(R.string.box_list_edit_dialog_cancel_button_text))) {
                            Dlog.i("dialog:cancelled");
                            //dialog.dismiss();
                        }
                    }
                });
                builder.show();

                return true;
            }
        });

        (findViewById(R.id.boxListAddButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dlog.i("dialog:add box");
                // add box
                // set an EditText view to get user input
                final EditText inputText = new EditText(BoxListActivity.this);
                inputText.setSingleLine();
                AlertDialog.Builder input = new AlertDialog.Builder(BoxListActivity.this);
                input.setTitle(R.string.box_list_add_dialog_title_text);
                input.setMessage(R.string.box_list_add_dialog_message_text);
                input.setView(inputText);
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
            }
        });
    }

    public class BoxListAdapter extends BaseAdapter {
        private Context context;
        private List<BoxDTO> list;

        public BoxListAdapter() {}

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
            Dlog.i("position:" + position);
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.activity_box_list_item, null);
            }
            TextView boxName = (TextView) view.findViewById(R.id.boxListViewItemText);
            boxName.setText(list.get(position).getName());
            return view;
        }
    }
}
