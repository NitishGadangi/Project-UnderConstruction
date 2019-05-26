package nitish.build.com.saavntest1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Search_Songs extends AppCompatActivity {
    String query=" ",searchRes;
    int listSize=0;
    JSONArray searchList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search__songs);

        EditText et_SearchBox=findViewById(R.id.et_searchBox);
        Button btn_search=findViewById(R.id.btn_searchBox);


        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                query=et_SearchBox.getText().toString();
                try {
                    searchRes=DataHandlers.getSearchResult(query);
                    Log.i("SEARR1",searchRes);
                    searchList = new JSONArray(searchRes);
                    Log.i("SEARR2",searchList.toString());
                    listSize=searchList.length();
                    Log.i("SEARR3",listSize+"L");
                    ListView resList =findViewById(R.id.list_searchres);
                    CustomAdapter customAdapter = new CustomAdapter();
                    resList.setAdapter(customAdapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        et_SearchBox.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    Toast.makeText(getApplicationContext(), et_SearchBox.getText(), Toast.LENGTH_SHORT).show();
                    return true;
                }


                return false;
            }
        });


    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            //Log.i("Countaaaa",Integer.toString(COURSES.length));
            //Toast.makeText(syllabus_select_course.this, COURSES.length, Toast.LENGTH_SHORT).show();
            return (listSize);
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
            convertView = getLayoutInflater().inflate(R.layout.custom_search_list,null);

            TextView songName= convertView.findViewById(R.id.csl_songName);
            TextView songInfo = convertView.findViewById(R.id.csl_info);
            TextView songType = convertView.findViewById(R.id.csl_type);

            try {
                JSONObject curSong = new JSONObject(searchList.getString(position));
                songName.setText(curSong.getString("title"));
                songInfo.setText(curSong.getString("description"));
                songType.setText(curSong.getString("type"));


            } catch (JSONException e) {
                e.printStackTrace();
            }


            return convertView;
        }
    }
}
