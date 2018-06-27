package com.example.dante.ai_demo;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.dante.ai_demo.DatabasePack.Contract;
import com.example.dante.ai_demo.DatabasePack.Db_helper;

import java.util.ArrayList;
import java.util.List;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    //EditText对象
    EditText editText ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //EditText的id绑定
        editText = findViewById(R.id.content_edt);

        //数据库的helper
        final Db_helper mDbHelper =  new Db_helper(MainActivity.this.getBaseContext());

        //查询按钮相关操作
        Button query_button = findViewById(R.id.query_btn);
        query_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //插入信息
                // Gets the data repository in write mode
                SQLiteDatabase db = mDbHelper.getWritableDatabase();

                // Create a new map of values, where column names are the keys
                //向数据库里插入信息
                ContentValues values = new ContentValues();
                values.put(Contract.Entry.COLUMN_NAME_RUBBISH_NAME, "苹果");
                values.put(Contract.Entry.COLUMN_NAME_RUBBISH_CATALOGUE, "不可回收");

                // Insert the new row, returning the primary key value of the new row
                long newRowId = db.insert(Contract.Entry.TABLE_NAME, null, values);




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
                String content = editText.getText().toString();
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

    }
}