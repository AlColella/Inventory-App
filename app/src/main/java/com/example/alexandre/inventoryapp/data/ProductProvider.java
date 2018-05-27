package com.example.alexandre.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ProductProvider extends ContentProvider {

    private ProductDbHelper mDbHelper;
    private static final int PRODUCTS = 100;
    private static final int PRODUCTS_ID = 200;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCT, PRODUCTS);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCT + "/#", PRODUCTS_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = null;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = db.query(InventoryContract.ProductEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case PRODUCTS_ID:
                selection = InventoryContract.ProductEntry.COLUMN_ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(InventoryContract.ProductEntry.TABLE_NAME, projection, selection,
                        selectionArgs,null,null,sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values) {

        // Checa se o nome não é nulo
        String name = values.getAsString(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME);
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Product requires a name");
        }

        Integer quantity = values.getAsInteger(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity < 0) {
            throw new IllegalArgumentException("Product must have 0 or more quantity");
        }

        Float price = values.getAsFloat(InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        if (price < 0) {
            throw new IllegalArgumentException("Product requires valid price");
        }

        String provider = values.getAsString(InventoryContract.ProductEntry.COLUMN_PRODUCT_PROVIDER);
        if (provider == null || provider.isEmpty()) {
            throw new IllegalArgumentException("Product requires a provider");
        }

        String email = values.getAsString(InventoryContract.ProductEntry.COLUMN_PRODUCT_PROVIDER_EMAIL);
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Product requires a provider email");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(InventoryContract.ProductEntry.TABLE_NAME, null, values);

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Obtém banco de dados com permissão de escrita
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Deleta todos os registros que correspondem ao selection e selection args
                rowsDeleted = database.delete(InventoryContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCTS_ID:
                // Deleta um único registro dado pelo ID na URI
                selection = InventoryContract.ProductEntry.COLUMN_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, values, selection, selectionArgs);
            case PRODUCTS_ID:
                selection = InventoryContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(InventoryContract.ProductEntry.COLUMN_PRODUCT_NAME);
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        if (values.containsKey(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY)) {
            Integer quantity = values.getAsInteger(InventoryContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity < 0) {
                throw new IllegalArgumentException("Product must have 0 or more quantity");
            }
        }

        if (values.containsKey(InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE)) {
            Float price = values.getAsFloat(InventoryContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            if (price < 0) {
                throw new IllegalArgumentException("Product requires valid price");
            }
        }

        if (values.containsKey(InventoryContract.ProductEntry.COLUMN_PRODUCT_PROVIDER)) {
            String provider = values.getAsString(InventoryContract.ProductEntry.COLUMN_PRODUCT_PROVIDER);
            if (provider == null || provider.isEmpty()) {
                throw new IllegalArgumentException("Product requires a provider");
            }
        }

        if (values.containsKey(InventoryContract.ProductEntry.COLUMN_PRODUCT_PROVIDER_EMAIL)) {
            String email = values.getAsString(InventoryContract.ProductEntry.COLUMN_PRODUCT_PROVIDER_EMAIL);
            if (email == null || email.isEmpty()) {
                throw new IllegalArgumentException("Product requires a provider email");
            }
        }

        if(values.size()==0) {
            return 0;
        }

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        int rowsUpdated = db.update(InventoryContract.ProductEntry.TABLE_NAME, values, selection, selectionArgs);
        if(rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
