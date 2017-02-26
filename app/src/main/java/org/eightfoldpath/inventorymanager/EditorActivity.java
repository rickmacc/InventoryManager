package org.eightfoldpath.inventorymanager;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.eightfoldpath.inventorymanager.data.InventoryItemContract.InventoryItemEntry;

import org.eightfoldpath.inventorymanager.R;

import static android.R.attr.name;
import static org.eightfoldpath.inventorymanager.data.InventoryItemContract.BASE_CONTENT_URI;
import static org.eightfoldpath.inventorymanager.data.InventoryItemContract.PATH_INVENTORY_ITEMS;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTORY_ITEM_LOADER = 1;
    private static final String LOG_TAG = EditorActivity.class.getSimpleName();
    private boolean itemDataEntered = false;

    InventoryItemCursorAdapter cursorAdapter;

    public static final String EDITOR_URI = "org.eightfoldpath.inventorymanager.EDITOR_URI";

    private EditText nameEditText;
    private EditText priceEditText;
    private EditText qtyEditText;

    private Uri currentInventoryItemUri = null;

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            itemDataEntered = true;
            return false;
        }
    };

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
        nameEditText.setOnTouchListener(touchListener);
        priceEditText = (EditText) findViewById(R.id.edit_item_price);
        priceEditText.setOnTouchListener(touchListener);
        qtyEditText = (EditText) findViewById(R.id.edit_item_qty);
        qtyEditText.setOnTouchListener(touchListener);

        if (currentInventoryItemUri == null) {
            // This is a new item, so change the app bar to say "Add a InventoryItem"
            setTitle(getString(R.string.editor_activity_title_new_item));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a item that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            Button deleteButton = (Button) findViewById(R.id.button_delete);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDeleteConfirmationDialog();
                }
            });

        }

        cursorAdapter = new InventoryItemCursorAdapter(this, null);
    }

    private void saveInventoryItem() {

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = nameEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        String qtyString = qtyEditText.getText().toString().trim();

        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, "InventoryItems must have a name", Toast.LENGTH_SHORT).show();
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

        if (currentInventoryItemUri == null) {
            Uri newRow = getContentResolver().insert(InventoryItemEntry.CONTENT_URI, values);
            if (newRow == null) {
                Toast.makeText(this, "Error saving item", Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast with the row ID.
                long newRowId = ContentUris.parseId(newRow);
                Toast.makeText(this, "InventoryItem saved with row id: " + newRowId, Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsUpdated = getContentResolver().update(currentInventoryItemUri, values, null, null);
            if (rowsUpdated == 0) {
                Toast.makeText(this, "Error updating item", Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast with the row ID.
                Toast.makeText(this, "InventoryItem updated", Toast.LENGTH_SHORT).show();
            }
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
                Toast.makeText(this, "Error deleting item", Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast with the row ID.
                Toast.makeText(this, "InventoryItem deleted", Toast.LENGTH_SHORT).show();
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new item, hide the "Delete" menu item.
        //if (mCurrentInventoryItemUri == null) {
        if (currentInventoryItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save item to database
                saveInventoryItem();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!itemDataEntered) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                InventoryItemEntry._ID,
                InventoryItemEntry.COLUMN_ITEM_NAME,
                InventoryItemEntry.COLUMN_ITEM_PRICE,
                InventoryItemEntry.COLUMN_ITEM_QTY
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
        }

        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        nameEditText.setText("");
        priceEditText.setText("");
        qtyEditText.setText("");

        cursorAdapter.swapCursor(null);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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

    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!itemDataEntered) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

}