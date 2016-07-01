package mysfmovietour.movietour;

/*
 *   SFMapTagActivity
 *   
 *   Displays the map with current location marked using a marker.
 *   Provides UI to the user allowing him/her to enter tag information - movie title, time, description
 *   Allows user to take a photo that will be a part of the movie tag information for the current location

 */
import com.google.android.maps.MapActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class SFMapTagActivity extends MapActivity {

	private MapView mapView;
	ArrayList<GeoPoint> pointsList;

	private LocationManager locationManager;
	Geocoder geocoder;
	Location location;
	LocationListener locationListener;
	CountDownTimer locationtimer;
	Movie movieToAdd;
	Spinner spinnerMovieList;
	static String selMovie;
	GeoPoint currPoint;
	XMLFunctions objXml;

	private static final int CAMERA_REQUEST = 1888;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.maptag);

		pointsList = new ArrayList<GeoPoint>();
		mapView = (MapView) findViewById(R.id.map_tagview);
		mapView.setBuiltInZoomControls(true);

		final List<Overlay> mapOverlays = mapView.getOverlays();
		final Drawable curLocDrawable = this.getResources().getDrawable(
				R.drawable.currlocation);
		final CustomItemizedOverlay curLocOverlay = new CustomItemizedOverlay(
				curLocDrawable, this, mapView, 0);

		final MapController controller = mapView.getController();
		OverlayItem overlayitem;
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		if (locationManager == null) {
			Toast.makeText(SFMapTagActivity.this,
					"Location Manager Not Available", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		location = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (location == null)
			location = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (location != null) {
			double lat = location.getLatitude();
			double lng = location.getLongitude();
			currPoint = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
			overlayitem = new OverlayItem(currPoint, "You are here!", "");

			curLocOverlay.addOverlay(overlayitem);
			mapOverlays.add(curLocOverlay);
			controller.animateTo(currPoint);
			controller.setZoom(15);
		}

		objXml = new XMLFunctions();

		ArrayList<String> movieList = SfmtourActivity.allMovies;
		// Load the movie list in the spinner
		spinnerMovieList = (Spinner) findViewById(R.id.spinner_movielist);
		ArrayAdapter<String> adapterMovieList = new ArrayAdapter<String>(
				getApplicationContext(), android.R.layout.simple_spinner_item,
				movieList);

		adapterMovieList
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerMovieList.setAdapter(adapterMovieList);
		selMovie = spinnerMovieList.getSelectedItem().toString();

	}

	/*
	 * Launch the camera activity that allows user to capture and save the image
	 */
	public void TakePic(View v) {
		selMovie = spinnerMovieList.getSelectedItem().toString();
		Intent cameraIntent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(cameraIntent, CAMERA_REQUEST);
	}

/*
 * Get the id of the movie with title 'name'
 */
	protected Movie getMovieId(String name) {
		for (Movie m : SfmtourActivity.allMovieObjects) {
			if (m.getTitle().equals(name)) {
				return m;
			}
		}
		return null;

	}

	/*
	 * This is executed after the user captures a image using camera
	 * (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
			Bitmap photo = (Bitmap) data.getExtras().get("data");

			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpContext localContext = new BasicHttpContext();

				// here, change it to your php;

				HttpPost httpPost = new HttpPost(
						"http://solaropportunity.org/sfmtServer/SFServer");
				MultipartEntity entity = new MultipartEntity(
						HttpMultipartMode.BROWSER_COMPATIBLE);
				
				ByteArrayOutputStream bos = new ByteArrayOutputStream();

				// CompressFormat set up to JPG, you can change to PNG or
				// whatever you want;
				photo.compress(CompressFormat.JPEG, 100, bos);
				byte[] data1 = bos.toByteArray();

				Movie tempM = getMovieId(SFMapTagActivity.selMovie);
				String fileName = tempM.getId();
				if (fileName == null || fileName.equals("null")) {
					fileName = "null";
				} else {
					String templ = Double.toString(location.getLatitude());
					if (templ.length() > 4) {
						templ = templ.substring(0, 5);
					}
					String templn = Double.toString(location.getLongitude());
					if (templn.length() > 4) {
						templn = templn.substring(0, 5);
					}
					fileName = tempM.getId() + "_" + templ + "_" + templn;
				}

				entity.addPart("myImage" + ":" + fileName, new ByteArrayBody(
						data1, "temp.jpg"));

				httpPost.setEntity(entity);
				HttpResponse response = httpClient.execute(httpPost,
						localContext);
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(
								response.getEntity().getContent(), "UTF-8"));
				String sResponse = reader.readLine();

			} catch (Exception e) {

				Log.e("log_tag", "Error in http connection " + e.toString());

			}

		}
	}

	/*
	 * Save the tag information entered by the user
	 */
	public void saveTag(View v) {
		EditText time = (EditText) findViewById(R.id.txt_time);
		EditText desc = (EditText) findViewById(R.id.txt_description);
		String title = spinnerMovieList.getSelectedItem().toString();
		movieToAdd = new Movie(title);
		LocationData locToAdd = new LocationData(Double.toString(location
				.getLatitude()), Double.toString(location.getLongitude()));
		locToAdd.setDescription(desc.getText().toString());
		locToAdd.setTime(time.getText().toString());
		movieToAdd.addLocation(locToAdd);
		boolean result = objXml
				.addMovieTag(getApplicationContext(), movieToAdd);
		if (result) {

			Toast.makeText(getApplicationContext(), "Location Tagged!",
					Toast.LENGTH_SHORT).show();
		} else {

			Toast.makeText(getApplicationContext(),
					"Oops! Tag failed. Try Again.", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
