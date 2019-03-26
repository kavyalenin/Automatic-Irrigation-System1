package irrigation.com.irrigation.Utils;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.couchbase.lite.Document;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.util.Log;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import irrigation.com.irrigation.Database.App;
import irrigation.com.irrigation.Database.DBHelper;
import irrigation.com.irrigation.Database.DBModel;
import irrigation.com.irrigation.Database.Types;
import irrigation.com.irrigation.Database.Views;
import irrigation.com.irrigation.R;

public class PushNotificationService extends Service {
    PNConfiguration pnConfiguration = new PNConfiguration();
    String title = "", description = "";
    NotificationManager mNotificationManager;
    int NOTIFICATION_ID = 0;
    int user_id;
    boolean hasloggedin;
    long timetoken;
    SubscribeCallback subscribeCallback;
    MessageNotification notificationIndospace = new MessageNotification();


    /*db document variable*/
    Document document;

    Map<String, Object> updateProperties = new HashMap<>();

    public PushNotificationService() {
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        try {
            pnConfiguration.setSubscribeKey(getString(R.string.subscribekey));
            pnConfiguration.setPublishKey(getString(R.string.publishkey));
            final PubNub pubnub = new PubNub(pnConfiguration);

            /*try{
                pubnub.unsubscribe()
                        .channels(Arrays.asList(getString(R.string.channelname)))
                        .execute();
                //pubnub.addListener(null);
            }catch (Exception e){
                e.printStackTrace();
            }*/

            //List<String> channel_list = new ArrayList<>();

            //channel_list.add(getString(R.string.channelname));
            try{
                ArrayList channel_list=new ArrayList();
                channel_list.add(getString(R.string.channel_name));
                if (hasloggedin) {
                    if (user_id != 0) {
                        channel_list.add(String.valueOf(user_id));
                       /* pubnub.subscribe()
                                .channels(Arrays.asList(String.valueOf(user_id)))
                                .execute();*/
                    }
                }
                /*pubnub.unsubscribe()
                        .channels(channel_list)
                        .execute();*/

                pubnub.subscribe()
                        .channels(channel_list)
                        .execute();
            }catch (Exception e){
                e.printStackTrace();
            }

            if (subscribeCallback != null){
                //pubnub.removeListener(subscribeCallback);
            }

            subscribeCallback = new SubscribeCallback() {
                @Override
                public void status(PubNub pubnub, PNStatus status) {
//                    Toast.makeText(getApplicationContext(),"Sample Notification",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void message(PubNub pubnub, PNMessageResult message) {

                    try{
                        if (message.getChannel() != null && timetoken != message.getTimetoken()){
                            timetoken = message.getTimetoken();
                            JsonObject message_obj = message.getMessage().getAsJsonObject();
                            title = message_obj.get("title").getAsString();
                            String sector = message_obj.get("sector").getAsString();
                            description = message_obj.get("message").getAsString();
//                            title = message_obj.getAsJsonPrimitive(DBModel.PUSHNOTIFICATION.message).getAsString();
                            /*Object gcmSummary = message_obj.getAsJsonObject("pn_gcm").getAsJsonObject("data").get("summary");
                            title = ((JsonElement) gcmSummary).getAsJsonPrimitive().getAsString();*///message_obj.getAsJsonObject("pn_gcm").getAsJsonObject("data").get("summary").toString();
                            title = title.replace("\"", "");
                            String channelid = message.getChannel();
                            notificationIndospace.notify(PushNotificationService.this, title, description, channelid);

                            if (!GeneralMethods.replaceNull(sector).equals("")){

                                //method to add or update the crop document
                                addOrUpdateDocument(title, sector, description);
                            }
                        }

                        /*if (message.getChannel() != null && push == 1 && timetoken != message.getTimetoken()) {

                            if(!hasloggedin && !message.getChannel().equalsIgnoreCase(getString(R.string.channelname)))
                            {
                                return;
                            }else {
                                timetoken = message.getTimetoken();
                                JsonObject message_obj = message.getMessage().getAsJsonObject();
                                //title = String.valueOf(message_obj.get("title"));
                                //description = String.valueOf(message_obj.get(DBModel.PUSHNOTIFICATION.message));
//                            title = message_obj.getAsJsonPrimitive(DBModel.PUSHNOTIFICATION.message).getAsString();
                                Object gcmSummary = message_obj.getAsJsonObject("pn_gcm").getAsJsonObject("data").get("summary");
                                title = ((JsonElement) gcmSummary).getAsJsonPrimitive().getAsString();//message_obj.getAsJsonObject("pn_gcm").getAsJsonObject("data").get("summary").toString();
                                title = title.replace("\"", "");
                                String channelid = message.getChannel();
                                notificationIndospace.notify(PushNotificationService.this, title, description, channelid);
                            }
                        }*/
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void presence(PubNub pubnub, PNPresenceEventResult presence) {
//                    Toast.makeText(getApplicationContext(),"Sample Notification ",Toast.LENGTH_SHORT).show();

                }

            };

            pubnub.addListener(subscribeCallback);



        } catch (Exception e) {
            e.printStackTrace();
        }
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        Intent restartService = new Intent("MyReceiver");
//        sendBroadcast(restartService);
    }


    //method to add or update the crop document
    public void addOrUpdateDocument(String crop_name, String sector, String value){

        try {

            if (GeneralMethods.replaceNull(sector).equals("")){
                return;
            }
            String view = Views.getCROP();

            com.couchbase.lite.View couchQuestionView =
                    App.getDatabase().getExistingView(view);

            Query query = couchQuestionView.createQuery();
            query.setKeys(GeneralMethods.getQueryKeys(crop_name));

            QueryEnumerator queryEnumerator = query.run();

            String crop_uuid = "";

            while (queryEnumerator.hasNext()) {
                QueryRow queryRow = queryEnumerator.next();
                Document document = queryRow.getDocument();

                final Map<String, Object> properties = document.getProperties();
                crop_uuid = (String) properties.get(DBModel.UUID);
            }

            App app = (App) this.getApplication();
            if (!GeneralMethods.replaceNull(crop_uuid).equals("")) {
                document = app.getDatabase().getExistingDocument(crop_uuid);
            } else {
                document = App.getDatabase().createDocument();
            }
            try {

                /*document standard*/
                Map<String, Object> documentproperties_map = new HashMap<>();

                //standard properties for crop document
                if (!GeneralMethods.replaceNull(crop_uuid).equals("")) {
                    documentproperties_map =
                            DBHelper.updatePropertiesHashmap
                                    (this, false, false);
                    Map<String,Object> originalProperties = document.getUserProperties();
                    originalProperties.putAll(documentproperties_map);
                    updateProperties = originalProperties;
                } else {
                    documentproperties_map =
                            DBHelper.createPropertiesHashmap(
                                    Types.CROP.toString(),
                                    false);


                    updateProperties = DBHelper.createFilesDocument(
                            "",
                            null,
                            null,
                            0.0,
                            documentproperties_map);
                }


                updateProperties.put(DBModel.UUID, document.getId());
                updateProperties.put(DBModel.Crop.crop_name, crop_name);
                updateProperties.put(sector, value);

                if(!GeneralMethods.replaceNull(crop_uuid).equals("")){

                    /*update the make document*/
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

            } catch (Exception e) {
            }
        } catch (Exception e) {
            Log.e("Error", "Error creating document.", e);
        }
    }

}