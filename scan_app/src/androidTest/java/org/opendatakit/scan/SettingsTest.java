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

import android.graphics.Color;
import org.opendatakit.scan.android.R;
import org.opendatakit.scan.android.activities.MainActivity;
import org.opendatakit.scan.android.utils.ScanUtils;
import org.opendatakit.scan.utils.EspressoUtils;
import org.opendatakit.scan.utils.ODKMatcher;
import org.opendatakit.scan.utils.StringUtils;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.core.deps.guava.io.Files;
import android.text.Html;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import org.hamcrest.Matcher;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.matcher.PreferenceMatchers.withKey;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SettingsTest {
  private static final String[] TEMPLATES_TO_USE = new String[] { "numbers", "example" };
  private static final String[] SEARCH_METHODS = new String[] { "Flat", "Recursive" };
  private static final String SELECT_TEMPLATE_KEY = "select_templates";
  private static final String NEW_TEMPLATE_NAME = "espresso test";
  private static final String SEARCH_METHOD_KEY = "directory_search";

  @Rule
  public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

  @Before
  public void openTemplateChooserFromMain() {
    EspressoUtils.adjustIdleWaitTimeout();

    //Go to Settings
    onView(withId(R.id.menu_scan_preferences)).perform(click());
  }

  @Test
  public void changeTemplateNameDisplay_OneTemplate_Settings() {
    //Select template
    toggleTemplates(TEMPLATES_TO_USE[0]);

    //Check summary text
    onData(withKey(SELECT_TEMPLATE_KEY)).onChildView(withId(android.R.id.summary)).check(matches(
        withText(String
            .format(StringUtils.getString(this.mActivityRule, R.string.specify_form_type),
                TEMPLATES_TO_USE[0]))));

    //Remove selection
    toggleTemplates(TEMPLATES_TO_USE[0]);

    //Check summary text
    onData(withKey(SELECT_TEMPLATE_KEY)).onChildView(withId(android.R.id.summary))
        .check(matches(withText(R.string.no_form_selected)));
  }

  @Test
  public void changeTemplateNameDisplay_MultiTemplate_Settings() {
    //Select templates
    toggleTemplates(TEMPLATES_TO_USE);

    //Check summary text
    onData(withKey(SELECT_TEMPLATE_KEY)).onChildView(withId(android.R.id.summary)).check(matches(
        anyOf(withText(String
            .format(StringUtils.getString(this.mActivityRule, R.string.specify_form_type),
                TEMPLATES_TO_USE[0] + ", " + TEMPLATES_TO_USE[1])), withText(String
            .format(StringUtils.getString(this.mActivityRule, R.string.specify_form_type),
                TEMPLATES_TO_USE[1] + ", " + TEMPLATES_TO_USE[0])))));

    //Remove selection
    toggleTemplates(TEMPLATES_TO_USE);

    //Check summary text
    onData(withKey(SELECT_TEMPLATE_KEY)).onChildView(withId(android.R.id.summary))
        .check(matches(withText(R.string.no_form_selected)));
  }

  @Test
  public void changeTemplateNameDisplay_OneTemplate_MainMenu() {
    //Choose template and go to main menu
    toggleTemplates(TEMPLATES_TO_USE[0]);
    Espresso.pressBack();

    //Check template name is displayed on MainMenu
    onView(withId(R.id.TemplateText)).check(matches(withText(Html.fromHtml(String
        .format(StringUtils.getString(this.mActivityRule, R.string.template_selected),
            TEMPLATES_TO_USE[0])).toString())));

    //Remove selection and go back
    onView(withId(R.id.menu_scan_preferences)).perform(click());
    toggleTemplates(TEMPLATES_TO_USE[0]);
    Espresso.pressBack();

    //Check template name display on MainMenu
    onView(withId(R.id.TemplateText)).check(matches(withText(R.string.no_template)));
    onView(withId(R.id.TemplateText)).check(matches(ODKMatcher.withTextColor(Color.RED)));
  }

  @Test
  public void changeTemplateNameDisplay_MultiTemplate_MainMenu() {
    //Choose template and go to main menu
    toggleTemplates(TEMPLATES_TO_USE);
    Espresso.pressBack();

    //Check template name is displayed on MainMenu
    onView(withId(R.id.TemplateText)).check(matches(anyOf(withText(Html.fromHtml(String
        .format(StringUtils.getString(this.mActivityRule, R.string.template_selected),
            TEMPLATES_TO_USE[0] + ", " + TEMPLATES_TO_USE[1])).toString()), withText(Html.fromHtml(
            String.format(StringUtils.getString(this.mActivityRule, R.string.template_selected),
                TEMPLATES_TO_USE[1] + ", " + TEMPLATES_TO_USE[0])).toString()))));

    //Remove selection and go back
    onView(withId(R.id.menu_scan_preferences)).perform(click());
    toggleTemplates(TEMPLATES_TO_USE);
    Espresso.pressBack();

    //Check template name display on MainMenu
    onView(withId(R.id.TemplateText)).check(matches(withText(R.string.no_template)));
    onView(withId(R.id.TemplateText)).check(matches(ODKMatcher.withTextColor(Color.RED)));
  }

  @Test
  public void templatesToUse_ChoiceDisplay() {
    //Retrieve list of templates
    File dir = new File(ScanUtils.getTemplateDirPath());
    String[] templateNames = dir.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        File templateFile = new File(dir, name);
        if (templateFile.isDirectory()) {
          // Make sure necessary files are present
          if (new File(templateFile, "template.json").exists() && new File(templateFile, "form.jpg")
              .exists()) {
            return true;
          }

        }
        return false;
      }
    });

    //Open template chooser
    onData(withKey(SELECT_TEMPLATE_KEY)).perform(click());

    //Check if every template is displayed
    for (String template : templateNames) {
      onData(is(template)).check(matches(isCompletelyDisplayed()));
    }

    //Get list of expected entries
    List<Matcher<? super String>> templatesList = new ArrayList<>();
    for (String s : templateNames) {
      templatesList.add(is(s));
    }

    //Check if there are extra entries
    //If no extra entries exist, no exception will be thrown
    //If extra entries exist, test fails
    //Very ugly but it works
    try {
      onData(not(anyOf(templatesList))).check(doesNotExist());
    } catch (RuntimeException e) {
    }
  }

  @Test
  public void templatesToUse_AddAndRemoveTemplateDisplay() {
    //Copy "example" to "espresso test" to simulate adding a template
    File dir = new File(ScanUtils.getTemplateDirPath());
    File newTemplateDir = new File(dir, NEW_TEMPLATE_NAME);
    newTemplateDir.mkdir();
    File jsonFile = new File(newTemplateDir, "template.json");
    File formFile = new File(newTemplateDir, "form.jpg");
    try {
      Files.copy(new File(new File(dir, "example"), "template.json"), jsonFile);
      Files.copy(new File(new File(dir, "example"), "form.jpg"), formFile);
    } catch (IOException e) {
      e.printStackTrace();
    }

    //return to main menu
    Espresso.pressBack();

    //Open template chooser
    onView(withId(R.id.menu_scan_preferences)).perform(click());
    onData(withKey(SELECT_TEMPLATE_KEY)).perform(click());

    //Check if "espresso test" is present
    onData(is(NEW_TEMPLATE_NAME)).check(matches(isCompletelyDisplayed()));

    //delete "espresso test"
    jsonFile.delete();
    formFile.delete();
    newTemplateDir.delete();

    //Re-enter Settings
    Espresso.pressBack();
    Espresso.pressBack();
    onView(withId(R.id.menu_scan_preferences)).perform(click());
    onData(withKey(SELECT_TEMPLATE_KEY)).perform(click());

    //Check if "espresso test" doesn't exist
    //If "espresso test" doesn't exist, no exception will be thrown
    //If it does, test fails
    //Very ugly but it works
    try {
      onData(is(NEW_TEMPLATE_NAME)).check(doesNotExist());
    } catch (RuntimeException e) {
    }
  }

  @Test
  public void searchMethodNameDisplay() {
    for (String method : SEARCH_METHODS) {
      //Open search method chooser
      onData(withKey(SEARCH_METHOD_KEY)).perform(click());

      //choose method
      onData(is(method)).perform(click());

      //check summary text
      onData(withKey(SEARCH_METHOD_KEY)).onChildView(withId(android.R.id.summary))
          .check(matches(withText(method)));
    }
  }

  /**
   * Toggle the specified templates
   * @param names
   */
  private void toggleTemplates(String... names) {
    //Open template chooser
    onData(withKey(SELECT_TEMPLATE_KEY)).perform(click());

    //Click on all names
    for (String name : names) {
      onData(is(name)).perform(click());
    }

    //Click OK
    onView(withId(android.R.id.button1)).perform(click());
  }
}