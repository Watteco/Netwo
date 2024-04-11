package wattecoDev.netwo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SendFragment extends DialogFragment {
    int resetNbFrame = 5, resetADR = 0;
    String resetSF = "12,5", resetComment = "";

    private enum Connected {False, Pending, True}

    String DEVEUI;
    CheckBox checkBoxADR;
    EditText editNumber;
    EditText Comment;
    Spinner spinnerSF, spinnerDEVEUI;
    ProgressBar progressBar;
    ImageView checked;
    OnMyDialogResult mDialogResult;
    OnMyDialogUpdate mDialogUpdateAPP;
    OnMyDialogUpdate mDialogUpdateDEV;

    String NumberValue, SFValue, SFValueIndex, ADRValue, CommentValue;
    boolean initSpinnerAPP = true;
    boolean initSpinnerDEV = true;

    TerminalFragment terminalFragment;

    static SendFragment newInstance() {
        return new SendFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arg = getArguments();
        assert arg != null;

        DEVEUI = arg.getString("DEVEUI");

        terminalFragment = (TerminalFragment) getFragmentManager().findFragmentByTag("terminal");
        assert terminalFragment != null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_send, container, false);
        Objects.requireNonNull(getDialog()).setCanceledOnTouchOutside(true);

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        String spNumberValue = sharedPref.getString("NumberValue", String.valueOf(resetNbFrame));
        String tmpSFValue = sharedPref.getString("SFValue", String.valueOf(resetSF));
        String spSFValue = tmpSFValue.split(",")[0];
        String spSFValueIndex = tmpSFValue.split(",")[1];
        String spADRValue = sharedPref.getString("ADRValue", String.valueOf(resetADR));
        String spCommentValue = sharedPref.getString("CommentValue", String.valueOf(resetComment));

        int spSpinnerEUI = 2;

        spinnerDEVEUI = v.findViewById(R.id.sendSpinnerDEVEUI);

        progressBar = v.findViewById(R.id.indeterminateBar);

        checked = v.findViewById(R.id.checkedProgress);


        if (DEVEUI.equals("Not available")) {
            spinnerDEVEUI.setEnabled(false);
        } else {


            List<String> arrayListDEVEUI = new ArrayList<>();
            for (int i = 0; i <= spSpinnerEUI; i++) {
                arrayListDEVEUI.add(DEVEUI.substring(0, 10) + i + DEVEUI.substring(11));
            }


            ArrayAdapter<CharSequence> adapterDEVEUI = new ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, arrayListDEVEUI);
            adapterDEVEUI.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinnerDEVEUI.setAdapter(adapterDEVEUI);

            spinnerDEVEUI.setSelection(arrayListDEVEUI.indexOf(DEVEUI));

        }

        spinnerDEVEUI.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (mDialogUpdateDEV != null && !initSpinnerDEV) {
                    mDialogUpdateDEV.update(spinnerDEVEUI.getSelectedItem().toString());
                    terminalFragment.setConnection(TerminalFragment.Connected.False);

                    progressBar.setVisibility(View.VISIBLE);
                    v.findViewById(R.id.sendParameters).setClickable(false);
                    spinnerDEVEUI.setEnabled(false);
                    final Handler handler = new Handler();
                    final int delay = 1000; // 1000 milliseconds == 1 second

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if ((terminalFragment.getConnection() != TerminalFragment.Connected.True || terminalFragment.getConnection().equals(TerminalFragment.Connected.Pending)) && !terminalFragment.DEVEUI.equals(spinnerDEVEUI.getSelectedItem().toString())) {
                                handler.postDelayed(this, delay);
                            } else {
                                progressBar.setVisibility(View.GONE);
                                checked.setVisibility(View.VISIBLE);
                                handler.postDelayed(() -> {
                                    checked.setVisibility(View.GONE);
                                    v.findViewById(R.id.sendParameters).setClickable(true);
                                    spinnerDEVEUI.setEnabled(true);
                                }, 3000);
                            }
                        }
                    }, delay);

                    handler.postDelayed(() -> {
                        progressBar.setVisibility(View.GONE);
                        checked.setVisibility(View.GONE);
                        v.findViewById(R.id.sendParameters).setClickable(true);
                        spinnerDEVEUI.setEnabled(true);
                    }, 20000);

                }
                initSpinnerDEV = false;
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });


        spinnerSF = v.findViewById(R.id.sendSpinnerSF);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.SF_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSF.setAdapter(adapter);
        spinnerSF.setSelection(Integer.parseInt(spSFValueIndex));

        editNumber = v.findViewById(R.id.sendNumber);
        editNumber.setText(spNumberValue);

        Comment = v.findViewById(R.id.sendComment);
        Comment.setText(spCommentValue);

        checkBoxADR = v.findViewById(R.id.sendADR);
        checkBoxADR.setChecked(spADRValue.equals("1"));

        v.findViewById(R.id.sendParameters).setOnClickListener(
                v12 -> {
                    if( mDialogResult != null){
                        TerminalFragment.Connected connected = terminalFragment.getConnection();
                        if(connected.equals(TerminalFragment.Connected.True)){

                            SFValue = (String) spinnerSF.getSelectedItem();
                            SFValueIndex = String.valueOf(spinnerSF.getSelectedItemPosition());
                            if(editNumber.getText().toString().isEmpty()) editNumber.setText("5");

                            NumberValue = editNumber.getText().toString();
                            CommentValue = Comment.getText().toString();

                            DEVEUI = (String) spinnerDEVEUI.getSelectedItem();

                            if(Integer.parseInt(NumberValue) >= 1 && Integer.parseInt(NumberValue) < 100) {

                                ADRValue = checkBoxADR.isChecked() ? "1" : "0";

                                SharedPreferences.Editor editor = sharedPref.edit();

                                editor.putString("NumberValue", NumberValue);
                                editor.putString("SFValue", SFValue + "," + SFValueIndex);
                                editor.putString("ADRValue", ADRValue);
                                editor.putString("CommentValue", CommentValue);
                                editor.apply();

                                mDialogResult.finish("");

                                InputMethodManager mgr = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                mgr.hideSoftInputFromWindow(editNumber.getWindowToken(), 0);
                                SendFragment.this.dismiss();
                            }else{
                                Toast.makeText(getActivity(), getContext().getResources().getString(R.string.wrongNbrTrame), Toast.LENGTH_LONG).show();
                            }


                        }else{
                            Toast.makeText(getContext(),"Waiting for BLE connection to establish", Toast.LENGTH_LONG).show();
                        }
                    }

                }
        );
        v.findViewById(R.id.resetParameters).setOnClickListener(
                v1 -> {
                    if( mDialogResult != null ){

                        editNumber.setText(Integer.toString(resetNbFrame));

                        Comment.setText(resetComment);

                        spinnerSF.setSelection(Integer.parseInt(resetSF.split(",")[1]), true);

                        checkBoxADR.setChecked(resetADR != 0);

                        InputMethodManager mgr = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        mgr.hideSoftInputFromWindow(editNumber.getWindowToken(), 0);
                    }
                }
        );
        return v;
    }

    public void setDialogResult(OnMyDialogResult dialogResult){ mDialogResult = dialogResult; }

    public interface OnMyDialogResult{
        void finish(String result);
    }


    public void setDEVEUI(OnMyDialogUpdate dialogUpdate){
        mDialogUpdateDEV = dialogUpdate;
    }

    public interface OnMyDialogUpdate{
        void update(String eui);
    }
}

