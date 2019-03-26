package irrigation.com.irrigation.Model;

import android.net.Uri;

public class CropsDetails {

    String crop_name;
    String temperature;
    String rain_fall;
    String soil_moisture;
    String water_level;
    String description;

    String sensortemperature;
    String sensorrain_fall;
    String sensorsoil_moisture;
    String sensorwater_level;
    String uuid;


    String date_added, file_name, file_type, file_path;

    private Double file_size;

    Uri imageuri = null;


    public CropsDetails() {
    }

    public CropsDetails(String crop_name, String temperature, String rain_fall, String soil_moisture, String water_level, String description) {
        this.crop_name = crop_name;
        this.temperature = temperature;
        this.rain_fall = rain_fall;
        this.soil_moisture = soil_moisture;
        this.water_level = water_level;
        this.description = description;
    }

    public CropsDetails(String uuid, String crop_name, String temperature, String rain_fall, String soil_moisture, String water_level, String description, String sensortemperature, String sensorrain_fall, String sensorsoil_moisture, String sensorwater_level) {
        this.uuid = uuid;
        this.temperature = temperature;
        this.rain_fall = rain_fall;
        this.soil_moisture = soil_moisture;
        this.water_level = water_level;
        this.description = description;
        this.sensortemperature = sensortemperature;
        this.sensorrain_fall = sensorrain_fall;
        this.sensorsoil_moisture = sensorsoil_moisture;
        this.sensorwater_level = sensorwater_level;
        this.sensorwater_level = sensorwater_level;
    }

    public CropsDetails(String crop_name, String temperature, String rain_fall, String soil_moisture, String water_level, String description, String sensortemperature, String sensorrain_fall, String sensorsoil_moisture, String sensorwater_level) {
        this.temperature = temperature;
        this.rain_fall = rain_fall;
        this.soil_moisture = soil_moisture;
        this.water_level = water_level;
        this.description = description;
        this.sensortemperature = sensortemperature;
        this.sensorrain_fall = sensorrain_fall;
        this.sensorsoil_moisture = sensorsoil_moisture;
        this.sensorwater_level = sensorwater_level;
        this.sensorwater_level = sensorwater_level;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getSensortemperature() {
        return sensortemperature;
    }

    public void setSensortemperature(String sensortemperature) {
        this.sensortemperature = sensortemperature;
    }

    public String getSensorrain_fall() {
        return sensorrain_fall;
    }

    public void setSensorrain_fall(String sensorrain_fall) {
        this.sensorrain_fall = sensorrain_fall;
    }

    public String getSensorsoil_moisture() {
        return sensorsoil_moisture;
    }

    public void setSensorsoil_moisture(String sensorsoil_moisture) {
        this.sensorsoil_moisture = sensorsoil_moisture;
    }

    public String getSensorwater_level() {
        return sensorwater_level;
    }

    public void setSensorwater_level(String sensorwater_level) {
        this.sensorwater_level = sensorwater_level;
    }

    public CropsDetails(String crop_name) {
        this.crop_name = crop_name;
    }

    public String getCrop_name() {
        return crop_name;
    }

    public void setCrop_name(String crop_name) {
        this.crop_name = crop_name;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getRain_fall() {
        return rain_fall;
    }

    public void setRain_fall(String rain_fall) {
        this.rain_fall = rain_fall;
    }

    public String getSoil_moisture() {
        return soil_moisture;
    }

    public void setSoil_moisture(String soil_moisture) {
        this.soil_moisture = soil_moisture;
    }

    public String getWater_level() {
        return water_level;
    }

    public void setWater_level(String water_level) {
        this.water_level = water_level;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getDate_added() {
        return date_added;
    }

    public void setDate_added(String date_added) {
        this.date_added = date_added;
    }
}