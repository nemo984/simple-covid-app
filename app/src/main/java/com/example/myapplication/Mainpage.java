package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.app.Dialog;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings.Secure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
public class Mainpage extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{

    Dialog myDialog;
    EditText editText;
    TextView Lat;
    double lat,Long;
    private TextView dateText;
<<<<<<< Updated upstream
    private String android_id;
    String name, day1;
    ExpandableListView expandableListView1;
    ArrayList<String> list = new ArrayList<>();
    HashMap<String,ArrayList<String>> listC = new HashMap<>();
    MainAdapter adapter ;

=======
    Home home;
>>>>>>> Stashed changes
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainpage);
        home = new Home();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navi);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                home).commit();
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        android_id = Secure.getString(getApplicationContext().getContentResolver(),Secure.ANDROID_ID);
        View view = LayoutInflater.from(getApplication()).inflate(R.layout.fragment_search, null);
        Log.i("android_id", android_id);

        expandableListView1 = view.findViewById(R.id.exp1);
        for(int g=0; g<10;g++){
            list.add("Group"+g);

            ArrayList<String> arrayList = new ArrayList<>();

            for(int c=0; c<5;c++){
                arrayList.add("item"+c);
            }
            listC.put(list.get(g),arrayList);
        }
        adapter = new MainAdapter(list,listC);
        expandableListView1.setAdapter(adapter);
        myDialog = new Dialog(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100 && resultCode==RESULT_OK){
            Place place=Autocomplete.getPlaceFromIntent(data);
            editText.setText(place.getName());
            name = editText.getText().toString();
            Lat.setText(String.valueOf(place.getLatLng()));
            lat = place.getLatLng().latitude;
            Long = place.getLatLng().longitude;

        }
        else if(resultCode== AutocompleteActivity.RESULT_ERROR){
            Status status=Autocomplete.getStatusFromIntent(data);
            Toast.makeText(getApplicationContext(),status.getStatusMessage(),Toast.LENGTH_SHORT).show();

        }
    }

    private void showDate(){
        DatePickerDialog datePickerDialog=new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    public void ShowPopup(View v){
        TextView txtclose;
        //TextView txtdone;
        myDialog.setContentView(R.layout.pop_menu);
        txtclose =(TextView) myDialog.findViewById(R.id.textView3);
        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog.dismiss();
            }
        });
        Places.initialize(getApplicationContext(), "AIzaSyAQDtDk9VFC_mTpq16k5PvTvSD-WHC7RLY");
        editText = (EditText) myDialog.findViewById(R.id.edit_text);
        Lat = (TextView) myDialog.findViewById(R.id.LatLong);
        editText.setFocusable(false);
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Place.Field> fieldsList = Arrays.asList(Place.Field.ADDRESS,Place.Field.LAT_LNG, Place.Field.NAME);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldsList).build(Mainpage.this);
                startActivityForResult(intent, 100);
            }
        });

        dateText = (TextView) myDialog.findViewById(R.id.dateTextTest);
        myDialog.findViewById(R.id.date_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDate();
            }
        });

        myDialog.show();

        //txtdone = (TextView) myDialog.findViewById(R.id.done);
        /*txtdone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list.add("day:"+dateText.getText().toString());
                ArrayList<String> arrayList = new ArrayList<>();
                arrayList.add(editText.getText().toString());
                listC.put(list.get(0),arrayList);
                myDialog.dismiss();
            }
        });*/

    }

    public void saveData(View v){
        list.add("day:"+day1);
        ArrayList<String> arrayList1 = new ArrayList<>();
        arrayList1.add(name);
        listC.put(list.get(0),arrayList1);

        adapter = new MainAdapter(list,listC);
        expandableListView1.setAdapter(adapter);
        myDialog.dismiss();
    }




    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()){
                        case R.id.nav_home:
                            selectedFragment = home;
                            break;
                        case R.id.nav_map:
                            selectedFragment = new MapsFragment();
                            break;
                        case R.id.nav_info:
                            selectedFragment = new info();
                            break;
                        case R.id.nav_time:
                            selectedFragment = new search();
                            break;
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();

                    return true;
                }
            };

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        dateText = (TextView) myDialog.findViewById(R.id.dateTextTest);
        String date=day+"/"+month+"/"+year;
        day1 =day+"/"+month+"/"+year;
        dateText.setText(date);
    }

    public void call1(View v){
        Intent intent2 = new Intent(Intent.ACTION_DIAL);
        intent2.setData(Uri.parse("tel:1330"));
        startActivity(intent2);
    }

    public void call2(View v){
        Intent intent2 = new Intent(Intent.ACTION_DIAL);
        intent2.setData(Uri.parse("tel:1668"));
        startActivity(intent2);
    }

    public void call3(View v){
        Intent intent2 = new Intent(Intent.ACTION_DIAL);
        intent2.setData(Uri.parse("tel:1669"));
        startActivity(intent2);
    }
}