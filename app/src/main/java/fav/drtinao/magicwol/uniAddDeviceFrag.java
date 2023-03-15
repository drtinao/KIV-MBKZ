package fav.drtinao.magicwol;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Fragment is used for adding new uni device to list.
 */
public class uniAddDeviceFrag extends Fragment {
    private static TextView logTV; //user visible log, should show reflect application state

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(R.string.uni_add_device_frag_title);

        View uniAddDeviceFragLayout = inflater.inflate(R.layout.uni_add_device_frag, container, false);

        logTV = uniAddDeviceFragLayout.findViewById(R.id.uni_add_device_frag_log_cont_tv_id); //event log
        logTV.setMovementMethod(new ScrollingMovementMethod());

        //set onclick listeners for layout items
        Button testDevBtn = uniAddDeviceFragLayout.findViewById(R.id.uni_add_device_frag_test_dev_btn_id);
        testDevBtn.setOnClickListener(view -> testDevice(uniAddDeviceFragLayout));
        Button sendReqBtn = uniAddDeviceFragLayout.findViewById(R.id.uni_add_device_frag_send_req_btn_id);
        sendReqBtn.setOnClickListener(view -> sendDeviceReq(uniAddDeviceFragLayout));

        return uniAddDeviceFragLayout;
    }

    /**
     * Executed when button for testing device state is clicked.
     * @param uniAddDeviceFragLayout inflated layout with UI components
     */
    public void testDevice(View uniAddDeviceFragLayout){
        //reference to items which are used for user interaction - MAC, segment, timeout
        EditText nameET = uniAddDeviceFragLayout.findViewById(R.id.uni_add_device_frag_name_et_id); //name
        EditText macET = uniAddDeviceFragLayout.findViewById(R.id.uni_add_device_frag_mac_et_id); //MAC
        Spinner segmentSpin = uniAddDeviceFragLayout.findViewById(R.id.uni_add_device_frag_seg_spin_id); //spinner with segment selection

        //text entered by user
        String nameETText = nameET.getText().toString().trim();
        String macETText = macET.getText().toString().trim();
        String segmentSpinText = segmentSpin.getSelectedItem().toString();


        Toast.makeText(uniAddDeviceFragLayout.getContext(), "Testing device clicked", Toast.LENGTH_LONG).show();
        logTV.append(getString(R.string.uni_add_device_frag_log_divider) + System.lineSeparator() + getString(R.string.uni_add_device_frag_log_test_1) + System.lineSeparator() + getString(R.string.uni_add_device_frag_log_test_2) + macETText + System.lineSeparator() + getString(R.string.uni_add_device_frag_log_test_3) + segmentSpinText + System.lineSeparator());
        //check whether device is already registered or not


        //check whether pingable or not
    }

    /**
     * Triggered when button for sending request is clicked.
     * @param uniAddDeviceFragLayout inflated layout with UI components
     */
    public void sendDeviceReq(View uniAddDeviceFragLayout){
        Toast.makeText(uniAddDeviceFragLayout.getContext(), "Send request clicked", Toast.LENGTH_LONG).show();
        logTV.append(getString(R.string.uni_add_device_frag_log_divider) + System.lineSeparator() + System.lineSeparator());
    }
}
