package com.lexoff.lvivtransport;

import android.content.Context;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.URLSpan;

import java.io.InputStream;

public class LicensesHelper {

    public static Spanned getLicenseText(Context context, License license){
        if (license==License.APACHE2){
            SpannableString result=new SpannableString(Html.fromHtml(readLicenseFromAssets(context, "apache2.html"), 0));

            //remove all possible clickable links
            //in this exact case it will be only one link that leads on apache site
            URLSpan spans[]=result.getSpans(0, result.length(), URLSpan.class);
            for (URLSpan span : spans){
                result.removeSpan(span);
            }

            return result;
        } else if (license==License.MIT){
            return Html.fromHtml(readLicenseFromAssets(context, "mit.html"), 0);
        } else if (license==License.LGPL){
            return new SpannableString(readLicenseFromAssets(context, "lgpl.html"));
        }

        //unreachable statement because enum is used
        return new SpannableString("");
    }

    private static String readLicenseFromAssets(Context context, String name) {
        try {
            InputStream stream = context.getAssets().open(name);

            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            return new String(buffer);
        } catch (Exception e) {
            //catch everything that looks like exception
            //and return nothing in case
            //it won't be critical
            return "";
        }
    }

    public enum License{
        APACHE2,
        MIT,
        LGPL
    }

}
