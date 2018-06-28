package com.example.dante.ai_demo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.dante.ai_demo.DatabasePack.Contract;
import com.example.dante.ai_demo.DatabasePack.Db_helper;
import com.example.dante.ai_demo.ListViewPack.RubbishAdapter;
import com.example.dante.ai_demo.ListViewPack.RubbishInfo;

import java.util.ArrayList;
import java.util.List;

import static java.security.AccessController.getContext;

public class MainActivity extends IatBasicActivity {


    //用户添加信息的EditText对象
    EditText addInfoEditText;

    //用户添加信息的Spinner对象
    Spinner spinner;

    //语音识别的ui组件
    private EditText mContent;
    private Button mBtnVoice;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        //语音识别的组件初始化
        initView();


        //用于添加信息的EditText的id绑定
        addInfoEditText  = findViewById(R.id.add_rubbish_edt);

        //Spinner组件的初始化
        spinner = (Spinner) findViewById(R.id.rubbish_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.rubbish_catalogue_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        //数据库的helper
        final Db_helper mDbHelper =  new Db_helper(MainActivity.this.getBaseContext());



        //添加按钮相关操作
        Button add_button = findViewById(R.id.add_btn);
        add_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //插入信息
                // Gets the data repository in write mode
                SQLiteDatabase db = mDbHelper.getWritableDatabase();

                // Create a new map of values, where column names are the keys
                //向数据库里插入信息
                ContentValues values = new ContentValues();
                String value_part_1 = addInfoEditText.getText().toString();
                String value_part_2 = (String) spinner.getSelectedItem();
                values.put(Contract.Entry.COLUMN_NAME_RUBBISH_NAME, value_part_1);
                values.put(Contract.Entry.COLUMN_NAME_RUBBISH_CATALOGUE, value_part_2);

                //防止用户忘记输垃圾名称
                if (value_part_1.equals(null)) {
                    // Insert the new row, returning the primary key value of the new row
                    long newRowId = db.insert(Contract.Entry.TABLE_NAME, null, values);
                }else{
                    Toast.makeText(MainActivity.this,"请输入垃圾名称",Toast.LENGTH_SHORT).show();
                }



            }
        });




        //查看信息的按钮
        Button check_button = findViewById(R.id.test_byn);
        check_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //查看信息
                SQLiteDatabase db1 = mDbHelper.getReadableDatabase();

                // Define a projection that specifies which columns from the database
                // you will actually use after this query.
                String[] projection = {
                        BaseColumns._ID,
                        Contract.Entry.COLUMN_NAME_RUBBISH_NAME,
                        Contract.Entry.COLUMN_NAME_RUBBISH_CATALOGUE
                };

                // Filter results WHERE "title" = 'My Title'
                String content = mContent.getText().toString();
                String selection = Contract.Entry.COLUMN_NAME_RUBBISH_NAME + " = ?";
                String[] selectionArgs = {content};

                // How you want the results sorted in the resulting Cursor
                String sortOrder =
                        Contract.Entry.COLUMN_NAME_RUBBISH_CATALOGUE + " DESC";

                Cursor cursor = db1.query(
                        Contract.Entry.TABLE_NAME,   // The table to query
                        projection,             // The array of columns to return (pass null to get all)
                        selection,              // The columns for the WHERE clause
                        selectionArgs,          // The values for the WHERE clause
                        null,                   // don't group the rows
                        null,                   // don't filter by row groups
                        sortOrder               // The sort order

                );

                List itemIds = new ArrayList<>();
                while(cursor.moveToNext()) {
                    //序号
                    long itemId = cursor.getLong(
                            cursor.getColumnIndexOrThrow(Contract.Entry._ID));
                    itemIds.add(itemId);
                    //垃圾名字
                    String r_name = cursor.getString(cursor.getColumnIndexOrThrow(Contract.Entry.COLUMN_NAME_RUBBISH_NAME));
                    //垃圾类别
                    String c_name = cursor.getString(cursor.getColumnIndexOrThrow(Contract.Entry.COLUMN_NAME_RUBBISH_CATALOGUE));
                    Log.v("AAA",itemId+" "+r_name+" "+c_name);
                    Toast.makeText(MainActivity.this,c_name,Toast.LENGTH_SHORT).show();
                }
                cursor.close();
                Log.v("AAA","搞定了");
            }
        });






        //listview的相关操作
        // Create an ArrayList of AndroidFlavor objects
        final ArrayList<RubbishInfo> rubbishInfos = new ArrayList<RubbishInfo>();



        // Create an {@link AndroidFlavorAdapter}, whose data source is a list of
        // {@link AndroidFlavor}s. The adapter knows how to create list item views for each item
        // in the list.
        final RubbishAdapter rubbishAdapter = new RubbishAdapter(this, rubbishInfos);

        // Get a reference to the ListView, and attach the adapter to the listView.
        final ListView listView = (ListView) findViewById(R.id.listview_rubbish);
        Button QueryButton = findViewById(R.id.query_btn);
        QueryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //先清空之前的ArrayList防止listview堆积
                rubbishInfos.clear();
                //先进行数据库查询操作，把数据库信息放入listview的ArrayList里面
                SQLiteDatabase db = mDbHelper.getReadableDatabase();

            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
                String[] projection = {
                        BaseColumns._ID,
                        Contract.Entry.COLUMN_NAME_RUBBISH_NAME,
                        Contract.Entry.COLUMN_NAME_RUBBISH_CATALOGUE
                };

            // Filter results WHERE "title" = 'My Title'
                String selection = Contract.Entry.COLUMN_NAME_RUBBISH_NAME + " = ?";
                String[] selectionArgs = { "My Title" };

            // How you want the results sorted in the resulting Cursor
                String sortOrder =
                        Contract.Entry.COLUMN_NAME_RUBBISH_CATALOGUE + " DESC";

                Cursor cursor = db.query(
                        Contract.Entry.TABLE_NAME,   // The table to query
                        projection,             // The array of columns to return (pass null to get all)
                        null,              // The columns for the WHERE clause
                        null,          // The values for the WHERE clause
                        null,                   // don't group the rows
                        null,                   // don't filter by row groups
                        null               // The sort order
                );

                //查询完毕，将信息添加进listview的ArrayList里
                List itemIds = new ArrayList<>();
                while(cursor.moveToNext()) {
                    long itemId = cursor.getLong(
                            cursor.getColumnIndexOrThrow(Contract.Entry._ID));
                    String itemName = cursor.getString(
                            cursor.getColumnIndexOrThrow(Contract.Entry.COLUMN_NAME_RUBBISH_NAME));
                    String itemCatalogue = cursor.getString(
                            cursor.getColumnIndexOrThrow(Contract.Entry.COLUMN_NAME_RUBBISH_CATALOGUE));
                    rubbishInfos.add(new RubbishInfo((int)itemId, itemName,itemCatalogue));
                }
                cursor.close();

                //显示listview内容

                listView.setAdapter(rubbishAdapter);
            }
        });
    }


    //语音识别的方法
    /**
     * 初始化视图
     */
    private void initView(){
        mContent = (EditText)findViewById(R.id.et_content);
        mBtnVoice =(Button)findViewById(R.id.btn_voice);
        mBtnVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               clickMethod();

            }
        });
        //调用语音识别类方法

        initIatData(mContent);
    }
}