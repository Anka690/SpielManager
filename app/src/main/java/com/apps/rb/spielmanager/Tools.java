package com.apps.rb.spielmanager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.util.NoSuchElementException;

/**
 * Created by Anka on 20.12.2017.
 */

public class Tools {

    public static String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 50, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }


    /**
     * @param encodedString
     * @return bitmap (from given string)
     */
    public static Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    public static Bitmap scaleBitmapToViewSize(Bitmap bm, int boundingValue){
        try {
            int width = bm.getWidth();
            int height = bm.getHeight();

            float xScale = ((float) boundingValue) / width;
            float yScale = ((float) boundingValue) / height;
            float scale = (xScale <= yScale) ? xScale : yScale;

            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);

            // Create a new bitmap and convert it to a format understood by the ImageView
            Bitmap scaledBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);

            return scaledBitmap;
        } catch (NullPointerException e) {
            throw new NoSuchElementException("Can't find bitmap on given view/drawable");
        }
    }

}
