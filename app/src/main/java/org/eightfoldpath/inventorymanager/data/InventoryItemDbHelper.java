package org.eightfoldpath.inventorymanager.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.eightfoldpath.inventorymanager.data.InventoryItemContract.InventoryItemEntry;

public class InventoryItemDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = InventoryItemDbHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "inventory_manager.db";

    private static final int DATABASE_VERSION = 1;

    public InventoryItemDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_INVENTORY_ITEMS_TABLE =  "CREATE TABLE " + InventoryItemEntry.TABLE_NAME + " ("
                + InventoryItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventoryItemEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                + InventoryItemEntry.COLUMN_ITEM_PRICE + " REAL NOT NULL DEFAULT 0, "
                + InventoryItemEntry.COLUMN_ITEM_QTY + " INTEGER NOT NULL DEFAULT 0);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_INVENTORY_ITEMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}