package fav.drtinao.magicwol;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.gson.JsonObject;

import java.io.IOException;

import fav.drtinao.magicwol.restapi.RestAPILogic;
import fav.drtinao.magicwol.restapi.action.ActionAPIInterface;
import fav.drtinao.magicwol.restapi.login.LoginAPIInterface;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Fragment is used for user login.
 */
public class LoginFrag extends Fragment {
    MainActivity curActivity; /* reference to currently active Activity */
    View loginFragLayout; /* layout which defines UI */

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        curActivity = (MainActivity) getActivity();
        curActivity.getSupportActionBar().setTitle(R.string.login_frag_title);

        loginFragLayout = inflater.inflate(R.layout.login_frag, container, false);

        //set onclick listeners for layout items
        Button loginBtn = loginFragLayout.findViewById(R.id.login_frag_login_btn_id);
        loginBtn.setOnClickListener(view -> perfOrionLogin());
        Button noLoginBtn = loginFragLayout.findViewById(R.id.login_frag_no_login_btn_id);
        noLoginBtn.setOnClickListener(view -> contWOLogin());
        return loginFragLayout;
    }

    /**
     * Triggered when user clicks button for login using Orion credentials.
     */
    public void perfOrionLogin(){
        //reference to Orion login fields filled by user - login_frag xml
        EditText orionLoginET = loginFragLayout.findViewById(R.id.login_frag_login_et_id);
        EditText orionPassET = loginFragLayout.findViewById(R.id.login_frag_pass_et_id);

        //get text entered by user
        String orionLoginText = orionLoginET.getText().toString().trim();
        String orionPassText = orionPassET.getText().toString().trim();

        Toast.makeText(loginFragLayout.getContext(), "Got login: " + orionLoginText + ", pass: " + orionPassText, Toast.LENGTH_LONG).show();

        //verify user using Kerberos - Orion login
        Retrofit restClient = RestAPILogic.getRestClient();
        LoginAPIInterface loginApiCall = restClient.create(LoginAPIInterface.class);

        //create object with user login credentials, send within POST req body
        JsonObject orionCreds = new JsonObject();
        orionCreds.addProperty("orionLogin", orionLoginText);
        orionCreds.addProperty("orionPass", orionPassText);
        Call<ResponseBody> loginCall = loginApiCall.postLogin(orionCreds);
        loginCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //response call
                if(response.code() != 200){ //HTTP 200 is OK
                    Toast.makeText(curActivity, "err call" + response.toString(), Toast.LENGTH_LONG).show();
                    try {
                        Log.i("magicwol_debug", call.toString() + " BODYERR "  + response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(curActivity, "OK call" + response.toString(), Toast.LENGTH_LONG).show();
                    try {
                        String loginResponse = response.body().string();
                        Log.i("magicwol_debug", call.toString() + " BODYOK "  + loginResponse);
                        if(loginResponse.equals("true")){ //login valid
                            Toast.makeText(curActivity, "Přihlášení proběhlo úspěšně!", Toast.LENGTH_SHORT).show();

                            UserLoginState userLoginState = RestAPILogic.getUserLoginState(orionLoginText, orionPassText);
                            switch(userLoginState){
                                case ORION_CLASSIC_USER_LOG: //normal user - authenticated
                                    curActivity.showDefFrag();
                                    curActivity.setVisibilityNav(userLoginState);
                                    break;
                                case ORION_ADMINISTRATOR_LOG: //admin user - authenticated
                                    curActivity.showDefFrag();
                                    break;
                                case ORION_ERR_NOT_FOUND: //user not found - err
                                    AlertDialog orionLoginNotFoundAlert = DialogFactory.genOrionLoginErrDialog(loginFragLayout.getContext(), UserLoginState.ORION_ERR_NOT_FOUND);
                                    orionLoginNotFoundAlert.show();
                                    break;
                                case ORION_ERR_PASS: //wrong password - err
                                    AlertDialog orionPassAlert = DialogFactory.genOrionLoginErrDialog(loginFragLayout.getContext(), UserLoginState.ORION_ERR_PASS);
                                    orionPassAlert.show();
                                    break;
                                default: //Kerberos server probably down
                                    AlertDialog orionServerAlert = DialogFactory.genOrionLoginErrDialog(loginFragLayout.getContext(), null);
                                    orionServerAlert.show();
                                    break;
                            }
                        }else if(loginResponse.equals("false")){ //login invalid
                            Toast.makeText(curActivity, "Uživatelské jméno či heslo není správné!", Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(curActivity, "Got response " + loginResponse, Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(curActivity, "failuree" + call.toString() + " "  + t.toString(), Toast.LENGTH_LONG).show();
                Log.i("magicwol_debug", "failuree" + call.toString() + " "  + t.toString());
            }
        });
    }

    /**
     * Method is executed when user wants to continue without Orion login.
     */
    public void contWOLogin(){
        Toast.makeText(loginFragLayout.getContext(), "Cont WO login", Toast.LENGTH_SHORT).show();
        curActivity.showDefFrag(); //switch to default fragment
    }
}
