package irrigation.com.irrigation.Database;

import com.couchbase.lite.Emitter;
import com.couchbase.lite.Mapper;

import java.util.Map;

import irrigation.com.irrigation.Utils.GeneralMethods;

public class Views {

    public static String CROP = "CROP";
    public static String CROPTYPE = "CROPTYPE";
    public static String IMAGE = "IMAGE";

    /*
     * to get all register vehicle documents
     * */
    public static String getCROP(){
        com.couchbase.lite.View cropView = GeneralMethods.getDatabase().
                getView(CROP);

        cropView.setMap(new Mapper() {

            @Override
            public void map(Map<String, Object> document, Emitter emitter) {

                Object mDeleted = document.get(DBModel.DELETED);
                Object crop_name = document.get(DBModel.Crop.crop_name);
                if (crop_name!= null) {

                    emitter.emit(document.get(DBModel.Crop.crop_name), document);
                }
            }
        }, "0.3");
        return CROP;
    }

    /*
     * to get all register vehicle documents
     * */
    public static String getDocByType(){
        com.couchbase.lite.View localView = GeneralMethods.getDatabase().
                getView(CROPTYPE);

        localView.setMap(new Mapper() {

            @Override
            public void map(Map<String, Object> document, Emitter emitter) {

                Object mDeleted = document.get(DBModel.DELETED);
                Object type = document.get(DBModel.TYPE);
                if (type!= null) {
                    emitter.emit(document.get(DBModel.TYPE), document);
                }
            }
        }, "0.3");
        return CROPTYPE;
    }

    /*
     * to get all register vehicle documents
     * */
    public static String getImageByType(){
        com.couchbase.lite.View imageView = GeneralMethods.getDatabase().
                getView(IMAGE);

        imageView.setMap(new Mapper() {

            @Override
            public void map(Map<String, Object> document, Emitter emitter) {

                Object mDeleted = document.get(DBModel.DELETED);
                Object type = document.get(DBModel.TYPE);
                if (type!= null) {

                    if (Types.IMAGEFILES.toString().equals(type)) {
                        emitter.emit(document.get(Types.IMAGEFILES.toString()), document);
                    }
                }
            }
        }, "0.3");
        return IMAGE;
    }
}