package co.cropbit.sahathanahomecare;

import android.content.Context;
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
        Log.v("test", requests.toString());
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

        TextView content = result.findViewById(R.id.list_content);
        TextView status = result.findViewById(R.id.list_stat);

        Request request = (Request) getItem(i);
        DateFormat dt = DateFormat.getDateInstance(DateFormat.LONG);
        DateFormat tdt = DateFormat.getTimeInstance(DateFormat.SHORT);
        content.setText(dt.format(new Date(request.datetime)) + "  " + tdt.format(new Date(request.datetime)));
        status.setText(request.statusString());
        return result;
    }
}