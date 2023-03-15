package fav.drtinao.magicwol;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

/**
 * Fragment is used for managing LAN devices within specific list.
 */
public class LANDevFrag extends Fragment {
    private LANDevAdapter LANAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int selLANListId = getArguments().getInt("lanListId");
        String selLANListName = getArguments().getString("lanListName");

        ((MainActivity)getActivity()).getSupportActionBar().setTitle(getResources().getString(R.string.lan_dev_frag_title) + " " + selLANListName);
        setHasOptionsMenu(true);

        View lanDevFragLayout = inflater.inflate(R.layout.lan_dev_frag, container, false);
        ListView LANDevLV = lanDevFragLayout.findViewById(R.id.lan_dev_frag_lv_id);
        SearchView LANDevSV = lanDevFragLayout.findViewById(R.id.lan_dev_frag_sv_id);

        DatabaseLogic dbLogic = new DatabaseLogic(getActivity());
        List<LANDevRowDB> LANDevRowDBList = dbLogic.getLANDevicesInList(selLANListId);

        LANAdapter = new LANDevAdapter(getActivity(), LANDevRowDBList);
        LANDevLV.setAdapter(LANAdapter);
        LANDevSV.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String userSearch) {
                LANAdapter.getFilter().filter(userSearch);
                return false;
            }
        });

        LANDevLV.setOnItemClickListener((adapterView, view, position, l) -> { //triggered when one of LAN devices from list is tapped, show details of LAN device
            LANDevRowDB selLANDev = (LANDevRowDB) adapterView.getItemAtPosition(position); //get object which represents selected LAN device

            AlertDialog.Builder devDetailDialogBuilder = new AlertDialog.Builder(getActivity());
            View devDetailDialogView = LayoutInflater.from(getActivity()).inflate(R.layout.lan_dev_detail_dialog, null);
            LinearLayout devDetailDialogOuterLL = devDetailDialogView.findViewById(R.id.lan_dev_detail_dialog_outer_lay_id);

            //ref to placeholders for mandatory items - name + MAC
            TextView nameHolderTV = devDetailDialogView.findViewById(R.id.lan_dev_detail_dialog_name_holder_id);
            TextView macHolderTV = devDetailDialogView.findViewById(R.id.lan_dev_detail_dialog_mac_holder_id);

            //fill name + MAC with data from database
            nameHolderTV.setText(selLANDev.getName());
            macHolderTV.setText(selLANDev.getMac());

            //fill IP if available
            String selLANDevIP = selLANDev.getIp();
            if(selLANDevIP != null){
                TextView ipHolderTV = devDetailDialogView.findViewById(R.id.lan_dev_detail_dialog_ip_holder_id);
                ipHolderTV.setText(selLANDevIP);
            }else{
                LinearLayout ipLay = devDetailDialogView.findViewById(R.id.lan_dev_detail_dialog_ip_lay_id);
                devDetailDialogOuterLL.removeView(ipLay);
            }

            //fill hostname if available
            String selLANDevHostname = selLANDev.getHostname();
            if(selLANDevHostname != null){
                TextView hostnameHolderTV = devDetailDialogView.findViewById(R.id.lan_dev_detail_dialog_hostname_holder_id);
                hostnameHolderTV.setText(selLANDevHostname);
            }else{
                LinearLayout hostnameLay = devDetailDialogView.findViewById(R.id.lan_dev_detail_dialog_hostname_lay_id);
                devDetailDialogOuterLL.removeView(hostnameLay);
            }

            devDetailDialogBuilder.setView(devDetailDialogView);
            devDetailDialogBuilder.setTitle(R.string.lan_dev_detail_dialog_title);
            devDetailDialogBuilder.setPositiveButton(R.string.alert_ok_btn, (dialogInterface, i) -> dialogInterface.dismiss());
            devDetailDialogBuilder.show();
        });

        LANDevLV.setOnItemLongClickListener((adapterView, view, position, l) -> { //show edit dialog on long item click
            LANDevRowDB selLANDev = (LANDevRowDB) adapterView.getItemAtPosition(position); //get object which represents selected LAN device

            AlertDialog.Builder devEditDialogBuilder = new AlertDialog.Builder(getActivity());
            View devEditDialogView = LayoutInflater.from(getActivity()).inflate(R.layout.lan_dev_edit_dialog, null);

            //ref to all ET items which can be edited by user - name, MAC, IP, hostname
            EditText nameET = devEditDialogView.findViewById(R.id.lan_dev_edit_dialog_name_et_id);
            EditText MACET = devEditDialogView.findViewById(R.id.lan_dev_edit_dialog_mac_et_id);
            EditText IPET = devEditDialogView.findViewById(R.id.lan_dev_edit_dialog_ip_et_id);
            EditText hostnameET = devEditDialogView.findViewById(R.id.lan_dev_edit_dialog_hostname_et_id);

            //fill already known items from DB - name + MAC is mandatory
            nameET.setText(selLANDev.getName());
            MACET.setText(selLANDev.getMac());

            //fill IP if available
            String selLANDevIP = selLANDev.getIp();
            if(selLANDevIP != null){
                IPET.setText(selLANDevIP);
            }

            //fill hostname if available
            String selLANDevHostname = selLANDev.getHostname();
            if(selLANDevHostname != null){
                hostnameET.setText(selLANDevHostname);
            }

            devEditDialogBuilder.setPositiveButton(R.string.dialog_positive_btn_edit, null);
            devEditDialogBuilder.setNegativeButton(R.string.dialog_negative_btn_cancel, null);

            AlertDialog devEditDialog = devEditDialogBuilder.create();
            devEditDialog.setView(devEditDialogView);
            devEditDialog.setTitle(R.string.lan_dev_edit_dialog_title);
            devEditDialog.setOnShowListener(dialogInterface -> {
                Button positiveBtn = devEditDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveBtn.setOnClickListener(view1 -> {
                    //get text fields which could be edited by user
                    String newDevName = nameET.getText().toString().trim();
                    String newDevMAC = MACET.getText().toString().trim();
                    String newDevIP = IPET.getText().toString().trim();
                    String newDevHostname = hostnameET.getText().toString().trim();

                    //device name + MAC is mandatory - do not update if not valid
                    if(newDevName.isEmpty()){ //device name not entered
                        Toast.makeText(getActivity(), R.string.lan_dev_edit_dialog_name_mis_toast, Toast.LENGTH_LONG).show();
                        return; //cannot continue WO new device name
                    }else{ //new device name valid, edit object
                        selLANDev.setName(newDevName);
                    }

                    if(!newDevMAC.isEmpty()){ //MAC entered
                        MagicPacketLogic magicPackLogic = new MagicPacketLogic();
                        if(!magicPackLogic.isMacValid(newDevMAC)){ //check whether MAC in valid format
                            AlertDialog devMACInvalidBuilder = new AlertDialog.Builder(getActivity()).create();
                            devMACInvalidBuilder.setTitle(R.string.lan_dev_edit_dialog_mac_alert_title);
                            devMACInvalidBuilder.setMessage(getString(R.string.lan_dev_edit_dialog_mac_alert_mes_1) + System.lineSeparator() + System.lineSeparator() + getString(R.string.lan_dev_edit_dialog_mac_alert_mes_2) + System.lineSeparator() + System.lineSeparator() + getString(R.string.lan_dev_edit_dialog_mac_alert_mes_3));
                            devMACInvalidBuilder.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.alert_ok_btn), (dialogInterface1, i1) -> dialogInterface1.dismiss());
                            devMACInvalidBuilder.show();
                            return; //cannot continue WO valid device MAC
                        }else{
                            selLANDev.setMac(newDevMAC);
                        }
                    }else{ //MAC not entered
                        Toast.makeText(getActivity(), R.string.lan_dev_edit_dialog_mac_mis_toast, Toast.LENGTH_LONG).show();
                        return; //cannot continue WO device MAC entered
                    }

                    //check whether device with same name / MAC already present - START
                    String presentInList = dbLogic.isLANDevPresent(newDevName.toLowerCase(), newDevMAC.toLowerCase(), selLANDev.getId());
                    if(presentInList != null){ //device already present, cannot continue
                        Toast.makeText(getActivity(), getString(R.string.add_device_frag_already_exists_toast) + "\"" + presentInList + "\"", Toast.LENGTH_LONG).show();
                        return;
                    }
                    //check whether device with same name / MAC already present - END

                    //OK - name + MAC valid, can perform database update
                    //check if IP filled + seems to be valid
                    if(!newDevIP.isEmpty()){ //IP entered
                        DeviceInfoLogic devInfoLogic = new DeviceInfoLogic();
                        if(devInfoLogic.isIPValid(newDevIP)){ //check whether filled IP is valid
                            selLANDev.setIp(newDevIP);
                        }else{ //user filled in invalid IP
                            Toast.makeText(getActivity(), getString(R.string.lan_dev_edit_dialog_ip_invalid_toast), Toast.LENGTH_LONG).show();
                            return; //cannot continue with wrong IP format entered
                        }
                    }else{ //IP not entered, leave null in DB
                        selLANDev.setIp(null);
                    }

                    if(!newDevHostname.isEmpty()){ //user filled in new hostname
                        selLANDev.setHostname(newDevHostname);
                    }else{ //hostname not entered, leave null in DB
                        selLANDev.setHostname(null);
                    }

                    dbLogic.updateLanDev(selLANDev); //perform update in DB
                    LANAdapter.updateDev(selLANDev, position);

                    dialogInterface.dismiss();
                });

                Button negativeBtn = devEditDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                negativeBtn.setOnClickListener(view1 -> dialogInterface.dismiss());
            });

            devEditDialog.show();
            return true;
        });

        return lanDevFragLayout;
    }

    /**
     * ActionBar will contain button through which can user add new LAN device into list.
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.add_bar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Is called when button / icon from ActionBar is tapped. Only button for adding new device is present.
     * @param item MenuItem object which represents tapped item
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        AddDeviceFrag addDeviceFrag = new AddDeviceFrag();
        Bundle infoBundle = new Bundle();
        infoBundle.putString("prefListName", getArguments().getString("lanListName"));
        addDeviceFrag.setArguments(infoBundle);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_frame_nav_id, addDeviceFrag).addToBackStack(null).commit();
        return super.onOptionsItemSelected(item);
    }
}
