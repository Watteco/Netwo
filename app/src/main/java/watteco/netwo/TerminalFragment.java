package watteco.netwo;

import static android.content.Context.LOCATION_SERVICE;
import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;
import static android.graphics.Color.WHITE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;





public class TerminalFragment extends Fragment implements ServiceConnection, SerialListener {

    //Variable par defaut configurable
    int MarginPerfect = 15, MarginGood = 10, MarginBad = 5, SNRPerfect = -5, SNRBad = -10, RSSIPerfect = -107, RSSIBad = -118;
    private final List<String> allTXInfo = new ArrayList<>();
    private static final String TAG = "TerminalFragment";

    enum Connected {False, Pending, True}
    private String deviceAddress;
    private String deviceName;

    private TextView receiveText;
    private TextView batteryTextTX;
    private TextView batteryTextRX;
    private LinearLayout linearLayoutGraphTX;
    private LinearLayout linearLayoutGraphRX;
    private LineChart graphSFRX;
    private LineChart graphSNR;
    private LineChart graphSFTX;
    private LineChart graphGateway;
    private LineChart graphRSSI;
    private LineChart graphMargin;
    private ImageView batteryImageTX;
    private ImageView batteryImageRX;
    private String currentMode = "simplified";
    private TextView percentageReceivedDataTX;
    private TextView percentageReceivedDataRX;
    private View viewBeforeSendText;
    private LinearLayout linearSendText;
    private LinearLayout linearSimplified;


    private TextView simplifiedGateway;
    private TextView simplifiedMargin;
    private TextView simplifiedRSSI;
    private TextView simplifiedSNR;
    private TextView simplifiedEmission;
    private TextView simplifiedReceptionInfo;
    private TextView simplifiedSend;
    private TextView simplifiedOperator;
    private ImageView simplifiedEmissionCheck;
    private ImageView simplifiedReceptionCheck;
    private ImageView simplifiedBatteryImage;
    private ImageView simplifiedGatewayImage;
    private TextView simplifiedBatteryText;


    int paramSF, paramADR, paramNumber;
    private List<String> datas = new ArrayList<>();
    private JSONArray reportData = new JSONArray();
    private int reportDataCount;
    private String reportType;

    private List<String> allCurrentNumber = new ArrayList<>();
    private List<String> allNumber = new ArrayList<>();
    private final Integer defaultSpinnerEUI = 3;
    private List<Integer> allGateway = new ArrayList<>();
    private List<Integer> allAverageGateway = new ArrayList<>();
    private List<Integer> allMargin = new ArrayList<>();
    private List<Integer> allAverageMargin = new ArrayList<>();
    private List<Integer> allSNR = new ArrayList<>();
    private List<Integer> allAverageSNR = new ArrayList<>();
    private List<Integer> allRSSI = new ArrayList<>();
    private List<Integer> allAverageRSSI = new ArrayList<>();
    private List<Integer> allSFTX = new ArrayList<>();
    private List<Integer> allSFRX = new ArrayList<>();
    private List<Integer> allWindows = new ArrayList<>();
    private List<Integer> allDelay = new ArrayList<>();
    private List<Integer> allOperator = new ArrayList<>();
    private final Hashtable allOperatorName = new Hashtable();
    private List<Float> allBatteryVoltage = new ArrayList<>();

    private long firstTimestamp = 0;
    private Integer offset = 0;
    private String lastTXInfo = "";
    private SerialService service;
    private boolean initialStart = true;
    public Connected connected = Connected.False;

    private String APPEUI = "Not available";
    private String APPEUI_value = "";

    private String DEVEUI = "Not available";
    private String DEVEUI_value = "";
    int spMarginPerfect, spMarginGood, spMarginBad, spRSSIPerfect, spRSSIBad, spSNRPerfect, spSNRBad, spSpinnerEUI;
    SerialSocket socket;
    FusedLocationProviderClient mFusedLocationClient;
    Location gLocation;

    SwipeRefreshLayout swipeRefreshLayout;

