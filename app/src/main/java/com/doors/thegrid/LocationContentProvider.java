package com.doors.thegrid;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.LinkedList;

/**
 * Created by banelematsebula on 5/15/16.
 */
public class LocationContentProvider extends BaseExpandableListAdapter {

    private Context context;
    final private String LOCATIONS = "LOCATIONS";
    private LinkedList<String> locations;

    public LocationContentProvider (Context context, LinkedList<String> locs) {
        this.context = context;
        this.locations = locs;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this.locations.get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.location_children, null);
        }

        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.location_children);
        txtListChild.setText(childText);
        return convertView;
    }

    private void linkToNewView(Context ctx, String text) {
        System.out.println(text);
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.locations.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.locations;
    }

    @Override
    public int getGroupCount() {
        return 1;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 1;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.location_header, null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.location_header);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
