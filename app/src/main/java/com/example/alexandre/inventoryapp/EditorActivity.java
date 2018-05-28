package com.example.alexandre.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.alexandre.inventoryapp.data.InventoryContract;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PRODUCT_LOADER = 1;
    private static final int REQUEST_CODE = 1;
    private EditText mProductName;
    private EditText mProductQuantity;
    private EditText mProductPrice;
    private EditText mProductProvider;
    private EditText mProviderEmail;
    private EditText mProviderPhone;
    private ImageView mProductImage;
    private Uri uri;
    private boolean mProductHasChanged = false;
    private Bitmap mSelectedImage;
    private Button mButtonMinus;
    private Button mButtonMore;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mProductName = findViewById(R.id.edit_product_name);
        mProductQuantity = findViewById(R.id.edit_quantity);
        mProductPrice = findViewById(R.id.edit_price);
        mProductProvider = findViewById(R.id.edit_provider);
        mProviderEmail = findViewById(R.id.edit_email);
        mProviderPhone = findViewById(R.id.edit_phone);
        mProductImage = findViewById(R.id.edit_image);
        mButtonMinus = findViewById(R.id.minus_button);
        mButtonMore = findViewById(R.id.more_button);

        mProductName.setOnTouchListener(mTouchListener);
        mProductImage.setOnTouchListener(mTouchListener);
        mProviderPhone.setOnTouchListener(mTouchListener);
        mProviderEmail.setOnTouchListener(mTouchListener);
        mProductQuantity.setOnTouchListener(mTouchListener);
        mProductPrice.setOnTouchListener(mTouchListener);
        mProductProvider.setOnTouchListener(mTouchListener);

        mProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE);
            }
        });

        mButtonMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addQuantity(mProductQuantity);
            }
        });

        mButtonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subtractQuantity(mProductQuantity);
            }
        });

        uri = null;
        Intent intent = getIntent();
        uri = intent.getData();
        if (uri == null) {
            this.setTitle(R.string.add_product);
            invalidateOptionsMenu();
        } else {
            this.setTitle(R.string.update_product);
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }
    }

    private void addQuantity(EditText productQuantity) {
        int qty = 0;
        if (!productQuantity.getText().toString().isEmpty()) {
            qty = Integer.parseInt(productQuantity.getText().toString());
        }
        qty += 1;
        productQuantity.setText(String.valueOf(qty));
    }

    private void subtractQuantity(EditText productQuantity) {
        int qty = 0;
        if (!productQuantity.getText().toString().isEmpty()) {
            qty = Integer.parseInt(productQuantity.getText().toString());
        }
        qty -= 1;
        if (qty >= 0) {
            productQuantity.setText(String.valueOf(qty));
        } else {
            Toast.makeText(this, R.string.quantity_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" and Contact Provider menu item.
        if (uri == null) {
            MenuItem deleteItem = menu.findItem(R.id.action_delete);
            deleteItem.setVisible(false);
            MenuItem providerItem = menu.findItem(R.id.contact_provider);
            providerItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (uri == null) {
                    try {
                        insertProduct();
                    } catch (IllegalArgumentException e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                } else {
                    updateProduct();
                }
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case R.id.contact_provider:
                String[] email = {mProviderEmail.getText().toString()};
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, email);
                intent.putExtra(Intent.EXTRA_SUBJECT, mProductName.getText().toString());
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                return true;
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                InventoryContract.ProductEntry._ID,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_IMAGE,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_PROVIDER_EMAIL,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_PROVIDER,
                InventoryContract.ProductEntry.COLUMN_PRODUCT_PROVIDER_PHONE
        };

        try {
            return new CursorLoader(this,
                    uri,
                    projection,
                    null,
                    null,
                    null);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            int productColumnIndex = data.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int quantityColumnIndex = data.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int priceColumnIndex = data.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            int imageColumnIndex = data.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_IMAGE);
            int providerColumnIndex = data.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_PROVIDER);
            int emailColumnIndex = data.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_PROVIDER_EMAIL);
            int phoneColumnIndex = data.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_PROVIDER_PHONE);

            String productName = data.getString(productColumnIndex);
            int productQuantity = data.getInt(quantityColumnIndex);
            float productPrice = data.getFloat(priceColumnIndex);
            byte[] productImage = data.getBlob(imageColumnIndex);
            String productProvider = data.getString(providerColumnIndex);
            String providerEmail = data.getString(emailColumnIndex);
            String providerPhone = data.getString(phoneColumnIndex);
            Bitmap image = BitmapFactory.decodeByteArray(productImage, 0, productImage.length);
            mProductImage.setImageBitmap(image);
            mProductName.setText(productName);
            mProductProvider.setText(productProvider);
            mProductPrice.setText(String.valueOf(productPrice));
            mProductQuantity.setText(productQuantity + "");
            mProviderEmail.setText(providerEmail);
            mProviderPhone.setText(providerPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                mSelectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                mProductImage.setImageBitmap(mSelectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void insertProduct() {
        String productName = mProductName.getText().toString().trim();
        String productProvider = mProductProvider.getText().toString().trim();
        String productPrice = mProductPrice.getText().toString().trim();
        String productQuantity = mProductQuantity.getText().toString().trim();
        String providerEmail = mProviderEmail.getText().toString().trim();
        String providerPhone = mProviderPhone.getText().toString().trim();
        Drawable draw = mProductImage.getDrawable();
        Bitmap bitmap = ((BitmapDrawable) draw).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] productImage = stream.toByteArray();
        int quantity = 0;
        float price = 0;
        if (!TextUtils.isEmpty(productPrice)) {
            price = Float.parseFloat(productPrice);
        }
        if (!TextUtils.isEmpty(productQuantity)) {
            quantity = Integer.parseInt(productQuantity);
        }

        ContentValues values = new ContentValues();
        values.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME, productName);
        values.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_PROVIDER, productProvider);
        values.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_IMAGE, productImage);
        values.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_PROVIDER_EMAIL, providerEmail);
        values.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_PROVIDER_PHONE, providerPhone);

        Log.e("Price: ", productPrice + "");

        long newRowId = ContentUris.parseId(getContentResolver().insert(InventoryContract.ProductEntry.CONTENT_URI, values));
        if (newRowId == -1) {
            Toast.makeText(this, getString(R.string.error_insert), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.insert_ok) + newRowId, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProduct() {
        String productName = mProductName.getText().toString().trim();
        String productProvider = mProductProvider.getText().toString().trim();
        String productPrice = mProductPrice.getText().toString().trim();
        String productQuantity = mProductQuantity.getText().toString().trim();
        String providerEmail = mProviderEmail.getText().toString().trim();
        String providerPhone = mProviderPhone.getText().toString().trim();
        Drawable draw = mProductImage.getDrawable();
        Bitmap bitmap = ((BitmapDrawable) draw).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] productImage = stream.toByteArray();
        int quantity = 0;
        float price = 0;
        if (!TextUtils.isEmpty(productPrice)) {
            price = Float.parseFloat(productPrice);
        }
        if (!TextUtils.isEmpty(productQuantity)) {
            quantity = Integer.parseInt(productQuantity);
        }

        ContentValues values = new ContentValues();
        values.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME, productName);
        values.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_PROVIDER, productProvider);
        values.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_IMAGE, productImage);
        values.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_PROVIDER_EMAIL, providerEmail);
        values.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_PROVIDER_PHONE, providerPhone);

        int rowsAffected = getContentResolver().update(uri, values, null, null);

        if (rowsAffected == 0) {
            Toast.makeText(this, R.string.update_failed, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.update_success, Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteProduct() {
        if (uri != null) {
            int rowsDeleted = getContentResolver().delete(uri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Cria um AlertDialog.Builder e configura a mensagem e click listeners
        // para os botões positivos e negativos do dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // O usuário clicou no botão "Continuar editando", então, feche a caixa de diálogo
                // e continue editando o pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Cria e mostra o AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
