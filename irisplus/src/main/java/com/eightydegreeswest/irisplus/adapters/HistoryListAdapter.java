package com.eightydegreeswest.irisplus.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.fragments.HistoryFragment;
import com.eightydegreeswest.irisplus.model.HistoryItem;
import com.eightydegreeswest.irisplus.tasks.HistoryViewTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoryListAdapter extends ArrayAdapter<HistoryItem> {
	private final Context context;
	private List<HistoryItem> historyItems = new ArrayList<HistoryItem>();
    HistoryFragment mFragment = null;

	public HistoryListAdapter(Context context, List<HistoryItem> historyItems, HistoryFragment fragment) {
		super(context, R.layout.list_history);
		this.context = context;
        this.mFragment = fragment;
		this.historyItems = historyItems;
	}

	@Override
	public View getView(int position, final View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View rowView = inflater.inflate(R.layout.list_history, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.history_text);
		textView.setText(historyItems.get(position).getDescription());
		TextView dateView = (TextView) rowView.findViewById(R.id.history_date);
        Date date = new Date();
        try {
            date.setTime(Long.parseLong(historyItems.get(position).getDate()));
        } catch (NumberFormatException nfe) {
            date.setTime(0);
        }
		dateView.setText(date.toString());

        final ListView historyList = (ListView) parent.findViewById(R.id.history_fragment_view);
        historyList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) { }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //what is the bottom iten that is visible
                int lastInScreen = firstVisibleItem + visibleItemCount;
                //check if bottom item is visible and load more items
                if((lastInScreen == totalItemCount) && mFragment.getHistoryViewTask() == null){
                    try {
                        Toast.makeText(context, "Loading more history...", Toast.LENGTH_SHORT).show();
                        mFragment.setPosition(historyItems.size() - 1);
                        String offsetId = historyItems.get(historyItems.size() - 1).getOffset();

                        try {
                            int x = 2;
                            while(offsetId == null) {
                                offsetId = historyItems.get(historyItems.size() - x).getOffset();
                                x++;
                            }
                        } catch(Exception e) {
                            //Probably end of list
                        }
                        if(offsetId != null) {
                            mFragment.setHistoryViewTask(new HistoryViewTask(mFragment, mFragment.getLimit(), offsetId));
                            TaskHelper.execute(mFragment.getHistoryViewTask());
                        } else {
                            Toast.makeText(context, "No more history", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(context, "No more history", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
        });

		return rowView;
	}

	@Override
	public int getCount() {
		return historyItems != null ? historyItems.size() : 0;
	}

    public void updateAdapterList(List<HistoryItem> newList) {
        this.historyItems.clear();
        this.historyItems.addAll(newList);
        this.notifyDataSetChanged();
    }
}