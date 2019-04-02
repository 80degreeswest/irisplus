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
import com.eightydegreeswest.irisplus.fragments.RuleFragment;
import com.eightydegreeswest.irisplus.model.RuleItem;
import com.eightydegreeswest.irisplus.tasks.RuleDoTask;

import java.util.ArrayList;
import java.util.List;

public class RuleListAdapter extends ArrayAdapter<RuleItem> {
	private final Context context;
	private List<RuleItem> rules = new ArrayList<RuleItem>();
	private RuleFragment fragment;
    private NotificationHelper notificationHelper;
    //private IrisPlusLogger logger = new IrisPlusLogger();

	public RuleListAdapter(Context context, List<RuleItem> rules, RuleFragment fragment) {
		super(context, R.layout.list_rule);
		this.context = context;
		this.rules = rules;
		this.fragment = fragment;
        this.notificationHelper = new NotificationHelper(context);
	}

	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, final View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View rowView = inflater.inflate(R.layout.list_rule, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.rule_name);
		TextView description = (TextView) rowView.findViewById(R.id.rule_description);
		final ToggleButton status = (ToggleButton) rowView.findViewById(R.id.rule_status_button);
		textView.setText(rules.get(position).getRuleName());
		description.setText(rules.get(position).getRuleDescription());

		String state = rules.get(position).getEnabled();

		if("ENABLED".equalsIgnoreCase(state)) {
			status.setChecked(true);
		} else {
			status.setChecked(false);
			//textView.setTextColor(Color.RED);
			//description.setTextColor(Color.RED);
		}

        final int currentRow = position;
		
		status.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                notificationHelper.buttonFeedback();
		        if (currentRow != ListView.INVALID_POSITION) {
		        	String id = rules.get(currentRow).getRuleId();
		        	if(id != null) {
		        		fragment.setRuleDoTask(new RuleDoTask(fragment, id, status));
						TaskHelper.execute(fragment.getRuleDoTask());
		        	}
		        }
			}			
		});

        return rowView;
    }

	@Override
	public int getCount() {
		return rules != null ? rules.size() : 0;
	}

    public void updateAdapterList(List<RuleItem> newList) {
        this.rules.clear();
        this.rules.addAll(newList);
        this.notifyDataSetChanged();
    }
}