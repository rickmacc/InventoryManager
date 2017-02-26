package org.eightfoldpath.inventorymanager.data;

import android.net.Uri;
import android.content.ContentResolver;
import android.provider.BaseColumns;

public final class InventoryItemContract {

    public static final String CONTENT_AUTHORITY = "org.eightfoldpath.inventorymanager";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_INVENTORY_ITEMS = "items";
    public static final String PATH_INVENTORY_ITEMS_ID = "items/#";

    private InventoryItemContract() {}

    public static final class InventoryItemEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY_ITEMS);
        public static final Uri CONTENT_URI_ID = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY_ITEMS_ID);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of inventory items.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY_ITEMS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single pet.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY_ITEMS_ID;

        public final static String TABLE_NAME = "items";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_ITEM_NAME ="name";

        public final static String COLUMN_ITEM_QTY = "quantity";

        public final static String COLUMN_ITEM_PRICE = "price";

        public final static String COLUMN_ITEM_QTY_ON_ORDER = "quantity_on_order";

    }

}

