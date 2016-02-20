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
import android.widget.GridView;
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
public class BoxListActivity extends AppCompatActivity {
    private EditText createBoxName;
    private BoxListAdapter boxListAdapter;
    private GridView Box_List_View;
    private List<BoxDTO> BoxList;
    private FlashCardDB db = null;
    private Button Btn_Box_List_Create;
    private BoxDTO boxDTO;
    private BoxDAO boxDAO;
    private Integer LastNumber;
    private String BoxName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_list);
        Box_List_View = (GridView) findViewById(R.id.Box_Custom_List_View);
        BoxList = new ArrayList<>();
        db = new FlashCardDB(this);
        boxDTO = new BoxDTO();
        boxDAO = db;
        BoxList = boxDAO.getBoxAll();
        boxListAdapter = new BoxListAdapter(this, BoxList);
        Box_List_View.setAdapter(boxListAdapter);

        Box_List_View.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), BoxList.get(position).getName(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), CardListActivity.class);
                Integer BoxId = BoxList.get(position).getId();
                intent.putExtra("BoxId",BoxId);//박스번호를 카드리스트에 전송
                startActivity(intent);
            }
        });

        Box_List_View.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                View view2 = (View) View.inflate(BoxListActivity.this, R.layout.custom_box_edit, null);
                final AlertDialog.Builder dialog2 = new AlertDialog.Builder(BoxListActivity.this);
                Button Btn_Box_List_Update = (Button)view2.findViewById(R.id.Custom_List_Update);
                Button Btn_Box_List_Delete= (Button)view2.findViewById(R.id.Custom_List_Delete);

                Btn_Box_List_Update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View view = (View) View.inflate(BoxListActivity.this, R.layout.custom_box_create, null);
                       AlertDialog.Builder dialog = new AlertDialog.Builder(BoxListActivity.this);//수정 다이아로그
                        final TextView updateBoxNumber = (TextView) view.findViewById(R.id.Custom_Box_Create_Number);
                        final EditText updateBoxName = (EditText)view.findViewById(R.id.Custom_Box_Create_Name);
                        final Integer Updateid = BoxList.get(position).getId();
                        updateBoxNumber.setText( Updateid.toString() );
                        updateBoxName.setText(BoxList.get(position).getName());
                        dialog.setTitle("박스 수정");
                        dialog.setView(view);

                        dialog.setPositiveButton("수정", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    BoxDTO dto = new BoxDTO();
                                    BoxName = updateBoxName.getText().toString();
                                    dto.setId(Updateid);
                                    dto.setName(BoxName);
                                    dto.setType(0);
                                    dto.setId(Updateid);
                                    boxDAO.updateBox(dto);
                                    BoxList.set(position, dto);
                                    refreshBox(BoxList);
                                    dialog.cancel();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        dialog.setNegativeButton("취소", null);
                        dialog.show();
                    }
                });

                    Btn_Box_List_Delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            final AlertDialog.Builder dlg = new AlertDialog.Builder(BoxListActivity.this);

                                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    boolean sw = false;
                                    int id = BoxList.get(position).getId();
                                    sw = boxDAO.deleteBox(id);
                                    if (sw == true) {
                                        Toast.makeText(getApplicationContext(), BoxList.get(position).getName()+"이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                        Log.i("삭제 : ", BoxList.get(position).getName());
                                        BoxList.remove(position);
                                        refreshBox(BoxList);
                                        finish();
                                        Intent intent = new Intent(getApplicationContext(),BoxListActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(getApplicationContext(), "삭제할 수 없습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            dlg.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dlg.setCancelable(true);
                                }
                            });
                            dlg.setTitle("삭제 확인");
                            dlg.show();
                    }
                    });
                dialog2.setTitle("메뉴");
                dialog2.setView(view2);

                dialog2.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                dialog2.setNegativeButton("취소", null);
                dialog2.show();
                return true;
            }
        });

        Btn_Box_List_Create = (Button)findViewById(R.id.Custom_List_Create);
        Btn_Box_List_Create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = (View) View.inflate(BoxListActivity.this, R.layout.custom_box_create, null);
                AlertDialog.Builder dialog = new AlertDialog.Builder(BoxListActivity.this);

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
        public BoxListAdapter() {}
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
            Log.i("getItem","check");
            return position;
        }

        @Override
        public long getItemId(int position) {
            Log.i("getItemId","check");
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
            Integer str1 = list.get(position).getId();
            String str2 = str1.toString();
            boxNum.setText(str2 + ". ");
            boxName.setText(str);
            LastNumber = list.get(position).getId()+1;
            return view;
        }

    }

    public void refreshBox(List<BoxDTO> list){//새로고침 함수
        BoxList = list;
        boxListAdapter.notifyDataSetChanged();
    }

    static class NoAscCompare implements Comparator<BoxDTO> {//오름차순 정렬 함수
        @Override
        public int compare(BoxDTO arg0, BoxDTO arg1) {
            // TODO Auto-generated method stub
            return arg0.getId() < arg1.getId() ? -1 : arg0.getId() > arg1.getId() ? 1:0;
        }

    }
}
