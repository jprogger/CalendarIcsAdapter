/*
 * Copyright (C) 2013, 2014 - Daniele Gobbetti and k3b
 * 
 * This file is part of android.calendar.ics.adapter.
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package de.k3b.android.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VEvent;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;

import de.k3b.android.calendar.Global;
import de.k3b.android.calendar.IcsImportIntentFactory;
import de.k3b.calendar.EventDto;
import de.k3b.calendar.IcsAsEventDto;

/**
 * Invisible Pseudo-Activity that imports a ics-calendar-event-file into the android Calendar.<br/>
 * Supports Android 4.0 and up. Runs on most Android 2.1 and up that have a calendar and a calendar provider.<br/><br/>
 *
 * @author k3b
 */
public class Ics2ACalendarActivity extends Activity {

    /**
     * loads filecontents from stream
     */
    private static InputStream getStreamFromOtherSource(Context context, Uri contentUri) {
        ContentResolver res = context.getApplicationContext().getContentResolver();
        Uri uri = Uri.parse(contentUri.toString());
        InputStream is;
        try {
            is = res.openInputStream(uri);
        } catch (FileNotFoundException e) {
            is = new ByteArrayInputStream(new byte[0]);
        }
        return is;
    }

    /**
     * gets file uri from activity intent and opens re-populated "Add Event-To-Calendar"-Activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        Uri data = intent.getData();

        if (Global.debugEnabled) {
            Log.d(IcsImportIntentFactory.TAG, "Ics2ACalendarActivity begin " + data);
        }
        startCalendarImportActivity(this, data);
        if (Global.debugEnabled) {
            Log.d(IcsImportIntentFactory.TAG, "Ics2ACalendarActivity done" + data);
        }

        this.finish();
    }

    /**
     * opens re-populated "Add Event-To-Calendar"-Activity from contents of file-uri.
     */
    void startCalendarImportActivity(Context context, Uri calendarEventFileUri) {
        if (calendarEventFileUri != null) {
            try {
                if (Global.debugEnabled) {
                    Log.d(IcsImportIntentFactory.TAG, "opening " + calendarEventFileUri);
                }

                //use ical4j to parse the event
                CalendarBuilder cb = new CalendarBuilder();
                Calendar calendar = cb.build(getStreamFromOtherSource(context, calendarEventFileUri));
                if (Global.debugEnabled) {
                    Log.d(IcsImportIntentFactory.TAG, "loaded " + calendar);
                }

                if (calendar != null) {
                    IcsImportIntentFactory importFactory = new IcsImportIntentFactory();

                    Iterator<?> i = calendar.getComponents(Component.VEVENT).iterator();

                    while (i.hasNext()) {
                        VEvent event = (VEvent) i.next();

                        if (Global.debugEnabled) {
                            Log.d(IcsImportIntentFactory.TAG, "processing event " + event.getName());
                        }
                        EventDto eventDto = new IcsAsEventDto(event);

                        Intent insertIntent = importFactory.createImportIntent(context, eventDto, event);

                        context.startActivity(insertIntent);
                    }
                }

            } catch (Exception e) {
                Log.e(IcsImportIntentFactory.TAG, "error processing " + calendarEventFileUri + " : " + e);
                e.printStackTrace();
            }
        }
    }
}
