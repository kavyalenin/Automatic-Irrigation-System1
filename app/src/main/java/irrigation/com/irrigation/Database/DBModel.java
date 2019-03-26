package irrigation.com.irrigation.Database;

import java.text.SimpleDateFormat;

public class DBModel {

    /*
     *
     * */
    public static final SimpleDateFormat DATE = new SimpleDateFormat(
            "yyyy-MM-dd' 'HH:mm:ss");
    /**
     * Constant key in the Couchbase Database for order
     */
    public static final String ORDER = "order";
    /**
     * Constant key in the Couchbase Database for deleted flag
     */
    public static final String DELETED = "deleted";
    public static final String TYPE = "type";

    public static final String CREATED_ON_DATE_TIME = "createdondatetime";

    /**
     * Constant key in the Couchbase Database for knowing the data and time of
     * the document modified
     */
    public static final String MODIFIED_ON_DATE_TIME = "modifiedOnDateTime";
    public static final String UUID = "uuid";

    public static final String temperature = "temperature";
    public static final String waterlevel = "waterlevel";
    public static final String soil = "soil";
    public static final String rainfall = "rainfall";

    public static class Crop {
        public static String crop_name = "crop_name";
        public static String temperature = "temperature";
        public static String rainfall = "rainfall";
        public static String soilmoisture = "soilmoisture";
        public static String waterlevel = "waterlevel";

        public static String sensortemperature = "sensortemperature";
        public static String sensorrainfall = "sensorrainfall";
        public static String sensorsoilmoisture = "sensorsoilmoisture";
        public static String sensorwaterlevel = "sensorwaterlevel";
    }

    public static class Files {

        public static final String MIMETYPE = "mime_type";
        public static final String SIZE = "size";
        public static final String NAME = "name";
        public static final String FILE_NAME = "image_file_name";
        public static final String LOCATION = "location";
        public static final String FILE_PATH = android.os.Environment.getExternalStorageDirectory() + "/" + "Irrigation";

    }
}