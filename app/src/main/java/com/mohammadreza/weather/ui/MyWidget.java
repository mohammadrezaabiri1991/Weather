package com.mohammadreza.weather.ui;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.graphics.Typeface;
import android.widget.RemoteViews;


import com.mohammadreza.weather.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.mohammadreza.weather.Initialization.loadSharedPreferencesCurrentDay;


/**
 * Implementation of App Widget functionality.
 */
public class MyWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {


//        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_my_widget);


        Typeface weather_font = Typeface.createFromAsset(context.getAssets(), "fonts/weather_icons.ttf");


        ArrayList<String> getCurrentDayResult = loadSharedPreferencesCurrentDay(context);

        if (getCurrentDayResult.size() > 0) {

            Date date = new Date();
            SimpleDateFormat simpDate;
            simpDate = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss");
            simpDate.format(date);


            views.setTextViewText(R.id.txtTimeWidget, String.valueOf(date.getHours() + ":" + date.getMinutes()));
            views.setTextViewText(R.id.txtDateWidget, String.valueOf(date.getDay()));

            views.setTextViewText(R.id.txtCityName, getCurrentDayResult.get(1));
            views.setTextViewText(R.id.txtTempWidget, getCurrentDayResult.get(3));
            views.setTextViewText(R.id.txtWeatherIconWidget, "Su");
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

}

