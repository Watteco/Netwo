package wattecoDev.netwo;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import java.io.IOException;
import java.util.Objects;

public class LogsFragment extends DialogFragment {

    OnMyDialogResult mDialogReport;
    String logs;
    TextView textView;
    Button sendButton;

    public LogsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle arg = getArguments();
        assert arg != null;

        logs = arg.getString("Logs");

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_logs, container, false);
        Objects.requireNonNull(getDialog()).setCanceledOnTouchOutside(true);

        sendButton = v.findViewById(R.id.sendLogsFile);
        sendButton.setOnClickListener(v1 -> {
            if( mDialogReport != null ){
                try {

                    mDialogReport.finish("");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                LogsFragment.this.dismiss();
            }
        });
        textView = v.findViewById(R.id.textLogs);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setText(logs);

        return v;
    }

    public void setDialogResult(OnMyDialogResult dialogResult){ mDialogReport = dialogResult; }

    public interface OnMyDialogResult{
        void finish(String result) throws IOException;
    }
}