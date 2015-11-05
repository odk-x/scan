/*
 * Copyright (C) 2015 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.scan;

import org.opendatakit.scan.android.R;
import org.opendatakit.scan.android.activities.MainMenuActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.preference.Preference;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.text.Html;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.PreferenceMatchers.withSummaryText;
import static android.support.test.espresso.matcher.ViewMatchers.hasContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.matcher.PreferenceMatchers.withKey;
import static android.support.test.espresso.matcher.PreferenceMatchers.withSummary;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TemplateNameDisplayTest {
    private static final String TEMPLATE_TO_USE = "numbers";
    private static final String PREFERENCE_KEY = "select_templates";

    @Rule
    public ActivityTestRule<MainMenuActivity> mActivityRule =
            new ActivityTestRule<MainMenuActivity>(MainMenuActivity.class);

    @Test
    public void changeTemplateNameDisplay_AppSettings() {
        //Go to AppSettings
        onView(withId(R.id.SettingsButton))
                .perform(click());

        //Change template
        onData(withKey(PREFERENCE_KEY)).perform(click());
        onView(withText(TEMPLATE_TO_USE)).perform(click());

        //Check template name is displayed in summary
        onData(withKey(PREFERENCE_KEY))
                .onChildView(withText(
                                String.format(
                                        mActivityRule.getActivity().getResources().getString(R.string.specify_form_type),
                                        TEMPLATE_TO_USE)
                        )
                ).check(matches(isDisplayed()));
    }

    @Test
    public void changeTemplateNameDisplay_ScanButtonText() {
        //Go to AppSettings
        onView(withId(R.id.SettingsButton))
                .perform(click());

        //Change template and go back to MainMenu
        onData(withKey(PREFERENCE_KEY)).perform(click());
        onView(withText(TEMPLATE_TO_USE)).perform(click());
        Espresso.pressBack();

        //Check template name is displayed on ScanButton
        onView(withId(R.id.ScanButton)).check(matches(withText(
                Html.fromHtml(
                        String.format(
                                mActivityRule.getActivity().getResources().getString(R.string.scan_new_form),
                                TEMPLATE_TO_USE)
                ).toString()
        )));
    }
}