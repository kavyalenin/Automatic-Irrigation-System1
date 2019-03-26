package irrigation.com.irrigation.Common;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.couchbase.lite.Document;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.support.Base64;
import com.couchbase.lite.util.Log;
import com.google.gson.JsonObject;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import irrigation.com.irrigation.Database.App;
import irrigation.com.irrigation.Database.DBHelper;
import irrigation.com.irrigation.Database.DBModel;
import irrigation.com.irrigation.Database.Types;
import irrigation.com.irrigation.Evens.AndroidBusProvider;
import irrigation.com.irrigation.Evens.TriggerFirstTimeEvent;
import irrigation.com.irrigation.Model.AttachedFileDetails;
import irrigation.com.irrigation.R;
import irrigation.com.irrigation.Utils.GeneralMethods;
import irrigation.com.irrigation.Utils.ImageFilePath;
import irrigation.com.irrigation.Utils.MimeUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Random;

public class AttachFilesDialogFragment extends DialogFragment {

    private static final int SELECT_PICTURE = 1;
    private static final int TAKE_PHOTO = 2;
    private static final String TAG = "AttachedFilesDialogFragment";
    EditText fileNameView;

    String mime_type, file_name, take_photo_file_name = "";
    double file_size;
    Uri selectedImageUri;
    Document fileDocument;
    AppCompatButton saveButton, cancelButton;

    String uuid;

    /**
     * Create a new instance of AttachedFilesDialogFragment, providing "boolean values for check email attached files are customer,
     * supplier/Estimate and workaddress attached files screen
     */

    public static AttachFilesDialogFragment newInstance(String uuid){
        AttachFilesDialogFragment frag = new AttachFilesDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("uuid", uuid);
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        
        setRetainInstance(true);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.attached_files_add, null);

        fileNameView = view.findViewById(R.id.fileNameView);
        saveButton = view.findViewById(R.id.saveButton);
        cancelButton = view.findViewById(R.id.cancelButton);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        try{
            uuid = getArguments().getString("uuid");
        }catch (Exception e){
            e.printStackTrace();
        }


        //get the takephoto view
        /*takephotoButton = (Button) view.findViewById(R.id.takephoto);
        takephotoButton.setOnClickListener(takephoto_listener);


        cancelOption = (Button) view.findViewById(R.id.cancel);*/
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                take_photo_file_name = fileNameView.getText().toString().trim();

                if (GeneralMethods.replaceNull(take_photo_file_name).equals("")){
                    fileNameView.setError(getString(R.string.error_field_required));
                    fileNameView.setFocusable(true);
                    return;
                }

                if (take_photo_file_name.length() < 3){
                    fileNameView.setError("Name should be at least 3 characters");
                    fileNameView.setFocusable(true);
                    return;
                }

