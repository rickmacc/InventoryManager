package org.eightfoldpath.inventorymanager;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import org.eightfoldpath.inventorymanager.data.InventoryItemContract;
import org.eightfoldpath.inventorymanager.data.InventoryItemContract.InventoryItemEntry;

public class ProductsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_LOADER = 0;
    private static final String LOG_TAG = ProductsActivity.class.getSimpleName();
    InventoryItemCursorAdapter cursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        ListView listView = (ListView) findViewById(R.id.inventory_items);
        cursorAdapter = new InventoryItemCursorAdapter(this, null);
        listView.setAdapter(cursorAdapter);

        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                InventoryItemEntry._ID,
                InventoryItemEntry.COLUMN_ITEM_NAME,
                InventoryItemEntry.COLUMN_ITEM_QTY,
                InventoryItemEntry.COLUMN_ITEM_PRICE
        };

        return new CursorLoader(this, InventoryItemEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }
}
