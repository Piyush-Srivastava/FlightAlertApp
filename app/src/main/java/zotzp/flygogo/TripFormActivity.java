package zotzp.flygogo;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class TripFormActivity extends BaseActivity  {

    // views
    private RadioButton roundTripBtn;
    private RadioButton oneWayBtn;
    private Button departBtn;
    private Button returnBtn;
    private Button addTripBtn;
    private TextView fromDate;
    private TextView toDate;
    private TextView fromAirportCode;
    private TextView toAirportCode;
    private EditText maxPrice;
    private AutoCompleteTextView fromAirport;
    private AutoCompleteTextView toAirport;
    private TextView selectedDateView;
    private TextView fromAirportNameView;
    private TextView toAirportNameView;
    private TextView tripNameView;
    private static Globals g = Globals.getInstance();
    private static final ArrayList<Airport> airports = g.getAirports();
    private String userId;
    private Trip selectedTrip;
    private String key;
    private String currencySymbol;
    private boolean edit;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_form);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.menu);
        setSupportActionBar(myToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // allow up navigation

        mDatabase = g.getDatabaseReference();
        userId = g.getUserId();
        selectedTrip = g.getSelectedTrip();
        if (selectedTrip != null) {
            edit = true;
        }

        currencySymbol = g.getUserCurrencySymbol();

        roundTripBtn = (RadioButton) findViewById(R.id.roundTripBtn);
        oneWayBtn = (RadioButton) findViewById(R.id.oneWayBtn);
        fromDate = (TextView) findViewById(R.id.fromDate);
        toDate = (TextView) findViewById(R.id.toDate);
        fromAirportCode = (TextView) findViewById(R.id.fromAirportCode);
        toAirportCode = (TextView) findViewById(R.id.toAirportCode);
        departBtn = (Button) findViewById(R.id.departBtn);
        returnBtn = (Button) findViewById(R.id.returnBtn);
        addTripBtn = (Button) findViewById(R.id.addTripBtn);
        maxPrice = (EditText) findViewById(R.id.maxPrice);
        fromAirport = (AutoCompleteTextView) findViewById(R.id.fromAirport);
        toAirport = (AutoCompleteTextView) findViewById(R.id.toAirport);
        tripNameView = (TextView) findViewById(R.id.tripName);

        fromAirportNameView = new TextView(this);
        toAirportNameView = new TextView(this);

        // set item click handler for airport selectors
        setOnItemClick(fromAirport, fromAirportCode, fromAirportNameView);
        setOnItemClick(toAirport, toAirportCode, toAirportNameView);

        // set click handlers for departure/return date buttons
        setOnClick(departBtn, fromDate);
        setOnClick(returnBtn, toDate);

        // if the user selected a trip to edit, get that here
        if (edit) {
            key = selectedTrip.id;
            roundTripBtn.setChecked(selectedTrip.roundTrip);
            oneWayBtn.setChecked(!selectedTrip.roundTrip);
            fromDate.setText(selectedTrip.departDate);
            toDate.setText(selectedTrip.returnDate);
            tripNameView.setText(selectedTrip.name);
            fromAirport.setText(selectedTrip.fromAirportName);
            toAirport.setText(selectedTrip.toAirportName);
            fromAirportCode.setText(selectedTrip.fromAirportCode);
            toAirportCode.setText(selectedTrip.toAirportCode);
            maxPrice.setText(currencySymbol + Integer.toString(selectedTrip.userPrice));
            addTripBtn.setText("Update Trip");

            // set selectedTrip to avoid information persisting if navigating away and back
            g.setSelectedTrip(null);


        }

        // add listener to prepend dollar sign to price field
        maxPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (maxPrice.getText().toString().startsWith(currencySymbol)) return;
                maxPrice.setText(currencySymbol + maxPrice.getText().toString());
                maxPrice.setSelection(maxPrice.getText().length());
            }
        });


        // create and set adapters for start and destination airport autocompletes
        // must be attached after setting text (if editing existing trip)
        ArrayAdapter<Airport> adapter = new ArrayAdapter<Airport>(
                this, android.R.layout.simple_dropdown_item_1line, airports);
        fromAirport.setAdapter(adapter);
        toAirport.setAdapter(adapter);

        // set click handler for add trip button
        addTripBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean error = false;
                String errorMsg = "";
                boolean roundTrip = true;
                if (oneWayBtn.isChecked()) {
                    roundTrip = false;
                }

                String fromCode = fromAirportCode.getText().toString();
                String toCode = toAirportCode.getText().toString();
                String departureDate = fromDate.getText().toString();
                String returnDate = toDate.getText().toString();
                String fromAirportName = fromAirportNameView.getText().toString();
                String toAirportName = toAirportNameView.getText().toString();
                String tripName = tripNameView.getText().toString();

                int price = 0;

                // start validation
                if (maxPrice.length() >= 2)
                    price = Integer.parseInt(maxPrice.getText().toString().substring(1));
                else {
                    errorMsg = "Please enter a valid price";
                    error = true;
                }

                if (fromCode == "---" || toCode == "---") {
                    errorMsg = "Please select valid starting & destination airports";
                    error = true;
                }

                if (error) {
                    Toast.makeText(TripFormActivity.this, errorMsg,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Trip thisTrip = new Trip();
                    thisTrip.roundTrip = roundTrip;
                    thisTrip.user = userId;
                    thisTrip.userPrice = price;
                    thisTrip.fromAirportCode = fromCode;
                    thisTrip.toAirportCode = toCode;
                    thisTrip.departDate = departureDate;
                    thisTrip.returnDate = returnDate;
                    thisTrip.fromAirportName = fromAirportName;
                    thisTrip.toAirportName = toAirportName;
                    thisTrip.name = tripName;
                    addTrip(thisTrip);

                }



            }

        });

    }

    // sets after text changed listener for airport selection fields and sets relevant airport code
    private void setOnItemClick(final AutoCompleteTextView acTextView, final TextView airportCode, final TextView airportName) {
        // clear text as soon as user clicks field
        acTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acTextView.setText("");
            }
        });

        acTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // get airport code and name by string parsing
                String airportString= acTextView.getText().toString();
                String code = airportString.substring(airportString.length() - 3);
                airportCode.setText(code);
                airportName.setText(airportString);
            }
        });
    }

    // sets onclick handler for date buttons and sets selected date view
    private void setOnClick(Button btn, final TextView dateView){
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDateView = dateView;
                Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(TripFormActivity.this,
                        new mDateSetListener(), mYear, mMonth, mDay);
                dialog.show();
            }
        });
    }

    // listener for depart/return datepickers
    class mDateSetListener implements DatePickerDialog.OnDateSetListener {

        @Override
        public void onDateSet(DatePicker view, int year, int month,
                              int day) {

            Date date = new Date (year - 1900, month, day);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dateString = sdf.format(date);
            selectedDateView.setText(dateString); // set in onclick listener


        }
    }

    // helper function for date validation (placeholder)
    private Date dateFromString(String date) {
        return new Date();
    }

    // adds trip and navigates up to user trips
    private void addTrip(Trip thisTrip) {
        if (!this.edit) {
            this.key = mDatabase.child("trips").push().getKey();
            thisTrip.id = key.toString();
        }

        mDatabase.child("trips").child(key).setValue(thisTrip);
        Intent myIntent = new Intent(TripFormActivity.this,
                UserTripsActivity.class);
        startActivity(myIntent);

    }

}

