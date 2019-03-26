package irrigation.com.irrigation.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import irrigation.com.irrigation.Evens.AndroidBusProvider;
import irrigation.com.irrigation.Evens.AttachmentEvent;
import irrigation.com.irrigation.Evens.TriggerFirstTimeEvent;
import irrigation.com.irrigation.Model.CropsDetails;
import irrigation.com.irrigation.R;

public class CropsListAdapter extends BaseAdapter {
    Context mContext;
    List<CropsDetails> data_list;

    public CropsListAdapter(@NonNull Context context, List<CropsDetails> data_list) {
        mContext = context;
        this.data_list = data_list;
    }

    @Override
    public int getCount() {
        return data_list.size();
    }

    @Override
    public Object getItem(int position) {
        return data_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        try {
            CropsDetails cropsDetails = data_list.get(position);
            //get the value of the class variables in the adapter using the position value
            String crop_name = cropsDetails.getCrop_name();
            String uuid = cropsDetails.getUuid();

            //LayoutInflater is used to get the View object which you define in a layout xml
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.crops_item, parent, false);

            //Get references of views
            TextView itemView = convertView.findViewById(R.id.itemView);
            LinearLayout cameraView = convertView.findViewById(R.id.cameraView);
            cameraView.setTag(uuid);
            cameraView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try{

                        String uuid = view.getTag().toString();
                        AndroidBusProvider.getInstance().post(new AttachmentEvent(uuid));

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });

            //set text for options
            itemView.setText(crop_name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;

    }


}