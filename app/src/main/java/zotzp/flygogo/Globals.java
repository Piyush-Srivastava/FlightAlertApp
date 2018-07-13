package zotzp.flygogo;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;


public class Globals{
    private static Globals instance;
    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private ArrayList<Airport> airports;
    private Trip selectedTrip;
    private FirebaseAuth mAuth;
    private HashMap<String, Trip> userTrips; // hashmap of user trips
    private Globals() { }

    public DatabaseReference getDatabaseReference() {
        if (this.mDatabase == null) {
            this.mDatabase = FirebaseDatabase.getInstance().getReference();
        }

        return this.mDatabase;
    }

    public String getUserId() {
        if (this.user == null) {
            this.mAuth = FirebaseAuth.getInstance();
            this.user = mAuth.getCurrentUser();
        }

        return this.user.getUid();
    }

    // sets ArrayList of airports with data pulled from db
    public void setAirports(){
        if (airports == null)  {
            airports = new ArrayList<Airport>();
        }

        ValueEventListener airportListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snap: dataSnapshot.getChildren()) {
                    Airport airport = snap.getValue(Airport.class);
                    if (airport.code != null && airport.name != null) {
                        airports.add(airport);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //
            }
        };

        if (this.mDatabase == null) {
            this.mDatabase = FirebaseDatabase.getInstance().getReference();
        }

        this.mDatabase.child("airports").addListenerForSingleValueEvent(airportListener);
    }

    // get airports ArrayList, initializing list if necessary
    public ArrayList<Airport> getAirports () {
        if (this.airports == null) {
            this.setAirports();
        }

        return this.airports;
    }

    public void setSelectedTrip(Trip selectedTrip) {
        this.selectedTrip = selectedTrip;
    }

    public Trip getSelectedTrip() {
        return this.selectedTrip;
    }

    // placeholder until user settings are implemented
    public String getUserCurrencySymbol() {
        return "$";
    }

    public HashMap<String, Trip> getUserTrips() {
        if (this.userTrips == null) {
            this.userTrips = new HashMap<String, Trip>();
        }

        return this.userTrips;
    }

    // inserts trip into userTrips hashmap at given key
    public void putTrip(String key, Trip trip) {
        // check for new low price and send push notification here
        if (trip.notifyUser && trip.userPriceMet) {
            // user hasn't been notified that price is new - make push notification here
            trip.notifyUser = false;
            // notifyUser set to true again in backend if price goes above user price then back down
        }

        this.getUserTrips().put(key, trip);
    }

    // removes trip at given key from userTrips hashmap
    public void deleteTrip(String key) {
        this.getUserTrips().remove(key);
    }

    // sign out of firebase auth and reset Globals
    public void logOut() {
        this.mAuth.signOut();
        setInstance();
    }

    public static synchronized Globals getInstance(){
        if(instance==null){
            instance=new Globals();
        }
        return instance;
    }

    public static synchronized void setInstance(){
        instance = null;
    }
}