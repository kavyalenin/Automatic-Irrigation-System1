package irrigation.com.irrigation.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.couchbase.lite.Document;
import com.couchbase.lite.UnsavedRevision;

import java.util.HashMap;
import java.util.Map;

import irrigation.com.irrigation.Database.App;
import irrigation.com.irrigation.Database.DBHelper;
import irrigation.com.irrigation.Database.DBModel;
import irrigation.com.irrigation.Database.Types;
import irrigation.com.irrigation.Evens.AndroidBusProvider;
import irrigation.com.irrigation.Evens.TriggerFirstTimeEvent;
import irrigation.com.irrigation.Model.CropsDetails;
import irrigation.com.irrigation.R;
import irrigation.com.irrigation.Utils.GeneralMethods;

public class CropAddEditFragment extends DialogFragment {

    public static CropsDetails cropsDetails;
    Dialog dialog;

    EditText cropNameView;
    EditText temperatureView;
    EditText rainfallView;
    EditText soilView;
    EditText waterlevelView;

    EditText sensortemperatureView;
    EditText sensorrainfallView;
    EditText sensorsoilView;
    EditText sensorwaterlevelView;

    String crop_uuid;

    LinearLayout sensorLayoutView;

    /*db document variable*/
    Document document;

    /*document standard*/
    Map<String, Object> documentproperties_map;

    public CropAddEditFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_crop_add_edit, container, false);

        ImageView backButton = view.findViewById(R.id.backButton);
        ImageView saveButton = view.findViewById(R.id.saveButton);

        sensorLayoutView = view.findViewById(R.id.sensorLayoutView);

        cropNameView = view.findViewById(R.id.cropNameView);
        temperatureView = view.findViewById(R.id.temperatureView);
        rainfallView = view.findViewById(R.id.rainfallView);
        soilView = view.findViewById(R.id.soilView);
        waterlevelView = view.findViewById(R.id.waterlevelView);

        sensortemperatureView = view.findViewById(R.id.sensortemperatureView);
        sensorrainfallView = view.findViewById(R.id.sensorrainfallView);
        sensorsoilView = view.findViewById(R.id.sensorsoilView);
        sensorwaterlevelView = view.findViewById(R.id.sensorwaterlevelView);

        backButton.setOnClickListener(clickListener);
        saveButton.setOnClickListener(clickListener);
        //method to load data
        loadData();

        return view;
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.backButton:

                    dismiss();
                    break;
                case R.id.saveButton:

                    //method to add or update the document
                    addOrUpdateDocument();

                    break;
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();

        dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }
    }

    //method to load data
    void loadData(){

        try{

            if (cropsDetails != null){

                crop_uuid = cropsDetails.getUuid();

                String crop_name = cropsDetails.getCrop_name();

                String sensortemperature = cropsDetails.getSensortemperature();
                String sensorrain_fall = cropsDetails.getSensorrain_fall();
                String sensorsoil_moisture = cropsDetails.getSensorsoil_moisture();
                String sensorwater_level = cropsDetails.getSensorwater_level();

                String temperature = cropsDetails.getTemperature();
                String rain_fall = cropsDetails.getRain_fall();
                String soil_moisture = cropsDetails.getSoil_moisture();
                String water_level = cropsDetails.getWater_level();

                cropNameView.setText(crop_name);
                cropNameView.setSelection(cropNameView.getText().length());

                temperatureView.setText(temperature);
                rainfallView.setText(rain_fall);
                soilView.setText(soil_moisture);
                waterlevelView.setText(water_level);

                sensortemperatureView.setText(sensortemperature);
                sensorrainfallView.setText(sensorrain_fall);
                sensorsoilView.setText(sensorsoil_moisture);
                sensorwaterlevelView.setText(sensorwater_level);
            }

            if (!GeneralMethods.replaceNull(crop_uuid).equals("")){
                sensorLayoutView.setVisibility(View.VISIBLE);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    ////method to add or update the document
    void addOrUpdateDocument(){
        try {

            String crop_name = cropNameView.getText().toString().trim();

            String temperature = temperatureView.getText().toString().trim();
            String rain_fall = rainfallView.getText().toString().trim();
            String soil_moisture = soilView.getText().toString().trim();
            String water_level = waterlevelView.getText().toString().trim();

            String sensortemperature = sensortemperatureView.getText().toString().trim();
            String sensorrain_fall = sensorrainfallView.getText().toString().trim();
            String sensorsoil_moisture = sensorsoilView.getText().toString().trim();
            String sensorwater_level = sensorwaterlevelView.getText().toString().trim();


            App app = (App) getActivity().getApplication();
            if (!GeneralMethods.replaceNull(crop_uuid).equals("")) {
                document = app.getDatabase().getExistingDocument(crop_uuid);
            } else {
                document = App.getDatabase().createDocument();
            }
            try {

                //standard properties for crop document
                if (!GeneralMethods.replaceNull(crop_uuid).equals("")) {
                    documentproperties_map =
                            DBHelper.updatePropertiesHashmap
                                    (getActivity(), false, false);
                    Map<String,Object> originalProperties = document.getUserProperties();
                    //originalProperties.putAll(documentproperties_map);
                    documentproperties_map = originalProperties;
                } else {
                    documentproperties_map =
                            DBHelper.createPropertiesHashmap(
                                    Types.CROP.toString(),
                                    false);
                }

                final Map<String, Object> updateProperties = DBHelper.createFilesDocument(
                        "",
                        null,
                        null,
                        0.0,
                        documentproperties_map);

                updateProperties.put(DBModel.UUID, document.getId());
                updateProperties.put(DBModel.Crop.crop_name, crop_name);

                updateProperties.put(DBModel.Crop.temperature, temperature);
                updateProperties.put(DBModel.Crop.rainfall, rain_fall);
                updateProperties.put(DBModel.Crop.soilmoisture, soil_moisture);
                updateProperties.put(DBModel.Crop.waterlevel, water_level);

                updateProperties.put(DBModel.Crop.sensortemperature, sensortemperature);
                updateProperties.put(DBModel.Crop.sensorrainfall, sensorrain_fall);
                updateProperties.put(DBModel.Crop.sensorsoilmoisture, sensorsoil_moisture);
                updateProperties.put(DBModel.Crop.sensorwaterlevel, sensorwater_level);

                if(!GeneralMethods.replaceNull(crop_uuid).equals("")){

                    /*update the crop document*/
                    document.update(new Document.DocumentUpdater() {
                        @Override
                        public boolean update(UnsavedRevision newRevision) {
                            newRevision.setUserProperties(updateProperties);
                            return true;
                        }
                    });

                }else{

                    /*insert document*/
                    document.putProperties(updateProperties);
                }

                AndroidBusProvider.getInstance().post(new TriggerFirstTimeEvent(true));

                //close the dialog
                dismiss();

            } catch (Exception e) {
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
