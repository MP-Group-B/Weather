package com.example.myapplication.ui.weather;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import org.json.JSONException;
import java.io.IOException;
import java.util.concurrent.Executors;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.Manifest;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.databinding.FragmentWeatherBinding;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.widget.Toast;
import java.util.List;
import java.util.Locale;



public class WeatherFragment extends Fragment {
    String values;
    String rainq,wind,rain,temperature,sky,windvec;
    String address="";
    private FragmentWeatherBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        WeatherView weatherView =
                new ViewModelProvider(this).get(WeatherView.class);

        binding = FragmentWeatherBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textWeather;
        weatherView.getText().observe(getViewLifecycleOwner(), textView::setText);
        final api at = new api(getActivity());

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    try {
                        values = at.func();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                }
            });
        try {
            at.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(values);
        String[] weatherarray = values.split(" ");
        rainq=weatherarray[0];
        wind=weatherarray[1];
        rain=weatherarray[2];
        temperature=weatherarray[3];
        sky=weatherarray[4];
        windvec=weatherarray[5];
        for(int i = 6; i < weatherarray.length; i++) {
            address = address+weatherarray[i]+(i<(weatherarray.length - 1)?" ":"");
        }

        /** Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    at.func();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }); **/
        return root;

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}