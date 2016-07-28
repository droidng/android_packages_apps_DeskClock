/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.deskclock.controller;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.UserManager;
import android.provider.AlarmClock;

import com.android.deskclock.DeskClock;
import com.android.deskclock.HandleApiCalls;
import com.android.deskclock.HandleDeskClockApiCalls;
import com.android.deskclock.R;
import com.android.deskclock.ScreensaverActivity;
import com.android.deskclock.data.DataModel;
import com.android.deskclock.data.Lap;
import com.android.deskclock.data.Stopwatch;
import com.android.deskclock.data.StopwatchListener;
import com.android.deskclock.events.Events;
import com.android.deskclock.events.ShortcutEventTracker;
import com.android.deskclock.uidata.UiDataModel;

import java.util.Arrays;
import java.util.Collections;

@TargetApi(Build.VERSION_CODES.N_MR1)
class ShortcutController {

    private final Context mContext;
    private final ComponentName mComponentName;
    private final ShortcutManager mShortcutManager;
    private final UserManager mUserManager;
    private final UiDataModel uidm = UiDataModel.getUiDataModel();

    ShortcutController(Context context) {
        mContext = context;
        mComponentName = new ComponentName(mContext, DeskClock.class);
        mShortcutManager = mContext.getSystemService(ShortcutManager.class);
        mUserManager = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
        Events.addEventTracker(new ShortcutEventTracker(mContext));
        DataModel.getDataModel().addStopwatchListener(new StopwatchWatcher());
    }

    void updateShortcuts() {
        final ShortcutInfo alarm = createNewAlarmShortcut();
        final ShortcutInfo timer = createNewTimerShortcut();
        final ShortcutInfo stopwatch = createStopwatchShortcut();
        final ShortcutInfo screensaver = createScreensaverShortcut();
        mShortcutManager.setDynamicShortcuts(Arrays.asList(alarm, timer, stopwatch, screensaver));
    }

    private ShortcutInfo createNewAlarmShortcut() {
        final Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(HandleDeskClockApiCalls.EXTRA_EVENT_LABEL, R.string.label_shortcut)
                .setClass(mContext, HandleApiCalls.class);
        final String setAlarmShortcut =
                uidm.getShortcutId(R.string.category_alarm, R.string.action_create);
        return new ShortcutInfo.Builder(mContext, setAlarmShortcut)
                .setIcon(Icon.createWithResource(mContext, R.drawable.shortcut_new_alarm))
                .setActivity(mComponentName)
                .setShortLabel(mContext.getString(R.string.shortcut_new_alarm_short))
                .setLongLabel(mContext.getString(R.string.shortcut_new_alarm_long))
                .setIntent(intent)
                .setRank(0)
                .build();
    }

    private ShortcutInfo createNewTimerShortcut() {
        final Intent intent = new Intent(AlarmClock.ACTION_SET_TIMER)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(HandleDeskClockApiCalls.EXTRA_EVENT_LABEL, R.string.label_shortcut)
                .setClass(mContext, HandleApiCalls.class);
        final String setTimerShortcut =
                uidm.getShortcutId(R.string.category_timer, R.string.action_create);
        return new ShortcutInfo.Builder(mContext, setTimerShortcut)
                .setIcon(Icon.createWithResource(mContext, R.drawable.shortcut_new_timer))
                .setActivity(mComponentName)
                .setShortLabel(mContext.getString(R.string.shortcut_new_timer_short))
                .setLongLabel(mContext.getString(R.string.shortcut_new_timer_long))
                .setIntent(intent)
                .setRank(1)
                .build();
    }

    private ShortcutInfo createStopwatchShortcut() {
        final String shortcutId =
                uidm.getShortcutId(R.string.category_stopwatch, (DataModel.getDataModel()
                        .getStopwatch().isRunning()) ? R.string
                        .action_pause : R.string.action_start);
        final ShortcutInfo.Builder shortcut = new ShortcutInfo.Builder(mContext, shortcutId)
                .setIcon(Icon.createWithResource(mContext, R.drawable.shortcut_stopwatch))
                .setActivity(mComponentName)
                .setRank(2);
        final Intent intent;
        if (DataModel.getDataModel().getStopwatch().isRunning()) {
            intent = new Intent(HandleDeskClockApiCalls.ACTION_PAUSE_STOPWATCH)
                    .putExtra(HandleDeskClockApiCalls.EXTRA_EVENT_LABEL, R.string.label_shortcut);
            shortcut.setShortLabel(mContext.getString(R.string.shortcut_pause_stopwatch_short))
                    .setLongLabel(mContext.getString(R.string.shortcut_pause_stopwatch_long));
        } else {
            intent = new Intent(HandleDeskClockApiCalls.ACTION_START_STOPWATCH)
                    .putExtra(HandleDeskClockApiCalls.EXTRA_EVENT_LABEL, R.string.label_shortcut);
            shortcut.setShortLabel(mContext.getString(R.string.shortcut_start_stopwatch_short))
                    .setLongLabel(mContext.getString(R.string.shortcut_start_stopwatch_long));
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setClass(mContext, HandleDeskClockApiCalls.class);
        return shortcut
                .setIntent(intent)
                .build();
    }

    private ShortcutInfo createScreensaverShortcut() {
        final Intent intent = new Intent(Intent.ACTION_DEFAULT)
                .setClass(mContext, ScreensaverActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(HandleDeskClockApiCalls.EXTRA_EVENT_LABEL, R.string.label_shortcut);
        final String screensaverShortcut =
                uidm.getShortcutId(R.string.category_screensaver, R.string.action_show);
        return new ShortcutInfo.Builder(mContext, screensaverShortcut)
                .setIcon(Icon.createWithResource(mContext, R.drawable.shortcut_screensaver))
                .setActivity(mComponentName)
                .setShortLabel((mContext.getString(R.string.shortcut_start_screensaver_short)))
                .setLongLabel((mContext.getString(R.string.shortcut_start_screensaver_long)))
                .setIntent(intent)
                .setRank(3)
                .build();
    }

    private class StopwatchWatcher implements StopwatchListener {

        @Override
        public void stopwatchUpdated(Stopwatch before, Stopwatch after) {
            if (mUserManager.isUserUnlocked()) {
                mShortcutManager.updateShortcuts(
                        Collections.singletonList(createStopwatchShortcut()));
            }
        }

        @Override
        public void lapAdded(Lap lap) {}
    }
}
