package mysfmovietour.movietour;

/*
 * LocationData 
 * 
 * Stores the location information for a movie
 */
import java.util.ArrayList;

public class LocationData {
	private float latitude;
	private float longitude;
	private String address;
	private String submitter;
	private String email;
	private ArrayList<String> comments;
	private String time;
	private String description;
	private String pic;
	
	public LocationData(String lat, String lon) {
		setLatitude(Float.parseFloat(lat));
		setLongitude(Float.parseFloat(lon));
		//setDescription(desc);
	}
	
	public void setLatitude(float lat) {
		this.latitude = lat;
	}
	
	public double getLatitude() {
	 return	this.latitude;
	}
	
	public void setLongitude(float lon) {
		this.longitude = lon;
	}
	
	public double getLongitude() {
	 return	this.longitude;
	}
	
	public void setDescription(String desc) {
		this.description = desc;
	}
	
	public String getDescription() {
	 return	this.description;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getSubmitter() {
		return submitter;
	}

	public void setSubmitter(String submitter) {
		this.submitter = submitter;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public ArrayList<String> getComments() {
		return comments;
	}

	public void setComments(ArrayList<String> comments) {
		this.comments = comments;
	}

	public void addComments(String comment) {
		this.comments.add(comment);
	}
	
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getPic() {
		return pic;
	}

	public void setPic(String pic) {
		this.pic = pic;
	}
	
}
