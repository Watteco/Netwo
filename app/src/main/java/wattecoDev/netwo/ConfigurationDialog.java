package wattecoDev.netwo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import java.util.Objects;

public class ConfigurationDialog extends DialogFragment implements View.OnClickListener{

    EditText SNRPerfectEditText,SNRBadEditText,MarginPerfectEditText,MarginGoodEditText,MarginBadEditText,RSSIPerfectEditText,RSSIBadEditText;

    int MarginPerfect,MarginGood,MarginBad,SNRPerfect,SNRBad,RSSIPerfect,RSSIBad;
    int resetMarginPerfect = 15, resetMarginGood = 10, resetMarginBad = 5, resetSNRPerfect = -5, resetSNRBad = -10, resetRSSIPerfect = -107, resetRSSIBad = -118;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_configuration_dialog, container, false);
        Objects.requireNonNull(getDialog()).setCanceledOnTouchOutside(true);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        int spMarginPerfect = sharedPref.getInt("MarginPerfect", 15);
        int spMarginGood = sharedPref.getInt("MarginGood", 10);
        int spMarginBad = sharedPref.getInt("MarginBad", 5);

        int spSNRPerfect = sharedPref.getInt("SNRPerfect", -5);
        int spSNRBad = sharedPref.getInt("SNRBad", -10);

        int spRSSIPerfect = sharedPref.getInt("RSSIPerfect", -107);
        int spRSSIBad = sharedPref.getInt("RSSIBad", -118);

        MarginPerfectEditText = v.findViewById(R.id.MarginPerfect);
        MarginPerfectEditText.setText(Integer.toString(spMarginPerfect));
        MarginGoodEditText = v.findViewById(R.id.MarginGood);
        MarginGoodEditText.setText(Integer.toString(spMarginGood));
        MarginBadEditText = v.findViewById(R.id.MarginBad);
        MarginBadEditText.setText(Integer.toString(spMarginBad));

        SNRPerfectEditText = v.findViewById(R.id.SNRPerfect);
        SNRPerfectEditText.setText(Integer.toString(spSNRPerfect));
        SNRBadEditText = v.findViewById(R.id.SNRBad);
        SNRBadEditText.setText(Integer.toString(spSNRBad));

        RSSIPerfectEditText = v.findViewById(R.id.RSSIPerfect);
        RSSIPerfectEditText.setText(Integer.toString(spRSSIPerfect));
        RSSIBadEditText = v.findViewById(R.id.RSSIBad);
        RSSIBadEditText.setText(Integer.toString(spRSSIBad));

        v.findViewById(R.id.updateConfig).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //It is just to make the keyboard disappear
                        InputMethodManager mgr = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        mgr.hideSoftInputFromWindow(MarginPerfectEditText.getWindowToken(), 0);
                        mgr.hideSoftInputFromWindow(MarginGoodEditText.getWindowToken(), 0);
                        mgr.hideSoftInputFromWindow(MarginBadEditText.getWindowToken(), 0);
                        mgr.hideSoftInputFromWindow(SNRPerfectEditText.getWindowToken(), 0);
                        mgr.hideSoftInputFromWindow(SNRBadEditText.getWindowToken(), 0);
                        mgr.hideSoftInputFromWindow(RSSIPerfectEditText.getWindowToken(), 0);
                        mgr.hideSoftInputFromWindow(RSSIBadEditText.getWindowToken(), 0);

                            MarginPerfect = Integer.parseInt(MarginPerfectEditText.getText().toString());
                            MarginGood = Integer.parseInt(MarginGoodEditText.getText().toString());
                            MarginBad = Integer.parseInt(MarginBadEditText.getText().toString());

                            RSSIPerfect = Integer.parseInt(RSSIPerfectEditText.getText().toString());
                            RSSIBad = Integer.parseInt(RSSIBadEditText.getText().toString());

                            SNRPerfect = Integer.parseInt(SNRPerfectEditText.getText().toString());
                            SNRBad = Integer.parseInt(SNRBadEditText.getText().toString());



                            if((MarginPerfect > MarginGood && MarginGood > MarginBad) && (RSSIPerfect > RSSIBad) && (SNRPerfect > SNRBad)) {

                                SharedPreferences sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();

                                editor.putInt("MarginPerfect", MarginPerfect);
                                editor.putInt("MarginGood", MarginGood);
                                editor.putInt("MarginBad", MarginBad);

                                editor.putInt("RSSIPerfect", RSSIPerfect);
                                editor.putInt("RSSIBad", RSSIBad);

                                editor.putInt("SNRPerfect", SNRPerfect);
                                editor.putInt("SNRBad", SNRBad);

                                editor.apply();

                                ConfigurationDialog.this.dismiss();
                            }else{
                                Toast.makeText(getActivity(), R.string.errorThrehsold, Toast.LENGTH_LONG).show();
                            }


                    }
                }
        );
        v.findViewById(R.id.resetConfig).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                            //It is just to make the keyboard disappear
                            InputMethodManager mgr = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            mgr.hideSoftInputFromWindow(MarginPerfectEditText.getWindowToken(), 0);
                            mgr.hideSoftInputFromWindow(MarginGoodEditText.getWindowToken(), 0);
                            mgr.hideSoftInputFromWindow(MarginBadEditText.getWindowToken(), 0);
                            mgr.hideSoftInputFromWindow(SNRPerfectEditText.getWindowToken(), 0);
                            mgr.hideSoftInputFromWindow(SNRBadEditText.getWindowToken(), 0);
                            mgr.hideSoftInputFromWindow(RSSIPerfectEditText.getWindowToken(), 0);
                            mgr.hideSoftInputFromWindow(RSSIBadEditText.getWindowToken(), 0);


                            //We reset all the editable
                            MarginPerfectEditText.setText(Integer.toString(resetMarginPerfect));
                            MarginGoodEditText.setText(Integer.toString(resetMarginGood));
                            MarginBadEditText.setText(Integer.toString(resetMarginBad));

                            SNRPerfectEditText.setText(Integer.toString(resetSNRPerfect));
                            SNRBadEditText.setText(Integer.toString(resetSNRBad));

                            RSSIPerfectEditText.setText(Integer.toString(resetRSSIPerfect));
                            RSSIBadEditText.setText(Integer.toString(resetRSSIBad));


                        }


                }
        );
        return v;
    }

    @Override
    public void onClick(View v) {

    }
}