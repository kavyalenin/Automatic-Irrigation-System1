package irrigation.com.irrigation.Actvities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.github.yangweigbh.volleyx.VolleyX;
import com.google.gson.JsonObject;
import com.nineoldandroids.view.ViewHelper;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

import irrigation.com.irrigation.Database.DBModel;
import irrigation.com.irrigation.Model.CropsDetails;
import irrigation.com.irrigation.R;
import irrigation.com.irrigation.Utils.Api;
import irrigation.com.irrigation.Utils.GeneralMethods;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class CropViewActivity extends AppCompatActivity implements ObservableScrollViewCallbacks {

    private ImageView mImageView;
    private Toolbar mToolbarView;
    private ObservableScrollView mScrollView;
    private int mParallaxImageHeight;
    static CropsDetails cropsDetails;
    String crop_name;

    TextView descriptionView;
    TextView soilView;
    TextView temperatureView;
    TextView rainfallView;
    TextView waterlevelView;

    TextView sensorsoilView;
    TextView sensortemperatureView;
    TextView sensorrainfallView;
    TextView sensorwaterlevelView;

    LinearLayout actualDataLayout;
    LinearLayout sensorDataLayout;

    String notification_message;

    LinearLayout debugMainLayout;

    ImageView debugImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop_view);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        actualDataLayout = findViewById(R.id.actualDataLayout);
        sensorDataLayout = findViewById(R.id.sensorDataLayout);

        debugImageView = findViewById(R.id.debugImageView);
        debugMainLayout = findViewById(R.id.debugMainLayout);

        descriptionView = findViewById(R.id.descriptionView);
        soilView = findViewById(R.id.soilView);
        temperatureView = findViewById(R.id.temperatureView);
        rainfallView = findViewById(R.id.rainfallView);
        waterlevelView = findViewById(R.id.waterlevelView);

        sensorsoilView = findViewById(R.id.sensorsoilView);
        sensortemperatureView = findViewById(R.id.sensortemperatureView);
        sensorrainfallView = findViewById(R.id.sensorrainfallView);
        sensorwaterlevelView = findViewById(R.id.sensorwaterlevelView);

        if (cropsDetails != null){
            crop_name = cropsDetails.getCrop_name();
            getSupportActionBar().setTitle(crop_name);

            if (crop_name.equalsIgnoreCase("Rice")){
                sensorDataLayout.setVisibility(View.VISIBLE);


                String sensortemperature = cropsDetails.getSensortemperature();
                String sensorrain_fall = cropsDetails.getSensorrain_fall();
                String sensorsoil_moisture = cropsDetails.getSensorsoil_moisture();
                String sensorwater_level = cropsDetails.getSensorwater_level();

                sensortemperatureView.setText(sensortemperature);
                sensorrainfallView.setText(sensorrain_fall);
                sensorsoilView.setText(sensorsoil_moisture);
                sensorwaterlevelView.setText(sensorwater_level);

                notification_message = "Temp " + sensortemperature +", Rainfall "+ sensorrain_fall + ", Water level " + sensorwater_level;

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        //method to add notification
                        //addNotification();
                    }
                }, 5000);

            }

            String temperature = cropsDetails.getTemperature();
            String rain_fall = cropsDetails.getRain_fall();
            String soil_moisture = cropsDetails.getSoil_moisture();
            String water_level = cropsDetails.getWater_level();
            String description = cropsDetails.getDescription();

            temperatureView.setText(temperature);
            rainfallView.setText(rain_fall);
            soilView.setText(soil_moisture);
            waterlevelView.setText(water_level);
            descriptionView.setText(description);
        }


        mImageView = findViewById(R.id.image);
        mToolbarView = findViewById(R.id.toolbar);
        mToolbarView.setBackgroundColor(ScrollUtils.getColorWithAlpha(0, getResources().getColor(R.color.colorPrimary)));

        mScrollView = findViewById(R.id.observableScrollView);
        mScrollView.setScrollViewCallbacks(this);

        mParallaxImageHeight = getResources().getDimensionPixelSize(R.dimen.parallax_image_height);

        try{


            int img = GeneralMethods.getCropImage(CropViewActivity.this, crop_name);
            mImageView.setBackgroundResource(img);
        }catch (Exception e){
            e.printStackTrace();
        }

        //method to get data
        getData();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        onScrollChanged(mScrollView.getCurrentScrollY(), false, false);
    }

    //method to get data
    void getData() {
        try {
            String file_name = "";
            if (cropsDetails.getFile_type().equalsIgnoreCase("pcm")) {
                cropsDetails.setFile_type("wav");
                file_name = cropsDetails.getFile_name().replace("pcm", "wav");
                cropsDetails.setFile_name(file_name);
            }

            String uuid = cropsDetails.getUuid();
            //localfilename stored  as uuid with mimetype

            String localfilename = cropsDetails.getFile_name() + ".jpeg";// + cropsDetails.getFile_type();
            Uri uri = Uri.parse(DBModel.Files.FILE_PATH + "/" + localfilename);

            File outFile = new File(DBModel.Files.FILE_PATH, localfilename);

            File local_filedirectory = new File(uri.toString());
            Uri data1 = Uri.fromFile(outFile);


                /*MimeTypeMap map = MimeTypeMap.getSingleton();

                String type = map.getMimeTypeFromExtension(cropsDetails.getFile_type());*/

            Picasso.get().load(data1).into(debugImageView);

            if (data1 != null){
                debugMainLayout.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                finish();
                //do whatever
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        int baseColor = getResources().getColor(R.color.colorPrimary);
        float alpha = Math.min(1, (float) scrollY / mParallaxImageHeight);
        mToolbarView.setBackgroundColor(ScrollUtils.getColorWithAlpha(alpha, baseColor));
        ViewHelper.setTranslationY(mImageView, scrollY / 2);
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }
}