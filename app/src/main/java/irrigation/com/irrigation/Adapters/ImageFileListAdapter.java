package irrigation.com.irrigation.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import com.couchbase.lite.Attachment;
import com.couchbase.lite.Document;
import com.couchbase.lite.Revision;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import irrigation.com.irrigation.Database.App;
import irrigation.com.irrigation.Database.DBModel;
import irrigation.com.irrigation.Model.AttachedFileDetails;
import irrigation.com.irrigation.R;

public class ImageFileListAdapter extends RecyclerView.Adapter<ImageFileListAdapter.ViewHolder> {

    Context mContext;
    List<AttachedFileDetails> data_list;
    RecyclerView listView;
    ImageFileListAdapter.ViewHolder vhItem;


    public ImageFileListAdapter(@NonNull Context context, List<AttachedFileDetails> list, RecyclerView view) {
        mContext = context;
        data_list = list;
        listView = view;
    }

    public static int getPosition() {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public ImageFileListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.attachment_item, parent, false);

        vhItem = new ImageFileListAdapter.ViewHolder(v, viewType);
        return vhItem;
    }

    @Override
    public void onBindViewHolder(final ImageFileListAdapter.ViewHolder holder, int position) {

        try {
            AttachedFileDetails fileItems = data_list.get(position);

            //LayoutInflater is used to get the View object which you define in a layout xml
            //LayoutInflater inflater = LayoutInflater.from(mContext);

            //Get references of views

            try {
                String file_name = "";
                if (fileItems.getFile_type().equalsIgnoreCase("pcm")) {
                    fileItems.setFile_type("wav");
                    file_name = fileItems.getFile_name().replace("pcm", "wav");
                    fileItems.setFile_name(file_name);
                }

                String uuid = fileItems.getUuid();
                //localfilename stored  as uuid with mimetype

                String localfilename = uuid + "." + fileItems.getFile_type();
                Uri uri = Uri.parse(DBModel.Files.FILE_PATH + "/" + localfilename);

                File outFile = new File(DBModel.Files.FILE_PATH, localfilename);

                File local_filedirectory = new File(uri.toString());
                Uri data1 = Uri.fromFile(outFile);


                /*MimeTypeMap map = MimeTypeMap.getSingleton();

                String type = map.getMimeTypeFromExtension(fileItems.getFile_type());*/

                holder.fileNameView.setText(fileItems.getFile_name());
                Picasso.get().load(data1).into(holder.imageView);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }    @Override
    public int getItemCount() {

        return data_list.size(); // the number of items in the list will be +1 the titles including the header view.
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView fileNameView;
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public ViewHolder(final View itemView, int ViewType) {
            // Creating ViewHolder Constructor with View and viewType As a parameter
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            fileNameView = itemView.findViewById(R.id.fileNameView);
        }
    }


}