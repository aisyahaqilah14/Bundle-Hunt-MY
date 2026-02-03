package com.example.thrifttime.activities;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentActivity;

import com.example.thrifttime.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapPickerActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SearchView searchView;
    private Button btnConfirm;
    private PlacesClient placesClient;

    private LatLng selectedLatLng;
    private String selectedLocationName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        //Initialize Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }
        placesClient = Places.createClient(this);

        searchView = findViewById(R.id.sv_location);
        btnConfirm = findViewById(R.id.btnConfirmLocation);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Search Listener using Places SDK
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchWithPlacesSDK(query);
                searchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) { return false; }
        });

        // Inside onCreate or wherever you handle the confirm button
        btnConfirm.setOnClickListener(v -> {
            if (selectedLatLng != null) {
                Intent intent = new Intent();
                intent.putExtra("selectedAddress", selectedLocationName); // Send address back
                intent.putExtra("lat", selectedLatLng.latitude);
                intent.putExtra("lng", selectedLatLng.longitude);
                setResult(RESULT_OK, intent);
                finish(); // Close activity and return to AddPost
            } else {
                Toast.makeText(this, "Please select a location first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        LatLng malaysia = new LatLng(4.2105, 101.9758);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(malaysia, 6));

        // Handle Shop Icon Clicks (POIs) - Best Accuracy
        mMap.setOnPoiClickListener(poi -> {
            placeMarker(poi.latLng, poi.name); // Returns "JBR Bundle"
        });

        // Handle Manual Pins
        mMap.setOnMapClickListener(latLng -> {
            String cleanName = getStreetAddress(latLng);
            placeMarker(latLng, cleanName);
        });
    }

    // SEARCH WITH PLACES SDK (Fixes "No Result") ---
    private void searchWithPlacesSDK(String query) {
        // Limit search to Malaysia ("MY")
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setCountries("MY")
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            List<AutocompletePrediction> predictions = response.getAutocompletePredictions();
            if (!predictions.isEmpty()) {
                // Get the top result (e.g., the most likely "JBR Bundle")
                AutocompletePrediction topResult = predictions.get(0);
                String placeId = topResult.getPlaceId();
                fetchPlaceDetails(placeId);
            } else {
                Toast.makeText(this, "Shop not found. Try a broader search.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(exception -> {
            Toast.makeText(this, "Search Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    // FETCH DETAILS
    private void fetchPlaceDetails(String placeId) {
        // We specifically ask for the NAME and LAT_LNG
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG);
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();

        placesClient.fetchPlace(request).addOnSuccessListener(response -> {
            Place place = response.getPlace();
            if (place.getLatLng() != null) {
                // place.getName() returns "JBR Bundle", NOT "3-2"
                placeMarker(place.getLatLng(), place.getName());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15f));
            }
        });
    }

    private void placeMarker(LatLng latLng, String title) {
        mMap.clear();
        selectedLatLng = latLng;
        selectedLocationName = title;

        mMap.addMarker(new MarkerOptions().position(latLng).title(title)).showInfoWindow();
        btnConfirm.setEnabled(true);
        btnConfirm.setText("Confirm: " + title);
    }

    //MANUAL PIN FALLBACK
    private String getStreetAddress(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                String feature = address.getFeatureName(); // e.g., "3-2"
                String street = address.getThoroughfare(); // e.g., "Jalan Plumbum 7"

                if (feature != null && feature.matches(".*\\d.*") && street != null) {
                    // It's a house number, return the street instead
                    return street;
                }

                // Otherwise return the best guess (Address Line 1 usually has the full info)
                return address.getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Pinned Location";
    }
}