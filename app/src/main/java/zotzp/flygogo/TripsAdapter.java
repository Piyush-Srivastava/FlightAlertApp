package zotzp.flygogo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

// adapter to attach trip data for trips listview
public class TripsAdapter extends ArrayAdapter<Trip> {



    public TripsAdapter(Context context, ArrayList<Trip> trips) {
        super(context, 0, trips);
    }

    private static class ViewHolder {
        TextView startAirportCode;
        TextView startAirportCity;
        TextView destinationAirportCode;
        TextView destinationAirportCity;
        TextView startAirportReturnCode;
        TextView startAirportReturnCity;
        TextView destinationAirportReturnCode;
        TextView destinationAirportReturnCity;
        TextView departDate;
        TextView returnDate;
        TextView userPrice;
        TextView lowPrice;
        TextView City;
        TextView tripTag;
        LinearLayout returnRow;
        ImageButton editTripBtn;
        ImageButton deleteTripBtn;
    }

    private Globals g;
    private Trip trip;
    private String tripKey;
    private String deleteTrip;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        trip = getItem(position);
        tripKey = trip.id;

        // get context - for handling navigation events in button clicks for this view
        final Context context = TripsAdapter.super.getContext();

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        g = Globals.getInstance();

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.trip_layout, parent, false);
            viewHolder.startAirportCode = (TextView) convertView.findViewById(R.id.startAirportCode);
            viewHolder.startAirportCity = (TextView) convertView.findViewById(R.id.startAirportCity);
            viewHolder.destinationAirportCode = (TextView) convertView.findViewById(R.id.destinationAirportCode);
            viewHolder.destinationAirportCity = (TextView) convertView.findViewById(R.id.destinationAirportCity);
            viewHolder.startAirportReturnCode = (TextView) convertView.findViewById(R.id.startAirportReturnCode);
            viewHolder.startAirportReturnCity = (TextView) convertView.findViewById(R.id.startAirportReturnCity);
            viewHolder.destinationAirportReturnCode = (TextView) convertView.findViewById(R.id.destinationAirportReturnCode);
            viewHolder.destinationAirportReturnCity = (TextView) convertView.findViewById(R.id.destinationAirportReturnCity);
            viewHolder.departDate = (TextView) convertView.findViewById(R.id.departDate);
            viewHolder.returnDate = (TextView) convertView.findViewById(R.id.returnDate);
            viewHolder.userPrice = (TextView) convertView.findViewById(R.id.userPrice);
            viewHolder.lowPrice = (TextView) convertView.findViewById(R.id.lowPrice);
            viewHolder.returnRow = (LinearLayout) convertView.findViewById(R.id.returnRow);
            viewHolder.City = (TextView) convertView.findViewById(R.id.tripName);
            viewHolder.tripTag = (TextView) convertView.findViewById(R.id.tripTag);
            viewHolder.editTripBtn = (ImageButton) convertView.findViewById(R.id.editTripBtn);
            viewHolder.deleteTripBtn = (ImageButton) convertView.findViewById(R.id.deleteTripBtn);

            // Cache the viewHolder object inside the fresh view
            convertView.setTag(viewHolder);

        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.City.setText(trip.name);
        viewHolder.tripTag.setText(trip.fromAirportName + " to " + trip.toAirportName);

        // Populate the data into the template view using the data object
        viewHolder.startAirportCode.setText(trip.fromAirportCode);
        viewHolder.startAirportCity.setText(trip.fromAirportName);
        viewHolder.destinationAirportCode.setText(trip.toAirportCode);
        viewHolder.destinationAirportCity.setText(trip.toAirportName);

        if (trip.roundTrip) {
            viewHolder.destinationAirportReturnCode.setText(trip.fromAirportCode);
            viewHolder.destinationAirportReturnCity.setText(trip.fromAirportName);
            viewHolder.startAirportReturnCode.setText(trip.toAirportCode);
            viewHolder.startAirportReturnCity.setText(trip.toAirportName);

        } else {
            viewHolder.returnRow.removeAllViews();
        }

        viewHolder.departDate.setText(trip.departDate);
        viewHolder.returnDate.setText(trip.returnDate);


        viewHolder.userPrice.setText(g.getUserCurrencySymbol() + trip.userPrice);

        // if price is zero, assume it hasn't been queried or fares haven't been found
        if (trip.currentPrice > 0) {
            viewHolder.lowPrice.setText(g.getUserCurrencySymbol() + trip.currentPrice);
            // set text to green if price has been found
            if (trip.currentPrice <= trip.userPrice) {
                viewHolder.lowPrice.setTextColor(ContextCompat.getColor(context, R.color.green_price));
            }
        } else {
            viewHolder.lowPrice.setText("Not yet found");
        }

        // click handler for edit button
        viewHolder.editTripBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // set this trip to selected in global static class to populate form after navigation
                g.setSelectedTrip(getItem(position)); // have to use getItem() here or it won't get correct trip (why?)
                context.startActivity(new Intent(context, TripFormActivity.class));

            }

        });

        // click handler for delete button
        viewHolder.deleteTripBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTrip = getItem(position).id; // set id of trip to be deleted for access in click listener
                builder.setMessage("Delete this trip?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
            }

        });

        // Return the completed view to render on screen
        return convertView;

    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    g.getDatabaseReference().child("trips").child(deleteTrip).removeValue();
                    g.getUserTrips().remove(deleteTrip); // remove from userTrips hashmap to remove from list
                    // check for empty hashmap and clear list if found
                    if (g.getUserTrips().isEmpty()) {
                        clear();
                    }

                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };








}

