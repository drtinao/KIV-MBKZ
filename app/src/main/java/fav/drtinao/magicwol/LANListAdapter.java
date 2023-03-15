package fav.drtinao.magicwol;

import android.app.Activity;
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

import androidx.appcompat.app.AlertDialog;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class represents bridge between ListView, which will be used for displaying available lists with LAN devices and ArrayList which contains data regarding to lists with LAN devices.
 */
public class LANListAdapter extends BaseAdapter implements Filterable {
    /* variables assigned in constructor - START */
    private final Activity appActivity; //reference to current Activity
    private List<LANListRowDB> savedLANLists; //contains data regarding to saved lists with LAN devices
    private List<LANListRowDB> filteredLANLists; //contains LAN lists which are visible in users ListView
    /* variables assigned in constructor - END */

    private String latestSearchQuery; //latest query entered by user

    /**
     * Constructor takes reference to Activity of the application and ArrayList with saved data regarding to LAN lists.
     * @param appActivity application Activity
     * @param savedLANLists ArrayList with LanListRowDB instances - each represents one LAN list
     */
    public LANListAdapter(Activity appActivity, List<LANListRowDB> savedLANLists){
        this.appActivity = appActivity;
        this.savedLANLists = savedLANLists;
        this.filteredLANLists = savedLANLists;
        this.latestSearchQuery = "";
    }

    /**
     * Returns number of items presented in the ListView object. In this case returns number of available lists with LAN devices.
     * @return number of lists with LAN devices
     */
    @Override
    public int getCount() {
        return filteredLANLists.size();
    }

