package com.example.alexandre.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class InventoryContract {

    public static final String CONTENT_AUTHORITY = "com.example.alexandre.inventory";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PRODUCT = "product";
    private InventoryContract() {
    }

    public static final class ProductEntry implements BaseColumns {
        /**
         * O tipo MIME do {@link #CONTENT_URI} para uma lista de produtos.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT;
        /**
         * O tipo MIME do {@link #CONTENT_URI} para um único produto.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT;
        public static final String TABLE_NAME = "products";
        public static final String COLUMN_ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_NAME = "product_name";
        public static final String COLUMN_PRODUCT_IMAGE = "product_image";
        public static final String COLUMN_PRODUCT_QUANTITY = "product_quantity";
        public static final String COLUMN_PRODUCT_PRICE = "product_price";
        public static final String COLUMN_PRODUCT_PROVIDER = "product_provider";
        public static final String COLUMN_PRODUCT_PROVIDER_EMAIL = "provider_email";
        public static final String COLUMN_PRODUCT_PROVIDER_PHONE = "provider_phone";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCT);
    }
}
