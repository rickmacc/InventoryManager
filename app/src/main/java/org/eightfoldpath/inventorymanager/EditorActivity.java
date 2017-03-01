package org.eightfoldpath.inventorymanager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.eightfoldpath.inventorymanager.data.InventoryItemContract.InventoryItemEntry;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_ITEM_LOADER = 1;
    private static final int SELECT_PHOTO = 1;
    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    InventoryItemCursorAdapter cursorAdapter;

    private EditText nameEditText;
    private EditText priceEditText;
    private EditText qtyEditText;
    private EditText imageEditText;
    private EditText qtyOnOrderText;
    private EditText qtySellText;
    private EditText qtyOrderText;
    private EditText qtyReceiveText;

    private Uri itemImageUri;

    private Uri currentInventoryItemUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        currentInventoryItemUri = intent.getData();

        if (currentInventoryItemUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_item));
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_item));
            long itemId = ContentUris.parseId(currentInventoryItemUri);
            Log.d(LOG_TAG, "Editing intentory with id " + itemId);
            getLoaderManager().initLoader(INVENTORY_ITEM_LOADER, null, this);
        }

        nameEditText = (EditText) findViewById(R.id.edit_item_name);
        priceEditText = (EditText) findViewById(R.id.edit_item_price);
        qtyEditText = (EditText) findViewById(R.id.edit_item_qty);

        Button selectItemImage = (Button) findViewById(R.id.select_item_image);
        selectItemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });

        qtyOnOrderText = (EditText) findViewById(R.id.on_order_item_qty);
        qtySellText = (EditText) findViewById(R.id.sell_item_qty);
        qtyOrderText = (EditText) findViewById(R.id.order_item_qty);
        qtyReceiveText = (EditText) findViewById(R.id.receive_item_qty);

        if (currentInventoryItemUri == null) {
            // This is a new item, so change the app bar to say "Add a InventoryItem"
            setTitle(getString(R.string.editor_activity_title_new_item));

            hideUpdateViews();
        } else {

            Button deleteButton = (Button) findViewById(R.id.button_delete);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDeleteConfirmationDialog();
                }
            });

            Button sellButton = (Button) findViewById(R.id.button_sell);
            sellButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sellInventoryItem();
                }
            });

            Button orderButton = (Button) findViewById(R.id.button_order);
            orderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    orderInventoryItem();
                }
            });

            Button receiveButton = (Button) findViewById(R.id.button_receive);
            receiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    receiveInventoryItem();
                }
            });

        }

        cursorAdapter = new InventoryItemCursorAdapter(this, null);
    }

    private void hideUpdateViews() {


        LinearLayout onOrderItemLayout = (LinearLayout) findViewById(R.id.on_order_item);
        onOrderItemLayout.setVisibility(View.INVISIBLE);

        View dividerSell = (View) findViewById(R.id.divider_sell);
        dividerSell.setVisibility(View.INVISIBLE);
        LinearLayout sellItemLayout = (LinearLayout) findViewById(R.id.sell_item_layout);
        sellItemLayout.setVisibility(View.INVISIBLE);
        Button sellButton = (Button) findViewById(R.id.button_sell);
        sellButton.setVisibility(View.INVISIBLE);

        View dividerOrder = (View) findViewById(R.id.divider_order);
        dividerOrder.setVisibility(View.INVISIBLE);
        LinearLayout orderItemLayout = (LinearLayout) findViewById(R.id.order_item_layout);
        orderItemLayout.setVisibility(View.INVISIBLE);
        Button orderButton = (Button) findViewById(R.id.button_order);
        orderButton.setVisibility(View.INVISIBLE);

        View dividerReceive = (View) findViewById(R.id.divider_receive);
        dividerReceive.setVisibility(View.INVISIBLE);
        LinearLayout receiveItemLayout = (LinearLayout) findViewById(R.id.receive_item_layout);
        receiveItemLayout.setVisibility(View.INVISIBLE);
        Button receiveButton = (Button) findViewById(R.id.button_receive);
        receiveButton.setVisibility(View.INVISIBLE);

        View dividerDelete = (View) findViewById(R.id.divider_delete);
        dividerDelete.setVisibility(View.INVISIBLE);
        Button deleteButton = (Button) findViewById(R.id.button_delete);
        deleteButton.setVisibility(View.INVISIBLE);
    }

    private void saveInventoryItem() {

        String nameString = nameEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        String qtyString = qtyEditText.getText().toString().trim();

        if (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) || TextUtils.isEmpty(qtyString) || (itemImageUri == null) || TextUtils.isEmpty(itemImageUri.toString())) {
            Toast.makeText(this, R.string.complete_all_entries_message, Toast.LENGTH_SHORT).show();
            return;
        }

        double price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Double.parseDouble(priceString);
        }

        int quantity = 0;
        if (!TextUtils.isEmpty(qtyString)) {
            quantity = Integer.parseInt(qtyString);
        }

        ContentValues values = new ContentValues();
        values.put(InventoryItemEntry.COLUMN_ITEM_NAME, nameString);
        values.put(InventoryItemEntry.COLUMN_ITEM_PRICE, price);
        values.put(InventoryItemEntry.COLUMN_ITEM_QTY, quantity);
        values.put(InventoryItemEntry.COLUMN_ITEM_IMAGE, itemImageUri.toString());
        Log.d(LOG_TAG, "Saving item " + nameString + ", price: " + price + ", qty: " + quantity + ", image url: " + itemImageUri.toString());

        if (currentInventoryItemUri == null) {
            Uri newRow = getContentResolver().insert(InventoryItemEntry.CONTENT_URI, values);
            if (newRow == null) {
                Toast.makeText(this, getResources().getString(R.string.save_error_message), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast with the row ID.
                long newRowId = ContentUris.parseId(newRow);
                Toast.makeText(this, getResources().getString(R.string.save_success_message) + newRowId, Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsUpdated = getContentResolver().update(currentInventoryItemUri, values, null, null);
            if (rowsUpdated == 0) {
                Toast.makeText(this, getResources().getString(R.string.update_error_message), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast with the row ID.
                Toast.makeText(this, getResources().getString(R.string.update_success_message), Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    private void sellInventoryItem() {

        String nameString = nameEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        String qtyString = qtyEditText.getText().toString().trim();
        int currentQuantity = Integer.parseInt(qtyString);
        String qtySellString = qtySellText.getText().toString().trim();
        int sellQuantity = 0;

        if (TextUtils.isEmpty(qtySellString)) {
            Toast.makeText(this, getResources().getString(R.string.sell_qty_message), Toast.LENGTH_SHORT).show();
            return;
        } else {
            sellQuantity = Integer.parseInt(qtySellString);
            if (sellQuantity <= 0) {
                Toast.makeText(this, getResources().getString(R.string.sell_qty_message), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        int newQuantity = currentQuantity - sellQuantity;
        if (newQuantity < 0) {
            Toast.makeText(this, getResources().getString(R.string.sell_qty_too_high_message), Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryItemEntry.COLUMN_ITEM_NAME, nameString);
        values.put(InventoryItemEntry.COLUMN_ITEM_PRICE, priceString);
        values.put(InventoryItemEntry.COLUMN_ITEM_QTY, newQuantity);

        int rowsUpdated = getContentResolver().update(currentInventoryItemUri, values, null, null);
        if (rowsUpdated == 0) {
            Toast.makeText(this, getResources().getString(R.string.sell_error_message), Toast.LENGTH_SHORT).show();
        } else {
            qtySellText.setText("");
        }
    }

    private void orderInventoryItem() {

        String nameString = nameEditText.getText().toString().trim();
        String currentQtyOnOrderString = qtyOnOrderText.getText().toString().trim();
        int currentQtyOnOrder = Integer.parseInt(currentQtyOnOrderString);
        String qtyOrderString = qtyOrderText.getText().toString().trim();
        int orderQuantity = 0;

        if (TextUtils.isEmpty(qtyOrderString)) {
            Toast.makeText(this, getResources().getString(R.string.order_qty_message), Toast.LENGTH_SHORT).show();
            return;
        } else {
            orderQuantity = Integer.parseInt(qtyOrderString);
            if (orderQuantity <= 0) {
                Toast.makeText(this, getResources().getString(R.string.order_qty_message), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        int newQuantity = currentQtyOnOrder + orderQuantity;

        ContentValues values = new ContentValues();
        values.put(InventoryItemEntry.COLUMN_ITEM_NAME, nameString);
        values.put(InventoryItemEntry.COLUMN_ITEM_QTY_ON_ORDER, newQuantity);

        int rowsUpdated = getContentResolver().update(currentInventoryItemUri, values, null, null);
        if (rowsUpdated == 0) {
            Toast.makeText(this, getResources().getString(R.string.order_error_message), Toast.LENGTH_SHORT).show();
        } else {
            qtyOrderText.setText("");

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getText(R.string.order_item));
            intent.putExtra(Intent.EXTRA_TEXT, nameString + " - " + qtyOrderString);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }


        }
    }

    private void receiveInventoryItem() {

        String nameString = nameEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        String qtyString = qtyEditText.getText().toString().trim();
        int currentQuantity = Integer.parseInt(qtyString);
        String currentQtyOnOrderString = qtyOnOrderText.getText().toString().trim();
        int currentQtyOnOrder = Integer.parseInt(currentQtyOnOrderString);
        String qtyReceiveString = qtyReceiveText.getText().toString().trim();
        int receiveQuantity = 0;

        if (TextUtils.isEmpty(qtyReceiveString)) {
            Toast.makeText(this, getResources().getString(R.string.receive_qty_message), Toast.LENGTH_SHORT).show();
            return;
        } else {
            receiveQuantity = Integer.parseInt(qtyReceiveString);
            if (receiveQuantity <= 0) {
                Toast.makeText(this, getResources().getString(R.string.receive_qty_message), Toast.LENGTH_SHORT).show();
                return;
            }
        }

        int newQuantity = currentQuantity + receiveQuantity;
        int newQuantityOnOrder = currentQtyOnOrder - receiveQuantity;
        if (newQuantityOnOrder <= 0) {
            newQuantityOnOrder = 0;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryItemEntry.COLUMN_ITEM_NAME, nameString);
        values.put(InventoryItemEntry.COLUMN_ITEM_PRICE, priceString);
        values.put(InventoryItemEntry.COLUMN_ITEM_QTY, newQuantity);
        values.put(InventoryItemEntry.COLUMN_ITEM_QTY_ON_ORDER, newQuantityOnOrder);

        int rowsUpdated = getContentResolver().update(currentInventoryItemUri, values, null, null);
        if (rowsUpdated == 0) {
            Toast.makeText(this, getResources().getString(R.string.receive_error_message), Toast.LENGTH_SHORT).show();
        } else {
            qtyReceiveText.setText("");
        }
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteInventoryItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the item in the database.
     */
    private void deleteInventoryItem() {
        if (currentInventoryItemUri != null) {
            int rowsUpdated = getContentResolver().delete(currentInventoryItemUri, null, null);
            if (rowsUpdated == 0) {
                Toast.makeText(this, getResources().getString(R.string.delete_error_message), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast with the row ID.
                Toast.makeText(this, getResources().getString(R.string.delete_success_message), Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                // Save item to database
                saveInventoryItem();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    itemImageUri = imageReturnedIntent.getData();
                    loadItemImage();
                }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                InventoryItemEntry._ID,
                InventoryItemEntry.COLUMN_ITEM_NAME,
                InventoryItemEntry.COLUMN_ITEM_PRICE,
                InventoryItemEntry.COLUMN_ITEM_QTY,
                InventoryItemEntry.COLUMN_ITEM_IMAGE,
                InventoryItemEntry.COLUMN_ITEM_QTY_ON_ORDER
        };

        Log.d(LOG_TAG, "Creating loader with URI : " + currentInventoryItemUri.toString());
        return new CursorLoader(this, currentInventoryItemUri, projection, null, null, null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data.moveToFirst()) {
            nameEditText.setText(data.getString(data.getColumnIndex(InventoryItemEntry.COLUMN_ITEM_NAME)));
            priceEditText.setText(data.getString(data.getColumnIndex(InventoryItemEntry.COLUMN_ITEM_PRICE)));
            qtyEditText.setText(data.getString(data.getColumnIndex(InventoryItemEntry.COLUMN_ITEM_QTY)));
            itemImageUri = Uri.parse(data.getString(data.getColumnIndex(InventoryItemEntry.COLUMN_ITEM_IMAGE)));
            qtyOnOrderText.setText(data.getString(data.getColumnIndex(InventoryItemEntry.COLUMN_ITEM_QTY_ON_ORDER)));

            loadItemImage();
        }

        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        nameEditText.setText("");
        priceEditText.setText("");
        qtyEditText.setText("");
        itemImageUri = null;

        cursorAdapter.swapCursor(null);
    }

    private void loadItemImage() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            try {
                ImageView imageView = (ImageView) findViewById(R.id.item_image);
                final InputStream imageStream = getContentResolver().openInputStream(itemImageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imageView.setImageBitmap(selectedImage);
            } catch (FileNotFoundException ex) {
                Log.d(LOG_TAG, ex.toString());
            }
        }
    }

}