    /*
     * Lifecycle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        assert getArguments() != null;
        deviceAddress = getArguments().getString("device");
        deviceName = getArguments().getString("deviceName");

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        getLastLocation();

    }


    @Override
    public void onDestroy() {

        disconnect();
        requireActivity().stopService(new Intent(getActivity(), SerialService.class));
        super.onDestroy();

    }

    public void onDestroyView() {
        disconnect();
        requireActivity().stopService(new Intent(getActivity(), SerialService.class));
        service.detach();
        super.onDestroyView();
    }
    @Override
    public void onStart() {
        super.onStart();
        if (service != null)
            service.attach(this);
        else
            requireActivity().startService(new Intent(getActivity(), SerialService.class)); // prevents service destroy on unbind from recreated activity caused by orientation change
    }

    @Override
    public void onStop() {
        if (service != null && !requireActivity().isChangingConfigurations())
            service.detach();
        super.onStop();
    }

    @SuppressWarnings("deprecation")
    // onAttach(context) was added with API 23. onAttach(activity) works for all API versions
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        requireActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try {
            requireActivity().unbindService(this);
        } catch (Exception ignored) {
        }
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();


        if (initialStart && service != null) {
            initialStart = false;
            requireActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if (initialStart && isResumed()) {
            initialStart = false;
            requireActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    /*
     * UI
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_terminal, container, false);

        Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(deviceName);

        // Change the size of title size
        View tmpView = toolbar.getChildAt(0);
        if (tmpView instanceof TextView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ((TextView) tmpView).setHorizontallyScrolling(false);
                ((TextView) tmpView).setAutoSizeTextTypeUniformWithConfiguration(13, 15, 1, 1);
            }
        }

        SharedPreferences sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE);

        spMarginPerfect = sharedPref.getInt("MarginPerfect", MarginPerfect);
        spMarginGood = sharedPref.getInt("MarginGood", MarginGood);
        spMarginBad = sharedPref.getInt("MarginBad", MarginBad);

        spSNRPerfect = sharedPref.getInt("SNRPerfect", SNRPerfect);
        spSNRBad = sharedPref.getInt("SNRBad", SNRBad);

        spRSSIPerfect = sharedPref.getInt("RSSIPerfect", RSSIPerfect);
        spRSSIBad = sharedPref.getInt("RSSIBad", RSSIBad);

        spSpinnerEUI = sharedPref.getInt("spinnerEUI", defaultSpinnerEUI) - 1;

        receiveText = view.findViewById(R.id.textview);// TextView performance decreases with number of spans
        receiveText.setTextColor(getResources().getColor(R.color.colorRecieveText)); // set as default color to reduce number of spans
        receiveText.setMovementMethod(ScrollingMovementMethod.getInstance());

        graphSNR = view.findViewById(R.id.lineChartSNR);
        graphRSSI = view.findViewById(R.id.lineChartRSSI);
        graphSFTX = view.findViewById(R.id.lineChartSFTX);
        graphGateway = view.findViewById(R.id.lineChartGateway);

        graphMargin = view.findViewById(R.id.lineChartMargin);
        graphSFRX = view.findViewById(R.id.lineChartSFRX);

        graphSNR.getXAxis().setAxisMaximum(2);
        graphSNR.getXAxis().setAxisMaximum(2);
        graphRSSI.getXAxis().setAxisMaximum(2);
        graphMargin.getXAxis().setAxisMaximum(2);
        graphSFRX.getXAxis().setAxisMaximum(2);
        graphSFTX.getXAxis().setAxisMaximum(2);
        graphGateway.getXAxis().setAxisMaximum(2);


        linearLayoutGraphTX = view.findViewById(R.id.graphModeTX);
        linearLayoutGraphRX = view.findViewById(R.id.graphModeRX);
        linearSimplified = view.findViewById(R.id.linearsimplifiedmode);

        percentageReceivedDataTX = view.findViewById(R.id.percentageDataReceiveTX);
        percentageReceivedDataRX = view.findViewById(R.id.percentageDataReceiveRX);

        batteryTextTX = view.findViewById(R.id.batteryTextTX);
        batteryImageTX = view.findViewById(R.id.batteryImageTX);

        batteryTextRX = view.findViewById(R.id.batteryTextRX);
        batteryImageRX = view.findViewById(R.id.batteryImageRX);

        viewBeforeSendText = view.findViewById(R.id.viewBeforeSendText);
        linearSendText = view.findViewById(R.id.linearSendText);


        simplifiedGateway = view.findViewById(R.id.simplifiedGateway);
        simplifiedGateway.setOnLongClickListener(v -> {
            showDialog("Gateway");
            return true;
        });

        simplifiedGatewayImage = view.findViewById(R.id.simplifiedGatewayImage);
        simplifiedGatewayImage.setOnLongClickListener(v -> {
            showDialog("Gateway");
            return true;
        });
        simplifiedMargin = view.findViewById(R.id.simplifiedMargin);
        simplifiedMargin.setOnLongClickListener(v -> {
            showDialog("Margin");
            return true;
        });
        simplifiedRSSI = view.findViewById(R.id.simplifiedRSSI);
        simplifiedRSSI.setOnLongClickListener(v -> {
            showDialog("RSSI");
            return true;
        });
        simplifiedSNR = view.findViewById(R.id.simplifiedSNR);
        simplifiedSNR.setOnLongClickListener(v -> {
            showDialog("SNR");
            return true;
        });
        simplifiedEmission = view.findViewById(R.id.simplifiedEmission);
        simplifiedEmission.setOnLongClickListener(v -> {
            showDialog("SF");
            return false;
        });

        simplifiedReceptionInfo = view.findViewById(R.id.simplifiedReceptionInfo);
        simplifiedReceptionInfo.setOnLongClickListener(v -> {
            showDialog("ReceptionInfo");
            return false;
        });

        simplifiedSend = view.findViewById(R.id.simplifiedSend);
        simplifiedSend.setOnClickListener(v -> {
            showDialogSend();
        });

        simplifiedOperator = view.findViewById(R.id.simplifiedOperator);

        allOperatorName.put(0, "private");
        allOperatorName.put(2, "actility");
        allOperatorName.put(3, "proximus");
        allOperatorName.put(4, "swisscom");
        allOperatorName.put(7, "objenious");
        allOperatorName.put(8, "orbiwise");
        allOperatorName.put(10, "kpn");
        allOperatorName.put(15, "orange");
        allOperatorName.put(18, "kerlink");
        allOperatorName.put(19, "ttn");
        allOperatorName.put(21, "cisco");
        allOperatorName.put(23, "multitech");
        allOperatorName.put(24, "loriot");
        allOperatorName.put(55, "Tektelic");

        simplifiedEmissionCheck = view.findViewById(R.id.simplifiedEmissionCheck);
        simplifiedEmissionCheck.setOnLongClickListener(v -> {
            showDialog("EmissionCheck");
            return false;
        });
        simplifiedReceptionCheck = view.findViewById(R.id.simplifiedReceptionCheck);
        simplifiedReceptionCheck.setOnLongClickListener(v -> {
            showDialog("ReceptionCheck");
            return false;
        });
        simplifiedBatteryText = view.findViewById(R.id.simplifiedBatteryText);
        simplifiedBatteryImage = view.findViewById(R.id.simplifiedBatteryImage);

        TextView sendText = view.findViewById(R.id.send_text);
        View sendBtn = view.findViewById(R.id.send_btn);
        sendBtn.setOnClickListener(v -> {
            InputMethodManager mgr = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            mgr.hideSoftInputFromWindow(sendText.getWindowToken(), 0);
            send(sendText.getText().toString());
            sendText.setText("");
        });

        swipeRefreshLayout = requireActivity().findViewById(R.id.swipe);
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setRefreshing(false);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_terminal, menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.clear) {
            receiveText.setText("~" + R.string.clear + "~\n");
            percentageReceivedDataTX.setText("");
            percentageReceivedDataRX.setText("");
            simplifiedRSSI.setText("RSSI");
            simplifiedMargin.setText("Margin");
            simplifiedSNR.setText("SNR");
            simplifiedEmission.setText(" SF : ");
            simplifiedReceptionInfo.setText("");
            simplifiedGateway.setText("");
            simplifiedOperator.setText("");

            simplifiedReceptionCheck.setImageResource(R.drawable.wifi_question_mark_flipped);
            simplifiedReceptionCheck.setScaleX(1);
            simplifiedEmissionCheck.setImageResource(R.drawable.wifi_question_mark);
            simplifiedBatteryText.setText("");
            simplifiedBatteryImage.setImageResource(R.drawable.battery_missing);


            datas = new ArrayList<>();
            reportData = new JSONArray();
            reportDataCount = 0;
            allCurrentNumber = new ArrayList<>();
            allNumber = new ArrayList<>();
            allGateway = new ArrayList<>();
            allMargin = new ArrayList<>();
            allSNR = new ArrayList<>();
            allRSSI = new ArrayList<>();
            allSFTX = new ArrayList<>();
            allSFRX = new ArrayList<>();
            allWindows = new ArrayList<>();
            allDelay = new ArrayList<>();
            allOperator = new ArrayList<>();
            allBatteryVoltage = new ArrayList<>();

            allAverageGateway = new ArrayList<>();
            allAverageMargin = new ArrayList<>();
            allAverageRSSI = new ArrayList<>();
            allAverageSNR = new ArrayList<>();

            graphSNR.getXAxis().setAxisMaximum(2);
            graphSNR.getXAxis().setAxisMaximum(2);
            graphRSSI.getXAxis().setAxisMaximum(2);
            graphMargin.getXAxis().setAxisMaximum(2);
            graphSFRX.getXAxis().setAxisMaximum(2);
            graphSFTX.getXAxis().setAxisMaximum(2);
            graphGateway.getXAxis().setAxisMaximum(2);

            offset = 0;

            addData("SNR", graphSNR);
            addData("RSSI", graphRSSI);
            addData("Margin",graphMargin);
            addData("SFTX",graphSFTX);
            addData("Nb gateway",graphGateway);
            addData("SFRX",graphSFRX);

            drawGradient(graphSNR);
            drawGradient(graphRSSI);
            drawGradient(graphMargin);

            return true;
        } else if (id == R.id.textmode) {
            if (!currentMode.equals("text")) {
                currentMode = "text";

                linearLayoutGraphTX.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 0));
                linearLayoutGraphRX.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 0));
                linearSimplified.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 0));
                viewBeforeSendText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, (float) 0.01));
                linearSendText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, (float) 0.5));
                receiveText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, (float) 9.49));

            } else {
            }
            return true;
        } else if (id == R.id.simplifiedmode) {
            if (!currentMode.equals("simplified")) {
                currentMode = "simplified";

                receiveText.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 0));
                viewBeforeSendText.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 0));
                linearSendText.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 0));
                linearLayoutGraphTX.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 0));
                linearLayoutGraphRX.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 0));
                LinearLayout.LayoutParams linearSimplifiedLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, (float) 10);
                linearSimplifiedLayout.setMargins(15, 15, 15, 15);
                linearSimplified.setLayoutParams(linearSimplifiedLayout);
            } else {
            }
            return true;
        } else if (id == R.id.graphicalmodetx) {
            if (!currentMode.equals("graphtx")) {
                currentMode = "graphtx";

                receiveText.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 0));
                viewBeforeSendText.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 0));
                linearSendText.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 0));
                linearSimplified.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 0));
                linearLayoutGraphRX.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 0));
                LinearLayout.LayoutParams linearLayoutGraphParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, (float) 10);
                linearLayoutGraphParam.setMargins(15, 15, 15, 15);
                linearLayoutGraphTX.setLayoutParams(linearLayoutGraphParam);

                drawGraph("Margin", 0f, 55f, graphMargin);
                drawGraph("SFTX", 6f, 13f, graphSFTX);
                drawGraph("Nb gateway", 0f, 10f, graphGateway);
            } else {
            }
            return true;
        } else if (id == R.id.graphicalmoderx) {
            if (!currentMode.equals("graphrx")) {
                currentMode = "graphrx";

                receiveText.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 0));
                viewBeforeSendText.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 0));
                linearSendText.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 0));
                linearSimplified.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 0));
                linearLayoutGraphTX.setLayoutParams(new LinearLayout.LayoutParams(0, 0, 0));
                LinearLayout.LayoutParams linearLayoutGraphParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, (float) 10);
                linearLayoutGraphParam.setMargins(15, 15, 15, 15);
                linearLayoutGraphRX.setLayoutParams(linearLayoutGraphParam);

                drawGraph("SFTX", 6f, 13f, graphSFRX);
                drawGraph("SNR", -20f, 15f, graphSNR);
                drawGraph("RSSI", -130f, -20f, graphRSSI);
            } else {
            }
            return true;
        } else if (id == R.id.configuration) {
            showDialogConfiguration();
            return true;
        } else if (id == R.id.about) {
            showDialog("about");
            return true;
        } else if (id == R.id.back) {
            requireActivity().getSupportFragmentManager().popBackStack();
            return true;
        } else if (id == R.id.supportRedirect2){
            redirectOnSupport();
            return true;
        }
        else if (id == R.id.envoi) {
            showDialogReport();
            return true;
        }
        else if(id == R.id.sendLogs){
            showLogsReport();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showLogsReport() {
        assert getFragmentManager() != null;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        LogsFragment logsFragment = new LogsFragment();

        Bundle bundle = new Bundle();

        String logsString = "";
        for(String log : socket.logs){
            logsString += log + "\n";
        }
        
        bundle.putString("Logs",logsString);

        logsFragment.setArguments(bundle);

        logsFragment.show(ft, "Logs Fragment");


        logsFragment.setDialogResult(result ->

                {
                    String r = result;
                    sendLogs();
                }
        );


    }

    private void sendLogs() {
        String fileName;
        long tsLong = System.currentTimeMillis()/1000;
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(tsLong * 1000L);
        String date = DateFormat.format("dd-MM-yyyy hh:mm:ss", cal).toString();

        fileName = "reportLogs_" + deviceName + "_" + date +".txt";



        File file = new File(requireContext().getCacheDir(), fileName);
        try{
            FileWriter writer = new FileWriter(file);

            String logsString = "";
            for(String log : socket.logs){
                logsString += log + "\n";
            }

            writer.append(logsString);
            writer.flush();
            writer.close();
        }catch (Exception e){
            Toast.makeText(getContext(), "File writing failed: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        File filePath = new File(getContext().getCacheDir(), "");
        File newFile = new File(filePath, fileName);
        Uri uri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", newFile);

        Intent intent = ShareCompat.IntentBuilder.from((Activity) getContext())
                .setType("text/plain")
                .setStream(uri)
                .setChooserTitle("Choose bar")
                .createChooserIntent()
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(intent);
    }

    private void redirectOnSupport() {
        Uri webpage = Uri.parse("https://support.nke-watteco.com/netwo/");
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /*
     * Serial + UI
     */
    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            status("connecting...");
            connected = Connected.Pending;
            socket = new SerialSocket(getActivity().getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = Connected.False;
        service.disconnect();
    }