    @Override
    public Object getItem(int i) {
        return filteredLANLists.get(i);
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
            view = inflater.inflate(R.layout.lan_list_single_item_lv, viewGroup, false);

            viewHolder.listName = view.findViewById(R.id.lan_list_single_item_lv_name_tv_id);
            viewHolder.listDesc = view.findViewById(R.id.lan_list_single_item_lv_desc_tv_id);
            viewHolder.listStart = view.findViewById(R.id.lan_list_single_item_lv_start_ib_id);
            viewHolder.listDelete = view.findViewById(R.id.lan_list_single_item_delete_ib_id);

            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) view.getTag();
        }

        final LANListRowDB savedLANList = filteredLANLists.get(i);

        viewHolder.listName.setText(savedLANList.getName());
        String listDescDB = savedLANList.getDescription();
        if(!listDescDB.isEmpty()){
            viewHolder.listDesc.setText(listDescDB);
        }else{
            viewHolder.listDesc.setText(appActivity.getString(R.string.lan_list_frag_no_desc_viewholder));
        }
        viewHolder.listStart.setOnClickListener(view1 -> startList(savedLANList.getId()));
        viewHolder.listDelete.setOnClickListener(view12 -> removeList(savedLANList));

        return view;
    }

    /**
     * Performs result filtering. Method is used when filtering of LAN lists with devices is required.
     * @return Filter object which can be used for filtering result
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
                    filteredRes.values = savedLANLists;
                    filteredRes.count = savedLANLists.size();
                }else{
                    List<LANListRowDB> filtered = new ArrayList<>();
                    for(LANListRowDB oneLANList : savedLANLists){ //go through all loaded lists and check whether match found
                        if(oneLANList.getName().toLowerCase().startsWith(userSearch) || oneLANList.getDescription().toLowerCase().startsWith(userSearch)){
                            filtered.add(oneLANList);
                        }
                    }
                    filteredRes.values = filtered;
                    filteredRes.count = filtered.size();
                }

                return filteredRes;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredLANLists = (List<LANListRowDB>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    /**
     * Used for removing list with specific id from table with LAN lists. Also deletes devices which belong to the list from table with devices.
     * @param lanListRowDB represents list with LAN devices which should be deleted
     */
    private void removeList(LANListRowDB lanListRowDB){
        AlertDialog.Builder removeListBuilder = new AlertDialog.Builder(appActivity);
        removeListBuilder.setTitle(R.string.lan_list_frag_remove_dialog_title);
        removeListBuilder.setMessage(appActivity.getResources().getString(R.string.lan_list_frag_remove_dialog_cont_1) + " \"" + lanListRowDB.getName() + "\" " + appActivity.getResources().getString(R.string.lan_list_frag_remove_dialog_cont_2));
        removeListBuilder.setNegativeButton(R.string.dialog_negative_btn_no, (dialog, which) -> dialog.dismiss());
        removeListBuilder.setPositiveButton(R.string.dialog_positive_btn_yes, (dialog, which) -> {
            Toast.makeText(appActivity, appActivity.getResources().getString(R.string.lan_list_frag_removed_toast) + " \"" + lanListRowDB.getName() + "\"", Toast.LENGTH_LONG).show();
            DatabaseLogic dbLogic = new DatabaseLogic(appActivity);
            dbLogic.removeLanList(lanListRowDB.getId()); //remove from database

            this.savedLANLists.remove(lanListRowDB); //remove from list with all LAN lists
            if(!latestSearchQuery.isEmpty() && (lanListRowDB.getName().toLowerCase().startsWith(latestSearchQuery) || lanListRowDB.getDescription().toLowerCase().startsWith(latestSearchQuery))){ //item is visible in filtered list - delete
                this.filteredLANLists.remove(lanListRowDB);
            }
            notifyDataSetChanged();

            dialog.dismiss();
        });
        removeListBuilder.show();
    }

    /**
     * Adds new list with LAN devices created by user into visible ListView object.
     * @param newLANList object which represents newly created LAN list
     */
    public void addList(LANListRowDB newLANList){
        this.savedLANLists.add(newLANList); //add to list with all LAN lists
        if(!latestSearchQuery.isEmpty() && (newLANList.getName().toLowerCase().startsWith(latestSearchQuery) || newLANList.getDescription().toLowerCase().startsWith(latestSearchQuery))){ //add to filtered LAN lists
            this.filteredLANLists.add(newLANList);
        }

        notifyDataSetChanged();
    }

    /**
     * Updates LAN list information visible in ListView object.
     * @param updatedLANList object which represents clicked LAN device
     * @param position position of clicked item within List with LAN devices
     */
    public void updateList(LANListRowDB updatedLANList, int position){
        this.filteredLANLists.get(position).setName(updatedLANList.getName());
        this.filteredLANLists.get(position).setDescription(updatedLANList.getDescription());

        notifyDataSetChanged();
    }

    /**
     * Attempts to start all computers which belong to list with specific id.
     * @param id id of list which should be started
     */
    private void startList(int id){
        MagicPacketLogic magicPackLogic = new MagicPacketLogic();
        DeviceInfoLogic devInfoLogic = new DeviceInfoLogic();
        InetAddress broadcastIP = devInfoLogic.retrBroadcastIP();

        if(broadcastIP == null){ //invalid broadcast IP detected
            androidx.appcompat.app.AlertDialog noBroadcastIPAlert = DialogFactory.genNoBroadcastIPDialog(appActivity);
            noBroadcastIPAlert.show();
            return;
        }

        DatabaseLogic dbLogic = new DatabaseLogic(appActivity);
        List<LANDevRowDB> devicesInList = dbLogic.getLANDevicesInList(id);
        if(devicesInList.isEmpty()){
            Toast.makeText(appActivity, appActivity.getResources().getString(R.string.lan_list_no_dev), Toast.LENGTH_LONG).show();
            return;
        }

        ExecutorService execServ = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        LANDevRowDB curLANDev;
        for(int i = 0; i < devicesInList.size(); i++){ //go through retrieved devices and attempt to start each one
            curLANDev = devicesInList.get(i);

            String finalCurLANDevMAC = curLANDev.getMac();
            String finalCurLANDevName = curLANDev.getName();
            execServ.execute(() -> {
                magicPackLogic.sendMagicPacket(broadcastIP, finalCurLANDevMAC);
                handler.post(() -> Toast.makeText(appActivity, appActivity.getResources().getString(R.string.lan_dev_started_1) + System.lineSeparator() + appActivity.getResources().getString(R.string.lan_dev_started_2) + finalCurLANDevName + System.lineSeparator() + appActivity.getResources().getString(R.string.lan_dev_started_3) + finalCurLANDevMAC, Toast.LENGTH_LONG).show());
            });
        }
    }

    /**
     * Represents one item in ListView => one LAN list.
     */
    static class ViewHolder{
        TextView listName; //name of list
        TextView listDesc; //list description
        ImageButton listStart; //button for starting all devices in list
        ImageButton listDelete; //button for deleting the whole list
    }
}
