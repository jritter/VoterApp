/**
 * 
 */
package ch.bfh.evoting.voterapp.fragment;


import org.achartengine.GraphicalView;

import ch.bfh.evoting.voterapp.R;
import ch.bfh.evoting.voterapp.entities.Poll;
import ch.bfh.evoting.voterapp.util.PieChartView;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * @author tgdriju1
 *
 */
public class ResultChartFragment extends Fragment {
	
	String [] labels;
	float [] values;
	
	private Poll poll;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		poll = (Poll) getActivity().getIntent().getSerializableExtra("poll");
		
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_result_chart, container,
				false);
		
		// Displaying the graph
		LinearLayout layoutGraph = (LinearLayout) v.findViewById(R.id.layout_result_chart);
		//values = calculateData(values);
		GraphicalView chartView = PieChartView.getNewInstance(getActivity(), poll);
		chartView.setClickable(true);
		
		
		chartView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ResultChartDialogFragment.newInstance().show(getFragmentManager(), "resultChartDialog");
			}
			
		});
		
		layoutGraph.addView(chartView);
		
		return v;
	}

}
