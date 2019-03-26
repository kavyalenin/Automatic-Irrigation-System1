package irrigation.com.irrigation.Actvities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.util.Log;
import com.google.gson.JsonObject;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import irrigation.com.irrigation.Adapters.CropsListAdapter;
import irrigation.com.irrigation.Adapters.ImageFileListAdapter;
import irrigation.com.irrigation.Common.AttachFilesDialogFragment;
import irrigation.com.irrigation.Evens.AndroidBusProvider;
import irrigation.com.irrigation.Evens.AttachmentEvent;
import irrigation.com.irrigation.Evens.TriggerFirstTimeEvent;
import irrigation.com.irrigation.Fragments.CropAddEditFragment;
import irrigation.com.irrigation.Database.App;
import irrigation.com.irrigation.Database.DBModel;
import irrigation.com.irrigation.Database.Types;
import irrigation.com.irrigation.Database.Views;
import irrigation.com.irrigation.Model.AttachedFileDetails;
import irrigation.com.irrigation.Model.CropsDetails;
import irrigation.com.irrigation.R;
import irrigation.com.irrigation.Utils.GeneralMethods;
import irrigation.com.irrigation.Utils.PushNotificationService;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Query>{

    ListView cropsListView;
    RecyclerView debugListView;
    List<CropsDetails> data_list = new ArrayList<>();
    List<AttachedFileDetails> image_list = new ArrayList<>();

    LiveQuery cropLivequery;
    LiveQuery debugLivequery;

    FloatingActionButton addButton;
    FloatingActionButton captureImageButton;
    TextView emptyView;
    int REQUEST_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.crops));

        emptyView = findViewById(R.id.emptyView);
        debugListView = findViewById(R.id.debugListView);
        cropsListView = findViewById(R.id.cropsListView);
        addButton = findViewById(R.id.addButton);
        captureImageButton = findViewById(R.id.captureImageButton);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CropAddEditFragment.cropsDetails = null;
                DialogFragment fragment =new CropAddEditFragment();
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragment.show(fragmentManager, "crop");
            }
        });

        captureImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //get permission to record audio
                int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
                } else {

                    PackageManager packageManager = getPackageManager();
                    if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                        Toast.makeText(MainActivity.this, getString(R.string.error_no_camera), Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    AttachFilesDialogFragment attachFilesDialogFragment = new AttachFilesDialogFragment();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    attachFilesDialogFragment.show(fragmentManager, "image");

                }

            }
        });

        cropsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                try{
                    CropsDetails cropsDetails = data_list.get(position);
                    CropViewActivity.cropsDetails = cropsDetails;
                    Intent intent = new Intent(MainActivity.this, CropViewActivity.class);
                    startActivity(intent);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        cropsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                try{

                    CropsDetails cropsDetails = data_list.get(i);

                    CropAddEditFragment.cropsDetails = cropsDetails;
                    DialogFragment fragment =new CropAddEditFragment();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragment.show(fragmentManager, "crop");

                }catch (Exception e){
                    e.printStackTrace();
                }

                return true;
            }
        });

        /*loader to loads the register list item*/
        getSupportLoaderManager().initLoader(12, null, MainActivity.this).startLoading();

        //method to load data
        loadData();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    @Override
    protected void onStart() {
        super.onStart();

        AndroidBusProvider.getInstance().register(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        /*MenuItem spinnerItem = menu.findItem(R.id.spinner);
        final MenuItem cameraItem = menu.findItem(R.id.action_camera);
        cameraItem.setVisible(false);
        Spinner spinner = (Spinner) spinnerItem.getActionView();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try{
                    String value = ((AppCompatTextView) view).getText().toString();
                    if (value.equalsIgnoreCase(getString(R.string.debug))){
                        *//*addButton.hide();
                        captureImageButton.show();*//*
                        debugListView.setVisibility(View.VISIBLE);
                        cropsListView.setVisibility(View.GONE);
                        addButton.setVisibility(View.GONE);
                        captureImageButton.setVisibility(View.VISIBLE);

                        if (image_list.size() ==0){

                            debugListView.setVisibility(View.GONE);
                            //emptyView.setVisibility(View.VISIBLE);
                        }else{

                            //emptyView.setVisibility(View.GONE);
                        }
                    }else {
                        cameraItem.setVisible(false);
                        *//*addButton.show();
                        captureImageButton.hide();*//*
                        debugListView.setVisibility(View.GONE);
                        cropsListView.setVisibility(View.VISIBLE);
                        addButton.setVisibility(View.VISIBLE);
                        captureImageButton.setVisibility(View.GONE);

                        if (data_list.size() ==0){

                            cropsListView.setVisibility(View.GONE);
                            //emptyView.setVisibility(View.VISIBLE);
                        }else{

                            //emptyView.setVisibility(View.GONE);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, getResources().getStringArray(R.array.spinner_list_item_array));


        *//*ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_list_item_array, android.R.layout.simple_spinner_item);*//*
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case android.R.id.home:

                finishAffinity();
                //do whatever
                return true;
            case R.id.action_camera:
                //Toast.makeText(MainActivity.this, "Action clicked", Toast.LENGTH_SHORT).show();
                //do whatever
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //method to open capture image screen
    void openImageCapture(String uuid){

        try{

            //get permission to record audio
            int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
            } else {

                PackageManager packageManager = getPackageManager();
                if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    Toast.makeText(MainActivity.this, getString(R.string.error_no_camera), Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                AttachFilesDialogFragment attachFilesDialogFragment = AttachFilesDialogFragment.newInstance(uuid);
                FragmentManager fragmentManager = getSupportFragmentManager();
                attachFilesDialogFragment.show(fragmentManager, "image");

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //method to load data
    void loadData(){
        try{

            CropsDetails cropsDetails = new CropsDetails(
                    "Wheat",
                    "15.5°C",
                    "30cm - 100cm",
                    "Light clay or Heavy loam",
                    "10 mm/day",
                    "The various wheat species (Triticum sp.) are probably the second oldest cultivated plants (after barley). They were first domesticated between 7500 and 6500 BC in the 'Fertile Crescent' region of the Middle East. The oldest evidence of systematic cultivation of wild wheat was found near the southern edge of the Euphrates valley during archaeological excavations at the Neolithic dwelling of Abu Hureyra. In historic times, wheat was already grown in ancient Greece, Persia, Egypt, and throughout Europe. In the oldest known historical records, it is also mentioned as an important crop. Spreading eastwards, it reached China around the third millennium BC; it arrived in the Americas with the Spanish fleet.");
            data_list.add(cropsDetails);

            cropsDetails = new CropsDetails(
                    "Rice",
                    "20°C-27°C",
                    "115cm",
                    "Fertile riverine alluvial",
                    "1,432 ltrs/kg",
                    "The cultivated rice plant, Oryza sativa, is an annual grass of the Gramineae family. It grows to about 1.2 metres (4 feet) in height. The leaves are long and flattened, and its panicle, or inflorescence, is made up of spikelets bearing flowers that produce the fruit, or grain.\n" +
                            "\n" +
                            "Many cultures have evidence of early rice cultivation, including China, India, and the civilizations of Southeast Asia. However, the earliest archaeological evidence comes from central and eastern China and dates to 7000–5000 BCE. With the exception of the type called upland rice, the plant is grown on submerged land in the coastal plains, tidal deltas, and river basins of tropical, semitropical, and temperate regions. The seeds are sown in prepared beds, and when the seedlings are 25 to 50 days old, they are transplanted to a field, or paddy, that has been enclosed by levees and submerged under 5 to 10 cm (2 to 4 inches) of water, remaining submerged during the growing season.",
                    "35°C",
                    "110cm",
                    "Fertile riverine alluvial",
                    "1,400 ltrs");
            data_list.add(cropsDetails);

            cropsDetails = new CropsDetails(
                    "Onions",
                    "20°C-25°C",
                    "650-750mm",
                    "Clay loam, Silt loam",
                    "5-7.25 mm/day",
                    "The onion (Allium cepa L., from Latin cepa \"onion\"), also known as the bulb onion or common onion, is a vegetable that is the most widely cultivated species of the genus Allium. This genus also contains several other species variously referred to as onions and cultivated for food, such as the Japanese bunching onion (Allium fistulosum), the tree onion (A. ×proliferum), and the Canada onion (Allium canadense).");
            data_list.add(cropsDetails);

            cropsDetails = new CropsDetails(
                    "Corn",
                    "10°C",
                    "723 mm/yr",
                    "Sandy loam",
                    "20-30 inches/yr",
                    "Corn, (Zea mays), also called Indian corn or maize, cereal plant of the grass family (Poaceae) and its edible grain. The domesticated crop originated in the Americas and is one of the most widely distributed of the world’s food crops. Corn is used as livestock feed, as human food, as biofuel, and as raw material in industry. In the United States the colourful variegated strains known as Indian corn are traditionally used in autumn harvest decorations.");
            data_list.add(cropsDetails);

            cropsDetails = new CropsDetails(
                    "Soybean",
                    "29.5°C",
                    "400mm",
                    "Loamy Soil",
                    " 20-25 inches",
                    "The soybean (Glycine max), or soya bean, is a species of legume native to East Asia, widely grown for its edible bean, which has numerous uses. Fat-free (defatted) soybean meal is a significant and cheap source of protein for animal feeds and many packaged meals.");
            data_list.add(cropsDetails);

            cropsDetails = new CropsDetails(
                    "Millet",
                    "",
                    "",
                    "21°C-26°C",
                    "450-650",
                    "");
            data_list.add(cropsDetails);

            cropsDetails = new CropsDetails("Garlic");
            data_list.add(cropsDetails);

            cropsDetails = new CropsDetails("Ginger");
            data_list.add(cropsDetails);

            cropsDetails = new CropsDetails("Pumpkins");
            data_list.add(cropsDetails);

            cropsDetails = new CropsDetails("Tomatoes");
            data_list.add(cropsDetails);

            cropsDetails = new CropsDetails("Potatoes");
            data_list.add(cropsDetails);

            CropsListAdapter cropsListAdapter = new CropsListAdapter(MainActivity.this, data_list);
            cropsListView.setAdapter(cropsListAdapter);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public Loader<Query> onCreateLoader(int id, Bundle args) {


        AsyncTaskLoader<Query> loader = new AsyncTaskLoader<Query>(this) {
            @Override
            public LiveQuery loadInBackground() {

                try {
                    String view = Views.getDocByType();

                    try {

                        /*List<Object> UUID = new ArrayList<>();
                        UUID.add(uuid);*/

                        com.couchbase.lite.View couchCropView =
                                App.getDatabase().getExistingView(view);

                        Query query = couchCropView.createQuery();
                        query.setKeys(GeneralMethods.getQueryKeys(Types.CROP.toString()));

                        cropLivequery = query.toLiveQuery();
                        cropLivequery.addChangeListener(cropLivequery_listener);
                        cropLivequery.start();

                    } catch (Exception e) {
                    }

                    /*try {

                        *//*List<Object> UUID = new ArrayList<>();
                        UUID.add(uuid);*//*

                        com.couchbase.lite.View couchFilesView =
                                App.getDatabase().getExistingView(view);

                        Query dQuery = couchFilesView.createQuery();
                        dQuery.setKeys(GeneralMethods.getQueryKeys(Types.IMAGEFILES.toString()));

                        debugLivequery = dQuery.toLiveQuery();
                        debugLivequery.addChangeListener(debugLivequery_listener);
                        debugLivequery.start();

                    } catch (Exception e) {
                    }*/
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return null;
            }

        };
        loader.forceLoad();
        return loader;


    }

    @Override
    public void onLoadFinished(Loader<Query> loader, Query data) {

    }

    @Override
    public void onLoaderReset(Loader<Query> loader) {

    }




    /**
     * Live query listener for crop list
     */
    LiveQuery.ChangeListener cropLivequery_listener = new LiveQuery.ChangeListener() {
        @Override
        public void changed(final LiveQuery.ChangeEvent event) {

            try {
                QueryEnumerator queryEnumerator = event.getRows();

                data_list = new ArrayList<>();

                while (queryEnumerator.hasNext()) {

                    CropsDetails cropsDetails = new CropsDetails();

                    QueryRow queryRow = queryEnumerator.next();
                    Document document = queryRow.getDocument();

                    final Map<String, Object> properties = document.getProperties();

                    try{

                        String uuid = (String) properties.get(DBModel.UUID);
                        String crop_name = (String) properties.get(DBModel.Crop.crop_name);
                        String temperature = (String) properties.get(DBModel.Crop.temperature);
                        String rain_fall = (String) properties.get(DBModel.Crop.rainfall);
                        String soil_moisture = (String) properties.get(DBModel.Crop.soilmoisture);
                        String water_level = (String) properties.get(DBModel.Crop.waterlevel);
                        String sensortemperature = (String) properties.get(DBModel.Crop.sensortemperature);
                        String sensorrain_fall = (String) properties.get(DBModel.Crop.sensorrainfall);
                        String sensorsoil_moisture = (String) properties.get(DBModel.Crop.sensorsoilmoisture);
                        String sensorwater_level = (String) properties.get(DBModel.Crop.sensorwaterlevel);



                        String file_name = (String) properties.get(DBModel.Files.FILE_NAME);
                        //String file_type = (String) properties.get(DBModel.Files.MIMETYPE);
                        Object file_size = properties.get(DBModel.Files.SIZE);
                        String file_path = (String) properties.get(DBModel.Files.LOCATION);
                        Map createdDate = (Map) properties.get(DBModel.CREATED_ON_DATE_TIME);
                        String file_date = "";

                        Date dateTime = null;
                        if (createdDate.size() != 0) {
                            file_date = (String) createdDate.get("date");
                            try {

                                /*SimpleDateFormat formatter = new SimpleDateFormat(DBModel.DATE);

                                try {
                                    dateTime = formatter.parse(file_date);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                file_date = GeneralMethods.getConvertDateFormat(file_date, Constants.DATE_HOURS_MINI_FORMAT, Constants.DISPLAY_DATEFORMAT);
*/
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        double file_size_value = 0;
                        if (file_size != null) {

                            file_size_value = Double.parseDouble(file_size.toString());
                            file_size_value = file_size_value / 1024; //convert KB to Mb size
                        }

                        String file_name_extension = "";
                        if (!GeneralMethods.replaceNull(file_name).equals("")){
                            file_name_extension = file_name.substring(file_name.lastIndexOf(".") + 1);
                        }

                        cropsDetails.setUuid(uuid);
                        cropsDetails.setCrop_name(crop_name);
                        cropsDetails.setTemperature(temperature);
                        cropsDetails.setRain_fall(rain_fall);
                        cropsDetails.setSoil_moisture(soil_moisture);
                        cropsDetails.setWater_level(water_level);

                        cropsDetails.setSensortemperature(sensortemperature);
                        cropsDetails.setSensorrain_fall(sensorrain_fall);
                        cropsDetails.setSensorsoil_moisture(sensorsoil_moisture);
                        cropsDetails.setSensorwater_level(sensorwater_level);

                        cropsDetails.setFile_type(file_name_extension);
                        cropsDetails.setFile_name(file_name);
                        cropsDetails.setDate_added(file_date);
                        cropsDetails.setFile_size(file_size_value);
                        cropsDetails.setFile_path(file_path);

                        data_list.add(cropsDetails);
                    }catch (Exception e){}

                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        try{

                            CropsListAdapter cropsListAdapter = new CropsListAdapter(MainActivity.this, data_list);
                            cropsListView.setAdapter(cropsListAdapter);

                            if (data_list.size() == 0){
                                //emptyView.setVisibility(View.VISIBLE);
                            }else {
                                //emptyView.setVisibility(View.GONE);
                            }
                        }catch (Exception e){}

                    }
                });

            } catch (Exception e) {
                Log.d("Error", "Error to get Diary Time Tracking Status Document");
            }

        }

    };
    /**
     * Live query listener for debug list
     */
    LiveQuery.ChangeListener debugLivequery_listener = new LiveQuery.ChangeListener() {
        @Override
        public void changed(final LiveQuery.ChangeEvent event) {

            try {
                QueryEnumerator queryEnumerator = event.getRows();

                image_list = new ArrayList<>();

                while (queryEnumerator.hasNext()) {

                    AttachedFileDetails attachedFileDetails = new AttachedFileDetails("", "", "", "", "", 0.0, null);

                    QueryRow queryRow = queryEnumerator.next();
                    Document document = queryRow.getDocument();

                    final Map<String, Object> properties = document.getProperties();

                    try{

                        String uuid = (String) properties.get(DBModel.UUID);

                        String files_uuid = (String) properties.get(DBModel.UUID);
                        String file_name = (String) properties.get(DBModel.Files.NAME);
                        //String file_type = (String) properties.get(DBModel.Files.MIMETYPE);
                        Object file_size = properties.get(DBModel.Files.SIZE);
                        String file_path = (String) properties.get(DBModel.Files.LOCATION);
                        Map createdDate = (Map) properties.get(DBModel.CREATED_ON_DATE_TIME);
                        String file_date = "";

                        Date dateTime = null;
                        if (createdDate.size() != 0) {
                            file_date = (String) createdDate.get("date");
                            try {

                                /*SimpleDateFormat formatter = new SimpleDateFormat(DBModel.DATE);

                                try {
                                    dateTime = formatter.parse(file_date);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                file_date = GeneralMethods.getConvertDateFormat(file_date, Constants.DATE_HOURS_MINI_FORMAT, Constants.DISPLAY_DATEFORMAT);
*/
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        double file_size_value = 0;
                        if (file_size != null) {

                            file_size_value = Double.parseDouble(file_size.toString());
                            file_size_value = file_size_value / 1024; //convert KB to Mb size
                        }

                        String file_name_extension = file_name.substring(file_name.lastIndexOf(".") + 1);
                        attachedFileDetails.setFile_type(file_name_extension);

                        attachedFileDetails.setUuid(files_uuid);
                        attachedFileDetails.setFile_name(file_name);
                        attachedFileDetails.setDate_added(file_date);
                        attachedFileDetails.setFile_size(file_size_value);
                        attachedFileDetails.setFile_path(file_path);

                        attachedFileDetails.setUuid(uuid);

                        image_list.add(attachedFileDetails);
                    }catch (Exception e){}

                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        try{

                            debugListView.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));
                            ImageFileListAdapter imageFileListAdapter = new ImageFileListAdapter(MainActivity.this, image_list, debugListView);
                            debugListView.setAdapter(imageFileListAdapter);

                        }catch (Exception e){}

                    }
                });

            } catch (Exception e) {
                Log.d("Error", "Error to get Diary Time Tracking Status Document");
            }

        }

    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (requestCode == REQUEST_EXTERNAL_STORAGE)
            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Context context = MainActivity.this;
                PackageManager packageManager = context.getPackageManager();
                if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    Toast.makeText(context, getString(R.string.error_no_camera), Toast.LENGTH_SHORT)
                            .show();
                    return;
                }

                try{
                    AttachFilesDialogFragment attachFilesDialogFragment = new AttachFilesDialogFragment();
                    FragmentManager fragmentManager = ((MainActivity) context).getSupportFragmentManager();
                    attachFilesDialogFragment.show(fragmentManager, "image");
                }catch (Exception e){
                    e.printStackTrace();

                    Toast.makeText(MainActivity.this, "Try again", Toast.LENGTH_SHORT).show();
                }
            }


    }

    @Subscribe
    public void processChange(TriggerFirstTimeEvent event) {
        try {

            if (MainActivity.this != null) {
                if (event.allowed){

                    boolean loadAgain = false;
                    if (image_list.size() == 0){
                        loadAgain = true;
                    }else if (data_list.size() == 0){
                        loadAgain = true;
                    }

                    if (loadAgain){
                        /*loader to loads the register list item*/
                        getSupportLoaderManager().restartLoader(12, null, MainActivity.this).startLoading();
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void processChange(AttachmentEvent event) {
        try {

            if (MainActivity.this != null) {
                if (!GeneralMethods.replaceNull(event.uuid).equals("")){

                    //method to open capture image screen
                    openImageCapture(event.uuid);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}