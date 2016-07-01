package mysfmovietour.movietour;

/*
 * Movie
 * 
 * Stores movie name and related location information
 */
import java.io.Serializable;
import java.util.ArrayList;

public class Movie implements Serializable{
	
	private String id;
	private String title;
	private ArrayList<LocationData> locations;
	
	public Movie() {
		this.locations = new ArrayList<LocationData>();		
	}
	public Movie(String name) {
		setTitle(name);
		this.locations = new ArrayList<LocationData>();		
	}
	
	public Movie(Movie copyObj) {
		this.title = copyObj.getTitle();
		this.locations = copyObj.getLocations();
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return this.id;
	}
	public void addLocation(LocationData loc) {
		this.locations.add(loc);
	}
	
	public ArrayList<LocationData> getLocations() {
		return this.locations;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	
	

}
