package watteco.netwo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.DialogFragment;

import java.io.IOException;
import java.util.Objects;


public class ResetFragment extends DialogFragment {

    OnMyDialogResult mDialogReport;
    Button btnYes, btnNo;

    public ResetFragment() {
        // Required empty public constructor
    }

    public static ResetFragment newInstance(String param1, String param2) {
        return new ResetFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Objects.requireNonNull(getDialog()).setCanceledOnTouchOutside(true);

        View v = inflater.inflate(R.layout.fragment_reset, container, false);

        btnYes = v.findViewById(R.id.btnYes);
        btnYes.setOnClickListener(v1 -> {
                    if (mDialogReport != null) {
                        try {
                            mDialogReport.finish("Yes");

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ResetFragment.this.dismiss();
                    }
                }
        );

        btnNo = v.findViewById(R.id.btnNo);
        btnNo.setOnClickListener(v12 -> {
                    if (mDialogReport != null) {
                        try {
                            mDialogReport.finish("No");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ResetFragment.this.dismiss();
                    }
                }
        );

        return v;
    }

    public void setDialogResult(OnMyDialogResult dialogResult) {
        mDialogReport = dialogResult;
    }

    public interface OnMyDialogResult {
        void finish(String result) throws IOException;
    }

}