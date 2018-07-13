package zotzp.flygogo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserTripsActivity extends BaseActivity implements
        View.OnClickListener {

    private static final String TAG = "UserTrips";
    private static Globals g;
    private Button addTrip;
    private ListView userTripsView;
    private Query userTripsQuery;
    private ArrayList<Trip> tripsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_trips);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.menu);
        setSupportActionBar(myToolbar);

        g = Globals.getInstance();

        showProgressDialog("Loading your trips...");

        tripsList = new ArrayList<Trip>();
        final TripsAdapter adapter = new TripsAdapter(this, tripsList);

        // views
        userTripsView = (ListView) findViewById(R.id.userTripsView);
        userTripsView.setAdapter(adapter);
        addTrip = (Button) findViewById(R.id.addTripButton);
        addTrip.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent myIntent = new Intent(UserTripsActivity.this,
                        TripFormActivity.class);
                startActivity(myIntent);
            }
        });

        // get instance of globals class
        g = Globals.getInstance();

        // check for logged in user
        if (g.getUserId() == null) {
            Toast.makeText(UserTripsActivity.this, "Not logged in",
                    Toast.LENGTH_SHORT).show();
        }

        // query firebase db for trips with user field matching logged in user id
        userTripsQuery = g.getDatabaseReference().child("trips").orderByChild("user").equalTo(g.getUserId());

        // attach listener to query to update list of user's trips
        userTripsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot tripSnapshot: dataSnapshot.getChildren()) {
                    System.out.println(tripSnapshot.toString());

                    Trip trip = tripSnapshot.getValue(Trip.class);
                    g.putTrip(trip.id, trip);
                    adapter.notifyDataSetChanged();
                }

                // clear trips ArrayList and re-insert entire HashMap
                // (look for ways to optimize in the future)
                tripsList.clear();
                tripsList.addAll(g.getUserTrips().values());
                hideProgressDialog(); // hide loading dialog

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

    }

}
