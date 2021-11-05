package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.myapplication.api.covid.CovidApi;
import com.example.myapplication.api.JsonReader;
import com.example.myapplication.api.covid.ProvinceLocationHashMap;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.slider.Slider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MapsFragment extends Fragment {

    AutocompleteSupportFragment autocompleteFragment;
    FrameLayout sliderLayout;
    Slider radiusSlider;
    Button searchButton;
    View resetCameraBtn;

    public final String BACKEND_URL = "https://544c-125-25-13-221.ngrok.io";
    final int DEFAULT_UNIT = 1;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            //default location
//            LatLng bangkok = new LatLng(13.7563, 100.5018);
//            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(bangkok, 6f));
            LatLngBounds thailandBounds = new LatLngBounds(
                    new LatLng(5.6130380, 97.3433960),
                    new LatLng(20.4651430, 105.6368120)
            );
            googleMap.setPadding(0, 150, 0, 0);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(thailandBounds, 0));
            googleMap.setPadding(0, 0, 0, 0);

            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker arg0) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    Context mContext = getContext();
                    LinearLayout info = new LinearLayout(mContext);
                    info.setOrientation(LinearLayout.VERTICAL);

                    TextView title = new TextView(mContext);
                    title.setTextColor(Color.BLACK);
                    title.setGravity(Gravity.CENTER);
                    title.setTypeface(null, Typeface.BOLD);
                    title.setText(marker.getTitle());

                    TextView snippet = new TextView(mContext);
                    snippet.setTextColor(Color.GRAY);
                    snippet.setText(marker.getSnippet());

                    info.addView(title);
                    info.addView(snippet);

                    return info;
                }
            });

            //Map each province
            try {
                mapProvinces(googleMap);
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }

            resetCameraBtn.setOnClickListener(view1 -> {
                googleMap.setPadding(0, 150, 0, 0);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(thailandBounds, 0));
                googleMap.setPadding(0, 0, 0, 0);
            });

            // Set up a PlaceSelectionListener to handle the response -- see this real!!!.
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                private static final String TAG = "auto-complete fragment";

                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    // TODO: Get info about the selected place.
                    LatLng latLng = place.getLatLng();
                    double lat = latLng.latitude;
                    double lon = latLng.longitude;
                    Log.i(TAG, "Place: " + place.getName() + ", " + place.getId() + place.getLatLng());
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(place.getViewport(), 0));
                }


                @Override
                public void onError(@NonNull Status status) {
                    // TODO: Handle the error.
                    Log.i(TAG, "An error occurred: " + status);
                }
            });

            //TODO: put timelines / cases into their own setup func.
            //On item selected listener for spinner
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    String choice = adapterView.getItemAtPosition(position).toString();
                    if (choice.equals("Timelines")) {
                        showMarkers(provincesMarkers, false);
                        sliderLayout.setVisibility(View.VISIBLE);
                        mapTimelines(googleMap);
                        searchButton.setVisibility(View.VISIBLE);
                        // TODO: put this in a func. call together with timelines setup
                        radiusSlider.addOnChangeListener(new Slider.OnChangeListener() {
                            @Override
                            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                                if (value <= 0) {
                                    return;
                                }
                                if (prevMarker != null && prevCircle == null) {
                                    CircleOptions circleOptions = new CircleOptions()
                                            .center(new LatLng(prevMarker.getPosition().latitude, prevMarker.getPosition().longitude))
                                            .radius(DEFAULT_UNIT * value) // In meters
                                            .fillColor(0x33FF0000)
                                            .strokeColor(Color.RED)
                                            .strokeWidth(0);

                                    prevCircle = googleMap.addCircle(circleOptions);
                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                            circleOptions.getCenter(), getZoomLevel(prevCircle)));

                                } else if (prevMarker != null && prevCircle != null) {
                                    prevCircle.setRadius(DEFAULT_UNIT * value);
                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                            prevCircle.getCenter(), getZoomLevel(prevCircle)));
                                }
                            }
                        });

                    } else {
                        showMarkers(provincesMarkers, true);
                        timelinesMarkers.clear();
                        if (prevMarker != null) {
                            prevMarker.remove();
                        }
                        radiusSlider.setValue(0);
                        if (prevCircle != null) {
                            prevCircle.remove();
                        }
                        googleMap.setOnMapClickListener(null);
                        searchButton.setVisibility(View.GONE);
                        sliderLayout.setVisibility(View.INVISIBLE);

                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapter) {
                }
            });

            //search button
            searchButton.setOnClickListener(new View.OnClickListener() {
                // verify + api call
                @Override
                public void onClick(View view) {
                    if (prevMarker != null && prevCircle != null && prevCircle.getRadius() > 0) {
                        double latitude = prevMarker.getPosition().latitude;
                        double longitude = prevMarker.getPosition().longitude;
                        double radius = prevCircle.getRadius() / DEFAULT_UNIT;
                        StringBuilder endpoint = new StringBuilder(BACKEND_URL).append("/api/timelines/?lat=").append(latitude)
                                .append("&lon=").append(longitude)
                                .append("&radius=").append(radius);
                        String URL = new String(endpoint);
                        Log.i("search", new String(endpoint));

                        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, URL, null, new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                for (int n = 0; n < response.length(); n++) {
                                    try {
                                        JSONObject Object = response.getJSONObject(n);
                                        double latitude = Object.getDouble("latitude");
                                        double longitude = Object.getDouble("longitude");
                                        LatLng latLng = new LatLng(latitude, longitude);
                                        BitmapDescriptor markerHue = getMarkerIcon("#b20000");
                                        timelinesMarkers.add(
                                                googleMap.addMarker(new MarkerOptions().icon(markerHue)
                                                        .position(latLng)
                                                ));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("Timelines",error.toString());
                            }
                        });
                        Volley.newRequestQueue(getContext()).add(jsonArrayRequest);

                    }
                }
            });
        }
    };

    public int getZoomLevel(Circle circle) {
        int zoomLevel = 11;
        if (circle != null) {
            double radius = circle.getRadius() + circle.getRadius() / 2;
            double scale = radius / 500;
            zoomLevel = (int) (16 - Math.log(scale) / Math.log(2));
        }
        return zoomLevel;
    }

    public BitmapDescriptor getMarkerIcon(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }

    List<Marker> provincesMarkers = new ArrayList<>();
    List<Marker> timelinesMarkers = new ArrayList<>();

    private void mapProvinces(GoogleMap googleMap) throws JSONException, IOException {

        new AsyncTask<String, Integer, Void>() {
            JSONArray json;

            @Override
            protected Void doInBackground(String... params) {
                try {
                    json = JsonReader.readJsonArrayFromUrl(CovidApi.TODAY_CASES_PROVINCES);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                HashMap<String, Double[]> provinceLocationMap = ProvinceLocationHashMap.getMap();
                for (int n = 0; n < json.length(); n++) {
                    try {
                        JSONObject Object = json.getJSONObject(n);
                        String province = Object.getString("province");
                        if (province.equals("ไม่ระบุ")) {
                            continue;
                        }
                        Double[] coordinates = provinceLocationMap.get(province);
                        LatLng provincePos = new LatLng(coordinates[0], coordinates[1]);

                        int new_case = Object.getInt("new_case");
                        int new_death = Object.getInt("new_death");
                        int total_case = Object.getInt("total_case");
                        int total_death = Object.getInt("total_death");
                        String update_date = Object.getString("update_date");


                        StringBuilder covidInfo = new StringBuilder(100);
                        covidInfo.append("New cases: ").append(new_case)
                                .append("\nNew deaths: ").append(new_death)
                                .append("\nTotal cases: ").append(total_case)
                                .append("\nTotal deaths: ").append(total_death);


                        BitmapDescriptor markerHue;
                        if (total_case > 100000) {
                            markerHue = getMarkerIcon("#b20000");
                        } else if (total_case > 50000) {
                            markerHue = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
                        } else if (total_case > 30000) {
                            markerHue = getMarkerIcon("#FF5733");
                        } else if (total_case > 10000) {
                            markerHue = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
                        } else if (total_case > 5000) {
                            markerHue = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
                        } else {
                            markerHue = getMarkerIcon("#ff2299");
                        }

                        provincesMarkers.add(
                                googleMap.addMarker(new MarkerOptions().icon(markerHue)
                                        .position(provincePos)
                                        .title(province)
                                        .snippet(new String(covidInfo)))
                        );
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();
    }

    Marker prevMarker;
    Circle prevCircle;

    public void mapTimelines(GoogleMap googleMap) {

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                if (prevMarker != null) {
                    prevMarker.remove();
                }
                radiusSlider.setValue(0);
                if (prevCircle != null) {
                    prevCircle.remove();
                    prevCircle = null;
                }
                prevMarker = googleMap.addMarker(new MarkerOptions().position(point));
            }
        });


    }


    public void showMarkers(List<Marker> markers, boolean bool) {
        for (Marker marker : markers) {
            marker.setVisible(bool);
        }
    }

    private Spinner spinner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_map, container, false);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        // Initialize the AutocompleteSupportFragment.
        Places.initialize(getActivity().getApplicationContext(), "AIzaSyAQDtDk9VFC_mTpq16k5PvTvSD-WHC7RLY");

        autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return. -->
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.VIEWPORT));

        //set location bounds -> Thailand
        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(
                new LatLng(15, 101),
                new LatLng(15, 101)
        ));

        autocompleteFragment.setCountry("TH");

        //spinner - drop down menu
        spinner = (Spinner) getActivity().findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.map_types, R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        //get slider layout
        sliderLayout = (FrameLayout) getActivity().findViewById(R.id.sliderLayout);
        sliderLayout.setVisibility(View.GONE);

        //get radius slider
        radiusSlider = getActivity().findViewById(R.id.radiusSlider);

        //reset camera btn;
        resetCameraBtn = getActivity().findViewById(R.id.resetCameraBtn);

        searchButton = getActivity().findViewById(R.id.searchButton);

        searchButton.setVisibility(View.INVISIBLE);


    }
}