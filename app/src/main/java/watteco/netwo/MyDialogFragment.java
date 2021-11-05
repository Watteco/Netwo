package watteco.netwo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyDialogFragment extends DialogFragment {
    String whoCalledMe;
    int MarginPerfect,MarginGood,MarginBad,SNRPerfect,SNRBad,RSSIPerfect,RSSIBad;
    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    static MyDialogFragment newInstance() {


        return new MyDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arg = getArguments();
        assert arg != null;
        whoCalledMe = arg.getString("whoCalledMe");

        // Pick a style based on the num.
        int style, theme;

        style = DialogFragment.STYLE_NO_TITLE;
        theme = android.R.style.Theme_Holo_Dialog;

        setStyle(style, theme);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_dialog, container, false);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        int spMarginPerfect = sharedPref.getInt("MarginPerfect", 15);
        int spMarginGood = sharedPref.getInt("MarginGood", 10);
        int spMarginBad = sharedPref.getInt("MarginBad", 5);

        int spSNRPerfect = sharedPref.getInt("SNRPerfect", -5);
        int spSNRBad = sharedPref.getInt("SNRBad", -10);

        int spRSSIPerfect = sharedPref.getInt("RSSIPerfect", -107);
        int spRSSIBad = sharedPref.getInt("RSSIBad", -118);


        Resources c = requireContext().getResources();
        TextView text = v.findViewById(R.id.textDialog);
        ImageView image = v.findViewById(R.id.imageDialog);

        image.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 0));
        switch (whoCalledMe) {
            case "about":
                text.setText(c.getString(R.string.aboutText));
                break;
            case "SNR":
                text.setText(R.string.descriptionSNR);
                break;
            case "RSSI":
                text.setText(R.string.descriptionRSSI);
                break;
            case "Margin":
                text.setText(R.string.descriptionMargin);
                break;
            case "SF":
                text.setText(R.string.descriptionSF);
                break;
            case "Gateway":
                text.setText(R.string.descriptionGateway);
                break;
            case "ReceptionInfo":
                text.setText(c.getString(R.string.descriptionSF) + "\n" + c.getString(R.string.descriptionRX) + "\n" + c.getString(R.string.descriptionDelay));
                break;
            case "EmissionCheck":
                text.setText("Margin >= " + spMarginPerfect + " -> " + c.getString(R.string.perfect) + " \n " + spMarginPerfect + " > Margin >= " + spMarginGood + "-> " + c.getString(R.string.excellent) + " \n " + spMarginGood + " > Margin >= " + spMarginBad + " -> " + c.getString(R.string.good) + " \n " + spMarginBad + " >= Margin -> " + c.getString(R.string.bad));
                break;
            case "ReceptionCheck":
                image.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
                text.setText("A:" + spSNRPerfect + "\nB:" + spSNRBad + "\nC:" + spRSSIBad + "\nD:" + spRSSIPerfect);
                break;
            default:
                text.setText(R.string.error);
        }
        return v;
    }
}