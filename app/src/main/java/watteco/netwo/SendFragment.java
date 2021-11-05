package watteco.netwo;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
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
    int resetSF = 5, resetNbFrame = 5, resetADR = 0;
    private enum Connected {False, Pending, True}
    String APPEUI, DEVEUI;
    Integer defaultSpinnerEUI;
    CheckBox checkBoxADR;
    EditText editNumber;
    Spinner spinnerSF, spinnerAPPEUI, spinnerDEVEUI;
    OnMyDialogResult mDialogResult;
    OnMyDialogUpdate mDialogUpdateAPP;
    OnMyDialogUpdate mDialogUpdateDEV;

    String NumberValue,SFValue,ADRValue;
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

        APPEUI = arg.getString("APPEUI");
        DEVEUI = arg.getString("DEVEUI");
        defaultSpinnerEUI = arg.getInt("defaultSpinnerEUI");

        terminalFragment = (TerminalFragment) getFragmentManager().findFragmentByTag("terminal");
        assert terminalFragment != null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_send, container, false);
        Objects.requireNonNull(getDialog()).setCanceledOnTouchOutside(true);

        spinnerAPPEUI = v.findViewById(R.id.sendSpinnerAPPEUI);
        spinnerDEVEUI = v.findViewById(R.id.sendSpinnerDEVEUI);

        List<String> arrayListAPPEUI = new ArrayList<>();

        for (int i = 0; i < defaultSpinnerEUI; i++) {
            arrayListAPPEUI.add("70B3D5E75F60000" + i);
        }

        List<String> arrayListDEVEUI = new ArrayList<>();
        for (int i = 0; i < defaultSpinnerEUI; i++) {
            arrayListDEVEUI.add("70B3D5E75E" + i + "0DEDC");
        }

        ArrayAdapter<CharSequence> adapterAPPEUI = new ArrayAdapter(getContext(),R.layout.support_simple_spinner_dropdown_item,arrayListAPPEUI);
        adapterAPPEUI.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> adapterDEVEUI = new ArrayAdapter(getContext(),R.layout.support_simple_spinner_dropdown_item,arrayListDEVEUI);
        adapterDEVEUI.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerAPPEUI.setAdapter(adapterAPPEUI);
        spinnerAPPEUI.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(mDialogUpdateAPP != null && !initSpinnerAPP){
                    mDialogUpdateAPP.update(spinnerAPPEUI.getSelectedItem().toString());
                    terminalFragment.setConnection(TerminalFragment.Connected.False);
                }
                initSpinnerAPP = false;
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });
        spinnerAPPEUI.setSelection(arrayListAPPEUI.indexOf(APPEUI));


        spinnerDEVEUI.setAdapter(adapterDEVEUI);
        spinnerDEVEUI.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(mDialogUpdateDEV != null && !initSpinnerDEV){
                    mDialogUpdateDEV.update(spinnerDEVEUI.getSelectedItem().toString());
                    terminalFragment.setConnection(TerminalFragment.Connected.False);
                }
                initSpinnerDEV = false;
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });
        spinnerDEVEUI.setSelection(arrayListDEVEUI.indexOf(DEVEUI));
        spinnerSF = v.findViewById(R.id.sendSpinnerSF);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.SF_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSF.setAdapter(adapter);
        spinnerSF.setSelection(5);


        editNumber = v.findViewById(R.id.sendNumber);
        editNumber.setText(Integer.toString(resetNbFrame));


        checkBoxADR = v.findViewById(R.id.sendADR);
        checkBoxADR.setChecked(resetADR != 0);

        v.findViewById(R.id.sendParameters).setOnClickListener(
                v12 -> {


                    if( mDialogResult != null){


                        TerminalFragment.Connected connected = terminalFragment.getConnection();
                        if(connected.equals(TerminalFragment.Connected.True)){


                            SFValue = (String) spinnerSF.getSelectedItem();
                            if(editNumber.getText().toString().isEmpty()) editNumber.setText("5");

                            NumberValue = editNumber.getText().toString();

                            APPEUI = (String) spinnerAPPEUI.getSelectedItem();
                            DEVEUI = (String) spinnerDEVEUI.getSelectedItem();

                            if(Integer.parseInt(NumberValue) >= 1 && Integer.parseInt(NumberValue) < 100){

                                ADRValue = checkBoxADR.isChecked() ? "1" : "0";

                                List<String> list = new ArrayList<>();
                                list.add(SFValue);
                                list.add(NumberValue);
                                list.add(ADRValue);
                                list.add(APPEUI);
                                list.add(DEVEUI);

                                mDialogResult.finish(list);
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

                        spinnerDEVEUI.setSelection(0);
                        spinnerAPPEUI.setSelection(0);

                        spinnerSF.setSelection(resetSF,true);

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
        void finish(List<String> result);
    }

    public void setAPPEUI(OnMyDialogUpdate dialogUpdate){
        mDialogUpdateAPP = dialogUpdate;
    }

    public void setDEVEUI(OnMyDialogUpdate dialogUpdate){
        mDialogUpdateDEV = dialogUpdate;
    }

    public interface OnMyDialogUpdate{
        void update(String eui);
    }
}

