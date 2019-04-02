package com.eightydegreeswest.irisplus.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.eightydegreeswest.irisplus.R;

import java.util.List;

/**
 * Created by ybelenitsky on 2/9/2015.
 */
public class BasicSpinnerAdapter extends ArrayAdapter<String> {

    private Context context;
    List<String> data = null;

    public BasicSpinnerAdapter(Context context, int resource, List<String> data) {
        super(context, resource, data);
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {   // Ordinary view in Spinner, we use android.R.layout.simple_spinner_item
        return super.getView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {   // This view starts when we click the spinner.
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.basic_spinner, parent, false);
        }

        String item = data.get(position);

        if (item != null) {
            TextView text = (TextView) row.findViewById(R.id.basic_spinner_text);
            text.setText(item);
        }

        return row;
    }

    public void updateAdapterList(List<String> newList) {
        this.data.clear();
        this.data.addAll(newList);
        this.notifyDataSetChanged();
    }
}
