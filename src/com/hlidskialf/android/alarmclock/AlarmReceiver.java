/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hlidskialf.android.alarmclock;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;

/**
 * Glue class: connects AlarmAlert IntentReceiver to AlarmAlert
 * activity.  Passes through Alarm ID.
 */
public class AlarmReceiver extends BroadcastReceiver {

    /** If the alarm is older than STALE_WINDOW seconds, ignore.  It
        is probably the result of a time or timezone change */
    private final static int STALE_WINDOW = 60 * 30;

    @Override
    public void onReceive(Context context, Intent intent) {
        long now = System.currentTimeMillis();
        int id = intent.getIntExtra(Alarms.ID, 0);
        long setFor = intent.getLongExtra(Alarms.TIME, 0);

        /* FIXME Intentionally verbose: always log this until we've
           fully debugged the app failing to start up */
        Log.v("AlarmReceiver.onReceive() id " + id + " setFor " + setFor +
              " now " + now);

        if (now > setFor + STALE_WINDOW * 1000) {
            if (Log.LOGV) Log.v("AlarmReceiver ignoring stale alarm intent id"
                                + id + " setFor " + setFor + " now " + now);
            return;
        }

        /* wake device */
        AlarmAlertWakeLock.acquire(context);

        /* start audio/vibe */
        AlarmKlaxon klaxon = AlarmKlaxon.getInstance(context);
        klaxon.play(context, id);

        /* launch UI, explicitly stating that this is not due to user action
         * so that the current app's notification management is not disturbed */
        Intent fireAlarm = new Intent(context, AlarmAlert.class);
        fireAlarm.putExtra(Alarms.ID, id);
        fireAlarm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(fireAlarm);
   }
}
