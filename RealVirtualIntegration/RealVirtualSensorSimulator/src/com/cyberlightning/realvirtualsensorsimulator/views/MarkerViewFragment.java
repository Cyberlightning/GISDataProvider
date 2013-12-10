package com.cyberlightning.realvirtualsensorsimulator.views;


import com.example.realvirtualsensorsimulator.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;


public class MarkerViewFragment extends Fragment implements OnClickListener {
	
	private ImageView markerView;
	private Button returnButton;
	private String markerId;
	
	public MarkerViewFragment () {
		
	}
	
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		LinearLayout view = (LinearLayout) inflater.inflate(R.layout.marker, container, false);
        this.markerView = (ImageView) view.findViewById(R.id.marker_view);
        this.returnButton = (Button) view.findViewById(R.id.return_button);
        this.returnButton.setOnClickListener(this);
        Bundle bundle=getArguments();
		 this.markerId = bundle.getString("markerID");
        this.changeMarker(this.markerId);
        setHasOptionsMenu(true);
        return view;
    }
	
	@Override
	public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
		((MenuItem)menu.findItem(R.id.menu_main)).setVisible(true);
    	super.onCreateOptionsMenu(menu, inflater);
	}
	
	public void changeMarker(String _id) {
		int i = getResources().getIdentifier(getActivity().getPackageName() + ":drawable/" + _id, null, null);
		if (i != 0) {
			this.markerView.setImageDrawable(this.getResources().getDrawable(i));
			this.markerView.invalidate();
		}
		
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.return_button) getActivity().onBackPressed();
	}
	
}
