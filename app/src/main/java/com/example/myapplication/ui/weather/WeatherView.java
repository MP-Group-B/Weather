package com.example.myapplication.ui.weather;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class WeatherView extends ViewModel {

    private final MutableLiveData<String> mText;

    public WeatherView() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Weather fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}