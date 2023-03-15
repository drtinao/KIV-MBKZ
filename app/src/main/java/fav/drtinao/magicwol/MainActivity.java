package fav.drtinao.magicwol;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;

import com.google.android.material.internal.NavigationMenuItemView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer_nav; /* reference to DrawerLayout - responsible for displaying navigation on left side */
    private SharedPreferences sharedPreferences; /* read / edit app preferences */
    private NavigationView navigation_nav; /* reference to NavigationView - delivers content to DrawerLayout */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
        setContentView(R.layout.activity_main);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String[] pingTimeoutPrefVals = getResources().getStringArray(R.array.ping_timeout_pref_values);
        String pingTimeoutPref = sharedPreferences.getString("ping_timeout_pref", pingTimeoutPrefVals[3]);
        DeviceInfoLogic.pingTimeoutMS = Integer.parseInt(pingTimeoutPref);

        drawer_nav = findViewById(R.id.activity_main_drawer_nav_id);

        navigation_nav = findViewById(R.id.activity_main_nav_id);
        navigation_nav.setNavigationItemSelectedListener(this);

        Toolbar toolbar = findViewById(R.id.activity_main_toolbar_nav_id);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawer_nav, toolbar, R.string.activity_main_drawer_open, R.string.activity_main_drawer_close);
        drawer_nav.addDrawerListener(drawerToggle);

        final InputMethodManager inputMM = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        drawer_nav.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View view, float v) {
                inputMM.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            @Override
            public void onDrawerOpened(@NonNull View view) {
                inputMM.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            @Override
            public void onDrawerClosed(@NonNull View view) {
                inputMM.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            @Override
            public void onDrawerStateChanged(int i) {

            }
        });

        drawerToggle.syncState();

        if(sharedPreferences.getBoolean("first_run", true)){ /* app started for first time, display login */
            getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_frame_nav_id, new LoginFrag()).addToBackStack(null).commit();
            navigation_nav.setCheckedItem(R.id.menu_nav_more_login_id);
            sharedPreferences.edit().putBoolean("first_run", false).apply();
        }else if(savedInstanceState == null){ /* if no previous state defined, then display one of the Fragments (fragment for adding new machine in this case) */
            showDefFrag();
        }
    }

    /**
     * Switches to default fragment, which is loaded on application start. Can be changed in settings.
     */
    public void showDefFrag(){
        String[] defFragPrefEntryVals = getResources().getStringArray(R.array.def_frag_pref_entry_values);
        String defFragPref = sharedPreferences.getString("def_frag_pref", defFragPrefEntryVals[1]); //by default show fragment for device add

        if(defFragPref.equals(defFragPrefEntryVals[0])){ //show LAN lists
            getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_frame_nav_id, new LANListFrag()).addToBackStack(null).commit();
            navigation_nav.setCheckedItem(R.id.menu_nav_lan_lists_id);
        }else if(defFragPref.equals(defFragPrefEntryVals[1])){ //show add device
            getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_frame_nav_id, new AddDeviceFrag()).addToBackStack(null).commit();
            navigation_nav.setCheckedItem(R.id.menu_nav_lan_add_device_id);
        }else if(defFragPref.equals(defFragPrefEntryVals[2])){ //show settings
            getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_frame_nav_id, new PrefFrag()).addToBackStack(null).commit();
            navigation_nav.setCheckedItem(R.id.menu_nav_more_settings_id);
        }
    }

    /**
     * Sets visibility of items in navigation panel according to the current user login state.
     * @param userLoginState value from UserLoginState enum - describes current login state
     */
    public void setVisibilityNav(UserLoginState userLoginState){
        switch(userLoginState){
            case ORION_CLASSIC_USER_LOG: //normal user - authenticated; change login to logout; show def fragment
            case ORION_ADMINISTRATOR_LOG: //admin user - authenticated; change login to logout; show def fragment
                MenuItem moreLoginItem = navigation_nav.getMenu().findItem(R.id.menu_nav_more_login_id);
                moreLoginItem.setTitle(getResources().getString(R.string.menu_nav_more_logout));
                showDefFrag();
                break;
            case ORION_ERR_NOT_FOUND: //user not found - err
            case ORION_ERR_PASS: //wrong password - err
                AlertDialog orionLoginNotFoundAlert = DialogFactory.genOrionLoginErrDialog(this, userLoginState);
                orionLoginNotFoundAlert.show();
                break;
            default: //Kerberos server probably down
                //AlertDialog orionServerAlert = DialogFactory.genOrionLoginErrDialog(loginFragLayout.getContext(), null);
                //orionServerAlert.show();
                break;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    /**
     * Method is called when user presses the back button (native Android function).
     * Behaviour is different, depending on situation:
     * a) navigation panel is visible -> navigation panel will be hidden
     * b) navigation panel is NOT visible -> application will be backgrounded
     */
    @Override
    public void onBackPressed() {
        if(drawer_nav.isDrawerOpen(GravityCompat.START)){
            drawer_nav.closeDrawer(GravityCompat.START);
        }else if(getSupportFragmentManager().getBackStackEntryCount() != 1){
            super.onBackPressed();
        }else{ //minimize app
            this.moveTaskToBack(true);
        }
    }

    /**
     * Code inside is executed when item from navigation panel is tapped.
     * Action which is performed depends on item, which is tapped. Simply said - this method displays Fragment (part of application) which is relevant
     * to item selected from navigation panel.
     * @param selectedItem MenuItem object which represents tapped item (from navigation panel)
     * @return true (= ok, item selected, action performed)
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem selectedItem) {
        switch(selectedItem.getItemId()){
            /* LAN - display manager for devices Fragment */
            case R.id.menu_nav_lan_lists_id:
                getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_frame_nav_id, new LANListFrag()).addToBackStack(null).commit();
                break;

            /* LAN - display add new device Fragment */
            case R.id.menu_nav_lan_add_device_id:
                getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_frame_nav_id, new AddDeviceFrag()).addToBackStack(null).commit();
                break;

            /* uni - display manager for devices Fragment */
            case R.id.menu_nav_uni_lists_id:
                getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_frame_nav_id, new LANListFrag()).addToBackStack(null).commit();
                break;

            /* uni - display add new device Fragment */
            case R.id.menu_nav_uni_add_device_id:
                getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_frame_nav_id, new uniAddDeviceFrag()).addToBackStack(null).commit();
                break;

            /* more - display login Fragment */
            case R.id.menu_nav_more_login_id:
                getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_frame_nav_id, new LoginFrag()).addToBackStack(null).commit();
                break;

            /* more - display settings Fragment */
            case R.id.menu_nav_more_settings_id:
                getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_frame_nav_id, new PrefFrag()).addToBackStack(null).commit();
                break;
        }
        getSupportFragmentManager().executePendingTransactions();

        /* close navigation after item selection */
        drawer_nav.closeDrawer(GravityCompat.START);
        return true;
    }
}