package com.mohammadreza.weather;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;


public class TextViewWeather extends AppCompatTextView {


    public TextViewWeather(Context context) {
        super(context);
        init(context);
    }

    public TextViewWeather(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TextViewWeather(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


        init(context);

    }

    private void init(Context context) {

        Typeface weather_font = Typeface.createFromAsset(context.getAssets(), "fonts/weather_icons.ttf");
        setTypeface(weather_font);
    }

//    public void setWeatherIcon(int id, long sunrise, long sunset) {
//        String weatherIconCode = "";
//
//        if (id == 800) { //clear
//            long currentTime = new Date().getTime();
//            if (currentTime > sunrise && currentTime < sunset) {
//                weatherIconCode = getResources().getString(R.string.weather_sunny);
//                setTextColor(getContext().getResources().getColor(R.color.color_day_clear));
//            } else {
//                weatherIconCode = getResources().getString(R.string.weather_clear_night);
//                setTextColor(getContext().getResources().getColor(R.color.color_night_clear));
//            }
//        } else {
//            id = id / 100;
//            switch (id) {
//                case 2:
//                    weatherIconCode = getResources().getString(R.string.weather_thunder);
//                    break;
//                case 3:
//                    weatherIconCode = getResources().getString(R.string.weather_drizzle);
//                    break;
//                case 5:
//                    weatherIconCode = getResources().getString(R.string.weather_rainy);
//                    break;
//                case 6:
//                    weatherIconCode = getResources().getString(R.string.weather_snowy);
//                    break;
//                case 7:
//                    weatherIconCode = getResources().getString(R.string.weather_foggy);
//                    break;
//                case 8:
//                    weatherIconCode = getResources().getString(R.string.weather_cloudy);
//                    setTextColor(getContext().getResources().getColor(R.color.color_night_clear));
//                    break;
//                default:
//                    break;
//            }
//        }
//
//        setText(Html.fromHtml(weatherIconCode));
//
//    }


}
