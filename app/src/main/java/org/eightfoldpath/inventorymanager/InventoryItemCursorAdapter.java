package org.eightfoldpath.inventorymanager;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import org.eightfoldpath.inventorymanager.data.InventoryItemContract.InventoryItemEntry;

import java.text.NumberFormat;

public class InventoryItemCursorAdapter extends CursorAdapter {

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
        viewName.setText(name);

        int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryItemEntry.COLUMN_ITEM_QTY));
        viewQty.setText(quantity);

        double price = cursor.getDouble(cursor.getColumnIndexOrThrow(InventoryItemEntry.COLUMN_ITEM_PRICE));
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
        viewPrice.setText(currencyFormatter.format(price));
    }
}