                //capture image view
                createCaptureImageView();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return builder.create();
    }


    /*
     * Create the new revision to attach the files on existing document and checks the mim_type to get the file size*/
    public void updateAttachment(Uri selectedImageUri, int imageRoll) {
        try {
            String uriString = selectedImageUri.toString();
            File myFile = new File(uriString);

            if(imageRoll==TAKE_PHOTO) {
                mime_type = GeneralMethods.getMimeType(uriString);
            }
            else if(uriString.startsWith("content://")){
                mime_type = getActivity().getContentResolver().getType(selectedImageUri);

            }

            if(uriString.startsWith("file:/")) {
                try {
                    file_name = myFile.getName();
                    mime_type = MimeUtils.getType(file_name);
                }catch (Exception e) {
                    Log.d(TAG, "Error getting on file size");
                }
            }
            String[] filetype = mime_type.split("/");

            if(imageRoll==TAKE_PHOTO)
                file_name = take_photo_file_name + "." + filetype[1];

            if (filetype[1].equals("exe") || filetype[1].equals("msi") || filetype[1].equals("dmg")) {
                Toast.makeText(getActivity(), getString(R.string.error_nonsuppoert_files), Toast.LENGTH_SHORT).show();
            } else {
                if (uriString.startsWith("content://")) {
                    Cursor cursor = null;
                    try {
                        cursor = getActivity().getContentResolver().query(selectedImageUri, null, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            file_name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                            if (imageRoll == TAKE_PHOTO) {
                                String selectedImagePath = getCapturedImagePath(selectedImageUri);
                                File file = new File(selectedImagePath);
                                double file_size = file.length();
                                // file_size = (int) length;
                                if (!take_photo_file_name.equals("")) {
                                    file_name = take_photo_file_name + "." + filetype[1];
                                }

                            } else {
                                String file_size_value = cursor.getString(cursor.getColumnIndex(OpenableColumns.SIZE));
                                file_size = Double.parseDouble(file_size_value) / 1024;
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), getString(R.string.error_attach_files), Toast.LENGTH_SHORT).show();
                    }
                }
                if(file_size/1024 > 30) {
                    Toast.makeText(getActivity(), getString(R.string.error_upload_files),Toast.LENGTH_SHORT).show();
                }else {
                    /*
                     * create new document to update takephoto/choose from gallery files and
                     * it directly attached choosen files to email form
                     * */
                    InputStream stream = getActivity().getContentResolver().openInputStream(selectedImageUri);

                    addRecord();

                    Document doc1 = App.getDatabase().getExistingDocument(fileDocument.getId());
                    UnsavedRevision newRev = doc1.getCurrentRevision().createRevision();
                    newRev.setAttachment(file_name, mime_type, stream);
                    newRev.save();
                    /*Document doc1 = App.getDatabase().getExistingDocument(fileDocument.getId());
                    UnsavedRevision newRev = doc1.getCurrentRevision().createRevision();
                    newRev.setAttachment(file_name, mime_type, stream);
                    newRev.save();

                    attachedFileDetails = new AttachedFileDetails("", "", "", "", "",0.0, null);

                    image_uri_list.add(selectedImageUri);
                    attachedFileDetails.setFilename(file_name);
                    attachedFileDetails.setFiletype(mime_type);
                    attachedFileDetails.setUri(selectedImageUri);
                    attachedFileDetails.setSelected(true);*/
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Create files document
    void addRecord() {
        try {
            /*fileDocument.update(new Document.DocumentUpdater() {
                @Override
                public boolean update(UnsavedRevision unsavedRevision) {

                    HashMap<String, Object> StandardUpdateProperties;

                    StandardUpdateProperties = DBHelper.createPropertiesHashmap(
                            Types.IMAGEFILES.toString(),
                            false);


                    //Update existing document fields with new updated fields
                    Map<String, Object> filePrperties = unsavedRevision.getUserProperties();

                    HashMap<String, Object> fileUpdateProperties = DBHelper.createFilesDocument(
                            fileDocument.getId(),
                            mime_type,
                            file_name,
                            file_size,
                            StandardUpdateProperties
                    );
                    filePrperties.putAll(fileUpdateProperties);
                    unsavedRevision.setUserProperties(filePrperties);
                    //successfully update record
                    Toast.makeText(getActivity(), getString(R.string.success_upload_attachment), Toast.LENGTH_SHORT).show();

                    return true;
                }
            });*/

            Map<String, Object> StandardUpdateProperties = fileDocument.getUserProperties();

            /*StandardUpdateProperties = DBHelper.createPropertiesHashmap(
                    Types.IMAGEFILES.toString(),
                    false);*/


            //Update existing document fields with new updated fields
            //Map<String, Object> filePrperties = unsavedRevision.getUserProperties();

            final Map<String, Object> fileUpdateProperties = DBHelper.createFilesDocument(
                    take_photo_file_name,
                    mime_type,
                    file_name,
                    file_size,
                    StandardUpdateProperties
            );


            /*StandardUpdateProperties.put(DBModel.Files.FILE_NAME, take_photo_file_name);
            StandardUpdateProperties.put(DBModel.Files.NAME, mime_type);
            StandardUpdateProperties.put(DBModel.Files.MIMETYPE, mime_type);
            StandardUpdateProperties.put(DBModel.Files.SIZE, file_size);*/
            //filePrperties.putAll(fileUpdateProperties);

            //fileDocument.putProperties(fileUpdateProperties);

            /*update the crop document*/
            fileDocument.update(new Document.DocumentUpdater() {
                @Override
                public boolean update(UnsavedRevision newRevision) {
                    newRevision.setUserProperties(fileUpdateProperties);

                    //method to add notification
                    addNotification();

                    return true;
                }
            });

            AndroidBusProvider.getInstance().post(new TriggerFirstTimeEvent(true));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getCapturedImagePath(Uri uri) {

        if (uri == null) {
            return null;
        }
        // this will only work for images selected from gallery
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }

        return uri.getPath();
    }


    /**
     * method to create a temp file in order to consume its file path
     * @return
     * @throws IOException
     */

    private File createTempImageFile(boolean isTakePhoto) throws IOException {
        // Create an image file name with a timestamp
        //if commusoft directory does not exist,create one
        File filedirectory = new File(DBModel.Files.FILE_PATH);
        if(!filedirectory.exists()) {
            filedirectory.mkdir();
        }

        //It stored local file name as document uuid to avoid same filename overwrite issue
        File image = null;
        if(isTakePhoto) {

            int min = 1;
            int max = 1000;

            Random r = new Random();
            int tempNum = r.nextInt(max - min + 1) + min;
            fileDocument = App.getDatabase().getExistingDocument(uuid);
            image = GeneralMethods.createTempFile(
                    take_photo_file_name,  /* prefix */
                    ".jpeg",         /* suffix */
                    filedirectory      /* directory */
            );
        }/*else {
            image = GeneralMethods.createTempFile(
                    fileDocument.getId(),  *//* prefix *//*
                    "."+file_name.substring(file_name.lastIndexOf('.') + 1),         *//* suffix *//*
                    filedirectory      *//* directory *//*
            );
        }*/
        // Save a file: path for use with ACTION_VIEW intents
        selectedImageUri =  Uri.parse("file:" + image.getAbsolutePath());
        return image;
    }

    /**
     * create permanent file after image is captured
     */
    public void createPermanentFile(){
        try {
            //broadcast that an image has been saved to the gallery
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File myFile = new File(selectedImageUri.getPath());
            //create the permanent file
            try {
                myFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Uri contentUri = Uri.fromFile(myFile);
            mediaScanIntent.setData(contentUri);
            getActivity().sendBroadcast(mediaScanIntent);


            if (myFile.exists()) {

                BitmapFactory.Options bounds = new BitmapFactory.Options();
                bounds.inJustDecodeBounds = true;
                bounds.inSampleSize = calculateInSampleSize(bounds,bounds.outWidth/2,bounds.outHeight/2);

                BitmapFactory.decodeFile(selectedImageUri.getPath(), bounds);

                //It stored how the device was oriented when the image was taken on device
                BitmapFactory.Options options = new BitmapFactory.Options();
                bounds.inSampleSize = calculateInSampleSize(bounds,bounds.outWidth/2,bounds.outHeight/2);
                Bitmap bm = BitmapFactory.decodeFile(selectedImageUri.getPath(), options);
                OutputStream fOut = null;

                ExifInterface exif = new ExifInterface(selectedImageUri.getPath());

                //String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                // int orientation = orientString != null ? Integer.parseInt(orientString) :  ExifInterface.ORIENTATION_NORMAL;

                //To get image rotated correctly
                int rotationAngle = 0;
                if (orientation == ExifInterface.ORIENTATION_ROTATE_90){
                    rotationAngle = 90;
                    exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_180));
                    exif.saveAttributes();
                }

                if (orientation == ExifInterface.ORIENTATION_ROTATE_180){
                    rotationAngle = 180;

                    exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_180));
                    exif.saveAttributes();
                }
                if (orientation == ExifInterface.ORIENTATION_ROTATE_270){
                    rotationAngle = 270;

                    exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_ROTATE_270));
                    exif.saveAttributes();
                }

                Matrix matrix = new Matrix();
                matrix.postRotate(rotationAngle);
                // matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
                Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
                //temp solution to fix out of memory issue, orientation change needs testing as it might break it
                //Bitmap rotatedBitmap = Bitmap.createScaledBitmap(bm, bounds.outWidth, bounds.outHeight, true);

                try {
                    fOut = new FileOutputStream(myFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                // 100 means no compression, the lower you go, the stronger the compression
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
                try {
                    fOut.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    fOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }


    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * handle incoming image intent
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {

            if (requestCode == TAKE_PHOTO) {
                try {

                    updateAttachment(selectedImageUri, TAKE_PHOTO);
                    dismiss();

                    createPermanentFile();

                } catch (Exception e) {
                    Toast.makeText(getActivity(), getString(R.string.error_capture_image), Toast.LENGTH_SHORT).show();
                }
            }
        }
        else{
            Toast.makeText(ctx, getString(R.string.error_capture_image), Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ctx = (FragmentActivity)context;
    }


    private FragmentActivity ctx;

    void createCaptureImageView() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Create the temporary file where the photo should go
        File photoFile = null;
        try {
            photoFile = createTempImageFile(true);
        } catch (IOException ex) {
            // Error occurred while creating the File
            ex.printStackTrace();
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {

            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(photoFile));

            startActivityForResult(takePictureIntent, TAKE_PHOTO);

        }

    }


    //method to add notification
    private void addNotification() {

        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey(getString(R.string.subscribekey));
        pnConfiguration.setPublishKey(getString(R.string.publishkey));
        pnConfiguration.setSecure(false);

        PubNub pubnub = new PubNub(pnConfiguration);

        String crop_name = (String) fileDocument.getUserProperties().get(DBModel.Crop.crop_name);

        Random r = new Random();
        int randomNumber = r.nextInt(2);

        String title = crop_name + " Warning";
        String message = getString(R.string.warning_bug);
        if (randomNumber == 0){
            title = crop_name + " Success";
            message = getString(R.string.warning_nobug);
        }

        JsonObject position = new JsonObject();
        position.addProperty("title", title);
        position.addProperty("message", message);
        position.addProperty("sector", "");

        pubnub.publish()
                .message(position)
                .channel(getString(R.string.channel_name))
                .async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        // handle publish result, status always present, result if successful
                        // status.isError() to see if error happened
                        if(!status.isError()) {
                            System.out.println("pub timetoken: " + result.getTimetoken());
                        }
                        System.out.println("pub status code: " + status.getStatusCode());
                    }
                });
    }
}