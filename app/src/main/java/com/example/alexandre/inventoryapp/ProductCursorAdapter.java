package com.example.alexandre.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.alexandre.inventoryapp.data.InventoryContract;

public class ProductCursorAdapter extends CursorAdapter {
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ImageView imageView = view.findViewById(R.id.product_image);
        TextView productTextView = view.findViewById(R.id.product_text_view);
        TextView quantityTextView = view.findViewById(R.id.quantity_text_view);
        TextView priceTextView = view.findViewById(R.id.price_text_view);

        // Find the columns of product attributes that we're interested in
        int imageColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_IMAGE);
        int productColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE);

        // Read the product attributes from the Cursor for the current product
        String productName = cursor.getString(productColumnIndex);
        byte[] productImage = cursor.getBlob(imageColumnIndex);
        int productQuantity = cursor.getInt(quantityColumnIndex);
        float productPrice = cursor.getFloat(priceColumnIndex);

        // Update the TextViews with the attributes for the current product
        productTextView.setText(productName);
        if(productImage != null) {
            imageView.setImageBitmap(null);
        }
        quantityTextView.setText("Qtd: " + productQuantity);
        priceTextView.setText("R$ " + productPrice);
    }
}
