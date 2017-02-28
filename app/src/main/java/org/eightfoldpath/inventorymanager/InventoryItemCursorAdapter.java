package org.eightfoldpath.inventorymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.eightfoldpath.inventorymanager.data.InventoryItemContract.InventoryItemEntry;

import java.text.NumberFormat;

import static android.R.attr.name;

public class InventoryItemCursorAdapter extends CursorAdapter {

    private static final String LOG_TAG = InventoryItemCursorAdapter.class.getSimpleName();

    /**
     * Constructs a new {@link InventoryItemCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_view, parent, false);
    }

    /**
     * This method binds the inventory item data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current inventory item can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView viewName = (TextView) view.findViewById(R.id.product_name);
        TextView viewQty = (TextView) view.findViewById(R.id.product_qty);
        TextView viewPrice = (TextView) view.findViewById(R.id.product_price);

        String name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryItemEntry.COLUMN_ITEM_NAME));
        Log.d(LOG_TAG, "Binding view name with value :" + name);
        viewName.setText(name);

        int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryItemEntry.COLUMN_ITEM_QTY));
        Log.d(LOG_TAG, "Binding view quantity with value :" + quantity);
        viewQty.setText(Integer.toString(quantity));

        double price = cursor.getDouble(cursor.getColumnIndexOrThrow(InventoryItemEntry.COLUMN_ITEM_PRICE));
        Log.d(LOG_TAG, "Binding view price with value :" + price);
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
        viewPrice.setText(currencyFormatter.format(price));

        int itemId = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryItemEntry._ID));
        if (quantity > 0) {
            ((Button) view.findViewById(R.id.button_sell)).setOnClickListener(
                    new sellButtonClickListener(context, itemId, name, quantity, price));
        } else {
            ((Button) view.findViewById(R.id.button_sell)).setEnabled(false);
        }

    }

    public class sellButtonClickListener implements View.OnClickListener{

        private Context context;
        private int itemId;
        String itemName;
        int itemQuantity;
        double itemPrice;

        public sellButtonClickListener(Context context, int itemId, String itemName, int itemQuantity, double itemPrice){
            this.context = context;
            this.itemId=itemId;
            this.itemName = itemName;
            this.itemQuantity = itemQuantity;
            this.itemPrice = itemPrice;
        }

        @Override
        public void onClick(View v) {
            Log.d(LOG_TAG, "Button clicked with inventory item :" + itemId);

            int newQuantity = itemQuantity - 1;
            if (newQuantity < 0) { newQuantity = 0; }

            ContentValues values = new ContentValues();
            values.put(InventoryItemEntry.COLUMN_ITEM_NAME, itemName);
            values.put(InventoryItemEntry.COLUMN_ITEM_PRICE, Double.toString(itemPrice));
            values.put(InventoryItemEntry.COLUMN_ITEM_QTY, newQuantity);

            Uri currentInventoryItemUri = Uri.withAppendedPath(InventoryItemEntry.CONTENT_URI, Integer.toString(itemId));
            int rowsUpdated = context.getContentResolver().update(currentInventoryItemUri, values, null, null);
        }

    }

}
