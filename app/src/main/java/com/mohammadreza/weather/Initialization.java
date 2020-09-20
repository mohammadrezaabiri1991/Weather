package com.mohammadreza.weather;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mohammadreza.weather.constant.WeatherConstant;
import com.mohammadreza.weather.ui.WeatherMainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static com.mohammadreza.weather.ui.AddCityDialog.dialog;
import static com.mohammadreza.weather.ui.AddCityDialog.dialogLoadWeather;


public class Initialization {


    private static String weatherImage = "";
    private static String strImageName = "";


    public static void getCurrentDayData(JSONObject response, AppCompatActivity appCompatActivity, String latLng, String cityName, TextView txtCityName,
                                         TextView txtWeatherState, TextView txtCityTemp, ImageView imgWeatherImage, ImageView imgWindSpeed,
                                         TextView txtWindSpeed, TextView txtPrecipitationChance, TextView txtHumidity, LinearLayout linear_loading) throws JSONException {


        JSONObject currently = response.getJSONObject(WeatherConstant.CURRENTLY);


        if (cityName != null && !cityName.isEmpty()) {
            txtCityName.setText(cityName);
        }


        String summary = currently.getString(WeatherConstant.SUMMARY);
        txtWeatherState.setText(summary);

        linear_loading.setVisibility(View.GONE);

        String temp = String.valueOf(convertFahrenheitToCelsius(currently.getDouble(WeatherConstant.TEMPT)));
        txtCityTemp.setText(temp);

        String icon = currently.getString(WeatherConstant.ICON);


        double windSpeed = Double.parseDouble(currently.getString(WeatherConstant.ST_WIND_SPEED));

        String strWindSpeed = "ic_wind_slow";

        if (windSpeed <= 25) {
            imgWindSpeed.setImageResource(R.drawable.ic_wind_slow);
            strWindSpeed = "ic_wind_slow";

        } else if (windSpeed > 26 && windSpeed <= 75) {
            imgWindSpeed.setImageResource(R.drawable.ic_wind_speed_3);
            strWindSpeed = "ic_wind_speed_3";

        } else {
            imgWindSpeed.setImageResource(R.drawable.ic_tornado);
            strWindSpeed = "ic_tornado";
        }

        double precipProbability = Double.parseDouble(currently.getString("precipProbability"));
        double humidity = Double.parseDouble(currently.getString("humidity"));


        String strPrecipProbability = "0";
        if (precipProbability != 1.0) {
            String[] separated = String.valueOf(precipProbability).split("0.");
            try {
                strPrecipProbability = separated[1];
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }

        } else {
            strPrecipProbability = "100";
        }


        String strHumidity = "0";
        if (humidity != 1.0) {
            String[] separated = String.valueOf(humidity).split("0.");
            try {
                strHumidity = separated[1];
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        } else {
            strHumidity = "100";
        }


        txtWindSpeed.setText(String.valueOf(windSpeed));
        txtPrecipitationChance.setText(strPrecipProbability);
        txtHumidity.setText(strHumidity);

        JSONObject daily = response.getJSONObject("daily").getJSONArray("data").getJSONObject(0);

        long currentTime = currently.getLong("time");

        long sunrise = daily.getLong(WeatherConstant.SUN_RISE);
        long sunset = daily.getLong(WeatherConstant.SUN_SET);

        boolean isDay = isDay(currentTime, sunrise, sunset);

        if (icon.equals(appCompatActivity.getString(R.string.clearDay))) {
            strImageName = "day_sunny";
            saveStatusState(appCompatActivity, R.color.color_day_clear);


        } else if (icon.equals(appCompatActivity.getString(R.string.clearNight))) {

            strImageName = "night_clear";

            saveStatusState(appCompatActivity, R.color.color_night_clear);

        } else if (icon.equals(appCompatActivity.getString(R.string.cloudy))) {
            if (isDay) {
                strImageName = "day_cloudy";
                saveStatusState(appCompatActivity, R.color.color_day_cloudy);

            } else {
                strImageName = "night_cloudy";
                saveStatusState(appCompatActivity, R.color.color_night_cloudy);
            }
        } else if (icon.equals(appCompatActivity.getString(R.string.rainy))) {
            if (isDay) {
                strImageName = "day_rain";
                saveStatusState(appCompatActivity, R.color.color_day_rainy);


            } else {
                strImageName = "night_rainy";
                saveStatusState(appCompatActivity, R.color.color_night_rainy);

            }

        } else if (icon.equals(appCompatActivity.getString(R.string.partlyCloudyDay))) {
            strImageName = "day_part_cloud";
            saveStatusState(appCompatActivity, R.color.color_day_part_cloud);


        } else if (icon.equals(appCompatActivity.getString(R.string.partlyCloudyNight))) {
            strImageName = "night_part_cloud";
            saveStatusState(appCompatActivity, R.color.color_night_part_cloud);

        } else if (icon.equals(appCompatActivity.getString(R.string.snowy))) {
            if (isDay) {
                strImageName = "day_snow";
                saveStatusState(appCompatActivity, R.color.color_day_snow);

            } else {
                strImageName = "night_snow";
                saveStatusState(appCompatActivity, R.color.color_night_snow);

            }

        } else if (icon.equals(appCompatActivity.getString(R.string.fog))) {
            strImageName = "day_foggy";
            saveStatusState(appCompatActivity, R.color.color_foggy);

        } else if (icon.equals(appCompatActivity.getString(R.string.sleety))) {
            strImageName = "sleet";
            saveStatusState(appCompatActivity, R.color.color_sleet);

        } else if (icon.equals(appCompatActivity.getString(R.string.wind))) {

            if (isDay) {
                strImageName = "day_wind";
                saveStatusState(appCompatActivity, R.color.color_day_wind);

            } else {
                strImageName = "night_wind";
                saveStatusState(appCompatActivity, R.color.color_night_wind);

            }


        } else if (icon.equals(appCompatActivity.getString(R.string.hail))) {
            strImageName = "hail";
            saveStatusState(appCompatActivity, R.color.color_sleet);


        } else if (icon.equals(appCompatActivity.getString(R.string.thunderstorm))) {
            strImageName = WeatherConstant.THUNDER_STORM;
            saveStatusState(appCompatActivity, R.color.color_thunderstorm);


        }

        imgWeatherImage.setImageResource(getImageId(appCompatActivity, strImageName));
//        imgWeatherImage.setImageResource(R.mipmap.day_sunny);

        saveCurrentDayResultToPreferences(appCompatActivity, latLng, cityName, summary, temp, strImageName, windSpeed, strPrecipProbability, strHumidity, strWindSpeed);


    }

    //    private static int getImageId(Context context, String imageName) {
//        return context.getResources().getIdentifier(WeatherConstant.STR_DRAWABLE + imageName, null, context.getPackageName());
//    }
    private static int getImageId(Context context, String imageName) {
        return context.getResources().getIdentifier(WeatherConstant.STR_MIPMAP + imageName, null, context.getPackageName());
    }

//    private static void setRandomImage(String imageName, int[] images, ImageView imgWeatherImage) {
//        Random random = new Random();
//        int rand = random.nextInt(images.length);
//        imgWeatherImage.setImageResource(images[rand]);
//        strImageName = imageName + (rand + 1);
//
//    }

    private static boolean isDay(long currentTime, long sunrise, long sunset) {
        return currentTime <= sunset && currentTime >= sunrise;

    }


    public static void calculateWeatherStateForecast(JSONObject response, TextView txtDay, TextView txtTemp, TextView txtIcon, int index, AppCompatActivity appCompatActivity) throws JSONException {

        JSONObject daily = response.getJSONObject(appCompatActivity.getString(R.string.daily)).getJSONArray("data").getJSONObject(index);

        if (dialog != null && dialogLoadWeather != null) {
            dialogLoadWeather.dismiss();
            dialog.dismiss();
        }


        String day = getDateFromTimSpam(daily.getLong("time"));
        txtDay.setText(day);

        double temperatureHigh = daily.getDouble("temperatureHigh");
        double temperatureLow = daily.getDouble("temperatureLow");


        int tempHigh = convertFahrenheitToCelsius((temperatureHigh));
        int tempLow = convertFahrenheitToCelsius((temperatureLow));

        String temp = tempHigh + "  " + tempLow;

        txtTemp.setText(temp);

        String icon = daily.getString("icon");
        double windSpeed = getKilometer(daily.getDouble("windGust"));

        double precipProbability = daily.getDouble("precipProbability");


        String precipType = "";
        if (precipProbability >= 50) {
            try {
                precipType = daily.getString("precipType");
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
        if (icon.equals(appCompatActivity.getString(R.string.clearDay))) {

            if (windSpeed <= 12) {
                weatherImage = appCompatActivity.getString(R.string.wi_day_sunny);
                txtIcon.setText(weatherImage);


            } else if (windSpeed > 12 && windSpeed <= 30) {

                weatherImage = appCompatActivity.getString(R.string.wi_day_light_wind);
                txtIcon.setText(weatherImage);

            } else if (windSpeed > 30 && windSpeed <= 50) {
                weatherImage = appCompatActivity.getString(R.string.wi_day_windy);
                txtIcon.setText(weatherImage);


            } else if (windSpeed > 50 && windSpeed <= 85) {
                weatherImage = appCompatActivity.getString(R.string.wi_strong_wind);
                txtIcon.setText(weatherImage);

            } else if (windSpeed > 85) {
                weatherImage = appCompatActivity.getString(R.string.wi_tornado);
                txtIcon.setText(weatherImage);

            }


        } else if (icon.equals(appCompatActivity.getString(R.string.partlyCloudyDay))) {
            if (windSpeed <= 15) {
                if (precipProbability < 50) {
                    weatherImage = appCompatActivity.getString(R.string.wi_day_cloudy);
                    txtIcon.setText(weatherImage);

                } else if (precipProbability >= 50) {
                    if (precipType.equals(appCompatActivity.getString(R.string.rainy))) {
                        weatherImage = appCompatActivity.getString(R.string.wi_day_rain);
                        txtIcon.setText(weatherImage);


                    } else if (precipType.equals(appCompatActivity.getString(R.string.snowy))) {
                        weatherImage = appCompatActivity.getString(R.string.wi_day_snow);
                        txtIcon.setText(weatherImage);

                    } else if (precipType.equals(appCompatActivity.getString(R.string.sleety))) {
                        weatherImage = appCompatActivity.getString(R.string.wi_day_sleet);
                        txtIcon.setText(weatherImage);

                    } else if (precipType.equals("")) {
                        weatherImage = appCompatActivity.getString(R.string.wi_forecast_io_partly_cloudy_day);
                        txtIcon.setText(weatherImage);
                    }
                }
            } else if (windSpeed > 15 && windSpeed <= 40) {
                if (precipProbability < 50) {
                    weatherImage = appCompatActivity.getString(R.string.wi_day_cloudy_windy);
                    txtIcon.setText(weatherImage);

                } else if (precipProbability >= 50) {
                    if (precipType.equals(appCompatActivity.getString(R.string.rainy))) {
                        weatherImage = appCompatActivity.getString(R.string.wi_day_rain_wind);
                        txtIcon.setText(weatherImage);


                    } else if (precipType.equals(appCompatActivity.getString(R.string.snowy))) {
                        weatherImage = appCompatActivity.getString(R.string.wi_day_snow_wind);
                        txtIcon.setText(weatherImage);


                    } else if (precipType.equals(appCompatActivity.getString(R.string.sleety))) {
                        weatherImage = appCompatActivity.getString(R.string.wi_day_sleet);
                        txtIcon.setText(weatherImage);

                    } else if (precipType.equals("")) {
                        weatherImage = appCompatActivity.getString(R.string.wi_forecast_io_partly_cloudy_day);
                        txtIcon.setText(weatherImage);
                    }
                }
            } else if (windSpeed > 40) {
                if (precipProbability <= 50) {
                    weatherImage = appCompatActivity.getString(R.string.wi_day_cloudy_gusts);
                    txtIcon.setText(weatherImage);

                } else if (precipProbability > 50) {
                    if (precipType.equals(appCompatActivity.getString(R.string.rainy))) {
                        weatherImage = appCompatActivity.getString(R.string.wi_day_rain_wind);
                        txtIcon.setText(weatherImage);


                    } else if (precipType.equals(appCompatActivity.getString(R.string.snowy))) {
                        weatherImage = appCompatActivity.getString(R.string.wi_day_snow_thunderstorm);
                        txtIcon.setText(weatherImage);


                    } else if (precipType.equals(appCompatActivity.getString(R.string.sleety))) {
                        weatherImage = appCompatActivity.getString(R.string.wi_day_sleet_storm);
                        txtIcon.setText(weatherImage);

                    } else if (precipType.equals("")) {
                        weatherImage = appCompatActivity.getString(R.string.wi_forecast_io_partly_cloudy_day);
                        txtIcon.setText(weatherImage);
                    }


                }
            }
        } else if (icon.equals(appCompatActivity.getString(R.string.cloudy))) {
            if (windSpeed <= 12) {
                weatherImage = appCompatActivity.getString(R.string.wi_cloudy);
                txtIcon.setText(weatherImage);


            } else if (windSpeed > 12 && windSpeed <= 40) {
                weatherImage = appCompatActivity.getString(R.string.wi_cloudy_windy);
                txtIcon.setText(weatherImage);


            } else if (windSpeed > 40 && windSpeed <= 80) {
                weatherImage = appCompatActivity.getString(R.string.wi_cloudy_windy);
                txtIcon.setText(weatherImage);


            } else if (windSpeed > 81) {
                weatherImage = appCompatActivity.getString(R.string.wi_tornado);
                txtIcon.setText(weatherImage);

            }

        } else if (icon.equals(appCompatActivity.getString(R.string.wind))) {
            if (windSpeed <= 12) {
                weatherImage = appCompatActivity.getString(R.string.wi_day_light_wind);
                txtIcon.setText(weatherImage);


            } else if (windSpeed > 12 && windSpeed <= 40) {
                weatherImage = appCompatActivity.getString(R.string.wi_day_windy);
                txtIcon.setText(weatherImage);


            } else if (windSpeed > 40 && windSpeed <= 80) {
                weatherImage = appCompatActivity.getString(R.string.wi_strong_wind);
                txtIcon.setText(weatherImage);


            } else if (windSpeed > 81) {
                weatherImage = appCompatActivity.getString(R.string.wi_tornado);
                txtIcon.setText(weatherImage);

            }

        } else if (icon.equals(appCompatActivity.getString(R.string.rainy))) {
            if (windSpeed <= 20) {
                weatherImage = appCompatActivity.getString(R.string.wi_rain);
                txtIcon.setText(weatherImage);


            } else if (windSpeed > 20) {
                weatherImage = appCompatActivity.getString(R.string.wi_rain_wind);
                txtIcon.setText(weatherImage);

            }
        } else if (icon.equals(appCompatActivity.getString(R.string.snowy))) {
            if (windSpeed <= 20) {
                weatherImage = appCompatActivity.getString(R.string.wi_snow);
                txtIcon.setText(weatherImage);


            } else if (windSpeed > 20) {
                weatherImage = appCompatActivity.getString(R.string.wi_snow_wind);
                txtIcon.setText(weatherImage);

            }
        } else if (icon.equals(appCompatActivity.getString(R.string.fog))) {
            weatherImage = appCompatActivity.getString(R.string.wi_fog);
            txtIcon.setText(weatherImage);

        }
        ((WeatherMainActivity) appCompatActivity).getStatusColor();

        saveForeCastResultToPreferences(appCompatActivity, index, day, temp, weatherImage);

    }


//    public void ImageViewAnimatedChange(final int i, Context context, final ImageView imgWeatherImage) {
//        final Animation anim_out = AnimationUtils.loadAnimation(context, android.R.anim.slide_out_right);
//        final Animation anim_in = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
//        anim_out.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                imgWeatherImage.setImageResource(i);
//                anim_in.setAnimationListener(new Animation.AnimationListener() {
//                    @Override
//                    public void onAnimationStart(Animation animation) {
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animation animation) {
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animation animation) {
//                    }
//                });
//                imgWeatherImage.startAnimation(anim_in);
//            }
//        });
//        imgWeatherImage.startAnimation(anim_out);
//    }


    public static String getDateFromTimSpam(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time * 1000);
        return cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());


    }

//    public static String getHourFromTimSpam(long time) {
//        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
//        cal.setTimeInMillis(time * 1000);
//        return cal.getDisplayName(Calendar.HOUR_OF_DAY, Calendar.LONG, Locale.getDefault());
//
//
//    }


    // Converts to Celsius
    public static int convertFahrenheitToCelsius(double fahrenheit) {
        return (int) ((fahrenheit - 32) * 5 / 9);
    }

    // Converts to fahrenheit
//    public static float convertCelsiusToFahrenheit(float celsius) {
//        return ((celsius * 9) / 5) + 32;
//    }


    private static double getKilometer(double mile) {
        return (mile * 1.60934);

    }

    private static void saveCurrentDayResultToPreferences(Context context, String latLng, String cityName, String summary, String temp, String imageName
            , double windSpeed, String precipitation, String humidity, String strWindSpeed) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(WeatherConstant.LAT_LNG, latLng);
        editor.putString(WeatherConstant.CITY_NAME, cityName);
        editor.putString(WeatherConstant.WEATHER_STATE, summary);
        editor.putString(WeatherConstant.TEMP, temp);
        editor.putString(WeatherConstant.IMAGE_CODE, String.valueOf(imageName));
        editor.putString(WeatherConstant.WIND_SPEED, String.valueOf(windSpeed));
        editor.putString(WeatherConstant.STR_WIND_SPEED, strWindSpeed);
        editor.putString(WeatherConstant.PRECIPITATION, precipitation);
        editor.putString(WeatherConstant.HUMIDITY, humidity);
        editor.apply();

    }

