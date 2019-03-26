package irrigation.com.irrigation.Database;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.util.Log;

import java.io.IOException;

import irrigation.com.irrigation.Utils.Constants;
import irrigation.com.irrigation.Utils.GeneralMethods;

public class App extends MultiDexApplication {

    //TODO set this flag to false before production
    public static final boolean DEBUG_MODE = true;

    private static Manager dbmanager = null;
    private static Database database = null;
    public static String TAG = "motor";
    public static String QUERYTAG = "query";


    private static Context mContext;


    @Override
    public void onCreate() {
        super.onCreate();
        OpenOrCreateDatabase();
        mContext = this;


        //Set the environment
        /*File mFile = new File(Environment.getExternalStorageDirectory()
                + "/commusoft.txt");*/
        //GeneralMethods.setEnvironment(mFile.exists() ? Constants.DEV : Constants.LIVE);
        GeneralMethods.setEnvironment(Constants.DEV);

        //initiliase bugsnag
//        Bugsnag.init(this);
        //Bugsnag.notify(new RuntimeException("Non-fatal"));
    }

    public static Context getContext() {
        return mContext;
    }

    private void OpenOrCreateDatabase() {
        try {
            dbmanager = new Manager(new AndroidContext(this), Manager.DEFAULT_OPTIONS);

            //Debug mode should be switched off for production releases in order to save device memory
            if (DEBUG_MODE) {

                Log.d(TAG, "Manager not created");

                Manager.enableLogging(Log.TAG, Log.VERBOSE);
                Manager.enableLogging(Log.TAG_SYNC_ASYNC_TASK, Log.VERBOSE);
                Manager.enableLogging(Log.TAG_SYNC, Log.VERBOSE);
                Manager.enableLogging(Log.TAG_QUERY, Log.VERBOSE);
                Manager.enableLogging(Log.TAG_VIEW, Log.VERBOSE);
                Manager.enableLogging(Log.TAG_DATABASE, Log.VERBOSE);


                Log.d(TAG, "Manager created");
            }
        } catch (IOException e) {
            e.printStackTrace();

            if (DEBUG_MODE) {
                Log.d(TAG, "Manager not created");
            }

        }

        //check if db already exists
        try {
            database = dbmanager.getExistingDatabase(Constants.DBNAME);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        //database doesnt exist
        if (database == null) {
            try {
                database = dbmanager.getDatabase(Constants.DBNAME);
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
                if (DEBUG_MODE) {
                    Log.d(TAG, "database not created");
                }
            }
        } else {

            if (DEBUG_MODE) {
                Log.d(TAG, "database already exists");
            }
        }
        //only to keep the app safe from crashing as many classes still use that database object
        if (database != null) {
            GeneralMethods.setDatabase(database);

        }


    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        dbmanager.close();
    }

    public static Database getDatabase() {
        return database;
    }

}