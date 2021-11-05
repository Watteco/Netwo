package watteco.netwo;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ReportFragment extends DialogFragment {

    OnMyDialogResult mDialogReport;
    TextInputEditText reportName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_report, container, false);
        Objects.requireNonNull(getDialog()).setCanceledOnTouchOutside(true);

        reportName = v.findViewById(R.id.reportName);

        v.findViewById(R.id.reportcsv).setOnClickListener(
                v12 -> {
                    InputMethodManager mgr = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(reportName.getWindowToken(), 0);
                    if( mDialogReport != null ){
                        try {
                            List<String> list = new ArrayList<>();
                            list.add("csv");
                            if (reportName.getText().length() != 0) {
                                list.add(reportName.getText().toString());
                            } else {
                                list.add("");
                            }
                            mDialogReport.finish(list);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ReportFragment.this.dismiss();
                    }

                }
        );

        v.findViewById(R.id.reportjson).setOnClickListener(
                v1 -> {
                    if( mDialogReport != null ){
                        try {
                            InputMethodManager mgr = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            mgr.hideSoftInputFromWindow(reportName.getWindowToken(), 0);

                            List<String> list = new ArrayList<>();
                            list.add("json");
                            if (reportName.getText().length() != 0) {
                                list.add(reportName.getText().toString());
                            } else {
                                list.add("");
                            }

                            mDialogReport.finish(list);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ReportFragment.this.dismiss();
                    }

                }
        );

        return v;
    }

    public void setDialogResult(OnMyDialogResult dialogResult){ mDialogReport = dialogResult; }

    public interface OnMyDialogResult{
        void finish(List<String> result) throws IOException;
    }
}