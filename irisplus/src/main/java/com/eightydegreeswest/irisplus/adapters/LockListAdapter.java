package com.eightydegreeswest.irisplus.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.fragments.LockFragment;
import com.eightydegreeswest.irisplus.model.LockItem;
import com.eightydegreeswest.irisplus.tasks.LockDoTask;

import java.util.ArrayList;
import java.util.List;

public class LockListAdapter extends ArrayAdapter<LockItem> {
	private final Context context;
	private List<LockItem> locks = new ArrayList<LockItem>();
	private LockFragment fragment;
    private NotificationHelper notificationHelper;
    //private IrisPlusLogger logger = new IrisPlusLogger();

	public LockListAdapter(Context context, List<LockItem> locks, LockFragment fragment) {
		super(context, R.layout.list_lock);
		this.context = context;
		this.locks = locks;
		this.fragment = fragment;
        this.notificationHelper = new NotificationHelper(context);
	}

	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, final View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View rowView = inflater.inflate(R.layout.list_lock, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.lock_name);
		final ToggleButton status = (ToggleButton) rowView.findViewById(R.id.lock_status_button);
		textView.setText(locks.get(position).getLockName());

		String state = locks.get(position).getState();

		try {		
			if("locked".equalsIgnoreCase(state) || "locking".equalsIgnoreCase(state) || "closed".equalsIgnoreCase(state) || "closing".equalsIgnoreCase(state)) {
				status.setChecked(true);
			} else {
				status.setChecked(false);
			}
		} catch (Exception e) {
			status.setChecked(false);
		}

        final int currentRow = position;

        rowView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentRow != ListView.INVALID_POSITION) {
                    notificationHelper.buttonFeedback();
					final int position = ((ListView) status.getParent().getParent()).getPositionForView(v);
                    String id = locks.get(position).getId();
					String lockType = locks.get(position).getType();
                    if(id != null) {
						status.setChecked(!status.isChecked());
						fragment.setLockDoTask(new LockDoTask(fragment, id, lockType, status, false));
						TaskHelper.execute(fragment.getLockDoTask());
                    }
                }
            }
        });

		rowView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (currentRow != ListView.INVALID_POSITION) {
					notificationHelper.buttonFeedback();
					final int position = ((ListView) status.getParent().getParent()).getPositionForView(v);
					String id = locks.get(position).getId();
					String lockType = locks.get(position).getType();
					if(id != null) {
						status.setChecked(false);
						fragment.setLockDoTask(new LockDoTask(fragment, id, lockType, status, true));
						TaskHelper.execute(fragment.getLockDoTask());
					}
				}
				return true;
			}
		});
		
		status.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                notificationHelper.buttonFeedback();
                final int position = ((ListView) status.getParent().getParent()).getPositionForView(v);
		        if (position != ListView.INVALID_POSITION) {
		        	String id = locks.get(position).getId();
					String lockType = locks.get(position).getType();
		        	if(id != null) {
		        		fragment.setLockDoTask(new LockDoTask(fragment, id, lockType, status, false));
						TaskHelper.execute(fragment.getLockDoTask());
		        	}
		        	//Toast.makeText(context, "Short click for row " + position, Toast.LENGTH_LONG).show();
		        }
			}			
		});

        status.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                notificationHelper.buttonFeedback();
                final int position = ((ListView) status.getParent().getParent()).getPositionForView(v);
                if (position != ListView.INVALID_POSITION) {
                    String id = locks.get(position).getId();
					String lockType = locks.get(position).getType();
                    if (id != null && locks.get(position).isBuzzIn()) {
                        status.setChecked(false);
                        fragment.setLockDoTask(new LockDoTask(fragment, id, lockType, status, true));
						TaskHelper.execute(fragment.getLockDoTask());
                    }
                }
                return true;
            }
        });

        return rowView;
    }

	@Override
	public int getCount() {
		return locks != null ? locks.size() : 0;
	}

    public void updateAdapterList(List<LockItem> newList) {
        this.locks.clear();
        this.locks.addAll(newList);
        this.notifyDataSetChanged();
    }
}