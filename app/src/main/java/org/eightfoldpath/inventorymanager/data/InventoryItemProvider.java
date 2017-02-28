package org.eightfoldpath.inventorymanager.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import org.eightfoldpath.inventorymanager.data.InventoryItemContract.InventoryItemEntry;

public class InventoryItemProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = InventoryItemProvider.class.getSimpleName();
    private InventoryItemDbHelper dbHelper = null;

    private static final int INVENTORY_ITEMS = 100;
    private static final int INVENTORY_ITEMS_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(InventoryItemContract.CONTENT_AUTHORITY, InventoryItemContract.PATH_INVENTORY_ITEMS, INVENTORY_ITEMS);
        Log.d(LOG_TAG, "Adding uri :" + InventoryItemContract.PATH_INVENTORY_ITEMS);
        sUriMatcher.addURI(InventoryItemContract.CONTENT_AUTHORITY, InventoryItemContract.PATH_INVENTORY_ITEMS_ID, INVENTORY_ITEMS_ID);
        Log.d(LOG_TAG, "Adding uri :" + INVENTORY_ITEMS_ID);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new InventoryItemDbHelper(this.getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // Get readable database
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        Log.d(LOG_TAG, "query uri: " + uri.toString());
        int match = sUriMatcher.match(uri);
        Log.d(LOG_TAG, "uri match: " + match);
        switch (match) {
            case INVENTORY_ITEMS:
                cursor = database.query(InventoryItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case INVENTORY_ITEMS_ID:
                selection = InventoryItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(InventoryItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        validateInventoryItemOnInsert(contentValues);

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY_ITEMS:
                return insertInventoryItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Could not create inventory item for " + uri);
        }
    }

    private Uri insertInventoryItem(Uri uri, ContentValues values) {

        // Gets the database in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        long newRowId = db.insert(InventoryItemEntry.TABLE_NAME, null, values);

        if (newRowId == -1) {
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, newRowId);

    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        if (contentValues.size() == 0) return 0;
        validateInventoryItemOnUpdate(contentValues);

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY_ITEMS:
                return updateInventoryItem(uri, contentValues, selection, selectionArgs);
            case INVENTORY_ITEMS_ID:
                selection = InventoryItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateInventoryItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Could not update inventory item " + uri);
        }
    }

    private int updateInventoryItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // Gets the database in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int numRowsUpdated = db.update(InventoryItemEntry.TABLE_NAME, values, selection, selectionArgs);

        if (numRowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        int numRowsDeleted = -1;

        // Get writeable database
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY_ITEMS:
                numRowsDeleted = database.delete(InventoryItemEntry.TABLE_NAME, selection, selectionArgs);
                Log.d(LOG_TAG, "Total number of rows deleted :" + numRowsDeleted);
                break;
            case INVENTORY_ITEMS_ID:
                selection = InventoryItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                numRowsDeleted = database.delete(InventoryItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Could not delete inventory item " + uri);
        }

        if (numRowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsDeleted;
    }

    private void validateInventoryItemOnInsert(ContentValues contentValues) {

        // Check that the name is not null
        if (contentValues.containsKey(InventoryItemEntry.COLUMN_ITEM_NAME)) {
            String name = contentValues.getAsString(InventoryItemEntry.COLUMN_ITEM_NAME);
            if (name == null) {
                throw new IllegalArgumentException("InventoryItem requires a name");
            }
        } else {
            throw new IllegalArgumentException("InventoryItem requires a name");
        }
        if (contentValues.containsKey(InventoryItemEntry.COLUMN_ITEM_QTY)) {
            Integer quantity = contentValues.getAsInteger(InventoryItemEntry.COLUMN_ITEM_QTY);
            if (quantity.intValue() <= 0) {
                throw new IllegalArgumentException("InventoryItem quantity must be greater than zero");
            }
        }
        if (contentValues.containsKey(InventoryItemEntry.COLUMN_ITEM_PRICE)) {
            Double price = contentValues.getAsDouble(InventoryItemEntry.COLUMN_ITEM_PRICE);
            if (price.doubleValue() <= 0) {
                throw new IllegalArgumentException("InventoryItem price must be greater than zero");
            }
        }
    }

    private void validateInventoryItemOnUpdate(ContentValues contentValues) {

        // Check that the name is not null
        if (contentValues.containsKey(InventoryItemEntry.COLUMN_ITEM_NAME)) {
            String name = contentValues.getAsString(InventoryItemEntry.COLUMN_ITEM_NAME);
            if (name == null) {
                throw new IllegalArgumentException("InventoryItem requires a name");
            }
        } else {
            throw new IllegalArgumentException("InventoryItem requires a name");
        }
        if (contentValues.containsKey(InventoryItemEntry.COLUMN_ITEM_PRICE)) {
            Double price = contentValues.getAsDouble(InventoryItemEntry.COLUMN_ITEM_PRICE);
            if (price.doubleValue() <= 0) {
                throw new IllegalArgumentException("InventoryItem price must be greater than zero");
            }
        }
        if (contentValues.containsKey(InventoryItemEntry.COLUMN_ITEM_QTY)) {
            Integer quantity = contentValues.getAsInteger(InventoryItemEntry.COLUMN_ITEM_QTY);
            if (quantity.intValue() <= 0) {
                throw new IllegalArgumentException("InventoryItem quantity must be greater than zero");
            }
        }
        if (contentValues.containsKey(InventoryItemEntry.COLUMN_ITEM_QTY_ON_ORDER)) {
            Integer quantity = contentValues.getAsInteger(InventoryItemEntry.COLUMN_ITEM_QTY_ON_ORDER);
            if (quantity.intValue() < 0) {
                throw new IllegalArgumentException("InventoryItem quantity on order must be greater than zero");
            }
        }
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY_ITEMS:
                return InventoryItemEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ITEMS_ID:
                return InventoryItemEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
