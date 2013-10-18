package ch.bfh.evoting.voterapp.util;

import ch.bfh.evoting.voterapp.AndroidApplication;
import de.mindpipe.android.logging.log4j.LogConfigurator;
import android.content.Context;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Utility class 
 * @author Philémon von Bergen
 *
 */
public class Utility {

	/**
	 * Method setting the height of a ListView when it is used in a ScrollView.
	 * Android doesn't support inserting ListView in ScrollView, what result in displaying only one line
	 * of the ListView. The method is a workaround which allows to display all lines of the ListView.
	 * @param listView
	 * @param wrapContent true if each item height has to wrap its content, false if the item height is fixed in the xml layout
	 * Inspired from: http://stackoverflow.com/questions/9484038/how-to-force-height-of-listview-to-be-size-so-that-every-row-is-visible
	 */
	public static void setListViewHeightBasedOnChildren(ListView listView, boolean wrapContent) {
		ListAdapter listAdapter = listView.getAdapter(); 
		if (listAdapter == null) {
			// pre-condition
			return;
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		if(wrapContent){
			//if item height is wrapContent
			int totalHeight = 0;
			for (int i = 0; i < listAdapter.getCount(); i++) {
				View listItem = listAdapter.getView(i, null, listView);
				listItem.measure(0, 0);
				totalHeight += listItem.getMeasuredHeight();
			}
			params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		} else {
			//if item height is fixed in xml layout
			int itemHeight = listAdapter.getView(0, null, listView).getLayoutParams().height;
			params.height = (itemHeight*listAdapter.getCount()) + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		}
		listView.setLayoutParams(params);
		listView.requestLayout();
	}
	
	/**
	 * Transform density pixel in pixel
	 * @param ctx Android context
	 * @param dp Density pixel to transform to pixels
	 * @return
	 * Source: http://stackoverflow.com/questions/5012840/android-specifying-pixel-units-like-sp-px-dp-without-using-xml/5012893#5012893
	 */
	public static int dp2px(Context ctx, int dp){
		DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
		float dp2 = (float)dp;
		//float fpixels = metrics.density * dp2;
		int pixels = (int) (metrics.density * dp2 + 0.5f);
		return pixels;
	}
	
	
	/**
	 * This method converts device specific pixels to density independent pixels.
	 * 
	 * @param px A value in px (pixels) unit. Which we need to convert into db
	 * @param context Context to get resources and device specific display metrics
	 * @return A float value to represent dp equivalent to px value
	 * Source: http://stackoverflow.com/questions/4605527/converting-pixels-to-dp-in-android
	 */
	public static float px2dp(Context context, float px){
	    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
	    float dp = px / (metrics.densityDpi);
	    return dp;
	}
	
	/**
	 * Configure Log4J to also log in LogCat
	 */
	public static void initialiseLogging() {
		final LogConfigurator logConfigurator = new LogConfigurator();

		logConfigurator.setFileName(Environment.getExternalStorageDirectory()
				+ "/evotingcircle.log");
		logConfigurator.setRootLevel(AndroidApplication.LEVEL);
		// max 3 rotated log files
		logConfigurator.setMaxBackupSize(3);
		// Max 500ko per file
		logConfigurator.setMaxFileSize(500000);
		logConfigurator.configure();
	}
}
