package com.eightydegreeswest.irisplus.adapters;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.fragments.PetFragment;
import com.eightydegreeswest.irisplus.model.PetItem;
import com.eightydegreeswest.irisplus.tasks.PetDoTask;

import java.util.ArrayList;
import java.util.List;

public class PetListAdapter extends ArrayAdapter<PetItem> {
	private final Context context;
	private List<PetItem> pets = new ArrayList<PetItem>();
	private PetFragment fragment;
    private int currentSelection = 9999;
    private NotificationHelper notificationHelper;
    //private IrisPlusLogger logger = new IrisPlusLogger();

	public PetListAdapter(Context context, List<PetItem> pets, PetFragment fragment) {
		super(context, R.layout.list_pet);
		this.context = context;
		this.pets = pets;
		this.fragment = fragment;
        this.notificationHelper = new NotificationHelper(context);
	}

	@SuppressLint("ViewHolder")
	@Override
	public View getView(final int position, final View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View rowView = inflater.inflate(R.layout.list_pet, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.pet_name);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.pet_icon);
		textView.setText(pets.get(position).getPetName());
		imageView.setImageResource(pets.get(position).getIcon());

        final Spinner status = (Spinner) rowView.findViewById(R.id.pet_status);
        List<String> statusList = new ArrayList<String>();
        statusList.add("LOCKED");
        statusList.add("UNLOCKED");
        statusList.add("AUTO");
        //ArrayAdapter<CharSequence> spinnerArrayAdapter = ArrayAdapter.createFromResource(context, R.array.pet_status_array, android.R.layout.simple_spinner_item);
        StatusSpinnerAdapter spinnerArrayAdapter = new StatusSpinnerAdapter(context, android.R.layout.simple_spinner_item, statusList);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        status.setAdapter(spinnerArrayAdapter);

        if("locked".equalsIgnoreCase(pets.get(position).getState())) {
            status.setSelection(0);
        } else if("unlocked".equalsIgnoreCase(pets.get(position).getState())) {
            status.setSelection(1);
        } else if("auto".equalsIgnoreCase(pets.get(position).getState())) {
            status.setSelection(2);
        }

		final int currentRow = position;

        status.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(currentSelection < 9999 && currentSelection != i) {
                    fragment.setPetDoTask(new PetDoTask(fragment, pets.get(position).getId(), status.getItemAtPosition(i).toString().toUpperCase()));
                    TaskHelper.execute(fragment.getPetDoTask());
                }
                currentSelection = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        rowView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentRow != ListView.INVALID_POSITION) {
                    notificationHelper.buttonFeedback();
                    String id = pets.get(currentRow).getId();
                    if(id != null) {
                        rowView.setSelected(true);
                        FragmentManager fragmentManager = fragment.getActivity().getFragmentManager();
                        //fragmentManager.beginTransaction().add(R.id.container, RuleFragment.newInstance(99, id)).addToBackStack(null).commit();
                    }
                }
            }
        });

        return rowView;
    }

	@Override
	public int getCount() {
		return pets != null ? pets.size() : 0;
	}

    public void updateAdapterList(List<PetItem> newList) {
        this.pets.clear();
        this.pets.addAll(newList);
        this.notifyDataSetChanged();
    }
}