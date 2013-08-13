package net.somethingdreadful.MAL.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MALSqlHelper extends SQLiteOpenHelper {

    protected static final String DATABASE_NAME = "MAL.db";
    private static final int DATABASE_VERSION = 6;

    private static MALSqlHelper instance;

    public static final String COLUMN_ID = "_id";
    public static final String TABLE_ANIME = "anime";
    public static final String TABLE_MANGA = "manga";

    private static final String CREATE_ANIME_TABLE = "create table "
            + TABLE_ANIME + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + "recordID integer, "
            + "recordName varchar, "
            + "recordType varchar, "
            + "imageUrl varchar, "
            + "recordStatus varchar, "
            + "myStatus varchar, "
            + "memberScore float, "
            + "myScore integer, "
            + "synopsis varchar, "
            + "episodesWatched integer, "
            + "episodesTotal integer, "
            + "dirty boolean DEFAULT false, "
            + "lastUpdate integer NOT NULL DEFAULT (strftime('%s','now'))"
            + ");";

    private static final String CREATE_MANGA_TABLE = "create table "
            + TABLE_MANGA + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + "recordID integer, "
            + "recordName varchar, "
            + "recordType varchar, "
            + "imageUrl varchar, "
            + "recordStatus varchar, "
            + "myStatus varchar, "
            + "memberScore float, "
            + "myScore integer, "
            + "synopsis varchar, "
            + "chaptersRead integer, "
            + "chaptersTotal integer, "
            + "volumesRead integer, "
            + "volumesTotal integer, "
            + "dirty boolean DEFAULT false, "
            + "lastUpdate integer NOT NULL DEFAULT (strftime('%s','now'))"
            + ");";

    //Since SQLite doesn't allow "dynamic" dates, we set the default timestamp an adequate distance in the
    //past (1 December 1982) to make sure it will be in the past for update calculations. This should be okay,
    //since we are going to update the column whenever we sync.
    private static final String ADD_ANIME_SYNC_TIME = "ALTER TABLE "
            + TABLE_ANIME
            + " ADD COLUMN lastUpdate integer NOT NULL DEFAULT 407570400";

    private static final String ADD_MANGA_SYNC_TIME = "ALTER TABLE "
            + TABLE_MANGA
            + " ADD COLUMN lastUpdate integer NOT NULL DEFAULT 407570400";

    private static final String ALTER_ANIME_TABLE = "ALTER TABLE " + TABLE_ANIME + " ";
    private static final String ALTER_MANGA_TABLE = "ALTER TABLE " + TABLE_MANGA + " ";

    private static final String ADD_SYNONYMS = "ADD COLUMN synonyms varchar";

    private static final String ADD_ENGLISH_TITLES = "ADD COLUMN englishTitles varchar";

    private static final String ADD_JAPANESE_TITLES = "ADD COLUMN japaneseTitles varchar";

    private static final String ADD_RANK = "ADD COLUMN rank integer";

    private static final String ADD_POPULARITY_RANK = "ADD COLUMN popularityRank integer";

    private static final String ADD_START_DATE = "ADD COLUMN startDate varchar";

    private static final String ADD_END_DATE = "ADD COLUMN endDate varchar";

    private static final String ADD_GENRES = "ADD COLUMN genres varchar";


    public MALSqlHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized MALSqlHelper getHelper(Context context) {
        if (instance == null) {
            instance = new MALSqlHelper(context);
        }
        return instance;

    }

    @Override
    public String getDatabaseName() {
        return DATABASE_NAME;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ANIME_TABLE);
        db.execSQL(CREATE_MANGA_TABLE);

        upgradeV6(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("MALX", "Upgrading database from version " + oldVersion + " to " + newVersion);

        if ((oldVersion < 3)) {
            db.execSQL(CREATE_MANGA_TABLE);
        }

        if (oldVersion < 4) {
            db.execSQL(ADD_ANIME_SYNC_TIME);
            db.execSQL(ADD_MANGA_SYNC_TIME);
        }

        if (oldVersion < 5) {
            db.execSQL("create table temp_table as select * from " + TABLE_ANIME);
            db.execSQL("drop table " + TABLE_ANIME);
            db.execSQL(CREATE_ANIME_TABLE);
            db.execSQL("insert into " + TABLE_ANIME + " select * from temp_table;");
            db.execSQL("drop table temp_table;");

            db.execSQL("create table temp_table as select * from " + TABLE_MANGA);
            db.execSQL("drop table " + TABLE_MANGA);
            db.execSQL(CREATE_MANGA_TABLE);
            db.execSQL("insert into " + TABLE_MANGA + " select * from temp_table;");
            db.execSQL("drop table temp_table;");
        }

        if (oldVersion < 6) {
            upgradeV6(db);
        }
    }

    private void upgradeV6(SQLiteDatabase db) {
        db.execSQL(ALTER_ANIME_TABLE + ADD_SYNONYMS); //Synonyms
        db.execSQL(ALTER_MANGA_TABLE + ADD_SYNONYMS); //Formatted as CSV

        db.execSQL(ALTER_ANIME_TABLE + ADD_ENGLISH_TITLES); //Other English titles, if any
        db.execSQL(ALTER_MANGA_TABLE + ADD_ENGLISH_TITLES); //Formatted as CSV

        db.execSQL(ALTER_ANIME_TABLE + ADD_JAPANESE_TITLES); //Title in Japanese characters, if applicable
        db.execSQL(ALTER_MANGA_TABLE + ADD_JAPANESE_TITLES); //Formatted as CSV

        db.execSQL(ALTER_ANIME_TABLE + ADD_RANK); //Rank based on a weighted user score
        db.execSQL(ALTER_MANGA_TABLE + ADD_RANK); //Integer

        db.execSQL(ALTER_ANIME_TABLE + ADD_POPULARITY_RANK); //Rank based on popularity
        db.execSQL(ALTER_MANGA_TABLE + ADD_POPULARITY_RANK); //Integer

        db.execSQL(ALTER_ANIME_TABLE + ADD_START_DATE); //Add the start and end dates. Anime only.
        db.execSQL(ALTER_ANIME_TABLE + ADD_END_DATE); //Time formatted string. Use time parser or something.

        db.execSQL(ALTER_ANIME_TABLE + ADD_GENRES); //Genres
        db.execSQL(ALTER_MANGA_TABLE + ADD_GENRES); //Formatted as CSV
    }
}