    private static void saveForeCastResultToPreferences(Context context, int index, String dayOfWeek, String temp, String imageName) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(WeatherConstant.DAY_OF_WEEKS + index, dayOfWeek);
        editor.putString(WeatherConstant.TEMP_FORE_CAST + index, temp);
        editor.putString(WeatherConstant.IMAGE_NAME + index, imageName);
        editor.apply();

    }

    public static ArrayList<String> loadSharedPreferencesCurrentDay(Context context) {
        ArrayList<String> weatherStateList = new ArrayList<>();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        weatherStateList.add(preferences.getString(WeatherConstant.LAT_LNG, ""));
        weatherStateList.add(preferences.getString(WeatherConstant.CITY_NAME, ""));
        weatherStateList.add(preferences.getString(WeatherConstant.WEATHER_STATE, ""));
        weatherStateList.add(preferences.getString(WeatherConstant.TEMP, ""));
        weatherStateList.add(preferences.getString(WeatherConstant.IMAGE_CODE, "day_sunny"));
        weatherStateList.add(preferences.getString(WeatherConstant.WIND_SPEED, "0"));
        weatherStateList.add(preferences.getString(WeatherConstant.STR_WIND_SPEED, ""));
        weatherStateList.add(preferences.getString(WeatherConstant.PRECIPITATION, "0"));
        weatherStateList.add(preferences.getString(WeatherConstant.HUMIDITY, "0"));


        return weatherStateList;
    }

    public static ArrayList<String> loadSharedPreferencesForeCast(Context context, int index) {
        ArrayList<String> weatherStateListForeCast = new ArrayList<>();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        weatherStateListForeCast.add(preferences.getString(WeatherConstant.DAY_OF_WEEKS + index, ""));
        weatherStateListForeCast.add(preferences.getString(WeatherConstant.TEMP_FORE_CAST + index, ""));
        weatherStateListForeCast.add(preferences.getString(WeatherConstant.IMAGE_NAME + index, ""));

        return weatherStateListForeCast;

    }

//    public static int getImageByName(Context context, String name) {
//        Resources resources = context.getResources();
//        return resources.getIdentifier(name, "drawable",
//                context.getPackageName());
//    }

    private static void saveStatusState(AppCompatActivity appCompatActivity, int color) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appCompatActivity);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(WeatherConstant.STATUS_BAR_COLOR, color);
        editor.apply();

    }


//    private void imageSlider() {
//        final int[] imageArray = {R.drawable.weather2, R.drawable.weather4, R.drawable.weather5, R.drawable.weather6, R.drawable.weather7};
//        final Handler handler = new Handler();
//        Runnable runnable = new Runnable() {
//            int i = 0;
//
//            public void run() {
//                ImageViewAnimatedChange(imageArray[i]);
//                i++;
//                if (i > imageArray.length - 1) {
//                    i = 0;
//                }
//                handler.postDelayed(this, 15000);
//            }
//        };
//        handler.postDelayed(runnable, 15200);
//    }


}
