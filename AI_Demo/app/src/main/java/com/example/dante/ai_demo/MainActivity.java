package com.example.dante.ai_demo;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.baidu.aip.imageclassify.AipImageClassify;
import com.example.dante.ai_demo.DatabasePack.Contract;
import com.example.dante.ai_demo.DatabasePack.Db_helper;
import com.example.dante.ai_demo.ListViewPack.RubbishAdapter;
import com.example.dante.ai_demo.ListViewPack.RubbishInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends IatBasicActivity {

    //百度识图返回的json请求的字符串形式
    String JsonResponse;

    //拍照获取的图片(bitmap格式)
    Bitmap bitmap;

    //启用照相机变量初始化
    public static final int TAKE_PHOTO = 1;
    Uri imageUri;


    //用户添加信息的EditText对象
    EditText addInfoEditText;

    //用户添加信息的Spinner对象
    Spinner spinner;

    //语音识别的ui组件
    private EditText mContent;
    private Button mBtnVoice;

    //百度识图相关api的设置：设置APPID/AK/SK

    AipImageClassify client;
    public static final String APP_ID = "11245976";
    public static final String API_KEY = "bzz14STCaVGZfmBo2dAlN1NQ";
    public static final String SECRET_KEY = "G1nFDGxIW92OBDQURS2UljY0CaxFiUSs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //拍照显示图片的按钮
        Button takePhotoButton = findViewById(R.id.take_photo_btn);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //创建File对象，用于存储拍照后的图片
                File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= 24) {
                    imageUri = FileProvider.getUriForFile(MainActivity.this,
                            "com.example.dante.ai_demo.fileprovider", outputImage);
                } else {
                    imageUri = Uri.fromFile(outputImage);
                }
                //启动相机程序
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, TAKE_PHOTO);
                Log.v("AAA", "相机程序已启动");
            }
        });


        //百度识图的初始化操作
        // 初始化一个AipImageClassifyClient
        client = new AipImageClassify(APP_ID, API_KEY, SECRET_KEY);
        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);
        // 可选：设置log4j日志输出格式，若不设置，则使用默认配置
        // 也可以直接通过jvm启动参数设置此环境变量
        System.setProperty("aip.log4j.conf", "path/to/your/log4j.properties");
        Log.v("AAA", "初始化成功");


        //语音识别的组件初始化
        initView();

        //用于添加信息的EditText的id绑定
        addInfoEditText = findViewById(R.id.add_rubbish_edt);

        //Spinner组件的初始化
        initSpinner();

        //数据库的helper
        final Db_helper mDbHelper = new Db_helper(MainActivity.this.getBaseContext());

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
                if (!TextUtils.isEmpty(addInfoEditText.getText().toString())) {
                    // Insert the new row, returning the primary key value of the new row
                    long newRowId = db.insert(Contract.Entry.TABLE_NAME, null, values);
                } else {
                    Toast.makeText(MainActivity.this, "请输入垃圾名称", Toast.LENGTH_SHORT).show();
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
                //对指定的垃圾进行数据库查询
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
                while (cursor.moveToNext()) {
                    //序号
                    long itemId = cursor.getLong(
                            cursor.getColumnIndexOrThrow(Contract.Entry._ID));
                    itemIds.add(itemId);
                    //垃圾名字
                    String r_name = cursor.getString(cursor.getColumnIndexOrThrow(Contract.Entry.COLUMN_NAME_RUBBISH_NAME));
                    //垃圾类别
                    String c_name = cursor.getString(cursor.getColumnIndexOrThrow(Contract.Entry.COLUMN_NAME_RUBBISH_CATALOGUE));
                    Log.v("AAA", itemId + " " + r_name + " " + c_name);
                    Toast.makeText(MainActivity.this, c_name, Toast.LENGTH_SHORT).show();
                }
                cursor.close();
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
                while (cursor.moveToNext()) {
                    long itemId = cursor.getLong(
                            cursor.getColumnIndexOrThrow(Contract.Entry._ID));
                    String itemName = cursor.getString(
                            cursor.getColumnIndexOrThrow(Contract.Entry.COLUMN_NAME_RUBBISH_NAME));
                    String itemCatalogue = cursor.getString(
                            cursor.getColumnIndexOrThrow(Contract.Entry.COLUMN_NAME_RUBBISH_CATALOGUE));
                    rubbishInfos.add(new RubbishInfo((int) itemId, itemName, itemCatalogue));
                }
                cursor.close();

                //显示listview内容

                listView.setAdapter(rubbishAdapter);
            }
        });
    }

    private void initSpinner() {
        spinner = (Spinner) findViewById(R.id.rubbish_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.rubbish_catalogue_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.v("AAA", "onActivityResult已启动:" + requestCode);
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        new RecognizingPicture().execute();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }

    //语音识别的方法

    /**
     * 初始化视图
     */
    private void initView() {
        mContent = (EditText) findViewById(R.id.et_content);
        mBtnVoice = (Button) findViewById(R.id.btn_voice);
        mBtnVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                clickMethod();

            }
        });
        //调用语音识别类方法

        initIatData(mContent);
    }


    //百度图像识别方法
    //图像识别方法
    public void sample(AipImageClassify client) throws Exception {
        // 传入可选参数调用接口
        HashMap<String, String> options = new HashMap<String, String>();

        // 参数为二进制数组
        //先将bitmap转为二进制数组
        byte[] file = bitmapToBase64(bitmap);

        Log.v("AAA", "已经获取照片" + file.length + file.toString());
        JSONObject res = client.advancedGeneral(file, options);
        Log.v("AAA", "开始识别咯");
        Log.v("AAA", res.toString(2) + "");
        JsonResponse = res.toString(2);
    }


    //百度识图处理网络连接时候单开的asyntask
    class RecognizingPicture extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            Toast.makeText(MainActivity.this, "正在识图,识图完毕后会自动显示", Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.v("AAA", "处理完咯");
            ParseJson();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                sample(client);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void ParseJson() {
        try {
            JSONObject base = new JSONObject(JsonResponse);
            JSONArray result = base.getJSONArray("result");
            JSONObject firstObject = (JSONObject) result.get(0);
            String keyword = firstObject.getString("keyword");
            mContent.setText(keyword);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * bitmap转为base64
     *
     * @param bitmap
     * @return
     */
    public static byte[] bitmapToBase64(Bitmap bitmap) {


        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                baos.flush();
                baos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return baos.toByteArray();
    }


}

