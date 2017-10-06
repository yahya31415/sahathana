package co.cropbit.sahathanahomecare;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import co.cropbit.sahathanahomecare.R;
import co.cropbit.sahathanahomecare.model.Request;

public class RequestAdapter extends BaseAdapter {
    ArrayList<Request> requests;
    Context context;
    LayoutInflater layoutInflater;

    public RequestAdapter(Context context, ArrayList<Request> requests) {
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.requests = requests;
    }

    @Override
    public int getCount() {
        return requests.size();
    }

    @Override
    public Object getItem(int i) {
        return requests.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View result = layoutInflater.inflate(R.layout.request_list_item, viewGroup, false);

        CardView cardView = result.findViewById(R.id.list_card);
        TextView date_text = result.findViewById(R.id.list_date);
        TextView time_text = result.findViewById(R.id.list_time);
        TextView type_text = result.findViewById(R.id.list_type);
        TextView status = result.findViewById(R.id.list_stat);
        TextView apb = result.findViewById(R.id.list_apb);

        Request request = (Request) getItem(i);

        DateFormat dt = DateFormat.getDateInstance(DateFormat.LONG);
        DateFormat tdt = DateFormat.getTimeInstance(DateFormat.SHORT);

        date_text.setText(dt.format(new Date(request.datetime)));
        // date_text.setAlpha((float)(request.isEmergency ? 0.56 : 0.87));
        date_text.setTextColor(!request.isEmergency ? Color.parseColor("#000000") : Color.WHITE);

        time_text.setText(tdt.format(new Date(request.datetime)));
        // time_text.setTextColor(!request.isEmergency ? Color.parseColor("#000000") : Color.WHITE);

        status.setText(request.statusString());
        status.setTextColor(!request.isEmergency ? Color.parseColor("#D32F2F") : Color.WHITE);

        apb.setText(request.approved);
        apb.setTextColor(!request.isEmergency ? Color.parseColor("#D32F2F") : Color.WHITE);

        type_text.setText(request.type);
        type_text.setTextColor(!request.isEmergency ? Color.parseColor("#000000") : Color.WHITE);
        // type_text.setAlpha((float)(request.isEmergency ? 0.56 : 0.87));

        cardView.setCardBackgroundColor(request.isEmergency ? Color.parseColor("#D32F2F") : Color.WHITE);
        return result;
    }
}