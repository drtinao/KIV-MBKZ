package fav.drtinao.magicwol;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fragment is used for adding new device in LAN to list.
 */
public class AddDeviceFrag extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(R.string.add_device_frag_title);

        View addDeviceFragLayout = inflater.inflate(R.layout.lan_add_device_frag, container, false);

        TextView logTV = addDeviceFragLayout.findViewById(R.id.add_device_frag_log_cont_tv_id); //ref to user visible log
        logTV.setMovementMethod(new ScrollingMovementMethod());

        //set onclick listeners for layout items
        Button sendMagicBtn = addDeviceFragLayout.findViewById(R.id.add_device_frag_send_magic_btn_id);
        sendMagicBtn.setOnClickListener(view -> sendMagicPacket(addDeviceFragLayout));
        Button addDeviceBtn = addDeviceFragLayout.findViewById(R.id.add_device_frag_add_device_btn_id);
        addDeviceBtn.setOnClickListener(view -> addDevice(addDeviceFragLayout));
        Button pingIPBtn = addDeviceFragLayout.findViewById(R.id.add_device_frag_ip_ping_btn_id);
        pingIPBtn.setOnClickListener(view -> pingDevice(addDeviceFragLayout, false));
        Button pingHostnameBtn = addDeviceFragLayout.findViewById(R.id.add_device_frag_hostname_ping_btn_id);
        pingHostnameBtn.setOnClickListener(view -> pingDevice(addDeviceFragLayout, true));

        return addDeviceFragLayout;
    }

    /**
     * Method is triggered when button for sending Magic Packet to user defined device is clicked.
     * @param addDeviceFragLayout needed for interaction with UI elements of layout
     */
    public void sendMagicPacket(View addDeviceFragLayout){
        //reference to fields filled by user - add_device_frag xml
        EditText macET = addDeviceFragLayout.findViewById(R.id.add_device_frag_mac_et_id);
        TextView logTV = addDeviceFragLayout.findViewById(R.id.add_device_frag_log_cont_tv_id); //ref to user visible log

        //get text from relevant UI fields
        String macUserText = macET.getText().toString();

        MagicPacketLogic magicPackLogic = new MagicPacketLogic();
        DeviceInfoLogic devInfoLogic = new DeviceInfoLogic();

        InetAddress broadcastIP = devInfoLogic.retrBroadcastIP();
        boolean macUserValid = magicPackLogic.isMacValid(macUserText);

        if(broadcastIP == null){ //invalid broadcast IP detected
            AlertDialog noBroadcastIPAlert = DialogFactory.genNoBroadcastIPDialog(addDeviceFragLayout.getContext());
            noBroadcastIPAlert.show();
            return;
        }

        if(!macUserValid){ //invalid MAC entered
            AlertDialog devMacValid = new AlertDialog.Builder(addDeviceFragLayout.getContext()).create();
            devMacValid.setTitle(getString(R.string.add_device_frag_dev_mac_alert_title));
            devMacValid.setMessage(getString(R.string.add_device_frag_dev_mac_alert_mes_1) + System.lineSeparator() + System.lineSeparator() + getString(R.string.add_device_frag_dev_mac_alert_mes_2) + System.lineSeparator() + System.lineSeparator() + getString(R.string.add_device_frag_dev_mac_alert_mes_3));
            devMacValid.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.alert_ok_btn), (dialogInterface, i) -> devMacValid.dismiss());
            devMacValid.show();
        }

        if(broadcastIP == null || !macUserValid){ //do not execute Magic Packet code if wrong info entered
            return;
        }

        ExecutorService execServ = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        logTV.append(getString(R.string.add_device_frag_log_divider) + System.lineSeparator() + getString(R.string.add_device_frag_log_magic_1) + System.lineSeparator() + getString(R.string.add_device_frag_log_magic_2) + broadcastIP.getHostAddress() + System.lineSeparator() + getString(R.string.add_device_frag_log_magic_3) + macUserText + System.lineSeparator());
        execServ.execute(() -> {
            magicPackLogic.sendMagicPacket(broadcastIP, macUserText);
            handler.post(() -> logTV.append(getString(R.string.add_device_frag_log_magic_4) + System.lineSeparator()));
        });
    }

    /**
     * Describes action which is performed when button for adding device to list is clicked ("@id/add_device_frag_add_device_btn_id").
     * Displays floating dialog which is used for device add / information recap (R.layout.add_device_confirm_dialog).
     * @param addDeviceFragLayout needed for interaction with UI elements of layout
     */
    public void addDevice(View addDeviceFragLayout){
        //reference to fields filled by user - add_device_frag xml
        EditText nameET = addDeviceFragLayout.findViewById(R.id.add_device_frag_name_et_id);
        EditText macET = addDeviceFragLayout.findViewById(R.id.add_device_frag_mac_et_id);
        EditText ipET = addDeviceFragLayout.findViewById(R.id.add_device_frag_ip_et_id);
        EditText hostnameET = addDeviceFragLayout.findViewById(R.id.add_device_frag_hostname_et_id);

        //get text entered by user
        String nameETText = nameET.getText().toString().trim();
        String macETText = macET.getText().toString().trim();
        String ipETText = ipET.getText().toString().trim();
        String hostnameETText = hostnameET.getText().toString().trim();

        boolean ipValid = false; //user entered IP + is valid
        boolean hostnameValid = false; //user entered hostname + is valid

        //check validity of user entered data - device name
        if(nameETText.isEmpty()){
            Toast.makeText(addDeviceFragLayout.getContext(), getString(R.string.add_device_frag_name_mis_toast), Toast.LENGTH_LONG).show();
            return; //cannot continue WO device name
        }

        //check validity of user entered data - MAC address
        if(!macETText.isEmpty()){ //user filled in
            MagicPacketLogic magicPackLogic = new MagicPacketLogic();
            if(!magicPackLogic.isMacValid(macETText)){ //check if valid
                AlertDialog devMacValid = new AlertDialog.Builder(addDeviceFragLayout.getContext()).create();
                devMacValid.setTitle(getString(R.string.add_device_frag_dev_mac_alert_title));
                devMacValid.setMessage(getString(R.string.add_device_frag_dev_mac_alert_mes_1) + System.lineSeparator() + System.lineSeparator() + getString(R.string.add_device_frag_dev_mac_alert_mes_2) + System.lineSeparator() + System.lineSeparator() + getString(R.string.add_device_frag_dev_mac_alert_mes_3));
                devMacValid.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.alert_ok_btn), (dialogInterface, i) -> devMacValid.dismiss());
                devMacValid.show();
                return; //cannot continue WO valid device MAC
            }
        }else{
            Toast.makeText(addDeviceFragLayout.getContext(), getString(R.string.add_device_frag_mac_mis_toast), Toast.LENGTH_LONG).show();
            return; //cannot continue WO device MAC entered
        }

        //OK, from this point, name + MAC is valid for sure, can save
        //check whether device with same name / MAC already present - START
        DatabaseLogic dbLogic = new DatabaseLogic(addDeviceFragLayout.getContext()); //create instance for working with DB
        String presentInList = dbLogic.isLANDevPresent(nameETText.toLowerCase(), macETText.toLowerCase(), -1);
        if(presentInList != null){ //device already present, cannot continue
            Toast.makeText(addDeviceFragLayout.getContext(), getString(R.string.add_device_frag_already_exists_toast) + "\"" + presentInList + "\"", Toast.LENGTH_LONG).show();
            return;
        }
        //check whether device with same name / MAC already present - END

        //check validity of user entered data - IP address
        if(!ipETText.isEmpty()){ //user filled in
            DeviceInfoLogic devInfoLogic = new DeviceInfoLogic();
            if(devInfoLogic.isIPValid(ipETText)){ //check if valid
                ipValid = true;
            }else{ //user filled in + invalid
                Toast.makeText(addDeviceFragLayout.getContext(), getString(R.string.add_device_frag_ip_invalid_toast), Toast.LENGTH_LONG).show();
                return; //cannot continue w wrong IP format entered
            }
        }

        //check validity of user entered data - hostname
        if(!hostnameETText.isEmpty()){ //user filled in
            hostnameValid = true;
        }

        AlertDialog.Builder deviceDialogBuilder = new AlertDialog.Builder(addDeviceFragLayout.getContext());
        View deviceDialogView = LayoutInflater.from(addDeviceFragLayout.getContext()).inflate(R.layout.add_device_confirm_dialog, null);
        Button addListBtn = deviceDialogView.findViewById(R.id.add_device_confirm_dialog_add_list_btn_id);

        deviceDialogBuilder.setPositiveButton(R.string.add_device_confirm_dialog_positive_btn, null);
        deviceDialogBuilder.setNegativeButton(R.string.add_device_confirm_dialog_negative_btn, null);

        LinearLayout deviceDialogRecapOuterLL = deviceDialogView.findViewById(R.id.add_device_confirm_dialog_recap_outer_lay_id);
        Spinner listSpinner = deviceDialogView.findViewById(R.id.add_device_confirm_dialog_list_spinner_id); //spinner with LAN lists

        //reference to placeholders which should be used to reflect user entered info - add_device_confirm_dialog xml
        TextView nameHolderTV = deviceDialogView.findViewById(R.id.add_device_confirm_dialog_name_holder_id);
        TextView macHolderTV = deviceDialogView.findViewById(R.id.add_device_confirm_dialog_mac_holder_id);

        //edit placeholders to reflect information entered by user
        nameHolderTV.setText(nameETText);
        macHolderTV.setText(macETText);

        TextView ipHolderTV = deviceDialogView.findViewById(R.id.add_device_confirm_dialog_ip_holder_id);
        if(ipValid){ //IP address is valid, append to recap
            ipHolderTV.setText(ipETText);
        }else{ //hide IP related fields in recap
            LinearLayout deviceDialogIPRecapLay = deviceDialogView.findViewById(R.id.add_device_confirm_dialog_ip_recap_lay_id);
            deviceDialogRecapOuterLL.removeView(deviceDialogIPRecapLay);
        }

        TextView hostnameHolderTV = deviceDialogView.findViewById(R.id.add_device_confirm_dialog_hostname_holder_id);
        if(hostnameValid){ //hostname is valid, append to recap
            hostnameHolderTV.setText(hostnameETText);
        }else{ //hide hostname related fields in recap
            LinearLayout deviceDialogHostnameRecapLay = deviceDialogView.findViewById(R.id.add_device_confirm_dialog_hostname_recap_lay_id);
            deviceDialogRecapOuterLL.removeView(deviceDialogHostnameRecapLay);
        }

        AlertDialog deviceDialog = deviceDialogBuilder.create();
        deviceDialog.setView(deviceDialogView);
        deviceDialog.setTitle(R.string.add_device_confirm_dialog_title);

        boolean finalIpValid = ipValid;
        boolean finalHostnameValid = hostnameValid;
        deviceDialog.setOnShowListener(dialogInterface -> {
            Button positiveBtn = deviceDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
            positiveBtn.setOnClickListener(view -> {
                LANDevRowDB newLANDevRowDB = new LANDevRowDB();
                newLANDevRowDB.setName(nameETText);
                newLANDevRowDB.setMac(macETText);
                if(finalIpValid){ //IP is valid, save it
                    newLANDevRowDB.setIp(ipETText);
                }

                if(finalHostnameValid){ //hostname is valid, save it
                    newLANDevRowDB.setHostname(hostnameETText);
                }

                //find out to which list device should be assigned
                LANListRowDB selectedList = (LANListRowDB) listSpinner.getSelectedItem();
                if(selectedList == null){ //no list selected / created, cannot continue
                    Toast.makeText(addDeviceFragLayout.getContext(), getString(R.string.add_device_frag_list_not_sel_toast), Toast.LENGTH_LONG).show();
                    return;
                }

                newLANDevRowDB.setLanListsId(selectedList.getId());
                dbLogic.addLanDev(newLANDevRowDB);

                Toast.makeText(addDeviceFragLayout.getContext(), getString(R.string.add_device_frag_lan_dev_saved_toast) +  " \"" + selectedList.getName() + "\"", Toast.LENGTH_LONG).show();
                nameET.setText("");
                macET.setText("");
                ipET.setText("");
                hostnameET.setText("");
                dialogInterface.dismiss();
            });

            Button negativeBtn = deviceDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
            negativeBtn.setOnClickListener(view -> dialogInterface.dismiss());
        });

        //fill spinner with loaded / user created LAN lists
        List<LANListRowDB> LANListRowDBList = dbLogic.getAllLanLists();
        ArrayAdapter<LANListRowDB> adapter = new ArrayAdapter<>(addDeviceFragLayout.getContext(), android.R.layout.simple_spinner_dropdown_item, LANListRowDBList);
        listSpinner.setAdapter(adapter);
        addListBtn.setOnClickListener(view -> addList(deviceDialogView, adapter));

        //set preferred item (if any)
        Bundle extraData = getArguments();
        if(extraData != null){
            String prefListName = getArguments().getString("prefListName");

            for(int i = 0; i < listSpinner.getAdapter().getCount(); i++){
                LANListRowDB spinnerLANList = (LANListRowDB) listSpinner.getAdapter().getItem(i);
                if(spinnerLANList.getName().equals(prefListName)){
                    listSpinner.setSelection(i);
                    break;
                }
            }
            Toast.makeText(addDeviceFragLayout.getContext(), getString(R.string.add_device_frag_pref_list_toast) + " \"" + prefListName + "\"", Toast.LENGTH_LONG).show();
        }

        deviceDialog.show();
    }

    /**
     * Method is called when button for pinging device is clicked by user.
     * @param addDeviceFragLayout needed for interaction with UI elements of layout
     * @param hostnameField true if value present in hostname field should be used / else value in IP field is used
     */
    public void pingDevice(View addDeviceFragLayout, boolean hostnameField){
        DeviceInfoLogic devInfoLogic = new DeviceInfoLogic();
        InetAddress broadcastIP = devInfoLogic.retrBroadcastIP();

        if(broadcastIP == null){ //invalid broadcast IP detected
            AlertDialog noBroadcastIPAlert = DialogFactory.genNoBroadcastIPDialog(addDeviceFragLayout.getContext());
            noBroadcastIPAlert.show();
            return;
        }

        EditText targetET;
        if(hostnameField){
            targetET = addDeviceFragLayout.findViewById(R.id.add_device_frag_hostname_et_id); //ref to hostname field
        }else{
            targetET = addDeviceFragLayout.findViewById(R.id.add_device_frag_ip_et_id); //ref to IP field
        }
        String ipHostnameText = targetET.getText().toString(); //get IP / hostname entered by user
        TextView logTV = addDeviceFragLayout.findViewById(R.id.add_device_frag_log_cont_tv_id); //ref to user visible log

        //check whether IP / hostname empty
        if(ipHostnameText.isEmpty()){
            logTV.append(getString(R.string.add_device_frag_log_divider) + System.lineSeparator() + getString(R.string.add_device_frag_log_ping_1_err) + System.lineSeparator());
            if(hostnameField){
                Toast.makeText(addDeviceFragLayout.getContext(), getResources().getString(R.string.add_device_frag_hostname_mis_toast), Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(addDeviceFragLayout.getContext(), getResources().getString(R.string.add_device_frag_ip_mis_toast), Toast.LENGTH_LONG).show();
            }
            return;
        }

        //check validity of user entered info
        if(!hostnameField && !devInfoLogic.isIPValid(ipHostnameText)){ //in case of IP check validity
            logTV.append(getString(R.string.add_device_frag_log_divider) + System.lineSeparator() + getString(R.string.add_device_frag_log_ping_1_err) + System.lineSeparator() + getString(R.string.add_device_frag_log_ping_2) + ipHostnameText + System.lineSeparator());
            Toast.makeText(addDeviceFragLayout.getContext(), getResources().getString(R.string.add_device_frag_ip_invalid_toast), Toast.LENGTH_LONG).show();
            return;
        }

        int pingTimeoutMS = devInfoLogic.getPingTimeoutMS(); //ping timeout in MS

        logTV.append(getString(R.string.add_device_frag_log_divider) + System.lineSeparator() + getString(R.string.add_device_frag_log_ping_1) + System.lineSeparator() + getString(R.string.add_device_frag_log_ping_2) + ipHostnameText + System.lineSeparator() + getString(R.string.add_device_frag_log_ping_3) + pingTimeoutMS + " " + getString(R.string.ping_timeout_lan_measure) + System.lineSeparator());
        ProgressDialog pingPd = new ProgressDialog(addDeviceFragLayout.getContext()); // ProgressDialog object; is displayed on top of the activity and tells user, that the application is busy
        pingPd.setTitle(getString(R.string.add_device_frag_ping_pd_title));
        pingPd.setMessage(getString(R.string.add_device_frag_ping_pd_mes_1) + System.lineSeparator() + getString(R.string.add_device_frag_ping_pd_mes_2) + ipHostnameText + System.lineSeparator() + getString(R.string.add_device_frag_ping_pd_mes_3) + pingTimeoutMS + " " + getString(R.string.ping_timeout_lan_measure));
        pingPd.setCancelable(false);
        pingPd.show();

        Thread pingThread = new Thread(() -> {
            boolean devPing = devInfoLogic.isDevPingable(ipHostnameText);
            getActivity().runOnUiThread(() -> {
                if(devPing){
                    logTV.append(getString(R.string.add_device_frag_log_ping_true) + System.lineSeparator());
                }else{
                    logTV.append(getString(R.string.add_device_frag_log_ping_false) + System.lineSeparator());
                }

                if(pingPd.isShowing()){ //if user did not minimized PD, hide it
                    pingPd.dismiss();
                }
            });
        });
        pingThread.start();
    }

    /**
     * Method is called when button for creating new LAN list is tapped.
     * @param deviceDialogView needed for interaction with UI elements of layout
     * @param adapter adapter which is used as bridge: DB <-> spinner with LAN lists
     */
    private void addList(View deviceDialogView, ArrayAdapter<LANListRowDB> adapter){
        AlertDialog addListDialog = DialogFactory.genAddListDialog(deviceDialogView, adapter, null);
        addListDialog.show();
    }
}
