package mysfmovietour.movietour;

/*
 * SFMapActivity
 *
 * Allows user to tour movie locations. 
 * Depending on the tour criteria selected by the user, all the relevant movie locations are displayed using markers on the map. 
 * Map bounds are calculated using GeoLocation.java and the markers are defined in CustomItemizedOverlay.java
 * 
 */
import com.google.android.maps.MapActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Toast;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class SFMapActivity extends MapActivity {

	private MapView mapView;
	ArrayList<GeoPoint> pointsList;

	private LocationManager locationManager;
	Geocoder geocoder;
	Location location;
	LocationListener locationListener;
	CountDownTimer locationtimer;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		boolean currLocTagged = false;
		GeoPoint lastPoint = null;
		pointsList = new ArrayList<GeoPoint>();
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);

		final List<Overlay> mapOverlays = mapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(R.drawable.cinema);
		final Drawable curLocDrawable = this.getResources().getDrawable(
				R.drawable.currlocation);
		final CustomItemizedOverlay curLocOverlay = new CustomItemizedOverlay(
				curLocDrawable, this, mapView, 0);

		final CustomItemizedOverlay itemizedOverlay = new CustomItemizedOverlay(
				drawable, this, mapView, 1);

		final MapController controller = mapView.getController();
		OverlayItem overlayitem;
		/*
		 * Get locations based on the selected movie
		 */
		if (SfmtourActivity.useMovie) {
			for (LocationData l : SfmtourActivity.selectedMovie.getLocations()) {

				int lat = (int) (l.getLatitude() * 1E6), lon = (int) (l
						.getLongitude() * 1E6);
				GeoPoint point = new GeoPoint(lat, lon);
				pointsList.add(point);
				lastPoint = point;
				String getTime = "-";
				String getDesc = "-";
				String locInfo = "";

				String templ = Double.toString(l.getLatitude());
				if (templ.length() > 4) {
					templ = templ.substring(0, 5);
				}
				String templn = Double.toString(l.getLongitude());
				if (templn.length() > 4) {
					templn = templn.substring(0, 5);
				}

				locInfo = "_" + templ + "_" + templn;
				if (l.getTime() != null && !l.getTime().equals("null")) {
					getTime = l.getTime();
				}
				if (l.getDescription() != null
						&& !l.getDescription().equals("null")) {
					getDesc = l.getDescription();
				}
				overlayitem = new OverlayItem(point,
						SfmtourActivity.selectedMovie.getTitle(), getTime + "%"
								+ getDesc + "%" + locInfo);

				itemizedOverlay.addOverlay(overlayitem);
				mapOverlays.add(itemizedOverlay);
			}

			int minLat = Integer.MAX_VALUE;
			int minLong = Integer.MAX_VALUE;
			int maxLat = Integer.MIN_VALUE;
			int maxLong = Integer.MIN_VALUE;

			for (GeoPoint point : pointsList) {
				minLat = Math.min(point.getLatitudeE6(), minLat);
				minLong = Math.min(point.getLongitudeE6(), minLong);
				maxLat = Math.max(point.getLatitudeE6(), maxLat);
				maxLong = Math.max(point.getLongitudeE6(), maxLong);
			}

			controller.zoomToSpan(Math.abs(minLat - maxLat),
					Math.abs(minLong - maxLong));
			controller.animateTo(new GeoPoint((maxLat + minLat) / 2,
					(maxLong + minLong) / 2));
		}
		/*
		 * Get locations based on the radius selected by the user
		 */
		else if (SfmtourActivity.useRadius) {
			locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			if (locationManager == null) {
				Toast.makeText(SFMapActivity.this,
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
				GeoPoint currPoint = new GeoPoint((int) (lat * 1E6),
						(int) (lng * 1E6));
				GeoLocation currentGeoLocation = GeoLocation.fromDegrees(lat,
						lng);

				for (Movie m : SfmtourActivity.movieData) {
					for (LocationData l : m.getLocations()) {

						int lati = (int) (l.getLatitude() * 1E6), lon = (int) (l
								.getLongitude() * 1E6);
						GeoPoint point = new GeoPoint(lati, lon);

						String getTime = "-";
						String getDesc = "-";
						String locInfo = "";
						String templ = Double.toString(l.getLatitude());
						if (templ.length() > 4) {
							templ = templ.substring(0, 5);
						}
						String templn = Double.toString(l.getLongitude());
						if (templn.length() > 4) {
							templn = templn.substring(0, 5);
						}
						locInfo = "_" + templ + "_" + templn;
						if (l.getTime() != null && !l.getTime().equals("null")) {
							getTime = l.getTime();
						}
						if (l.getDescription() != null
								&& !l.getDescription().equals("null")) {
							getDesc = l.getDescription();
						}
						overlayitem = new OverlayItem(point, m.getTitle(),
								getTime + "%" + getDesc + "%" + locInfo);

						itemizedOverlay.addOverlay(overlayitem);
						mapOverlays.add(itemizedOverlay);

						// check if current location is already tagged
						if ((l.getLatitude() == lat)
								&& (l.getLongitude() == lng)) {
							currLocTagged = true;
						}

					}
				}
				// Show current location
				if (!currLocTagged) {
					overlayitem = new OverlayItem(currPoint, "You are here!",
							"");

					curLocOverlay.addOverlay(overlayitem);
					mapOverlays.add(curLocOverlay);
					currLocTagged = true;

				}

				// Obtain the spanwidth in metres.
				double spanWidthInKM = (double) SfmtourActivity.radiusMiles / 1.609344;

				final double EARTH_RADIUS = 6371.01;
				// Create span
				GeoLocation[] boundingBoxSpan = currentGeoLocation
						.boundingCoordinates(spanWidthInKM, EARTH_RADIUS);

				// Create min/max values for final span calculation
				int minLatSpan = (int) (boundingBoxSpan[0]
						.getLatitudeInDegrees() * 1E6);
				int maxLatSpan = (int) (boundingBoxSpan[1]
						.getLatitudeInDegrees() * 1E6);
				int minLongSpan = (int) (boundingBoxSpan[0]
						.getLongitudeInDegrees() * 1E6);
				int maxLongSpan = (int) (boundingBoxSpan[1]
						.getLongitudeInDegrees() * 1E6);

				// Finally calculate span
				int latSpanE6 = Math.abs(minLatSpan - maxLatSpan);
				int lonSpanE6 = Math.abs(minLongSpan - maxLongSpan);

				// Set center
				controller.setCenter(currPoint);
				// controller.animateTo(currPoint);

				// Zoom to span
				controller.zoomToSpan(latSpanE6, lonSpanE6);

			}
			locationListener = new LocationListener() {

				public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
				}

				public void onProviderEnabled(String arg0) {
				}

				public void onProviderDisabled(String arg0) {
				}

				public void onLocationChanged(Location l) {
					location = l;
					if (l.getLatitude() == 0 || l.getLongitude() == 0) {
					} else {
						double lat = l.getLatitude();
						double lng = l.getLongitude();
						GeoPoint point = new GeoPoint((int) (lat * 1E6),
								(int) (lng * 1E6));

						curLocOverlay.clearOverlays();
						mapOverlays.remove(curLocOverlay);
						// show current location
						OverlayItem newoverlayitem = new OverlayItem(point,
								"You are here!", "");

						curLocOverlay.addOverlay(newoverlayitem);
						mapOverlays.add(curLocOverlay);
						controller.animateTo(point);

					}
				}
			};
			if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
				locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 1000, 10f,
						locationListener);
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 1000, 10f,
					locationListener);
			locationtimer = new CountDownTimer(30000, 5000) {
				@Override
				public void onTick(long millisUntilFinished) {
					if (location != null)
						locationtimer.cancel();
				}

				@Override
				public void onFinish() {
					if (location == null) {
					}
				}
			};
			locationtimer.start();

		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

}
