package com.doors.thegrid;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.LinkedList;

public class Profile implements Parcelable{
	private String name;
	private LinkedList<Location> locations;
	private LinkedList<Device> devices;
	
	public Profile(String name) {
		this.setName(name);
		locations = new LinkedList<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void addLocation(Location loc) {
		locations.add(loc);
	}
	
	public void removeLocation(Location loc) {
		if(locations.contains(loc)) 
			locations.remove(loc);
	}
	
	public LinkedList<Location> getLocations() {
		return this.locations;
	}
	
	public boolean hasLocations() {
		return !locations.isEmpty();
	}
	
	public LinkedList<Device> getDevices() {
		for(int i = 0; i < locations.size(); i++) {
			Location loc = locations.get(i);
			for(int j = 0; j < loc.getRooms().size(); j++) {
				Room room = loc.getRooms().get(j);
				devices.addAll(room.getDevices());
			}
		}
		return devices;
	}

	public boolean hasLocation(String place) {
		for(int i = 0; i < locations.size(); i++ ) {
			Location loc = locations.get(i);
			if(loc.getName().equalsIgnoreCase(place))
				return true;
		}
		return false;
	}
	
	public Location getLocation(String place) {
		for(int i = 0; i < locations.size(); i++ ) {
			Location loc = locations.get(i);
			if(loc.getName().equalsIgnoreCase(place))
				return loc;
		}
		return null;
	}
	
	public String toString() {
		String fin = this.name;
		for(int i = 0; i < this.locations.size(); i++) {
			fin = fin  + "\n\t" + this.locations.get(i).toString();
		}
		return fin;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeValue(this);
	}
}
