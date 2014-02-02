package ch.bfh.evoting.voterapp.cgs97.util;

import java.lang.reflect.Field;

import ch.bfh.evoting.voterapp.cgs97.AndroidApplication;
import de.mindpipe.android.logging.log4j.LogConfigurator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Utility class 
 * @author Philémon von Bergen
 *
 */
public class Utility {
	
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

		logConfigurator.setFileName(AndroidApplication.getInstance().getFilesDir() + "/evotingcircle.log");
		logConfigurator.setRootLevel(AndroidApplication.LEVEL);
		
		// max 3 rotated log files
		logConfigurator.setMaxBackupSize(3);
		// Max 500ko per file
		logConfigurator.setMaxFileSize(500000);
		logConfigurator.configure();
	}
	
	/**
	 * 
	 * Hack to change the color of the separator and the title text in the dialog.
	 * Many thanks to David Wasser
	 * http://stackoverflow.com/questions/14770400/android-alertdialog-styling
	 * 
	 * @param alert dialog to modify
	 * @param color color to attribute to the title and the separator
	 */
	public static void setTextColor(DialogInterface alert, int color) {
	    try {
	        Class<?> c = alert.getClass();
	        Field mAlert = c.getDeclaredField("mAlert");
	        mAlert.setAccessible(true);
	        Object alertController = mAlert.get(alert);
	        c = alertController.getClass();
	        Field mTitleView = c.getDeclaredField("mTitleView");
	        mTitleView.setAccessible(true);
	        Object dialogTitle = mTitleView.get(alertController);
	        TextView dialogTitleView = (TextView)dialogTitle;
	        // Set text color on the title
	        dialogTitleView.setTextColor(color);
	        // To find the horizontal divider, first
	        //  get container around the Title
	        ViewGroup parent = (ViewGroup)dialogTitleView.getParent();
	        // Then get the container around that container
	        parent = (ViewGroup)parent.getParent();
	        for (int i = 0; i < parent.getChildCount(); i++) {
	            View v = parent.getChildAt(i);
	            if (v instanceof View) {
	            	if (v.getHeight() < 5){
	            		v.setBackgroundColor(color);
	            	}
	            }
	        }
	    } catch (Exception e) {
	        // Ignore any exceptions, either it works or it doesn't
	    }
	}
	
	public static IntentFilter[] getNFCIntentFilters() {
		IntentFilter nfcIntentFilter = new IntentFilter();
		nfcIntentFilter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
		nfcIntentFilter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
		return new IntentFilter[] { nfcIntentFilter };
	}
	
	public static int factorial(int n) {
        int fact = 1; // this  will be the result
        for (int i = 1; i <= n; i++) {
            fact *= i;
        }
        return fact;
    }
}
