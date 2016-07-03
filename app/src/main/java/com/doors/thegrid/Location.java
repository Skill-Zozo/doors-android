package com.doors.thegrid;
import java.util.LinkedList;


public class Location {
	private String nameOfPlace;
	private LinkedList<Room> rooms;
	private Profile owner;
	
	public Location(Profile owner, String name) {
		this.setOwner(owner);
		setNameOfPlace(name);
		rooms = new LinkedList<>();
	}

	public String getName() {
		return nameOfPlace;
	}

	private void setNameOfPlace(String nameOfPlace) {
		this.nameOfPlace = nameOfPlace;
	}
	
	public void addRoom(Room room) {
		rooms.add(room);
	}
	
	public void removeRoom(Room room) {
		if(rooms.contains(room))
			rooms.remove(room);
	}
	
	public LinkedList<Room> getRooms() {
		return this.rooms;
	}

	public Profile getOwner() {
		return owner;
	}

	public void setOwner(Profile owner) {
		this.owner = owner;
		owner.addLocation(this);
	}

	public boolean hasRoom(String place) {
		for(int i = 0; i < rooms.size(); i++ ) {
			Room room = rooms.get(i);
			if(room.getName().equalsIgnoreCase(place))
				return true;
		}
		return false;
	}
	
	public Room getRoom(String room) {
		for(int i = 0; i < rooms.size(); i++ ) {
			Room r = rooms.get(i);
			if(r.getName().equalsIgnoreCase(room))
				return r;
		}
		return null;
	}
	
	public String toString() {
		String fin = this.nameOfPlace;
		for(int i = 0; i < this.rooms.size(); i++){
			fin = fin + "\n\t" + this.rooms.get(i).toString();
		}
		return fin;
	}
}
