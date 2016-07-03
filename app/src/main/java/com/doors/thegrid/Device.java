package com.doors.thegrid;
public class Device {

	private boolean on;
	private String nameofDevice;
	private Room parentRoom;
	
	public Device(String name,Room room ,boolean state) {
		setNameofDevice(name);
		setParentRoom(room);
		setOn(state);
	}

	public Device(String name,Room room ) {
		setNameofDevice(name);
		setParentRoom(room);
	}
	
	public String getNameofDevice() {
		return nameofDevice;
	}

	private void setNameofDevice(String nameofDevice) {
		this.nameofDevice = nameofDevice;
	}

	public Room getParentRoom() {
		return parentRoom;
	}

	private void setParentRoom(Room parentRoom) {
		this.parentRoom = parentRoom;
		parentRoom.addDevice(this);
	}

	public boolean isOn() {
		return on;
	}

	private void setOn(boolean state) {
		this.on = state;
	}
	
	public String toString() {
		return this.nameofDevice;
	}
}
