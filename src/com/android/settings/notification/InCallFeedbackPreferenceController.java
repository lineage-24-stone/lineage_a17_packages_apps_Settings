/*
 * Copyright (C) 2022 crDroid Android Project
 * Copyright (C) 2024 The LineageOS Project
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

package com.android.settings.notification;

import static com.android.settings.notification.SettingPref.TYPE_SYSTEM;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class InCallFeedbackPreferenceController extends SettingPrefController {

    private static final String KEY_INCALL_FEEDBACK = "incall_feedback_vibrate";
    private static final String KEY_VIBRATE_ON_CONNECT = "vibrate_on_connect";
    private static final String KEY_VIBRATE_ON_DISCONNECT = "vibrate_on_disconnect";
    private static final String KEY_VIBRATE_ON_CALLWAITING = "vibrate_on_callwaiting";

    public InCallFeedbackPreferenceController(Context context, SettingsPreferenceFragment parent,
            Lifecycle lifecycle) {
        super(context, parent, lifecycle);
        mPreference = new SettingPref(
                TYPE_SYSTEM, KEY_INCALL_FEEDBACK, KEY_INCALL_FEEDBACK, 0 /* default off */) {
            @Override
            public boolean isApplicable(Context context) {
                return Utils.isVoiceCapable(context);
            }

            @Override
            protected boolean setSetting(Context context, int value) {
                final ContentResolver cr = context.getContentResolver();
                final boolean b1 = Settings.System.putInt(cr, KEY_VIBRATE_ON_CONNECT, value);
                final boolean b2 = Settings.System.putInt(cr, KEY_VIBRATE_ON_DISCONNECT, value);
                final boolean b3 = Settings.System.putInt(cr, KEY_VIBRATE_ON_CALLWAITING, value);
                final boolean b4 = Settings.System.putInt(cr, KEY_INCALL_FEEDBACK, value);
                return b1 && b2 && b3 && b4;
            }

            @Override
            public void update(Context context) {
                final boolean enabled =
                        Settings.System.getInt(context.getContentResolver(),
                                KEY_INCALL_FEEDBACK, 0) != 0
                        || Settings.System.getInt(context.getContentResolver(),
                                KEY_VIBRATE_ON_CONNECT, 0) != 0
                        || Settings.System.getInt(context.getContentResolver(),
                                KEY_VIBRATE_ON_DISCONNECT, 0) != 0
                        || Settings.System.getInt(context.getContentResolver(),
                                KEY_VIBRATE_ON_CALLWAITING, 0) != 0;
                if (mTwoState != null) {
                    mTwoState.setChecked(enabled);
                }
            }
        };
    }
}
