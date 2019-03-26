package irrigation.com.irrigation.Utils;

import android.app.Activity;
import android.webkit.MimeTypeMap;

import com.couchbase.lite.Database;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import irrigation.com.irrigation.R;

public class GeneralMethods {

    private static String ENVIRONMENT;

    /**
     * Variable for Couchbase
     */
    private static Database sDatabase;

    public static void setEnvironment(String str) {
        ENVIRONMENT = str;
    }

    /**
     * Gets the Couchbase Database that have been initialized in the Main
     * Activity
     *
     * @return the sDatabase
     */
    public static Database getDatabase() {

        return sDatabase;
    }

    /**
     * Sets the Couchbase Database that have been initialized in the Main
     * Activity
     *
     * @param sDatabase the Database to set
     */
    public static void setDatabase(Database sDatabase) {
        GeneralMethods.sDatabase = sDatabase;
    }

    public static List<Object> getQueryKeys(String key1, String key2) {
        List<Object> UUIDs = new ArrayList<>();
        UUIDs.add(key1);
        UUIDs.add(key2);
        List<Object> keys = new ArrayList<Object>();
        keys.add(UUIDs);
        return keys;
    }

    public static List<Object> getQueryKeys(int key1, String key2) {
        List<Object> UUIDs = new ArrayList<>();
        UUIDs.add(key1);
        UUIDs.add(key2);
        List<Object> keys = new ArrayList<Object>();
        keys.add(UUIDs);
        return keys;
    }


    public static List<Object> getQueryKeys(String key1, String key2, String key3) {
        List<Object> UUIDs = new ArrayList<>();
        UUIDs.add(key1);
        UUIDs.add(key2);
        UUIDs.add(key3);
        List<Object> keys = new ArrayList<Object>();
        keys.add(UUIDs);
        return keys;
    }

    public static List<Object> getQueryKeys(String key1) {
        List<Object> UUIDs = new ArrayList<>();
        UUIDs.add(key1);
        return UUIDs;
    }

    /*
     * It replace null values to empty string
     */
    public static String replaceNull(String input) {

        if (input == null || input.equals("null") || input.equals("NULL") || input.equals("Null")) {
            return "";
        }
        return input == null ? "" : input;
    }

    //method to get image based on crop
    public static int getCropImage(Activity activity, String crop) {

        int image_resource = 0;
        try {

            if (crop.equalsIgnoreCase(activity.getString(R.string.wheat))) {
                image_resource = R.drawable.wheat;
            } else if (crop.equalsIgnoreCase(activity.getString(R.string.Rye))) {
                image_resource = R.drawable.rye;
            } else if (crop.equalsIgnoreCase(activity.getString(R.string.Corn))) {
                image_resource = R.drawable.corn;
            } else if (crop.equalsIgnoreCase(activity.getString(R.string.Rice))) {
                image_resource = R.drawable.rice;
            } else if (crop.equalsIgnoreCase(activity.getString(R.string.Soybean))) {
                image_resource = R.drawable.soybean;
            } else if (crop.equalsIgnoreCase(activity.getString(R.string.Peanut))) {
                image_resource = R.drawable.peanut;
            } else if (crop.equalsIgnoreCase(activity.getString(R.string.Jowar))) {
                image_resource = R.drawable.jowar;
            } else if (crop.equalsIgnoreCase(activity.getString(R.string.Garlic))) {
                image_resource = R.drawable.garlic;
            } else if (crop.equalsIgnoreCase(activity.getString(R.string.Ginger))) {
                image_resource = R.drawable.ginger;
            } else if (crop.equalsIgnoreCase(activity.getString(R.string.Pumpkins))) {
                image_resource = R.drawable.pumpkins;
            } else if (crop.equalsIgnoreCase(activity.getString(R.string.Tomatoes))) {
                image_resource = R.drawable.tomatoes;
            } else if (crop.equalsIgnoreCase(activity.getString(R.string.Onions))) {
                image_resource = R.drawable.onions;
            } else if (crop.equalsIgnoreCase(activity.getString(R.string.Potatoes))) {
                image_resource = R.drawable.potatoes;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return image_resource;

    }

    // url = file path or whatever suitable URL you want.
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (GeneralMethods.replaceNull(extension).equals(""))
            extension = "jpeg";

        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    /**
     * Creates an empty temporary file in the given directory using the given
     * prefix and suffix as part of the file name. If {@code suffix} is null, {@code .tmp} is used.
     */
    public static File createTempFile(String prefix, String suffix, File directory)
            throws IOException {
        // Force a prefix null check first
        if (prefix.length() < 3) {
            throw new IllegalArgumentException("prefix must be at least 3 characters");
        }
        if (suffix == null) {
            suffix = ".tmp";
        }
        //File tmpDirFile = directory;
        if (directory == null) {
            String tmpDir = System.getProperty("java.io.tmpdir", ".");
            directory = new File(tmpDir);
        }
        File result;
        do {
            result = new File(directory, prefix + suffix);
        } while (!result.createNewFile());
        return result;
    }

    //method to convert date format
    public static String getConvertDateFormat(String mydate, String fromFormat, String toFormat) {
        String dateformate = "";

        try {
            SimpleDateFormat dateformat = new SimpleDateFormat(fromFormat);

            Date sampledate = dateformat.parse(mydate);
            SimpleDateFormat formatter = new SimpleDateFormat(toFormat);
            dateformate = formatter.format(sampledate);
        } catch (Exception e) {
           e.printStackTrace();
        }

        return dateformate;
    }

}