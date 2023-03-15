package fav.drtinao.magicwol;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

/**
 * Class provides static methods for generating dialogs which are used across multiple parts of user interface.
 */
public class DialogFactory {
    /**
     * Generates dialog which is used for adding list with LAN devices into DB.
     * @param layoutView reference to layout within which is dialog created
     * @param spinnerAdapter adapter which is used as bridge: DB <-> spinner with LAN lists ; null if not used
     * @param LANAdapter adapter used as bridge: DB <-> ListView with LAN lists ; null if not used
     * @return AlertDialog object
     */
    public static AlertDialog genAddListDialog(View layoutView, ArrayAdapter<LANListRowDB> spinnerAdapter, LANListAdapter LANAdapter){
        AlertDialog.Builder addListBuilder = new AlertDialog.Builder(layoutView.getContext());
        View addListView = LayoutInflater.from(layoutView.getContext()).inflate(R.layout.add_lan_list_dialog, null);

        //reference to fields which contain user entered info - add_lan_list_dialog xml
        TextView listName = addListView.findViewById(R.id.add_lan_list_dialog_name_et_id); //name of list
        TextView listDesc = addListView.findViewById(R.id.add_lan_list_dialog_desc_et_id); //description of list

        addListBuilder.setPositiveButton(R.string.add_lan_list_dialog_positive_btn, null);
        addListBuilder.setNegativeButton(R.string.add_lan_list_dialog_negative_btn, null);

        AlertDialog addListDialog = addListBuilder.create();
        addListDialog.setView(addListView);
        addListDialog.setTitle(R.string.add_lan_list_dialog_title);
        addListDialog.setOnShowListener(dialogInterface -> {
            Button positiveBtn = addListDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
            positiveBtn.setOnClickListener(view -> {
                //user entered text content
                String listNameText = listName.getText().toString().trim();
                if(listNameText.isEmpty()){
                    Toast.makeText(layoutView.getContext(), R.string.add_lan_list_name_mis_toast, Toast.LENGTH_LONG).show();
                    return;
                }
                String listDescText = listDesc.getText().toString().trim();

                DatabaseLogic dbLogic = new DatabaseLogic(layoutView.getContext());

                if(dbLogic.isLANListPresent(listNameText.toLowerCase())){ //if list with same name exist, error
                    Toast.makeText(layoutView.getContext(), R.string.add_lan_list_already_exists_toast, Toast.LENGTH_LONG).show();
                }else{ //list not present yet, save
                    LANListRowDB lanListRowDB = new LANListRowDB();
                    lanListRowDB.setName(listNameText);
                    lanListRowDB.setDescription(listDescText);
                    long insertRes = dbLogic.addLanList(lanListRowDB);
                    lanListRowDB.setId((int)insertRes);
                    if(spinnerAdapter != null){ //update spinner data
                        spinnerAdapter.add(lanListRowDB);
                    }else if(LANAdapter != null){ //update user visible ListView
                        LANAdapter.addList(lanListRowDB);
                    }
                    dialogInterface.dismiss();

                    Toast.makeText(layoutView.getContext(), layoutView.getResources().getString(R.string.lan_list_frag_saved_toast) + " \"" + listName.getText().toString() + "\"", Toast.LENGTH_LONG).show();
                }
            });
            Button negativeBtn = addListDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
            negativeBtn.setOnClickListener(view -> dialogInterface.dismiss());
        });

        return addListDialog;
    }

    /**
     * Generates dialog which reminds user about the fact that he / she is probably not connected to WiFi.
     * @param layoutContext reference to layout within which is dialog created
     * @return AlertDialog object
     */
    public static AlertDialog genNoBroadcastIPDialog(Context layoutContext){
        AlertDialog broadcastIPAlert = new AlertDialog.Builder(layoutContext).create();
        broadcastIPAlert.setTitle(layoutContext.getResources().getString(R.string.add_device_frag_broad_ip_alert_title));
        broadcastIPAlert.setMessage(layoutContext.getResources().getString(R.string.add_device_frag_broad_ip_alert_mes_1) + System.lineSeparator() + System.lineSeparator() + layoutContext.getResources().getString(R.string.add_device_frag_broad_ip_alert_mes_2));
        broadcastIPAlert.setButton(Dialog.BUTTON_POSITIVE, layoutContext.getResources().getString(R.string.alert_ok_btn), (dialogInterface, i) -> broadcastIPAlert.dismiss());
        return broadcastIPAlert;
    }

    /**
     * Generates dialog which warns user about the fact that error with Orion login occured.
     * @param layoutContext reference to layout within which is dialog created
     * @return AlertDialog object
     */
    public static AlertDialog genOrionLoginErrDialog(Context layoutContext, UserLoginState userLoginState){
        AlertDialog orionLoginAlert = new AlertDialog.Builder(layoutContext).create();
        orionLoginAlert.setIcon(android.R.drawable.ic_dialog_alert);
        orionLoginAlert.setTitle(R.string.login_frag_orion_alert_title);
        orionLoginAlert.setButton(Dialog.BUTTON_POSITIVE, layoutContext.getResources().getString(R.string.alert_ok_btn), (dialogInterface, i) -> dialogInterface.dismiss());
        switch(userLoginState){
            case ORION_ERR_NOT_FOUND: //user not found - err
                orionLoginAlert.setMessage(layoutContext.getString(R.string.login_frag_orion_alert_mes_not_found) + System.lineSeparator() + layoutContext.getString(R.string.login_frag_orion_alert_end_try));
                break;
            case ORION_ERR_PASS: //wrong password - err
                orionLoginAlert.setMessage(layoutContext.getString(R.string.login_frag_orion_alert_mes_wrong_pass) + System.lineSeparator() + layoutContext.getString(R.string.login_frag_orion_alert_end_try));
                break;
            default: //Kerberos server probably down
                orionLoginAlert.setMessage(layoutContext.getString(R.string.login_frag_orion_alert_mes_server) + System.lineSeparator() + layoutContext.getString(R.string.login_frag_orion_alert_end_try));
                break;
        }
        return orionLoginAlert;
    }
}
