package watteco.netwo;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConfigurationDialog extends DialogFragment implements View.OnClickListener{

    EditText SNRPerfectEditText,SNRBadEditText,MarginPerfectEditText,MarginGoodEditText,MarginBadEditText,RSSIPerfectEditText,RSSIBadEditText;
    Spinner spinnerEUI;

    int MarginPerfect,MarginGood,MarginBad,SNRPerfect,SNRBad,RSSIPerfect,RSSIBad;
    int resetMarginPerfect = 15, resetMarginGood = 10, resetMarginBad = 5, resetSNRPerfect = -5, resetSNRBad = -10, resetRSSIPerfect = -107, resetRSSIBad = -118;

   int defaultSpinnerEUI;
   int resetDefaultSpinnerEUI = 3;

   OnMyDialogResult mDialogResult;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arg = getArguments();
        assert arg != null;
        MarginPerfect = arg.getInt("MarginPerfect");
        MarginGood = arg.getInt("MarginGood");
        MarginBad = arg.getInt("MarginBad");

        SNRPerfect = arg.getInt("SNRPerfect");
        SNRBad = arg.getInt("SNRBad");

        RSSIPerfect = arg.getInt("RSSIPerfect");
        RSSIBad = arg.getInt("RSSIBad");

        defaultSpinnerEUI = arg.getInt("defaultSpinnerEUI");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_configuration_dialog, container, false);
        Objects.requireNonNull(getDialog()).setCanceledOnTouchOutside(true);

        spinnerEUI = v.findViewById(R.id.configEUI);

        ArrayList<Integer> arrayEUI = new ArrayList<>();
        for(int i = 0; i < 10 ; i++){
            arrayEUI.add(i+1);
        }

        ArrayAdapter arrayAdapter = new ArrayAdapter(getContext(),R.layout.support_simple_spinner_dropdown_item,arrayEUI);
        spinnerEUI.setAdapter(arrayAdapter);
        spinnerEUI.setSelection(2);

        MarginPerfectEditText = v.findViewById(R.id.MarginPerfect);
        MarginPerfectEditText.setText(Integer.toString(MarginPerfect));
        MarginGoodEditText = v.findViewById(R.id.MarginGood);
        MarginGoodEditText.setText(Integer.toString(MarginGood));
        MarginBadEditText = v.findViewById(R.id.MarginBad);
        MarginBadEditText.setText(Integer.toString(MarginBad));

        SNRPerfectEditText = v.findViewById(R.id.SNRPerfect);
        SNRPerfectEditText.setText(Integer.toString(SNRPerfect));
        SNRBadEditText = v.findViewById(R.id.SNRBad);
        SNRBadEditText.setText(Integer.toString(SNRBad));

        RSSIPerfectEditText = v.findViewById(R.id.RSSIPerfect);
        RSSIPerfectEditText.setText(Integer.toString(RSSIPerfect));
        RSSIBadEditText = v.findViewById(R.id.RSSIBad);
        RSSIBadEditText.setText(Integer.toString(RSSIBad));

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
                        if( mDialogResult != null ){

                            MarginPerfect = Integer.parseInt(MarginPerfectEditText.getText().toString());
                            MarginGood = Integer.parseInt(MarginGoodEditText.getText().toString());
                            MarginBad = Integer.parseInt(MarginBadEditText.getText().toString());

                            RSSIPerfect = Integer.parseInt(RSSIPerfectEditText.getText().toString());
                            RSSIBad = Integer.parseInt(RSSIBadEditText.getText().toString());

                            SNRPerfect = Integer.parseInt(SNRPerfectEditText.getText().toString());
                            SNRBad = Integer.parseInt(SNRBadEditText.getText().toString());


                            if((MarginPerfect > MarginGood && MarginGood > MarginBad) && (RSSIPerfect > RSSIBad) && (SNRPerfect > SNRBad)){
                                List<Integer> list = new ArrayList<>();
                                list.add(MarginPerfect);
                                list.add(MarginGood);
                                list.add(MarginBad);
                                list.add(RSSIPerfect);
                                list.add(RSSIBad);
                                list.add(SNRPerfect);
                                list.add(SNRBad);
                                list.add((Integer) spinnerEUI.getSelectedItem());
                                mDialogResult.finish(list);
                                ConfigurationDialog.this.dismiss();
                            }else{
                                Toast.makeText(getActivity(), R.string.errorThrehsold, Toast.LENGTH_LONG).show();
                            }

                        }

                    }
                }
        );
        v.findViewById(R.id.resetConfig).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if( mDialogResult != null ){

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

                            spinnerEUI.setSelection(2);

                            // We refresh the list to give back to the terminal fragment
                            List<Integer> list = new ArrayList<>();
                            list.add(resetMarginPerfect);
                            list.add(resetMarginGood);
                            list.add(resetMarginBad);
                            list.add(resetSNRPerfect);
                            list.add(resetSNRBad);
                            list.add(resetRSSIPerfect);
                            list.add(resetRSSIBad);
                            list.add(resetDefaultSpinnerEUI);

                            mDialogResult.finish(list);
                        }

                    }
                }
        );
        return v;
    }

    public void setDialogResult(OnMyDialogResult dialogResult){
        mDialogResult = dialogResult;
    }

    public interface OnMyDialogResult{
        void finish(List<Integer> result);
    }

    @Override
    public void onClick(View v) {

    }
}