package mysfmovietour.movietour;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
/*
 * SfmtourActivity.java
 Main activity that is launched when the application starts. Handles the home screen, allows user to select TAG or TOUR.
 If user selects TAG, SFMapTagActivity is started.
 If user selects TOUR, the tour criteria are displayed. User selects the criteria and clicks 'GO' to start the SFMapActivity activity.
 The user can also launch the augmented reality tour by clicking on 'Switch to AR' button.
 AREngine project's main activity is launched to show the movie locations in augmented reality view.
 *
 */
import java.util.ArrayList;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;
public class MainActivity extends AppCompatActivity {
    public static ArrayList<Movie> movieData;
    public static Movie selectedMovie;
    public static ArrayList<String> taggedMovies;
    public static ArrayList<String> allMovies;
    public static ArrayList<Movie> allMovieObjects;
    public static RelativeLayout tourLayout;
    public static boolean useMovie = false;
    public static boolean useRadius = false;
    public static double radiusMiles;
    Spinner spinnerMovie, spinnerRadius;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tourLayout = (RelativeLayout) findViewById(R.id.tourlayout);
        tourLayout.setVisibility(View.GONE);
        //XMLFunctions myXml = new XMLFunctions();
        movieData = new ArrayList<>();//myXml.getMovies();
        allMovies = new ArrayList<>();;//myXml.getAllMovies(this);
        ArrayList<String> movieTitles = getMovieTitles();

        // Select Movie
        spinnerMovie = (Spinner) findViewById(R.id.spinner_selectmovie);
        spinnerMovie
                .setOnItemSelectedListener(new MovieOnItemSelectedListener());
        ArrayAdapter<String> adapterMovie = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, movieTitles);
        adapterMovie.insert("Select Movie", 0);
        adapterMovie
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMovie.setAdapter(adapterMovie);

        // Select Location Radius
        spinnerRadius = (Spinner) findViewById(R.id.spinner_radius);
        spinnerRadius
                .setOnItemSelectedListener(new RadiusOnItemSelectedListener());
        ArrayAdapter<CharSequence> adapterRadius = ArrayAdapter
                .createFromResource(this, R.array.select_locationradius,
                        android.R.layout.simple_spinner_item);
        adapterRadius
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRadius.setAdapter(adapterRadius);

        Button go = (Button) findViewById(R.id.button_go);
    }


    /*
     * Launches activity that allows user to tag a location to a movie
     */
    public void launchTag(View v) {
        Intent mapIntent = new Intent(getApplicationContext(),
                SFMapTagActivity.class);
        startActivity(mapIntent);

    }

    /*
     * Executed when user clicks on Tour button
     */
    public void onClick(View v) {

        if (!useRadius && !useMovie) {
            Toast.makeText(getApplicationContext(),
                    "Select either Movie or Location radius",
                    Toast.LENGTH_SHORT).show();

        } else if (useRadius && useMovie) {
            Toast.makeText(
                    getApplicationContext(),
                    "You have selected both. Showing all movies around current location",
                    Toast.LENGTH_SHORT).show();
            useMovie = false;
            Intent mapIntent = new Intent(getApplicationContext(),
                    SFMapActivity.class);
            startActivity(mapIntent);

        } else {
            Intent mapIntent = new Intent(getApplicationContext(),
                    SFMapActivity.class);
            startActivity(mapIntent);

        }

    }

    /*
     * Launches augmented reality view with all the movie locations
     */
    public void launchAR(View v) {
        LocalDataSource lds = new LocalDataSource();
        for (Movie m : SfmtourActivity.movieData) {
            for (LocationData l : m.getLocations()) {
                lds.setMarkers(l.getLatitude(), l.getLongitude(), m.getTitle());
            }
        }
        Intent mapIntent = new Intent(getApplicationContext(), Demo.class);
        startActivity(mapIntent);
    }

    /*
     * Display the UI that allows user to choose movie or radius for the tour
     */
    public void showTour(View v) {
        tourLayout.setVisibility(View.VISIBLE);
    }

    /*
     * Get the list of all movies in SF
     */
    public ArrayList<String> getMovieTitles() {
        ArrayList<String> titles = new ArrayList<String>();
        for (Movie m : movieData) {
            titles.add(m.getTitle());
        }

        return titles;

    }

    public class MovieOnItemSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                   long id) {

            String selectedTitle = parent.getItemAtPosition(pos).toString();
            // Get the movie Data from
            XMLFunctions myXml = new XMLFunctions();
            Movie m = myXml.getMovieData(selectedTitle);
            if (m != null) {
                selectedMovie = m;
                useMovie = true;

            }
        }

        public void onNothingSelected(AdapterView parent) {
            // Do nothing.
        }
    }

    public class RadiusOnItemSelectedListener implements OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                   long id) {

            XMLFunctions myXml = new XMLFunctions();
            useRadius = true;
            switch (pos) {
                case 0:
                    radiusMiles = 0;
                    useRadius = false;
                    break;
                case 1:
                    radiusMiles = 0.5;
                    break;
                case 2:
                    radiusMiles = 1;
                    break;
                case 3:
                    radiusMiles = 5;
                    break;
                case 4:
                    radiusMiles = 10;
                    break;
                default:
                    radiusMiles = 1;
                    break;
            }

        }

        public void onNothingSelected(AdapterView parent) {
            // Do nothing.
        }
    }

}
