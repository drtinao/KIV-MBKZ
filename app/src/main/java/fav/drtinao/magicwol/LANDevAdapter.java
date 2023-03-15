package fav.drtinao.magicwol;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LANDevAdapter extends BaseAdapter implements Filterable {

    /* variables assigned in constructor - START */
    private final Activity appActivity; //reference to current Activity
    private List<LANDevRowDB> savedLANDevices; //contains data regarding to saved LAN devices within specific list
    private List<LANDevRowDB> filteredLANDevices; //contains LAN devices which are visible within ListView
    /* variables assigned in constructor - END */

    private String latestSearchQuery; //latest query entered by user

    /**
     * Constructor takes reference to active Activity and List which contains LanDevRowDB objects, each represents one device in LAN.
     * @param appActivity application Activity
     * @param savedLANDevices List with LanDevRowDB instances
     */
    public LANDevAdapter(Activity appActivity, List<LANDevRowDB> savedLANDevices){
        this.appActivity = appActivity;
        this.savedLANDevices = savedLANDevices;
        this.filteredLANDevices = savedLANDevices;
        this.latestSearchQuery = "";
    }

    /**
     * Returns number of items currently presented in the ListView object with LAN devices.
     * @return number of visible LAN devices
     */
    @Override
    public int getCount() {
        return filteredLANDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return filteredLANDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * Method is called for every item passed to the adapter - sets layout for each item.
     */
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = new ViewHolder();

        if(view == null) { /* if layout not yet inflated, inflate it */
            LayoutInflater inflater = appActivity.getLayoutInflater();
            view = inflater.inflate(R.layout.lan_dev_single_item_lv, viewGroup, false);

            viewHolder.devName = view.findViewById(R.id.lan_dev_single_item_lv_name_tv_id);
            viewHolder.devMAC = view.findViewById(R.id.lan_dev_single_item_lv_mac_tv_id);
            viewHolder.devStart = view.findViewById(R.id.lan_dev_single_item_lv_start_ib_id);
            viewHolder.devDelete = view.findViewById(R.id.lan_dev_single_item_lv_delete_ib_id);

            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) view.getTag();
        }

        final LANDevRowDB savedLANDev = filteredLANDevices.get(i);

        viewHolder.devName.setText(savedLANDev.getName());
        viewHolder.devMAC.setText(savedLANDev.getMac());
        viewHolder.devStart.setOnClickListener(view1 -> startDev(savedLANDev.getMac(), savedLANDev.getName()));
        viewHolder.devDelete.setOnClickListener(view12 -> removeDev(savedLANDev));

        return view;
    }

    /**
     * Returns object which can be used for filtering results. In this case saved LAN devices within specific list are filtered.
     * @return Filter object used for result filtering
     */
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                latestSearchQuery = charSequence.toString();

                FilterResults filteredRes = new FilterResults();
                String userSearch = charSequence.toString().toLowerCase();

                if(charSequence.length() == 0){
                    filteredRes.values = savedLANDevices;
                    filteredRes.count = savedLANDevices.size();
                }else{
                    List<LANDevRowDB> filtered = new ArrayList<>();
                    for(LANDevRowDB oneLANDev : savedLANDevices){
                        if(oneLANDev.getName().toLowerCase().startsWith(userSearch) || oneLANDev.getMac().toLowerCase().startsWith(userSearch)){
                            filtered.add(oneLANDev);
                        }
                    }
                    filteredRes.values = filtered;
                    filteredRes.count = filtered.size();
                }

                return filteredRes;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredLANDevices = (List<LANDevRowDB>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    /**
     * Attempts to start LAN device with specific MAC address.
     * @param devMAC MAC of device which should be started
     * @param devName name of device which should be started
     */
    private void startDev(String devMAC, String devName){
        MagicPacketLogic magicPackLogic = new MagicPacketLogic();
        DeviceInfoLogic devInfoLogic = new DeviceInfoLogic();
        InetAddress broadcastIP = devInfoLogic.retrBroadcastIP();

        if(broadcastIP == null){ //invalid broadcast IP detected
            androidx.appcompat.app.AlertDialog noBroadcastIPAlert = DialogFactory.genNoBroadcastIPDialog(appActivity);
            noBroadcastIPAlert.show();
            return;
        }

        ExecutorService execServ = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        execServ.execute(() -> {
            magicPackLogic.sendMagicPacket(broadcastIP, devMAC);
            handler.post(() -> Toast.makeText(appActivity, appActivity.getResources().getString(R.string.lan_dev_started_1) + System.lineSeparator() + appActivity.getResources().getString(R.string.lan_dev_started_2) + devName + System.lineSeparator() + appActivity.getResources().getString(R.string.lan_dev_started_3) + devMAC, Toast.LENGTH_LONG).show());
        });
    }

    /**
     * Removes device with specific id from database.
     * @param lanDevRowDB represents removed device
     */
    private void removeDev(LANDevRowDB lanDevRowDB){
        AlertDialog.Builder removeDevBuilder = new AlertDialog.Builder(appActivity);
        removeDevBuilder.setTitle(appActivity.getResources().getString(R.string.lan_dev_frag_remove_dialog_title));
        removeDevBuilder.setMessage(appActivity.getResources().getString(R.string.lan_dev_frag_remove_dialog_cont_1) + " \"" + lanDevRowDB.getName() + "\"" + appActivity.getResources().getString(R.string.lan_dev_frag_remove_dialog_cont_2));
        removeDevBuilder.setNegativeButton(R.string.dialog_negative_btn_no, (dialog, which) -> dialog.dismiss());
        removeDevBuilder.setPositiveButton(R.string.dialog_positive_btn_yes, (dialog, which) -> {
            Toast.makeText(appActivity, appActivity.getResources().getString(R.string.lan_dev_frag_removed_toast) + " \"" + lanDevRowDB.getName() + "\"", Toast.LENGTH_LONG).show();
            DatabaseLogic dbLogic = new DatabaseLogic(appActivity);
            dbLogic.removeLanDev(lanDevRowDB.getId());

            this.savedLANDevices.remove(lanDevRowDB);
            if(!latestSearchQuery.isEmpty() && (lanDevRowDB.getName().toLowerCase().startsWith(latestSearchQuery) || lanDevRowDB.getMac().toLowerCase().startsWith(latestSearchQuery))){ //item is visible in filtered list - delete
                this.filteredLANDevices.remove(lanDevRowDB);
            }
            notifyDataSetChanged();
            dialog.dismiss();
        });
        removeDevBuilder.show();
    }

    /**
     * Updates LAN device information visible in ListView object.
     * @param updatedLANDev object which represents clicked LAN device
     * @param position position of clicked item within List with LAN devices
     */
    public void updateDev(LANDevRowDB updatedLANDev, int position){
        this.filteredLANDevices.get(position).setName(updatedLANDev.getName());
        this.filteredLANDevices.get(position).setMac(updatedLANDev.getMac());

        notifyDataSetChanged();
    }

    /**
     * Represents one item in ListView => one LAN device.
     */
    static class ViewHolder{
        TextView devName; //name of device
        TextView devMAC; //MAC of device
        ImageButton devStart; //button for device start
        ImageButton devDelete; //button for device delete
    }
}
