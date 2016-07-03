package com.doors.thegrid;
import java.util.LinkedList;


public class Room {
	private Location place;
	private String roomName;
	private LinkedList<Device> devices;
	
	public Room (String room, Location place) {
		setRoomName(room);
		this.setPlace(place);
		devices = new LinkedList<>();
	}

	public String getName() {
		return roomName;
	}

	private void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public Location getPlace() {
		return place;
	}

	private void setPlace(Location place) {
		this.place = place;
		place.addRoom(this);
	}
	
	public void addDevice(Device d) {
		devices.add(d);
	}
	
	public void removeDevice(Device d) {
		if(devices.contains(d))
			devices.remove(d);
	}
	
	public LinkedList<Device> getDevices() {
		return this.devices;
	}

	public boolean hasDevice(String dev) {
		for(int i = 0; i < devices.size(); i++ ) {
			Device device = devices.get(i);
			if(device.getNameofDevice().equalsIgnoreCase(dev))
				return true;
		}
		return false;
	}
	
	public String toString() {
		String fin = this.roomName;
		for(int i = 0; i < this.devices.size(); i++) {
			fin = fin + "\n" + this.devices.get(i).toString();
		}
		return fin;
	}
}