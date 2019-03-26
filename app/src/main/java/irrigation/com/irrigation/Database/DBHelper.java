package irrigation.com.irrigation.Database;

import android.content.Context;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.UnsavedRevision;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class DBHelper {

    public static HashMap<String, Object> createPropertiesHashmap(
            String TYPE,
            Boolean DELETED

    ) {
        HashMap<String, Object> PropertiesMap = new HashMap<>();

        PropertiesMap.put(DBModel.TYPE, TYPE);
        Calendar calendar = GregorianCalendar.getInstance();

        String dateTime = DBModel.DATE
                .format(calendar.getTime());

        Map<String, String> mCurrentTimeString = new HashMap<String, String>();
        mCurrentTimeString.put("date", dateTime);

        PropertiesMap.put(DBModel.CREATED_ON_DATE_TIME, mCurrentTimeString);
        PropertiesMap.put(DBModel.DELETED, DELETED);

        return PropertiesMap;

    }

    /**
     * Generic method to update standard document properties by taking in its map and updating it with default properties
     */

    public static Map<String, Object> updatePropertiesHashmap(
            Context ctx,
            Boolean SHOULD_PROCESS,
            Boolean DELETED

    ) {

        HashMap<String, Object> PropertiesMap = new HashMap<>();

        Calendar calendar = GregorianCalendar.getInstance();

        String dateTime = DBModel.DATE
                .format(calendar.getTime());

        Map<String, String> mCurrentTimeString = new HashMap<String, String>();
        mCurrentTimeString.put("date", dateTime);


        PropertiesMap.put(DBModel.MODIFIED_ON_DATE_TIME, mCurrentTimeString);

        return PropertiesMap;

    }

    public static boolean deleteDocument(String UUID, final Context ctx) {


        Document document = null;
        App app = (App) ctx.getApplicationContext();
        if (app.getDatabase() != null) {

            if (UUID != null) {
                document = app.getDatabase().getExistingDocument(UUID);
            }
        }
        if (document != null) {
            try {
                document.update(new Document.DocumentUpdater() {
                    @Override
                    public boolean update(UnsavedRevision unsavedRevision) {

                        Map<String, Object> properties = unsavedRevision.getUserProperties();

                        Map<String, Object> StandardUpdateProperties =
                                updatePropertiesHashmap
                                        (ctx, true, true);

                        properties.putAll(StandardUpdateProperties);

                        unsavedRevision.setUserProperties(properties);

                        return true;
                    }
                });
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    /**
     * Generic method to insert values into customer type document
     */
    public static Map<String, Object> createFilesDocument(
            String file_name,
            String mimetype,
            String name,
            double size,
            //  String location,
            Map<String, Object> Properties
    ) {
        //document id
        Properties.put(DBModel.Files.FILE_NAME, file_name);
        Properties.put(DBModel.Files.NAME, name);
        Properties.put(DBModel.Files.MIMETYPE, mimetype);
        Properties.put(DBModel.Files.SIZE, size);

        return Properties;
    }
}