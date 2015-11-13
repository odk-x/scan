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

import org.hamcrest.Matcher;
import org.opendatakit.scan.android.R;
import org.opendatakit.scan.android.activities.MainMenuActivity;
import org.opendatakit.scan.android.utils.ScanUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.graphics.Color;
import android.preference.Preference;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ViewScannedFormsTest {
    @Rule
    public ActivityTestRule<MainMenuActivity> mActivityRule = new ActivityTestRule<>(MainMenuActivity.class);

    @Test
    public void viewForms_displayEntries() {
        String[] photoNames = getPhotoNames();

        //Go to View Scanned Forms
        onView(withId(R.id.ViewFormsButton)).perform(click());

        //Check if each output is displayed
        for (String s : photoNames) {
            onData(is(s)).check(matches(isCompletelyDisplayed()));
        }

        //List of expected entries
        List<Matcher<? super String>> photoList = new ArrayList<>();
        for (String s : photoNames) {
            photoList.add(is(s));
        }

        //Check if there are extra entries
        //If no extra entries exist, no exception will be thrown
        //If extra entries exist, test fails
        //Very ugly but it works
        try {
            onData(not(anyOf(photoList))).check(doesNotExist());
        } catch (RuntimeException e) {}
    }

    @Test
    public void viewForms_displayEntriesMetadata() {
        String[] photoNames = getPhotoNames();

        //Go to View Scanned Forms
        onView(withId(R.id.ViewFormsButton)).perform(click());

        //Check if metadata of each output is displayed correctly
        for (String s : photoNames) {
            //check PhotoStatus
            int color = Color.parseColor("#FF0000"); //red
            if (new File(ScanUtils.getJsonPath(s)).exists()) {
                color = Color.parseColor("#00FF00"); //green
            } else if (new File(ScanUtils.getAlignedPhotoPath(s)).exists()) {
                color = Color.parseColor("#FFFF00"); //yellow
            }
            onData(is(s)).onChildView(withId(R.id.photoStatus)).check(
                    matches(ColorMatcher.withTextColor(color))
            );

            //Check templateName
            onData(is(s)).onChildView(withId(R.id.templateName)).check(
                    matches(withText(s.split("_")[0]))
            );

            //Check createdTime
            onData(is(s)).onChildView(withId(R.id.createdTime)).check(
                    matches(withText(new Date(new File(ScanUtils.getPhotoPath(s)).lastModified()).toString()))
            );
        }
    }

    @Test
    public void viewForms_DisplayProcessedForm() {
        //Go to View Scanned Forms
        onView(withId(R.id.ViewFormsButton)).perform(click());

        //if first item is green, activity should land on Display Processed Form
        //if otherwise, should stay on the same activity
        try {
            //check color
            onData(anything()).atPosition(0).onChildView(withId(R.id.photoStatus)).check(
                    matches(ColorMatcher.withTextColor(Color.parseColor("#00FF00")))
            );

            //click first item
            onData(anything()).atPosition(0).onChildView(withId(R.id.templateName)).perform(click());
            //check title
            onView(withId(android.R.id.title)).check(
                    matches(withText(
                                    mActivityRule.getActivity().getResources()
                                            .getString(R.string.display_processed_form_activity))
                    )
            );
        } catch (junit.framework.AssertionFailedError e) {
            //click first item
            onData(anything()).atPosition(0).onChildView(withId(R.id.templateName)).perform(click());
            //check title
            onView(withId(android.R.id.title)).check(
                    matches(withText(
                                    mActivityRule.getActivity().getResources()
                                            .getString(R.string.view_bubble_forms_activity))
                    )
            );
        }
    }

    private String[] getPhotoNames() {
        return new File(ScanUtils.getOutputDirPath()).list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (new File(dir, name)).isDirectory();
            }
        });
    }
}