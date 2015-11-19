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

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.core.deps.guava.io.Files;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.text.Html;

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
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AppSettingsActivityTest {
   private static final String TEMPLATE_TO_USE = "numbers";
   private static final String PREFERENCE_KEY = "select_templates";
   private static final String NEW_TEMPLATE_NAME = "espresso test";

   @Rule
   public ActivityTestRule<MainMenuActivity> mActivityRule = new ActivityTestRule<>(MainMenuActivity.class);

   @Test
   public void changeTemplateNameDisplay_AppSettings() {
      openTemplateChooserFromMain();

      //Select template
      onView(withText(TEMPLATE_TO_USE)).perform(click());

      //Check template name is displayed in summary
      onView(withId(android.R.id.summary)).check(matches(withText(
              String.format(
                  mActivityRule.getActivity().getResources().getString(R.string.specify_form_type),
                  TEMPLATE_TO_USE
              )
          )
      ));
   }

   @Test
   public void changeTemplateNameDisplay_ScanButtonText() {
      openTemplateChooserFromMain();

      //Choose template and go back
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

   @Test
   public void templatesToUse_ChoiceDisplay() {
      //Retrieve list of templates
      File dir = new File(ScanUtils.getTemplateDirPath());
      String[] templateNames = dir.list(new FilenameFilter() {
         public boolean accept(File dir, String name) {
            File templateFile = new File(dir, name);
            if (templateFile.isDirectory()) {
               // Make sure necessary files are present
               if (new File(templateFile, "template.json").exists()
                   && new File(templateFile, "form.jpg").exists()) {
                  return true;
               }

            }
            return false;
         }
      });

      openTemplateChooserFromMain();

      //check if every template is displayed
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
      } catch (RuntimeException e) {}
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

      openTemplateChooserFromMain();

      //Check if "espresso test" is present
      onData(is(NEW_TEMPLATE_NAME)).check(matches(isCompletelyDisplayed()));

      //delete "espresso test"
      jsonFile.delete();
      formFile.delete();
      newTemplateDir.delete();

      //Re-enter AppSettings
      Espresso.pressBack();
      Espresso.pressBack();
      onView(withId(R.id.SettingsButton)).perform(click());

      //Check if "espresso test" doesn't exist
      //If "espresso test" doesn't exist, no exception will be thrown
      //If it does, test fails
      //Very ugly but it works
      try {
         onData(is(NEW_TEMPLATE_NAME)).check(doesNotExist());
      } catch (RuntimeException e) {}
   }

   private void openTemplateChooserFromMain() {
      //Go to AppSettings
      onView(withId(R.id.SettingsButton)).perform(click());

      //Open template chooser
      onData(withKey(PREFERENCE_KEY)).perform(click());
   }
}