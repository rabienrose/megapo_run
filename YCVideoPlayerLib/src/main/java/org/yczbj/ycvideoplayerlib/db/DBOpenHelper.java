package org.yczbj.ycvideoplayerlib.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.Entity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import org.yczbj.ycvideoplayerlib.model.Media;
import org.yczbj.ycvideoplayerlib.utils.Empty;

import java.util.ArrayList;

public class DBOpenHelper extends SQLiteOpenHelper {

    private SQLiteDatabase db;


    public DBOpenHelper(Context context) {
        super(context, "db_test", null, 1);
        db = getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS media(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "videoName TEXT," +
                "videoImage TEXT,unlock TEXT,position TEXT,percentage TEXT,num TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS media");
        onCreate(db);
    }

    public void add(Media media) {
        db.execSQL("INSERT INTO media (videoName,videoImage,unlock,position,percentage,num) VALUES(?,?,?,?,?,?)", new Object[]{
                media.getVideoName(), media.getVideoImage(), media.getUnlock(), media.getPosition(), media.getPercentage(), media.getNum()});
    }

    public void update(Media media) {
        ContentValues valuse = new ContentValues();
        if (!Empty.isEmpty(media.getUnlock())) {
            valuse.put("unlock", media.getUnlock());
        }
        if (!Empty.isEmpty(media.getPosition())) {
            valuse.put("position", media.getPosition());
        }
        if (!Empty.isEmpty(media.getPercentage())) {
            valuse.put("percentage", media.getPercentage());
        }
        if (!Empty.isEmpty(media.getNum())) {
            valuse.put("num", media.getNum());
        }
        int media1 = db.update("media", valuse, "id = ?", new String[]{media.getId() + ""});
    }

    public ArrayList<Media> getAllData() {
        ArrayList<Media> list = new ArrayList<>();
        Cursor cursor = db.query("media", null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String videoName = cursor.getString(cursor.getColumnIndex("videoName"));
            String videoImage = cursor.getString(cursor.getColumnIndex("videoImage"));
            String unlock = cursor.getString(cursor.getColumnIndex("unlock"));
            String position = cursor.getString(cursor.getColumnIndex("position"));
            String percentage = cursor.getString(cursor.getColumnIndex("percentage"));
            String num = cursor.getString(cursor.getColumnIndex("num"));
            list.add(new Media(id, videoName, videoImage, unlock, position, percentage,num));
        }
        return list;
    }


//    public ArrayList<Media> getAllById(String idV){
//        ArrayList<Media> list = new ArrayList<Media>();
//        Cursor cursor = db.query("media",null,"_id=?",new String[]{idV},null,null,null);
//        while(cursor.moveToNext()){
//            String name = cursor.getString(cursor.getColumnIndex("name"));
//            String id = cursor.getString(cursor.getColumnIndex("_id"));
//            String signature = cursor.getString(cursor.getColumnIndex("signature"));
//            list.add(new Media(id,signature,name));
//        }
//        return list;
//    }

}
