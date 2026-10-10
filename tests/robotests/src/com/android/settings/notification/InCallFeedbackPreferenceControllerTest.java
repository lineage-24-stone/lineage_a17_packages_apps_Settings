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

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;

import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(shadows = {
        com.android.settings.testutils.shadow.ShadowFragment.class,
})
public class InCallFeedbackPreferenceControllerTest {

    private static final String KEY_INCALL_FEEDBACK = "incall_feedback_vibrate";
    private static final String KEY_VIBRATE_ON_CONNECT = "vibrate_on_connect";
    private static final String KEY_VIBRATE_ON_DISCONNECT = "vibrate_on_disconnect";
    private static final String KEY_VIBRATE_ON_CALLWAITING = "vibrate_on_callwaiting";

    @Mock
    private Context mContext;
    @Mock
    private TelephonyManager mTelephonyManager;
    @Mock
    private PreferenceScreen mScreen;
    @Mock
    private FragmentActivity mActivity;
    @Mock
    private SoundSettings mSetting;

    private ContentResolver mContentResolver;
    private InCallFeedbackPreferenceController mController;
    private SwitchPreference mPreference;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mContentResolver = RuntimeEnvironment.application.getContentResolver();

        when(mContext.getContentResolver()).thenReturn(mContentResolver);
        when(mContext.getResources()).thenReturn(RuntimeEnvironment.application.getResources());
        when(mContext.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(mTelephonyManager);

        when(mActivity.getContentResolver()).thenReturn(mContentResolver);
        when(mActivity.getResources()).thenReturn(RuntimeEnvironment.application.getResources());
        when(mActivity.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(mTelephonyManager);

        when(mTelephonyManager.isVoiceCapable()).thenReturn(true);
        when(mTelephonyManager.isDeviceVoiceCapable()).thenReturn(true);

        when(mSetting.getActivity()).thenReturn(mActivity);
        doReturn(mScreen).when(mSetting).getPreferenceScreen();

        mPreference = new SwitchPreference(RuntimeEnvironment.application);
        mController = new InCallFeedbackPreferenceController(mContext, mSetting, null);
        when(mScreen.findPreference(mController.getPreferenceKey())).thenReturn(mPreference);
    }

    @Test
    public void isAvailable_voiceCapable_shouldReturnTrue() {
        assertThat(mController.isAvailable()).isTrue();
    }

    @Test
    public void isAvailable_notVoiceCapable_shouldReturnFalse() {
        when(mTelephonyManager.isVoiceCapable()).thenReturn(false);
        when(mTelephonyManager.isDeviceVoiceCapable()).thenReturn(false);

        assertThat(mController.isAvailable()).isFalse();
    }

    @Test
    public void displayPreference_inCallFeedbackEnabled_shouldCheckedPreference() {
        System.putInt(mContentResolver, KEY_INCALL_FEEDBACK, 1);

        mController.displayPreference(mScreen);

        assertThat(mPreference.isChecked()).isTrue();
    }

    @Test
    public void displayPreference_vibrateOnConnectFallback_shouldCheckedPreference() {
        System.putInt(mContentResolver, KEY_INCALL_FEEDBACK, 0);
        System.putInt(mContentResolver, KEY_VIBRATE_ON_CONNECT, 1);
        System.putInt(mContentResolver, KEY_VIBRATE_ON_DISCONNECT, 0);
        System.putInt(mContentResolver, KEY_VIBRATE_ON_CALLWAITING, 0);

        mController.displayPreference(mScreen);

        assertThat(mPreference.isChecked()).isTrue();
    }

    @Test
    public void displayPreference_vibrateOnDisconnectFallback_shouldCheckedPreference() {
        System.putInt(mContentResolver, KEY_INCALL_FEEDBACK, 0);
        System.putInt(mContentResolver, KEY_VIBRATE_ON_CONNECT, 0);
        System.putInt(mContentResolver, KEY_VIBRATE_ON_DISCONNECT, 1);
        System.putInt(mContentResolver, KEY_VIBRATE_ON_CALLWAITING, 0);

        mController.displayPreference(mScreen);

        assertThat(mPreference.isChecked()).isTrue();
    }

    @Test
    public void displayPreference_vibrateOnCallWaitingFallback_shouldCheckedPreference() {
        System.putInt(mContentResolver, KEY_INCALL_FEEDBACK, 0);
        System.putInt(mContentResolver, KEY_VIBRATE_ON_CONNECT, 0);
        System.putInt(mContentResolver, KEY_VIBRATE_ON_DISCONNECT, 0);
        System.putInt(mContentResolver, KEY_VIBRATE_ON_CALLWAITING, 1);

        mController.displayPreference(mScreen);

        assertThat(mPreference.isChecked()).isTrue();
    }

    @Test
    public void displayPreference_inCallFeedbackDisabled_shouldUncheckedPreference() {
        System.putInt(mContentResolver, KEY_INCALL_FEEDBACK, 0);
        System.putInt(mContentResolver, KEY_VIBRATE_ON_CONNECT, 0);
        System.putInt(mContentResolver, KEY_VIBRATE_ON_DISCONNECT, 0);
        System.putInt(mContentResolver, KEY_VIBRATE_ON_CALLWAITING, 0);

        mController.displayPreference(mScreen);

        assertThat(mPreference.isChecked()).isFalse();
    }

    @Test
    public void onPreferenceChanged_preferenceChecked_shouldEnableAllKeys() {
        mController.displayPreference(mScreen);

        mPreference.getOnPreferenceChangeListener().onPreferenceChange(mPreference, true);

        assertThat(System.getInt(mContentResolver, KEY_INCALL_FEEDBACK, 0)).isEqualTo(1);
        assertThat(System.getInt(mContentResolver, KEY_VIBRATE_ON_CONNECT, 0)).isEqualTo(1);
        assertThat(System.getInt(mContentResolver, KEY_VIBRATE_ON_DISCONNECT, 0)).isEqualTo(1);
        assertThat(System.getInt(mContentResolver, KEY_VIBRATE_ON_CALLWAITING, 0)).isEqualTo(1);
    }

    @Test
    public void onPreferenceChanged_preferenceUnchecked_shouldDisableAllKeys() {
        System.putInt(mContentResolver, KEY_INCALL_FEEDBACK, 1);
        System.putInt(mContentResolver, KEY_VIBRATE_ON_CONNECT, 1);
        System.putInt(mContentResolver, KEY_VIBRATE_ON_DISCONNECT, 1);
        System.putInt(mContentResolver, KEY_VIBRATE_ON_CALLWAITING, 1);
        mController.displayPreference(mScreen);

        mPreference.getOnPreferenceChangeListener().onPreferenceChange(mPreference, false);

        assertThat(System.getInt(mContentResolver, KEY_INCALL_FEEDBACK, 1)).isEqualTo(0);
        assertThat(System.getInt(mContentResolver, KEY_VIBRATE_ON_CONNECT, 1)).isEqualTo(0);
        assertThat(System.getInt(mContentResolver, KEY_VIBRATE_ON_DISCONNECT, 1)).isEqualTo(0);
        assertThat(System.getInt(mContentResolver, KEY_VIBRATE_ON_CALLWAITING, 1)).isEqualTo(0);
    }
}
