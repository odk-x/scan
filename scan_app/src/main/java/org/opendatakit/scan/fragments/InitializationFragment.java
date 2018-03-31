package org.opendatakit.scan.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.opendatakit.activities.IAppAwareActivity;
import org.opendatakit.activities.IInitResumeActivity;
import org.opendatakit.fragment.AlertDialogFragment.ConfirmAlertDialog;
import org.opendatakit.fragment.AlertNProgessMsgFragmentMger;
import org.opendatakit.listener.DatabaseConnectionListener;
import org.opendatakit.listener.InitializationListener;
import org.opendatakit.logging.WebLogger;
import org.opendatakit.scan.R;
import org.opendatakit.scan.application.Scan;
import org.opendatakit.scan.utils.ScanUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Attempt to initialize data directories using the APK Expansion files.
 *
 * @author jbeorse@cs.washington.edu
 */
public class InitializationFragment extends Fragment
    implements InitializationListener, ConfirmAlertDialog, DatabaseConnectionListener {

   private static final String t = InitializationFragment.class.getSimpleName();

   private static final int ID = R.layout.copy_expansion_files_layout;
   private static final String ALERT_DIALOG_TAG = "alertDialogScan";
   private static final String PROGRESS_DIALOG_TAG = "progressDialogScan";

   private static final String INIT_STATE_KEY = "IF_initStateKeyScan";

   // The types of dialogs we handle
   public enum InitializationState {
      START, IN_PROGRESS, FINISH
   }

   private static String appName;

   private InitializationState initState = InitializationState.START;
   private AlertNProgessMsgFragmentMger msgManager;

   private String mainDialogTitle;

   @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
       Bundle savedInstanceState) {
      appName = ((IAppAwareActivity) getActivity()).getAppName();

      View view = inflater.inflate(ID, container, false);

      mainDialogTitle = getString(R.string.configuring_app,
          getString(Scan.getInstance().getApkDisplayNameResourceId()));
      // restore any state
      if (savedInstanceState != null) {
         if (savedInstanceState.containsKey(INIT_STATE_KEY)) {
            initState = InitializationState.valueOf(savedInstanceState.getString(INIT_STATE_KEY));
         }
         msgManager = AlertNProgessMsgFragmentMger
             .restoreInitMessaging(appName, ALERT_DIALOG_TAG, PROGRESS_DIALOG_TAG,
                 savedInstanceState);
      }

      // if message manager was not created from saved state, create fresh
      if (msgManager == null) {
         msgManager = new AlertNProgessMsgFragmentMger(appName, ALERT_DIALOG_TAG,
             PROGRESS_DIALOG_TAG, false, false);
      }

      return view;
   }

   @Override public void onStart() {
      super.onStart();
      Scan.getInstance().possiblyFireDatabaseCallback(getActivity(), this);
   }


   @Override public void onResume() {
      super.onResume();

      if (initState == InitializationState.START) {
         WebLogger.getLogger(((IAppAwareActivity) getActivity()).getAppName()).i(t,
             "onResume -- calling initializeAppName");
         Scan.getInstance().initializeAppName(((IAppAwareActivity) getActivity()).getAppName(),
             this);
         initState = InitializationState.IN_PROGRESS;
      } else {

         msgManager.restoreDialog(getFragmentManager(), getId());

         // re-attach to the task for task notifications...
         Scan.getInstance().establishInitializationListener(this);
      }
   }


   @Override public void onPause() {
      msgManager.clearDialogsAndRetainCurrentState(getFragmentManager());
      super.onPause();
   }

   @Override public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      msgManager.addStateToSaveStateBundle(outState);
      outState.putString(INIT_STATE_KEY, initState.name());
   }



   @Override public void initializationComplete(boolean overallSuccess, ArrayList<String> result) {

    /* Add runtime initialization */

      // Create output dir if it doesn't exist
      new File(ScanUtils.getOutputDirPath()).mkdirs();

      try {

         //Creates a .nomedia file to prevent the images from showing up in the gallery.
      /*
      new File(ScanUtils.getSystemPath() + File.separator + ".nomedia").createNewFile();
      new File(ScanUtils.getConfigPath() + File.separator + ".nomedia").createNewFile();
      new File(ScanUtils.getOutputDirPath() + File.separator + ".nomedia").createNewFile();
      */

         // TODO: Only adding dummy data while sync doesn't support empty files. Remove dummy data
         // when that is fixed
         PrintWriter systemPW = new PrintWriter(
             ScanUtils.getSystemPath() + File.separator + "" + ".nomedia");
         PrintWriter configPW = new PrintWriter(
             ScanUtils.getConfigPath() + File.separator + "" + ".nomedia");
         PrintWriter outputPW = new PrintWriter(
             ScanUtils.getOutputDirPath() + File.separator + "" + ".nomedia");

         systemPW.print("Dummy data");
         configPW.print("Dummy data");
         outputPW.print("Dummy data");

         systemPW.close();
         configPW.close();
         outputPW.close();
      } catch (IOException e) {
         e.printStackTrace();
         WebLogger.getLogger(((IAppAwareActivity) getActivity()).getAppName())
             .i(t, "Error creating nomedia");
      }


    /* Finish initialization */
      initState = InitializationState.FINISH;
      Scan.getInstance().clearInitializationTask();

      if (overallSuccess && result.isEmpty()) {
         // do not require an OK if everything went well
         if(msgManager != null) {
            msgManager.dismissProgressDialog(getFragmentManager());
         }

         ((IInitResumeActivity) getActivity()).initializationCompleted();
         return;
      }

      StringBuilder b = new StringBuilder();
      for (String k : result) {
         b.append(k);
         b.append("\n\n");
      }

      if(msgManager != null) {
         String revisedTitle = overallSuccess ?
             getString(R.string.initialization_complete) :
             getString(R.string.initialization_failed);
         msgManager.createAlertDialog(revisedTitle, b.toString().trim(), getFragmentManager(),
             getId());
      }
   }

   @Override public void initializationProgressUpdate(String displayString) {
      if(msgManager != null) {
         updateProgressDialog(displayString);
      }
   }


   @Override public void okAlertDialog() {
      ((IInitResumeActivity) getActivity()).initializationCompleted();
   }


   @Override public void databaseAvailable() {
      if (initState == InitializationState.IN_PROGRESS) {
         Scan.getInstance()
             .initializeAppName(((IAppAwareActivity) getActivity()).getAppName(), this);
      }
   }

   @Override public void databaseUnavailable() {
      if(msgManager != null) {
         updateProgressDialog(getString(R.string.database_unavailable));
      }
   }

   private void updateProgressDialog(String displayString) {
      if (!msgManager.displayingProgressDialog()) {
         msgManager.createProgressDialog(mainDialogTitle, getString(R.string.please_wait),
             getFragmentManager());
      } else {
         if (msgManager.hasDialogBeenCreated()) {
            msgManager.updateProgressDialogMessage(displayString, getFragmentManager());
         }
      }
   }
}
