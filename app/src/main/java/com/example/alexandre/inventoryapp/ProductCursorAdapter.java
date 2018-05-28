package com.example.alexandre.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.alexandre.inventoryapp.data.InventoryContract;
import java.text.NumberFormat;


public class ProductCursorAdapter extends CursorAdapter {
    private Context context;

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        this.context = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        ImageView imageView = view.findViewById(R.id.product_image);
        TextView productTextView = view.findViewById(R.id.product_text_view);
        TextView quantityTextView = view.findViewById(R.id.quantity_text_view);
        TextView priceTextView = view.findViewById(R.id.price_text_view);
        ImageButton sellButton = view.findViewById(R.id.sell_image);

        // Find the columns of product attributes that we're interested in
        int idColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_ID);
        int imageColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_IMAGE);
        int productColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE);

        // Read the product attributes from the Cursor for the current product
        String productName = cursor.getString(productColumnIndex);
        byte[] productImage = cursor.getBlob(imageColumnIndex);
        final int productQuantity = cursor.getInt(quantityColumnIndex);
        float productPrice = cursor.getFloat(priceColumnIndex);
        final int Id = cursor.getInt(idColumnIndex);

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = ContentUris.withAppendedId(InventoryContract.ProductEntry.CONTENT_URI, Id );
                makeSell(uri, productQuantity);
            }
        });

        // Update the TextViews with the attributes for the current product
        productTextView.setText(productName);
        if (productImage != null) {
            imageView.setImageBitmap(null);
        }
        NumberFormat baseFormat = NumberFormat.getCurrencyInstance();
        String priceStr = baseFormat.format(productPrice);
        StringBuilder builder = new StringBuilder();
        builder.append(context.getString(R.string.qtd));
        builder.append(productQuantity);
        String qtdFormatted = builder.toString();
        quantityTextView.setText(qtdFormatted);
        priceTextView.setText(priceStr);
        Bitmap image = BitmapFactory.decodeByteArray(productImage, 0, productImage.length);
        imageView.setImageBitmap(image);
    }

    private void makeSell(Uri uri, int productQuantity) {
        productQuantity -= 1;
        ContentValues values = new ContentValues();
        values.put(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);
        try {
            int rowsAffected = context.getContentResolver().update(uri, values, null, null);
        } catch (IllegalArgumentException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
