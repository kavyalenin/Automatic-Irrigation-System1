package irrigation.com.irrigation.Model;

import android.net.Uri;

public class AttachedFileDetails {

    /**
     * UUID is the search item document UUID
     */
    private String uuid, date_added, file_name, file_type, file_path;

    private Double file_size;

    Uri imageuri = null;

    public AttachedFileDetails(String uuid, String date_added, String file_name, String file_type, String file_path, Double file_size, Uri imageuri) {
        this.uuid = uuid;
        this.date_added = date_added;
        this.file_name = file_name;
        this.file_type = file_type;
        this.file_path = file_path;
        this.file_size = file_size;
        this.imageuri = imageuri;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDate_added() {
        return date_added;
    }

    public void setDate_added(String date_added) {
        this.date_added = date_added;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getFile_type() {
        return file_type;
    }

    public void setFile_type(String file_type) {
        this.file_type = file_type;
    }

    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public Double getFile_size() {
        return file_size;
    }

    public void setFile_size(Double file_size) {
        this.file_size = file_size;
    }

    public Uri getImageuri() {
        return imageuri;
    }

    public void setImageuri(Uri imageuri) {
        this.imageuri = imageuri;
    }
}