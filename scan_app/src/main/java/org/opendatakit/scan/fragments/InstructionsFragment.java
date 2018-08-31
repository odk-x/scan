package org.opendatakit.scan.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.opendatakit.scan.R;

public class InstructionsFragment extends Fragment {

  private View view;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.bubble_instructions, container, false);
    return view;
  }

}
