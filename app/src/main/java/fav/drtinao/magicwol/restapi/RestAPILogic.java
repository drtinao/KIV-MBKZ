package fav.drtinao.magicwol.restapi;

import fav.drtinao.magicwol.UserLoginState;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

/**
 * Contains methods which are used for communication with rest API which is used for performing various tasks related to university computers.
 */
public class RestAPILogic {
    public static final String MASTER_BASE_URL = "http://147.228.127.76:8080"; //base URL of master server
    public static Retrofit restClient = null; //instance of rest client

    /**
     * Returns instance of rest API client which can be used to making API calls later on. Uses singleton pattern.
     * @return instance of rest API client
     */
    public static Retrofit getRestClient(){
        if(restClient == null){
            restClient = new Retrofit.Builder().baseUrl(MASTER_BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        }
        return restClient;
    }

    /**
     * Function contacts Rest API which determines user role according to given credentials.
     * @param orionLogin
     * @param orionPass
     * @return
     */
    public static UserLoginState getUserLoginState(String orionLogin, String orionPass){
        return UserLoginState.ORION_CLASSIC_USER_LOG;
    }

    /**
     * Method posts data regarding to newly created user device request to server.
     * Used by casual user.
     * @param devName name of device (user defined)
     * @param devMAC device´s MAC
     * @param devSeg segment where device is located
     */
    public static void postAddDeviceReq(String devName, String devMAC, String devSeg){

    }

    /**
     * Method posts data regarding to newly created device to server. Skips approval.
     * Used by system administrator.
     * @param devName name of device (user defined)
     * @param devMAC device´s MAC
     * @param devSeg segment where device is located
     */
    public static void postAddDeviceAdmin(String devName, String devMAC, String devSeg){

    }

    /**
     * Checks whether device with given MAC is already registered / waiting / not yet registered.
     * @param devMAC device´s MAC
     */
    public static void getDevRegState(String devMAC){

    }

    /**
     * Retrieves all devices which belong to the user.
     */
    public static void getUserDevices(){

    }
}
