package kr.co.bit.osf.flashcard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import kr.co.bit.osf.flashcard.db.BoxDAO;
import kr.co.bit.osf.flashcard.db.BoxDTO;
import kr.co.bit.osf.flashcard.db.FlashCardDB;

/**
 * Created by bit-user on 2016-02-19.
 */
public class BoxListModeActivity extends AppCompatActivity {
    EditText createBoxName;
    BoxListAdapter boxListAdapter;
    ListView Box_List_View;
    List<BoxDTO> BoxList;
    FlashCardDB db = null;
    Button Btn_Box_List_Create;
    Button Btn_Box_List_Delete;
    Button Btn_Box_List_Update;
    BoxDTO boxDTO;
    BoxDAO boxDAO;
    Long LastNumber;
    String BoxName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_list_mode);




        Box_List_View = (ListView) findViewById(R.id.Box_Custom_List_View);
        BoxList = new ArrayList<>();
        db = new FlashCardDB(this);
        boxDTO = new BoxDTO();
        boxDAO = db;
        BoxList = boxDAO.getBoxAll();
        boxListAdapter = new BoxListAdapter(this, BoxList);
        Box_List_View.setAdapter(boxListAdapter);

        //todo: short click
        Box_List_View.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), BoxList.get(position).getName(), Toast.LENGTH_SHORT).show();
                //todo: Box Choice
                Intent intent = new Intent(getApplicationContext(), CardListActivity.class);
                startActivity(intent);
            }
        });

        //todo: long click
        Box_List_View.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                View view2 = (View) View.inflate(BoxListModeActivity.this,R.layout.custom_box_edit,null);
                AlertDialog.Builder dialog = new AlertDialog.Builder(BoxListModeActivity.this);

                Button Btn_Box_List_Update = (Button)view2.findViewById(R.id.Custom_List_Update);
                Button Btn_Box_List_Delete= (Button)view2.findViewById(R.id.Custom_List_Delete);


                //todo:box update
                Btn_Box_List_Update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                //todo: box delete
                Btn_Box_List_Delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        boolean sw = false;
                        Long id = boxDAO.getBox(position).getId();
                        try {
                            sw = boxDAO.deleteBox(id.intValue());
                            if (sw == true) {
                                BoxList.remove(position);
                                refreshBox(BoxList);
                            } else {
                                Log.i("Delete Check", "false!"+id);
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }

                });

                dialog.setTitle("메뉴");
                dialog.setView(view2);
                dialog.setPositiveButton("확인",null);
                dialog.setNegativeButton("취소",null);
                dialog.show();



                return true;
            }
        });


        //todo: Create Box Button
        Btn_Box_List_Create = (Button)findViewById(R.id.Custom_List_Create);
        Btn_Box_List_Create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo:Create Box
                View view = (View) View.inflate(BoxListModeActivity.this, R.layout.custom_box_create, null);
                AlertDialog.Builder dialog = new AlertDialog.Builder(BoxListModeActivity.this);

                final TextView createBoxNumber = (TextView) view.findViewById(R.id.Custom_Box_Create_Number);
                createBoxName = (EditText) view.findViewById(R.id.Custom_Box_Create_Name);

                // BoxList = boxDAO.getBoxAll();


                createBoxNumber.setText( LastNumber.toString() );

                dialog.setTitle("박스 생성");
                dialog.setView(view);
                dialog.setPositiveButton("생성", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            BoxDTO dto = new BoxDTO();
                            BoxName = createBoxName.getText().toString();
                            dto.setId(LastNumber);
                            dto.setName(BoxName);
                            dto.setType(0);
                            dto.setId(LastNumber);
                            boxDAO.addBox(dto);
                            BoxList.add(dto);
                            refreshBox(BoxList);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
                dialog.setNegativeButton("취소", null);
                dialog.show();

            }
        });

    }

    public class BoxListAdapter extends BaseAdapter {
        private Context context;
        private List<BoxDTO> list;

        public BoxListAdapter() {
        }

        public BoxListAdapter(Context c, List<BoxDTO> list) {
            context = c;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //todo: CustomListView Create
            View view = convertView;

            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.custom_box_list, null);
            }
            Collections.sort( list,new NoAscCompare() );//정렬 id 번호별로

            TextView boxNum = (TextView) view.findViewById(R.id.Custom_Box_Number);
            TextView boxName = (TextView) view.findViewById(R.id.Custom_Box_Text);

            String str = list.get(position).getName();
            Long str1 = list.get(position).getId();
            String str2 = str1.toString();
            boxNum.setText(str2 + ". ");
            boxName.setText(str);
            LastNumber = list.get(position).getId()+1;

            return view;
        }

    }

    public void refreshBox(List<BoxDTO> list){
        BoxList = list;
        boxListAdapter.notifyDataSetChanged();
    }

    static class NoAscCompare implements Comparator<BoxDTO> {

        /**
         * 오름차순(ASC)
         */
        @Override
        public int compare(BoxDTO arg0, BoxDTO arg1) {
            // TODO Auto-generated method stub
            return arg0.getId() < arg1.getId() ? -1 : arg0.getId() > arg1.getId() ? 1:0;
        }

    }
}