    private void send(String str) {
        if(connected != Connected.True) {
            Toast.makeText(getActivity(), "not connected", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            SpannableStringBuilder spn = new SpannableStringBuilder(str+'\n');
            spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorSendText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            receiveText.append(spn);
            byte[] data = (str + "\r\n").getBytes();
            service.write(data);
        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void receive(byte[] data) {
        receiveText.append(new String(data));
        collectData(new String(data));
    }

    private void status(String str) {
        try{
            SpannableStringBuilder spn = new SpannableStringBuilder(str+'\n');
            spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            receiveText.append(spn);
        }catch(Exception e){
        }

    }

    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {
        status("connected");
        Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT).show();
        connected = Connected.True;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onSerialConnectError(Exception e) {
        try{
            status("connection failed: " + e.getMessage());
            Toast.makeText(getActivity(), "Not connected: "+  e.getMessage(), Toast.LENGTH_LONG).show();
            disconnect();

            Handler handler = new Handler();
            handler.postDelayed(this::connect, 5000);
        }catch(Exception err){
        }

    }

    public Connected getConnection(){
        return connected;
    }

    public void setConnection(Connected c){
        connected = c;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onSerialRead(byte[] data) {
        receive(data);
    }

    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        Toast.makeText(getActivity(), "Connection lost: "+  e.getMessage(), Toast.LENGTH_LONG).show();
        disconnect();
        Handler handler = new Handler();
        handler.postDelayed(this::connect, 5000);
    }


    /*
     * SerialListener
     */

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void collectData(String data){
        datas.add(data);
        try{
            if(data.contains("NOK")) {
                Toast.makeText(getActivity(), "Not ok", Toast.LENGTH_LONG).show();
                send("X");
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

                String spNumberValue = sharedPref.getString("NumberValue", "5");
                String tmpSFValue = sharedPref.getString("SFValue", "12,5");
                String spSFValue = tmpSFValue.split(",")[0];
                String spADRValue = sharedPref.getString("ADRValue", "0");

                send("S" + spNumberValue + "," + spSFValue + "," + spADRValue);
            }else if (data.contains("OK")){
                Toast.makeText(getActivity(), "Ok", Toast.LENGTH_LONG).show();

                if(!allCurrentNumber.isEmpty()){
                    offset = Integer.valueOf(allCurrentNumber.get(allCurrentNumber.size()-1).split("/")[1]);
                }
            }else{
                parseData(data);
            }

        }catch (Exception e){
            Log.e("Error", e.toString());
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void parseData(String data) throws JSONException {

        if(data.startsWith(Objects.requireNonNull(System.getProperty("line.separator")))){
            data = data.substring(1);
        }
        String[] lines = data.split(Objects.requireNonNull(System.getProperty("line.separator")));


        boolean isResult = false;
        boolean isTxInfo = false;

        for(String line: lines){
            if (line.contains("RESULT")) {
                isResult = true;
            }else if(line.contains("TX") && line.contains("/")){
                isTxInfo = true;
            }else if(line.startsWith("APPEUI")){
                APPEUI = line.split(":")[1];
                APPEUI_value = APPEUI.substring(APPEUI.length()-2);
            }else if(line.startsWith("DEVEUI")){
                DEVEUI = line.split(":")[1];
                DEVEUI_value = DEVEUI.substring(10,11);
            }
        }

        if((lines.length > 1 && !data.equals("\n")) && !isResult && !isTxInfo) {

            if(firstTimestamp == 0){
                firstTimestamp = System.currentTimeMillis()/1000;
            }
            long currentTimeStamp = System.currentTimeMillis()/1000 - firstTimestamp;

            int x = Math.round(currentTimeStamp/15);
            Integer currentNumberOfData = allCurrentNumber.size();

            // On itère sur toutes les lignes, et on vérifie si la ligne contient ce qu'on a besoin d'afficher
            int cptLine = 0;
            for(String line: lines){
                if(line.contains("/")){
                    Integer tmpValue = Integer.valueOf(lines[cptLine].split("/")[1]) + offset ;
                    String tmpString = lines[cptLine].split("/")[0] + "/"+ tmpValue;
                    allCurrentNumber.add(tmpString);
                    allNumber.add(lines[cptLine]);
                }
                if(line.contains("Gateway")) allGateway.add(Integer.valueOf(lines[cptLine].split(":")[1].trim()));
                if(line.contains("TX Margin")) allMargin.add(Integer.valueOf(lines[cptLine].split(":")[1].trim()));
                if(line.contains("TX SF")) allSFTX.add(Integer.valueOf(lines[cptLine].split(":")[1].trim()));
                if(line.contains("RX SNR")) allSNR.add(Integer.valueOf(lines[cptLine].split(":")[1].trim()));
                if(line.contains("RX RSSI")) allRSSI.add(Integer.valueOf(lines[cptLine].split(":")[1].trim()));
                if(line.contains("RX Window")) allWindows.add(Integer.valueOf(lines[cptLine].split(":")[1].trim()));
                if(line.contains("RX SF")) allSFRX.add(Integer.valueOf(lines[cptLine].split(":")[1].trim()));
                if(line.contains("RX Delay")) allDelay.add(Integer.valueOf(lines[cptLine].split(":")[1].trim()));
                if(line.contains("Battery")) allBatteryVoltage.add((float) ((Integer.parseInt(lines[cptLine].split(":")[1].trim()) /100))/10);
                if(line.contains("NetId"))  allOperator.add(Integer.valueOf(lines[cptLine].split(":")[1].trim()));
                cptLine++;
            }

            if(!currentNumberOfData.equals(allCurrentNumber.size())){
                Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                cal.setTimeInMillis(System.currentTimeMillis()/1000 * 1000L);
                String date = DateFormat.format("dd-MM-yyyy hh:mm:ss", cal).toString();

                putReportData(date);

                addData("SNR", graphSNR);
                addData("RSSI", graphRSSI);
                addData("Margin",graphMargin);
                addData("SFTX",graphSFTX);
                addData("Nb gateway",graphGateway);
                addData("SFRX",graphSFRX);

                displayPercentageOfDataReceived();
                displaySimplifiedData(false, false);
                displayIndicatorOfBattery();

                drawGradient(graphSNR);
                drawGradient(graphRSSI);
                drawGradient(graphMargin);
            }


        }else if (isResult && (!data.equals("\n") || !data.equals(""))){

            int cptLine = 0;
            for(String line: lines){
                if(line.contains("GATEWAY")) allAverageGateway.add(Integer.valueOf(lines[cptLine].split(":")[1].trim()));
                if(line.contains("MARGIN")) allAverageMargin.add(Integer.valueOf(lines[cptLine].split(":")[1].trim()));
                if(line.contains("SNR")) allAverageSNR.add(Integer.valueOf(lines[cptLine].split(":")[1].trim()));
                if(line.contains("RSSI")) allAverageRSSI.add(Integer.valueOf(lines[cptLine].split(":")[1].trim()));
                cptLine++;
            }

            offset = Integer.valueOf(allCurrentNumber.get(allCurrentNumber.size()-1).split("/")[1]);

            displaySimplifiedData(true, false);

        }else if(isTxInfo){
            for(String line: lines){
                if(line.contains("TX")){
                    lastTXInfo = line;
                    allTXInfo.add(line);

                     displaySimplifiedData(false, true);
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                // getting last
                // location from
                // FusedLocationClient
                // object
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        if (location == null) {
                            requestNewLocationData();
                        } else {
                            gLocation = location;
                        }
                    }
                });
            } else {
                Toast.makeText(getContext(), "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions();
        }
    }

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(requireActivity(), new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, 44);
    }

    private final LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            gLocation = locationResult.getLastLocation();
        }
    };

    // method to check
    // if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // If everything is alright then
    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void putReportData(String date) throws JSONException {

        JSONObject temp = new JSONObject();

        reportDataCount++;

        
        temp.put("Date",date);

        if(gLocation == null){
            try {
                temp.put("Latitude","/");
                temp.put("Longitude","/");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            getLastLocation();
        }else{
            try {
                temp.put("Latitude",gLocation.getLatitude());
                temp.put("Longitude",gLocation.getLongitude());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Integer lastMargin = allMargin.get(allMargin.size()-1);
        Integer lastRSSI = allRSSI.get(allRSSI.size()-1);
        Integer lastSNR = allSNR.get(allSNR.size()-1);
        Integer lastSFTX = allSFTX.get(allSFTX.size()-1);
        Integer lastSFRX = allSFRX.get(allSFRX.size()-1);
        Integer lastGateway = allGateway.get(allGateway.size()-1);
        Integer lastOperator = allOperator.get(allOperator.size()-1);
        Integer lastWindows = allWindows.get(allWindows.size()-1);
        Integer lastDelay = allDelay.get(allDelay.size()-1);
        String lastOperatorName = Objects.requireNonNull(allOperatorName.get(lastOperator)).toString().toUpperCase();

        temp.put("Gateway",lastGateway);
        temp.put("Margin",lastMargin);
        temp.put("SFTX",lastSFTX);
        temp.put("SNR",lastSNR);
        temp.put("RSSI",lastRSSI);
        temp.put("Windows",lastWindows);
        temp.put("SFRX",lastSFRX);
        temp.put("Delay",lastDelay);
        temp.put("OperatorIndex",lastOperator);
        temp.put("OperatorName",lastOperatorName);


        reportData.put(temp);

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void putReportDataCSV(FileWriter writer) throws IOException, JSONException {
        writer.append("Date,");
        writer.append("Latitude,");
        writer.append("Longitude,");
        writer.append("Gateway,");
        writer.append("Margin,");
        writer.append("SFTX,");
        writer.append("SNR,");
        writer.append("RSSI,");
        writer.append("RXWindow,");
        writer.append("SFRX,");
        writer.append("Delay,");
        writer.append("OperatorIndex,");
        writer.append("OperatorName");
        writer.append("\n");
        for(int i = 1; i <= reportDataCount; i++){

            JSONObject currentResult = (JSONObject) reportData.get(i-1);
            writer.append(String.valueOf(currentResult.get("Date"))).append(",");
            writer.append(String.valueOf(currentResult.get("Latitude"))).append(",");
            writer.append(String.valueOf(currentResult.get("Longitude"))).append(",");
            writer.append(String.valueOf(currentResult.get("Gateway"))).append(",");
            writer.append(String.valueOf(currentResult.get("Margin"))).append(",");
            writer.append(String.valueOf(currentResult.get("SFTX"))).append(",");
            writer.append(String.valueOf(currentResult.get("SNR"))).append(",");
            writer.append(String.valueOf(currentResult.get("RSSI"))).append(",");
            writer.append(String.valueOf(currentResult.get("Windows"))).append(",");
            writer.append(String.valueOf(currentResult.get("SFRX"))).append(",");
            writer.append(String.valueOf(currentResult.get("Delay"))).append(",");
            writer.append(String.valueOf(currentResult.get("OperatorIndex"))).append(",");
            writer.append(String.valueOf(currentResult.get("OperatorName")));
            writer.append("\n");
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void sendReport(String name) {
        if(reportType.equals("json")){
            String fileName;
            if(name.equals("")){
                long tsLong = System.currentTimeMillis()/1000;
                Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                cal.setTimeInMillis(tsLong * 1000L);
                String date = DateFormat.format("dd-MM-yyyy hh:mm:ss", cal).toString();

                fileName = "report_" + deviceName + "_" + date +".json";
            }else{
                fileName = name + ".json";
            }


            File file = new File(requireContext().getCacheDir(), fileName);
            try{
                FileWriter writer = new FileWriter(file);
                writer.append(reportData.toString());
                writer.flush();
                writer.close();
            }catch (Exception e){
                Toast.makeText(getContext(), "File writing failed: " + e.toString(), Toast.LENGTH_LONG).show();
            }

            File filePath = new File(getContext().getCacheDir(), "");
            File newFile = new File(filePath, fileName);
            Uri uri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", newFile);

            Intent intent = ShareCompat.IntentBuilder.from((Activity) getContext())
                    .setType("text/json")
                    .setStream(uri)
                    .setChooserTitle("Choose bar")
                    .createChooserIntent()
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(intent);
        }else if(reportType.equals("csv")){


            String fileName;
            if(name.equals("")){
                long tsLong = System.currentTimeMillis()/1000;
                Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                cal.setTimeInMillis(tsLong * 1000L);
                String date = DateFormat.format("dd-MM-yyyy hh:mm:ss", cal).toString();

                fileName = "report_" + deviceName + "_" + date +".csv";
            }else{
                fileName = name + ".json";
            }

            File file = new File(requireContext().getCacheDir(), fileName);
            try{
                FileWriter writer = new FileWriter(file);
                putReportDataCSV(writer);
                writer.flush();
                writer.close();
            }catch (Exception e){
                Toast.makeText(getContext(), "File writing failed: " + e.toString(), Toast.LENGTH_LONG).show();
            }

            File filePath = new File(getContext().getCacheDir(), "");
            File newFile = new File(filePath, fileName);
            Uri uri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", newFile);

            Intent intent = ShareCompat.IntentBuilder.from((Activity) getContext())
                    .setType("text/comma-separated-values")
                    .setStream(uri)
                    .setChooserTitle("Choose bar")
                    .createChooserIntent()
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(intent);

        }

    }



    private void displaySimplifiedData(boolean isAverage, boolean isTX){

        Resources c = requireContext().getResources();

        if(isAverage){
                String lastAverageGateway = allAverageGateway.get(allAverageGateway.size()-1).equals(-1) ? "/": allAverageGateway.get(allAverageGateway.size()-1).toString();
                String lastAverageSNR = allAverageSNR.get(allAverageSNR.size()-1).equals(-1) ? "/": allAverageSNR.get(allAverageSNR.size()-1).toString();
                String lastAverageMargin = allAverageMargin.get(allAverageMargin.size()-1).equals(-1) ? "/": allAverageMargin.get(allAverageMargin.size()-1).toString();
                String lastAverageRSSI = allAverageRSSI.get(allAverageRSSI.size()-1).equals(-1) ? "/": allAverageRSSI.get(allAverageRSSI.size()-1).toString();

                simplifiedGateway.setText(c.getString(R.string.averageGateway) + lastAverageGateway);
                simplifiedSNR.setText(c.getString(R.string.average) + " SNR\n" + lastAverageSNR + " dB");
                simplifiedMargin.setText( c.getString(R.string.average) + " Margin\n" + lastAverageMargin + " dB");
                simplifiedRSSI.setText(c.getString(R.string.average) + " RSSI\n" + lastAverageRSSI + " dBm");

        }
        if(!isAverage & !isTX){
            // We will have to use all the last data we parsed so we create variable to stock and use them more properly
            Integer lastMargin = allMargin.get(allMargin.size()-1);
            Integer lastRSSI = allRSSI.get(allRSSI.size()-1);
            Integer lastSNR = allSNR.get(allSNR.size()-1);
            Integer lastSFTX = allSFTX.get(allSFTX.size()-1);
            Integer lastSFRX = allSFRX.get(allSFRX.size()-1);
            Integer lastGateway = allGateway.get(allGateway.size()-1);
            Integer lastOperator = allOperator.get(allOperator.size()-1);
            String lastOperatorName;
            try {
                lastOperatorName = ((String) Objects.requireNonNull(allOperatorName.get(lastOperator))).toUpperCase();
            }
            catch(Exception e) {
                lastOperatorName = "";
            }
            float lastBatteryLevel = allBatteryVoltage.get(allBatteryVoltage.size()-1);

            if (lastGateway.equals(1)) {
                simplifiedGateway.setText(c.getString(R.string.thereIs) + " " + lastGateway + " " + c.getString(R.string.gateway));
            } else {
                simplifiedGateway.setText(c.getString(R.string.thereIs) + " " + lastGateway + " " + c.getString(R.string.gateways));
            }
            simplifiedSNR.setText("SNR \n" + lastSNR + " dB");
            simplifiedMargin.setText("Margin \n" + lastMargin + " dB");
            simplifiedRSSI.setText("RSSI \n" + lastRSSI + " dBm");
            simplifiedOperator.setText(lastOperatorName);
            simplifiedBatteryText.setText("Battery Voltage = " + allBatteryVoltage.get(allBatteryVoltage.size()-1)+ "V");

            if(lastBatteryLevel > 2.6){
                simplifiedBatteryImage.setImageResource(R.drawable.battery_full);
            }else if(lastBatteryLevel >= 2.45){
                simplifiedBatteryImage.setImageResource(R.drawable.battery_good);
            }else if(lastBatteryLevel >= 2.3){
                simplifiedBatteryImage.setImageResource(R.drawable.battery_half);
            }else if(lastBatteryLevel >= 2){
                simplifiedBatteryImage.setImageResource(R.drawable.battery_weak);
            }else{
                simplifiedBatteryImage.setImageResource(R.drawable.battery_empty);
            }

            // We change the color of the text to warn the user if needed
            if(lastGateway == 1){
                simplifiedGateway.setTextColor(RED);
            }else if(lastGateway == 2 || lastGateway == 3){
                simplifiedGateway.setTextColor(Color.rgb(255, 165, 0)); // Orange
            }else{
                simplifiedGateway.setTextColor(WHITE);
            }

            simplifiedEmission.setText("SF"+lastSFTX +"   " + allTXInfo.get(allTXInfo.size()-1));

            simplifiedReceptionInfo.setText( " SF" + lastSFRX + " RX" + allWindows.get(allWindows.size()-1) + " " + allDelay.get(allDelay.size()-1) + "s");

            // Connection indicator of the emmision

            if (lastMargin >= spMarginPerfect) {
                simplifiedEmissionCheck.setImageResource(R.drawable.wifi_perfect_green);
            } else if (lastMargin >= spMarginGood) {
                simplifiedEmissionCheck.setImageResource(R.drawable.wifi_good_light_green);
            } else if (lastMargin >= spMarginBad) {
                simplifiedEmissionCheck.setImageResource(R.drawable.wifi_bad_orange);
            } else {
                simplifiedEmissionCheck.setImageResource(R.drawable.wifi_terribe_red);
            }


            // Connection indicator of the reception

            if (lastRSSI >= spRSSIPerfect && lastSNR >= spSNRPerfect) {
                simplifiedReceptionCheck.setImageResource(R.drawable.wifi_perfect_green);
            } else if (lastRSSI >= spRSSIPerfect && lastSNR >= spSNRBad || lastSNR >= spSNRPerfect && lastRSSI >= spRSSIBad) {
                simplifiedReceptionCheck.setImageResource(R.drawable.wifi_good_light_green);
            } else if (lastRSSI >= spRSSIBad && lastSNR <= spSNRBad || lastRSSI <= spRSSIBad && lastSNR >= spSNRBad || lastRSSI <= spRSSIPerfect && lastRSSI >= spRSSIBad) {
                simplifiedReceptionCheck.setImageResource(R.drawable.wifi_bad_orange);
            } else {
                simplifiedReceptionCheck.setImageResource(R.drawable.wifi_terribe_red);
            }
            simplifiedReceptionCheck.setScaleX(-1);
        }
        if(isTX && !isAverage){

            if(!simplifiedEmission.getText().toString().isEmpty()){
                simplifiedEmission.setText(simplifiedEmission.getText().toString().split(" ")[0] + "   "+ allTXInfo.get(allTXInfo.size()-1));
            }else{
                simplifiedEmission.setText("SF    "+ allTXInfo.get(allTXInfo.size()-1));
            }

        }

    }

    private void drawGraph(String label, Float min, Float max, LineChart graph){

        LineDataSet lineDataSet = new LineDataSet(dataValue(label), label);
        lineDataSet.setValueTextColor(WHITE);
        lineDataSet.setValueTextSize(8);

        XAxis xAxis = graph.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);


        // We put a margin on the left and the right of the graph, so as our data don't collide with the border.
        xAxis.setAxisMinimum(-1);
        xAxis.setTextColor(WHITE);

        YAxis yAxisRight = graph.getAxisRight();
        yAxisRight.setEnabled(false);

        YAxis yAxisLeft = graph.getAxisLeft();
        yAxisLeft.setAxisMinimum(min);
        yAxisLeft.setAxisMaximum(max);
        yAxisLeft.setTextColor(WHITE);

        graph.setDrawBorders(true);
        graph.setBorderColor(WHITE);
        graph.getLegend().setTextColor(WHITE);
        graph.setNoDataText(requireContext().getResources().getString(R.string.chartNoData));
        graph.getDescription().setEnabled(false);

        //Refresh the graph
        graph.invalidate();
    }

    public void drawGradient(LineChart graph){

        Paint paint = graph.getRenderer().getPaintRender();
        LinearGradient linearGradient;

        linearGradient = new LinearGradient(
                0, 0, 0, 575,
                GREEN,RED,
                Shader.TileMode.CLAMP);


        paint.setShader(linearGradient);

        //Refresh the graph
        graph.invalidate();
    }

    public void addData(String label, LineChart graph){

        LineDataSet lineDataSet = new LineDataSet(dataValue(label), label);

        if(allCurrentNumber.isEmpty()){
            graph.getXAxis().setAxisMaximum(2F);
        }else{
            graph.getXAxis().setAxisMaximum(graph.getXAxis().getAxisMaximum()+1F);
        }

        lineDataSet.setValueTextColor(WHITE);
        lineDataSet.setValueTextSize(8);

        //We give to our graph the data formatted
        LineData data = new LineData(lineDataSet);
        graph.setData(data);

        //Refresh the graph
        graph.invalidate();
    }

    private ArrayList dataValue(String label){

        // We create a list of bar entry
        ArrayList<Entry> dataVals = new ArrayList<>();
        switch(label){
            case "SNR":
                for(int i=0; i<allCurrentNumber.size(); i++){
                    dataVals.add(new Entry(Integer.valueOf(allCurrentNumber.get(i).split("/")[1]) -1,allSNR.get(i)));
                }
                break;
            case "RSSI":
                for(int i=0; i<allCurrentNumber.size(); i++){
                    dataVals.add(new Entry(Integer.valueOf(allCurrentNumber.get(i).split("/")[1]) -1,allRSSI.get(i)));
                }
                break;
            case "Margin":
                for(int i=0; i<allCurrentNumber.size(); i++){
                    
                    dataVals.add(new Entry(Integer.valueOf(allCurrentNumber.get(i).split("/")[1]) -1,allMargin.get(i)));
                }
                break;
            case "SFTX":
                for(int i=0; i<allCurrentNumber.size(); i++){
                    
                    dataVals.add(new Entry(Integer.valueOf(allCurrentNumber.get(i).split("/")[1]) -1,allSFTX.get(i)));
                }
                break;
            case "Nb gateway":
                for(int i=0; i<allCurrentNumber.size(); i++){
                    
                    dataVals.add(new Entry(Integer.valueOf(allCurrentNumber.get(i).split("/")[1]) -1,allGateway.get(i)));
                }
                break;
            case "SFRX":
                for(int i=0; i<allCurrentNumber.size(); i++){
                    
                    dataVals.add(new Entry(Integer.valueOf(allCurrentNumber.get(i).split("/")[1]) -1,allSFRX.get(i)));
                }
                break;
            case "window":
                for(int i=0; i<allCurrentNumber.size(); i++){
                    dataVals.add(new Entry(Integer.valueOf(allCurrentNumber.get(i).split("/")[1]) -1,allWindows.get(i)));
                }
                break;
            default:
                break;
        }

        return dataVals;
    }


    private void displayIndicatorOfBattery(){

        if(allBatteryVoltage.size() > 0){
            Float lastBatterVoltage = allBatteryVoltage.get(allBatteryVoltage.size()-1);
            batteryTextTX.setText("Battery Voltage = " + allBatteryVoltage.get(allBatteryVoltage.size()-1)+ "V");
            batteryTextRX.setText("Battery Voltage = " + allBatteryVoltage.get(allBatteryVoltage.size()-1)+ "V");
            simplifiedBatteryText.setText("Battery Voltage = " + allBatteryVoltage.get(allBatteryVoltage.size()-1)+ "V");

            if(lastBatterVoltage >= 2.8){
                batteryImageTX.setImageResource(R.drawable.battery_full);
                batteryImageRX.setImageResource(R.drawable.battery_full);
                simplifiedBatteryImage.setImageResource(R.drawable.battery_full);
            }else if(lastBatterVoltage >= 2.4 ){
                batteryImageTX.setImageResource(R.drawable.battery_half);
                batteryImageRX.setImageResource(R.drawable.battery_half);
                simplifiedBatteryImage.setImageResource(R.drawable.battery_half);
            }else if(lastBatterVoltage >= 2){
                batteryImageTX.setImageResource(R.drawable.battery_weak);
                batteryImageRX.setImageResource(R.drawable.battery_weak);
                simplifiedBatteryImage.setImageResource(R.drawable.battery_weak);
            }else{
                batteryImageTX.setImageResource(R.drawable.battery_empty);
                batteryImageRX.setImageResource(R.drawable.battery_empty);
                simplifiedBatteryImage.setImageResource(R.drawable.battery_empty);
            }
        }else{
            batteryTextTX.setText("Battery Voltage = 3.6V");
            batteryImageTX.setImageResource(R.drawable.battery_missing);

            batteryTextTX.setText("Battery Voltage = 3.6V");
            batteryImageTX.setImageResource(R.drawable.battery_missing);

            simplifiedBatteryText.setText("Battery Voltage = 3.6V");
            simplifiedBatteryImage.setImageResource(R.drawable.battery_missing);
        }
    }

    private void displayPercentageOfDataReceived(){
        String lastCurrentNumber = allNumber.get(allNumber.size()-1);
        int a = Integer.parseInt(lastCurrentNumber.split("/")[1]);
        int b = Integer.parseInt(lastCurrentNumber.split("/")[0]);
        float percentage = (float)Math.round(100*b / a);

        percentageReceivedDataTX.setText(getContext().getResources().getString(R.string.received) + " " + percentage + getContext().getResources().getString(R.string.percentage) );
        if(percentage > 100){
            percentageReceivedDataTX.setTextColor(Color.parseColor("#00d700"));
            percentageReceivedDataTX.setText(getContext().getResources().getString(R.string.received) + " " + "100" + getContext().getResources().getString(R.string.percentage) );
        }else if(percentage == 100){
            percentageReceivedDataTX.setTextColor(Color.parseColor("#00d700"));
        }else if(percentage >= 80){
            percentageReceivedDataTX.setTextColor(Color.parseColor("#90ff00"));
        }else if (percentage >= 40){
            percentageReceivedDataTX.setTextColor(Color.parseColor("#ffa500"));
        }else{
            percentageReceivedDataTX.setTextColor(Color.parseColor("#ff0000"));
        }

        percentageReceivedDataTX.setTypeface(null, Typeface.ITALIC);

        percentageReceivedDataRX.setText(getContext().getResources().getString(R.string.received) + " " + percentage + getContext().getResources().getString(R.string.percentage) );
        if(percentage > 100){
            percentageReceivedDataRX.setTextColor(Color.parseColor("#00d700"));
            percentageReceivedDataRX.setText(getContext().getResources().getString(R.string.received) + " " + "100" + getContext().getResources().getString(R.string.percentage) );
        }else if(percentage == 100){
            percentageReceivedDataRX.setTextColor(Color.parseColor("#00d700"));
        }else if(percentage >= 80){
            percentageReceivedDataRX.setTextColor(Color.parseColor("#90ff00"));
        }else if (percentage >= 40){
            percentageReceivedDataRX.setTextColor(Color.parseColor("#ffa500"));
        }else{
            percentageReceivedDataRX.setTextColor(Color.parseColor("#ff0000"));
        }

        percentageReceivedDataRX.setTypeface(null, Typeface.ITALIC);
    }

    void showDialog(String str) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        assert getFragmentManager() != null;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        Bundle bundle = new Bundle();
        bundle.putString("whoCalledMe",str);


        // Create and show the dialog.
        DialogFragment newFragment = MyDialogFragment.newInstance();
        newFragment.setArguments(bundle);

        newFragment.show(ft, "dialog");
    }

    void showDialogConfiguration() {


        assert getFragmentManager() != null;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ConfigurationDialog configurationDialog=new ConfigurationDialog();
        configurationDialog.show(ft, "Dialog Fragment");


    }

    void showDialogSend() {

        Bundle bundle = new Bundle();

        bundle.putString("APPEUI",APPEUI);
        bundle.putString("DEVEUI", DEVEUI);

        assert getFragmentManager() != null;
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        SendFragment sendFragment=new SendFragment();
        sendFragment.setArguments(bundle);
        sendFragment.show(ft, "Dialog Fragment");

        sendFragment.setDialogResult(result -> {
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

            String spNumberValue = sharedPref.getString("NumberValue", "5");
            String tmpSFValue = sharedPref.getString("SFValue", "12,5");
            String spSFValue = tmpSFValue.split(",")[0];
            String spADRValue = sharedPref.getString("ADRValue", "0");

            send("S" + spNumberValue + "," + spSFValue + "," + spADRValue);
        });


        sendFragment.setAPPEUI( result -> {
            APPEUI = result;
            APPEUI_value = APPEUI.substring(APPEUI.length()-1);
            send("MAPPEUI" + APPEUI_value);
        });

        sendFragment.setDEVEUI( result -> {
            DEVEUI = result;
            DEVEUI_value = DEVEUI.substring(10,11);
            send("MDEVEUI" + DEVEUI_value);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void showDialogReport() {

        assert getFragmentManager() != null;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ReportFragment reportFragment=new ReportFragment();
        reportFragment.show(ft, "Dialog Fragment");

        reportFragment.setDialogResult(result -> {
            reportType = result.get(0);
            String name = result.get(1);
            sendReport(name);
        });
    }

}
