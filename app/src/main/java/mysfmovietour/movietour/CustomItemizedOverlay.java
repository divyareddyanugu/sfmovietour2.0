package mysfmovietour.movietour;

/*
 * CustomItemizedOverlay
 * 
 * Implements the custom balloon that is displayed when the user clicks on a map marker
 *  
 */
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class CustomItemizedOverlay extends ItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> mapOverlays = new ArrayList<OverlayItem>();

	private Context context;
	private View parentView;
	// Used to denote which type of balloon is to be constructed.
	// 0 - current location marker
	// 1 - map marker with movie tag information
	private int balloonType;

	/*
	 * Constructor
	 */
	public CustomItemizedOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}

	/*
	 * overloaded  constructor
	 */
	public CustomItemizedOverlay(Drawable defaultMarker, Context context,
			View v, int type) {
		this(defaultMarker);
		this.context = context;
		this.parentView = v;
		this.balloonType = type;
	}

	/*
	 * (non-Javadoc)
	 * @see com.google.android.maps.ItemizedOverlay#createItem(int)
	 */
	@Override
	protected OverlayItem createItem(int i) {
		return mapOverlays.get(i);
	}

	/*
	 * (non-Javadoc)
	 * @see com.google.android.maps.ItemizedOverlay#size()
	 */
	@Override
	public int size() {
		return mapOverlays.size();
	}

	/*
	 * (non-Javadoc)
	 * @see com.google.android.maps.ItemizedOverlay#onTap(int)
	 * 
	 * This function is called when the user taps on a marker in the map. A balloon is constructed with
	 * the movie tag information and related photo (if it exists)
	 */
	@Override
	protected boolean onTap(int index) {
		OverlayItem item = mapOverlays.get(index);

		if (balloonType == 1) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View pop = inflater.inflate(R.layout.balloon, null, false);
			final PopupWindow popupWindow = new PopupWindow(pop, 400, 500, true);

			ImageView b = (ImageView) pop.findViewById(R.id.closeballoon);
			TextView t = (TextView) pop.findViewById(R.id.tagtitle);

			t.setText(Html.fromHtml("<a href=\"http://www.imdb.com/find?q="
					+ item.getTitle() + "&s=1\">" + item.getTitle() + "</a> "));
			t.setMovementMethod(LinkMovementMethod.getInstance());

			TextView t1 = (TextView) pop.findViewById(R.id.tagtime);
			String locInfo = "";
			String[] desc = item.getSnippet().split("%");
			String time1 = "", descp = "";
			if (desc.length > 0) {
				time1 = desc[0];
				if (desc.length > 1)
					descp = desc[1];
				locInfo = desc[2];
			}
			t1.setText(time1);
			t1.setTextColor(Color.BLACK);
			TextView t2 = (TextView) pop.findViewById(R.id.tagdesc);

			t2.setText(descp);
			t2.setTextColor(Color.BLACK);
			TextView t4 = (TextView) pop.findViewById(R.id.tagtime1);

			t4.setTextColor(Color.BLACK);
			TextView t5 = (TextView) pop.findViewById(R.id.tagdesc1);

			t5.setTextColor(Color.BLACK);
			TextView t6 = (TextView) pop.findViewById(R.id.tagtitle1);

			t6.setTextColor(Color.BLACK);

			String movieId = getMovieId(item.getTitle());
			if (!movieId.equals("null") && movieId != null) {

				Bitmap bitmap = DownloadImage("http://solaropportunity.org/sfmtServer/img/"
						+ movieId + locInfo + ".jpg");
				ImageView img = (ImageView) pop.findViewById(R.id.pictaken);
				if (bitmap != null) {
					img.setImageBitmap(bitmap);
				} else {
					// Default Image
				}
			}
			b.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					popupWindow.dismiss();
				}
			});
			popupWindow.setBackgroundDrawable(new BitmapDrawable());
			popupWindow.setOutsideTouchable(true);
			popupWindow.showAtLocation(parentView, Gravity.CENTER, 0, 0);

		} else {
			AlertDialog.Builder dialog = new AlertDialog.Builder(context);
			dialog.setTitle(item.getTitle());
			dialog.setMessage(item.getSnippet());
			dialog.show();
		}
		return true;
	}

	/*
	 * Get the id of the movie with title 'name'
	 */
	protected String getMovieId(String name) {

		for (Movie m : SfmtourActivity.allMovieObjects) {
			if (m.getTitle().equals(name)) {
				return m.getId();
			}
		}
		return "null";

	}

	/*
	 * Add a marker to the map
	 */
	public void addOverlay(OverlayItem overlay) {
		mapOverlays.add(overlay);
		this.populate();
	}

	/*
	 * Clear all markers
	 */
	public void clearOverlays() {
		mapOverlays.clear();
	}

	/*
	 * Allows the app to connect to server
	 */
	private InputStream OpenHttpConnection(String urlString) throws IOException {
		InputStream in = null;
		int response = -1;

		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();

		if (!(conn instanceof HttpURLConnection))
			throw new IOException("Not an HTTP connection");

		try {
			HttpURLConnection httpConn = (HttpURLConnection) conn;
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			httpConn.connect();
			response = httpConn.getResponseCode();
			if (response == HttpURLConnection.HTTP_OK) {
				in = httpConn.getInputStream();
			}
		} catch (Exception ex) {
			throw new IOException("Error connecting");
		}
		return in;
	}

	/*
	 * Download the image from the server
	 */
	private Bitmap DownloadImage(String URL) {
		Bitmap bitmap = null;
		InputStream in = null;
		try {
			in = OpenHttpConnection(URL);
			if (in == null) {
				return null;
			}
			bitmap = BitmapFactory.decodeStream(in);
			in.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return bitmap;
	}

}