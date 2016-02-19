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
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import kr.co.bit.osf.flashcard.db.BoxDAO;
import kr.co.bit.osf.flashcard.db.BoxDTO;
import kr.co.bit.osf.flashcard.db.FlashCardDB;
import kr.co.bit.osf.flashcard.db.StateDAO;
import kr.co.bit.osf.flashcard.db.StateDTO;

/**
 * Created by bit-user on 2016-02-19.
 */
public class BoxListModeActivity extends AppCompatActivity {
    ListView Box_List_View;
    List<BoxDTO> BoxList;
    FlashCardDB db = null;
    StateDAO stateDao = null;
    StateDTO cardState = null;
    BoxDTO boxDTO;
    BoxDAO boxDAO;
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
        BoxListAdapter boxListAdapter = new BoxListAdapter(this, BoxList);
        Box_List_View.setAdapter(boxListAdapter);


        Box_List_View.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //todo: Box Choice
                Intent intent = new Intent(getApplicationContext(), CardListActivity.class);
                startActivity(intent);
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
            TextView boxNum = (TextView) view.findViewById(R.id.Custom_Box_Number);
            TextView boxName = (TextView) view.findViewById(R.id.Custom_Box_Text);

            String str = list.get(position).getName();
            Integer str1 = list.get(position).getSeq();
            String str2 = str1.toString();
            boxNum.setText(str2 + ". ");
            boxName.setText(str);


            return view;
        }
    }
}
