package fav.drtinao.magicwol;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.List;

/**
 * User can add manage lists of LAN devices through this Fragment.
 */
public class LANListFrag extends Fragment {
    private LANListAdapter LANAdapter;
    private View lanListFragLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(R.string.lan_list_frag_title);
        setHasOptionsMenu(true);

        lanListFragLayout = inflater.inflate(R.layout.lan_list_frag, container, false);

        ListView LANListView = lanListFragLayout.findViewById(R.id.lan_list_frag_lv_id);
        SearchView LANSearchView = lanListFragLayout.findViewById(R.id.lan_list_frag_sv_id);

        DatabaseLogic dbLogic = new DatabaseLogic(getActivity());
        List<LANListRowDB> LANListRowDBList = dbLogic.getAllLanLists();

        LANAdapter = new LANListAdapter(getActivity(), LANListRowDBList);
        LANListView.setAdapter(LANAdapter);
        LANSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

        LANListView.setOnItemClickListener((adapterView, view, position, l) -> { //action is triggered when one of LAN lists is selected by user
            LANListRowDB selectedLANList = (LANListRowDB) adapterView.getItemAtPosition(position);

            LANDevFrag lanDevFrag = new LANDevFrag();
            Bundle infoBundle = new Bundle();
            infoBundle.putInt("lanListId", selectedLANList.getId()); //id of selected list
            infoBundle.putString("lanListName", selectedLANList.getName()); //name of selected list
            lanDevFrag.setArguments(infoBundle); //put id and name into Bundle, then read them in target Fragment
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_frame_nav_id, lanDevFrag).addToBackStack(null).commit(); //switch Fragment
        });

        LANListView.setOnItemLongClickListener((adapterView, view, position, l) -> {
            LANListRowDB selLANList = (LANListRowDB) adapterView.getItemAtPosition(position); //get selected LAN list

            AlertDialog.Builder listEditDialogBuilder = new AlertDialog.Builder(getActivity());
            View listEditDialogView = LayoutInflater.from(getActivity()).inflate(R.layout.lan_list_edit_dialog, null);

            //ref to list name + description ETs
            EditText nameET = listEditDialogView.findViewById(R.id.lan_list_edit_dialog_name_et_id);
            EditText descET = listEditDialogView.findViewById(R.id.lan_list_edit_dialog_desc_et_id);

            //fill known items - name is mandatory, desc can be omitted
            nameET.setText(selLANList.getName());

            String selLANListDesc = selLANList.getDescription();
            if(selLANListDesc != null){ //if desc is defined, fill it
                descET.setText(selLANListDesc);
            }

            listEditDialogBuilder.setPositiveButton(R.string.dialog_positive_btn_edit, null);
            listEditDialogBuilder.setNegativeButton(R.string.dialog_negative_btn_cancel, null);

            AlertDialog listEditDialog = listEditDialogBuilder.create();
            listEditDialog.setView(listEditDialogView);
            listEditDialog.setTitle(R.string.lan_list_edit_dialog_title);
            listEditDialog.setOnShowListener(dialogInterface -> {
                Button positiveBtn = listEditDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
                positiveBtn.setOnClickListener(view1 -> {
                    //get text filled by user
                    String newListName = nameET.getText().toString().trim();
                    String newListDesc = descET.getText().toString().trim();

                    if(newListName.isEmpty()){ //list name not entered
                        Toast.makeText(getActivity(), R.string.lan_list_edit_dialog_name_mis_toast, Toast.LENGTH_LONG).show();
                        return; //cannot continue WO list name
                    }else if(!selLANList.getName().equalsIgnoreCase(newListName) && dbLogic.isLANListPresent(newListName.toLowerCase())){ //list with same name already present
                        Toast.makeText(getActivity(), R.string.add_lan_list_already_exists_toast, Toast.LENGTH_LONG).show();
                        return;
                    }else{
                        selLANList.setName(newListName);
                    }

                    if(!newListDesc.isEmpty()){ //list description entered
                        selLANList.setDescription(newListDesc);
                    }else{ //not entered, set null in DB
                        selLANList.setDescription("");
                    }

                    dbLogic.updateLanList(selLANList);
                    LANAdapter.updateList(selLANList, position);

                    dialogInterface.dismiss();
                });

                Button negativeBtn = listEditDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
                negativeBtn.setOnClickListener(view1 -> dialogInterface.dismiss());
            });

            listEditDialog.show();
            return true;
        });

        return lanListFragLayout;
    }

    /**
     * ActionBar will contain button through which can user add new LAN list.
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.add_bar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Is called when button / icon from ActionBar is tapped. Just button for adding new list of LAN devices is present.
     * @param item MenuItem object which represents tapped item
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        AlertDialog addListDialog = DialogFactory.genAddListDialog(lanListFragLayout, null, LANAdapter);
        addListDialog.show();
        return super.onOptionsItemSelected(item);
    }
}
