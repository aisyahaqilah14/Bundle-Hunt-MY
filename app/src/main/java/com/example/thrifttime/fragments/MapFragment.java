package com.example.thrifttime.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thrifttime.R;
import com.example.thrifttime.adapters.ShopListAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private SearchView searchView;
    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentUserLocation;

    private RecyclerView rvShopList;
    private ShopListAdapter shopAdapter;
    private List<ShopListAdapter.ShopItem> shopItemsList;

    private static class MarkerData {
        String address;
        Double rating;
        Integer userRatingsTotal;
        Bitmap shopImage;

        public MarkerData(String address, Double rating, Integer userRatingsTotal) {
            this.address = address;
            this.rating = rating;
            this.userRatingsTotal = userRatingsTotal;
        }
    }

    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                if (fineLocationGranted != null && fineLocationGranted) {
                    if (googleMap != null) enableUserLocation();
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_maps_key));
        }
        placesClient = Places.createClient(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        mapView = view.findViewById(R.id.mapView);
        searchView = view.findViewById(R.id.searchView);
        rvShopList = view.findViewById(R.id.rvShopList);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        shopItemsList = new ArrayList<>();
        shopAdapter = new ShopListAdapter(shopItemsList, item -> {
            if (googleMap != null) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(item.latLng, 16f));
            }
        });

        rvShopList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvShopList.setAdapter(shopAdapter);

        setupSmartSearch();

        return view;
    }

    private void setupSmartSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (googleMap != null) googleMap.clear();
                shopItemsList.clear();
                shopAdapter.notifyDataSetChanged();
                rvShopList.setVisibility(View.GONE);

                performSearch(query);
                searchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) { return false; }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        // Listener to Navigate
        googleMap.setOnInfoWindowClickListener(marker -> {
            LatLng position = marker.getPosition();
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + position.latitude + "," + position.longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) startActivity(mapIntent);
        });

        LatLng malaysia = new LatLng(4.2105, 101.9758);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(malaysia, 6f));

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableUserLocation();
        } else {
            locationPermissionRequest.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
        }
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;
        googleMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                currentUserLocation = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 12f));
                performSearch("Thrift Store Bundle Shop");
            }
        });
    }

    private void performSearch(String query) {
        if (currentUserLocation == null) return;

        RectangularBounds biasBounds = RectangularBounds.newInstance(
                new LatLng(currentUserLocation.latitude - 0.2, currentUserLocation.longitude - 0.2),
                new LatLng(currentUserLocation.latitude + 0.2, currentUserLocation.longitude + 0.2)
        );

        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setCountries("MY")
                .setLocationBias(biasBounds)
                .setOrigin(currentUserLocation)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            List<AutocompletePrediction> predictions = response.getAutocompletePredictions();
            for (int i = 0; i < predictions.size(); i++) {
                boolean isRecommended = (i < 5);
                fetchPlaceAndPlot(predictions.get(i).getPlaceId(), isRecommended);
            }
        });
    }

    private void fetchPlaceAndPlot(String placeId, boolean isRecommended) {
        List<Place.Field> fields = Arrays.asList(
                Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS,
                Place.Field.RATING, Place.Field.USER_RATINGS_TOTAL, Place.Field.PHOTO_METADATAS
        );

        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, fields).build();

        placesClient.fetchPlace(request).addOnSuccessListener(response -> {
            Place place = response.getPlace();
            if (place.getLatLng() != null) {

                // Tap to nagivate marker
                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(place.getLatLng())
                        .title(place.getName())
                        .snippet("Tap to navigate 🚗") //
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                MarkerData markerData = new MarkerData(
                        place.getAddress(), place.getRating(), place.getUserRatingsTotal()
                );
                marker.setTag(markerData);

                // Add to List
                String ratingStr = (place.getRating() != null) ? place.getRating() + " ★" : "No Rating";

                ShopListAdapter.ShopItem listItem = new ShopListAdapter.ShopItem(
                        place.getName(),
                        place.getAddress(),
                        ratingStr,
                        null,
                        place.getLatLng(),
                        isRecommended
                );

                shopItemsList.add(listItem);

                Collections.sort(shopItemsList, new Comparator<ShopListAdapter.ShopItem>() {
                    @Override
                    public int compare(ShopListAdapter.ShopItem o1, ShopListAdapter.ShopItem o2) {
                        return o1.name.compareToIgnoreCase(o2.name);
                    }
                });

                shopAdapter.notifyDataSetChanged();

                if (rvShopList.getVisibility() == View.GONE) {
                    rvShopList.setVisibility(View.VISIBLE);
                }

                // Fetch Photo
                if (place.getPhotoMetadatas() != null && !place.getPhotoMetadatas().isEmpty()) {
                    PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);
                    FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                            .setMaxWidth(300).setMaxHeight(200).build();

                    placesClient.fetchPhoto(photoRequest).addOnSuccessListener(fetchPhotoResponse -> {
                        Bitmap bitmap = fetchPhotoResponse.getBitmap();

                        int index = shopItemsList.indexOf(listItem);
                        if (index != -1) {
                            listItem.image = bitmap;
                            shopAdapter.notifyItemChanged(index);
                        }
                    });
                }
            }
        });
    }

    @Override public void onResume() { super.onResume(); mapView.onResume(); }
    @Override public void onPause() { super.onPause(); mapView.onPause(); }
    @Override public void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
}