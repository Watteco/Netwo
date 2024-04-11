package wattecoDev.netwo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.ListFragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Collections;

/**
 * show list of BLE devices
 */
public class DevicesFragment extends ListFragment {

    SwipeRefreshLayout swipeRefreshLayout;

    private static final String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_PRIVILEGED
    };

    private ScanState scanState = ScanState.NONE;
    private static final long LE_SCAN_PERIOD = 10000; // similar to bluetoothAdapter.startDiscovery
    private final Handler leScanStopHandler = new Handler();
    private final BluetoothAdapter.LeScanCallback leScanCallback;
    private final BroadcastReceiver discoveryBroadcastReceiver;
    private final IntentFilter discoveryIntentFilter;

    private Menu menu;
    private BluetoothAdapter bluetoothAdapter;
    private final ArrayList<BluetoothDevice> listItems = new ArrayList<>();
    private ArrayAdapter<BluetoothDevice> listAdapter;

    public DevicesFragment() {
        leScanCallback = (device, rssi, scanRecord) -> {
            if (device != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    updateScan(device);
                });
            }
        };


        discoveryBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                assert action != null;
                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR);
                    switch (state) {
                        case BluetoothAdapter.STATE_OFF:
                            setEmptyText(requireContext().getResources().getString(R.string.blDisabled));
                            if (menu != null) {
                                listItems.clear();
                                listAdapter.notifyDataSetChanged();
                                menu.findItem(R.id.ble_scan).setEnabled(false);
                            }
                            break;
                        case BluetoothAdapter.STATE_ON:
                            setEmptyText(requireContext().getResources().getString(R.string.useScan));
                            if (menu != null)
                                menu.findItem(R.id.ble_scan).setEnabled(true);
                            break;
                    }
                }
                if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device.getType() != BluetoothDevice.DEVICE_TYPE_CLASSIC && getActivity() != null) {
                        getActivity().runOnUiThread(() -> updateScan(device));
                    }

                }
                if (intent.getAction().equals((BluetoothAdapter.ACTION_DISCOVERY_FINISHED))) {
                    scanState = ScanState.DISCOVERY_FINISHED; // don't cancel again
                    stopScan();
                }
            }
        };
        discoveryIntentFilter = new IntentFilter();
        discoveryIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        discoveryIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        discoveryIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    /**
     * sort by name, then address. sort named devices first
     */
    @SuppressLint("MissingPermission")
    static int compareTo(BluetoothDevice a, BluetoothDevice b) {

        boolean aValid = a.getName() != null && !a.getName().isEmpty();
        boolean bValid = b.getName() != null && !b.getName().isEmpty();
        if (aValid && bValid) {
            int ret = a.getName().compareTo(b.getName());
            if (ret != 0) return ret;
            return a.getAddress().compareTo(b.getAddress());
        }
        if (aValid) return -1;
        if (bValid) return +1;
        return a.getAddress().compareTo(b.getAddress());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH))
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        listAdapter = new ArrayAdapter<BluetoothDevice>(getActivity(), 0, listItems) {
            @NonNull
            @Override
            public View getView(int position, View view, @NonNull ViewGroup parent) {
                BluetoothDevice device = listItems.get(position);
                if (view == null)
                    view = requireActivity().getLayoutInflater().inflate(R.layout.device_list_item, parent, false);
                TextView text1 = view.findViewById(R.id.text1);
                TextView text2 = view.findViewById(R.id.text2);

                if (device.getName() == null || device.getName().isEmpty())
                    text1.setText("<unnamed>");
                else
                    text1.setText(device.getName());
                text2.setText(device.getAddress());
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.zoomout);
                view.startAnimation(animation);
                return view;
            }
        };

        swipeRefreshLayout = requireActivity().findViewById(R.id.swipe);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startScan();
            }
        });

        requireContext().registerReceiver(discoveryBroadcastReceiver, discoveryIntentFilter);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(null);
        View header = getActivity().getLayoutInflater().inflate(R.layout.device_list_header, null, false);
        getListView().addHeaderView(header, null, false);
        setEmptyText("initializing...");
        ((TextView) getListView().getEmptyView()).setTextSize(18);
        setListAdapter(listAdapter);

        // Check if the location permission has been granted
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Location permission has already been granted, do something
        } else {
            // Location permission has not been granted, show pop-up dialog to request permission
            showLocationPermissionDialog();
        }
    }

    private void showLocationPermissionDialog() {
        // Create a new AlertDialog builder
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());

        // Set the dialog's view to your location_permission_dialog layout
        builder.setView(getLayoutInflater().inflate(R.layout.location_permission_dialog, null));

        // Set a positive button click listener for the dialog
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // User clicked the OK button, request location permission
                //ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                int permission = ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            getActivity(),
                            PERMISSIONS_LOCATION,
                            1
                    );
                }
            }
        });

        // Create and show the dialog
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_devices, menu);
        this.menu = menu;
        if (bluetoothAdapter == null) {
            menu.findItem(R.id.bt_settings).setEnabled(false);
            menu.findItem(R.id.ble_scan).setEnabled(false);
        } else if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable(); // We just ask the user to enable the bluetooth
            if (!bluetoothAdapter.isEnabled()) {
                menu.findItem(R.id.ble_scan).setEnabled(false);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().registerReceiver(discoveryBroadcastReceiver, discoveryIntentFilter);
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(requireContext().getResources().getString(R.string.app_name));
        View tmpView = toolbar.getChildAt(0);
        if (tmpView instanceof TextView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ((TextView) tmpView).setHorizontallyScrolling(false);
                ((TextView) tmpView).setAutoSizeTextTypeUniformWithConfiguration(20, 30, 1, 1);
            }
        }
        if (bluetoothAdapter == null) {
            setEmptyText("<bluetooth LE not supported>");
        } else if (!bluetoothAdapter.isEnabled()) {
            setEmptyText(requireContext().getResources().getString(R.string.blDisabled));
            if (menu != null) {
                listItems.clear();
                listAdapter.notifyDataSetChanged();
                menu.findItem(R.id.ble_scan).setEnabled(false);
            }
        } else {
            setEmptyText(requireContext().getResources().getString(R.string.useScan));

            if (menu != null)
                menu.findItem(R.id.ble_scan).setEnabled(true);
        }

        swipeRefreshLayout.setEnabled(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopScan();
        getActivity().unregisterReceiver(discoveryBroadcastReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        menu = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.ble_scan) {
            startScan();
            return true;
        } else if (id == R.id.ble_scan_stop) {
            stopScan();
            return true;
        } else if (id == R.id.bt_settings) {
            Intent intent = new Intent();
            intent.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivity(intent);
            return true;
        } else if (id == R.id.supportRedirect) {
            redirectOnSupport();
            return true;
        } else if (id == R.id.about) {
            showDialog();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void redirectOnSupport() {
        Uri webpage = Uri.parse("https://support.nke-watteco.com/netwo/");
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @SuppressLint("StaticFieldLeak") // AsyncTask needs reference to this fragment
    private void startScan() {
        if (scanState != ScanState.NONE)
            return;
        scanState = ScanState.LE_SCAN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                scanState = ScanState.NONE;
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.location_permission_title);
                builder.setMessage(R.string.location_permission_message);
                builder.setPositiveButton(android.R.string.ok,
                        (dialog, which) -> requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0));
                builder.show();
                return;
            }
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            boolean locationEnabled = false;
            try {
                locationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception ignored) {
            }
            try {
                locationEnabled |= locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch (Exception ignored) {
            }
            if (!locationEnabled)
                scanState = ScanState.DISCOVERY;
            // Starting with Android 6.0 a bluetooth scan requires ACCESS_COARSE_LOCATION permission, but that's not all!
            // LESCAN also needs enabled 'location services', whereas DISCOVERY works without.
            // Most users think of GPS as 'location service', but it includes more, as we see here.
            // Instead of asking the user to enable something they consider unrelated,
            // we fall back to the older API that scans for bluetooth classic _and_ LE
            // sometimes the older API returns less results or slower
        }
        listItems.clear();
        listAdapter.notifyDataSetChanged();
        setEmptyText(requireContext().getResources().getString(R.string.scanning));
        menu.findItem(R.id.ble_scan).setVisible(false);
        menu.findItem(R.id.ble_scan_stop).setVisible(true);
        if (scanState == ScanState.LE_SCAN) {
            leScanStopHandler.postDelayed(this::stopScan, LE_SCAN_PERIOD);
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void[] params) {
                    bluetoothAdapter.startLeScan(null, leScanCallback);
                    return null;
                }
            }.execute(); // start async to prevent blocking UI, because startLeScan sometimes take some seconds
        } else {
            bluetoothAdapter.startDiscovery();
        }

        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // ignore requestCode as there is only one in this fragment
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            new Handler(Looper.getMainLooper()).postDelayed(this::startScan,1); // run after onResume to avoid wrong empty-text
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getText(R.string.location_denied_title));
            builder.setMessage(getText(R.string.location_denied_message));
            builder.setPositiveButton(android.R.string.ok, null);
            builder.show();
        }
    }

    @SuppressLint("MissingPermission")
    private void updateScan(BluetoothDevice device) {
        if(scanState == ScanState.NONE)
            return;
        if(!listItems.contains(device) && device.getName() != null && device.getName().startsWith("WTC")) {
            listItems.add(device);
            Collections.sort(listItems, DevicesFragment::compareTo);
            listAdapter.notifyDataSetChanged();
        }
    }

    private void stopScan() {
        swipeRefreshLayout.setRefreshing(false);
        if(scanState == ScanState.NONE)
            return;
        setEmptyText("<no bluetooth devices found>");
        if(menu != null) {
            menu.findItem(R.id.ble_scan).setVisible(true);
            menu.findItem(R.id.ble_scan_stop).setVisible(false);
        }
        switch(scanState) {
            case LE_SCAN:
                leScanStopHandler.removeCallbacks(this::stopScan);
                bluetoothAdapter.stopLeScan(leScanCallback);
                break;
            case DISCOVERY:
                bluetoothAdapter.cancelDiscovery();
                break;
            default:
                // already canceled
        }
        scanState = ScanState.NONE;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        stopScan();

        BluetoothDevice device = listItems.get(position-1);

        Bundle args = new Bundle();
        args.putString("device", device.getAddress());
        args.putString("deviceName", device.getName());

        Fragment terminalFragment = new TerminalFragment();
        terminalFragment.setArguments(args);

        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(R.id.fragment, terminalFragment, "terminal");
        transaction.addToBackStack(null);
        transaction.commit();

    }

    private enum ScanState {NONE, LE_SCAN, DISCOVERY, DISCOVERY_FINISHED}


    void showDialog() {

        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = requireActivity().getSupportFragmentManager().beginTransaction();
        Fragment prev = requireActivity().getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        Bundle bundle = new Bundle();
        bundle.putString("whoCalledMe", "about");
        // Create and show the dialog.
        DialogFragment newFragment = MyDialogFragment.newInstance();
        newFragment.setArguments(bundle);
        newFragment.show(ft, "dialog");
    }
}

