package fav.drtinao.magicwol;

import android.util.Log;
import android.util.Patterns;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * Contains methods for retrieving information about computer in network.
 */
public class DeviceInfoLogic {
    private final String LOG_TAG = "magicwol_debug";
    public static int pingTimeoutMS = 5000; //ping timeout in ms

    /**
     * Check whether device specified by devToPing is pingable within timeout specified by PING_TIMEOUT_MS constant.
     * @param devToPing IP / hostname of device which should be pinged
     * @return true if device is pinging, else false
     */
    public boolean isDevPingable(String devToPing){
        Log.d(LOG_TAG, "Pinging device: " + devToPing);
        try {
            InetAddress inAddr = InetAddress.getByName(devToPing);
            if(inAddr.isReachable(pingTimeoutMS)){
                Log.d(LOG_TAG, "Device: " + devToPing + " is reachable");
                return true;
            }else{
                Log.d(LOG_TAG, "Device: " + devToPing + " is NOT reachable");
                return false;
            }
        } catch (UnknownHostException e) {
            Log.d(LOG_TAG, "Encountered UnknownHostException error while pinging device: " + devToPing);
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            Log.d(LOG_TAG, "Encountered IOException error while pinging device: " + devToPing);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Just getter for variable which specifies ping timeout.
     * @return ping timeout
     */
    public int getPingTimeoutMS(){
        return pingTimeoutMS;
    }

    /**
     * Checks whether specified IP is in valid format or not.
     * @param IP IP which should be checked
     * @return true if given IP is valid, else false
     */
    public boolean isIPValid(String IP){
        boolean isValid = Patterns.IP_ADDRESS.matcher(IP).matches();
        if(isValid){
            Log.d(LOG_TAG, "IP: " + IP + " is valid");
        }else{
            Log.d(LOG_TAG, "IP: " + IP + " is NOT valid");
        }
        return isValid;
    }

    /**
     * Retrieves broadcast IP of current network to which is device connected.
     * @return broadcast IP of given network
     */
    public InetAddress retrBroadcastIP(){
        try {
            Enumeration<NetworkInterface> netwIntfs = NetworkInterface.getNetworkInterfaces();
            while(netwIntfs.hasMoreElements()){ //go through network interfaces
                NetworkInterface netwIntf = netwIntfs.nextElement();

                if(netwIntf.isLoopback()){ //skip localhost
                    continue;
                }

                for(InterfaceAddress intfAddr : netwIntf.getInterfaceAddresses()){
                    InetAddress broadcastAddr = intfAddr.getBroadcast();
                    if(broadcastAddr == null){
                        continue;
                    }
                    Log.d(LOG_TAG, "Broadcast IP detected as: " + broadcastAddr);
                    return broadcastAddr;
                }
            }
        } catch (SocketException e) {
            Log.d(LOG_TAG, "Encountered SocketException error while retrieving broadcast IP");
            e.printStackTrace();
        }
        return null;
    }
}
