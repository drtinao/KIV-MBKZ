package fav.drtinao.magicwol;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Contains various methods which are used for working with Magic Packets. These are special packets which are used for achieving WOL (Wake-on-LAN) function on LAN.
 */
public class MagicPacketLogic {
    private final String LOG_TAG = "magicwol_debug";
    private final int MAC_BYTE_SIZE = 6; //size of MAC address in bytes (XX:XX:XX:XX:XX:XX / XX-XX-XX-XX-XX-XX)

    /**
     * Method sends Magic Packet to specific device in network.
     * @param devIdent broadcast IP to which Magic Packet should be sent to
     * @param devMac MAC of device to which Magic Packet should be sent to
     */
    public void sendMagicPacket(InetAddress devIdent, String devMac){
        Log.d(LOG_TAG, "Sending Magic Packet to broadcast IP: " + devIdent.getHostAddress() + ", MAC: " + devMac);
        byte[] devMacByte = convMacToBytes(devMac); //convert MAC to bytes arr

        byte[] bytesToSend = new byte[6 + 16 * devMacByte.length];
        for(int i = 0; i < 6; i++){ //first 6 bytes are 255
            bytesToSend[i] = (byte) 0xff;
        }

        for(int i = 6; i < bytesToSend.length; i += devMacByte.length){ //fill remaining bytes with device MAC, 16x repeated
            System.arraycopy(devMacByte, 0, bytesToSend, i, devMacByte.length);
        }

        try {
            InetAddress inAddr = InetAddress.getByName(devIdent.getHostAddress());
            DatagramPacket dgPacket = new DatagramPacket(bytesToSend, bytesToSend.length, inAddr, 9);
            DatagramSocket dgSock = new DatagramSocket();
            dgSock.send(dgPacket);
            dgSock.close();
        } catch (UnknownHostException e) {
            Log.d(LOG_TAG, "Encountered UnknownHostException error while sending Magic Packet to broadcast IP: " + devIdent.getHostAddress() + ", MAC: " + devMac);
            e.printStackTrace();
        } catch (SocketException e) {
            Log.d(LOG_TAG, "Encountered SocketException error while sending Magic Packet to broadcast IP: " + devIdent.getHostAddress() + ", MAC: " + devMac);
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(LOG_TAG, "Encountered IOException error while sending Magic Packet to broadcast IP: " + devIdent.getHostAddress() + ", MAC: " + devMac);
            e.printStackTrace();
        }
    }

    /**
     * Converts MAC address of device (in form of String) to form of byte array which can be used for WOL purposes later on.
     * @param devMac MAC of device which should be converted to byte array
     * @return byte array which represents MAC address
     */
    private byte[] convMacToBytes(String devMac){
        byte[] macBytes = new byte[MAC_BYTE_SIZE]; //MAC in form of bytes array
        String[] splittedMac = devMac.split("[-:]"); //split MAC in form of String to individual bytes

        Log.d(LOG_TAG, "Size is: " + splittedMac.length);
        for(int i = 0; i < splittedMac.length; i++){
            macBytes[i] = Integer.decode("0x" + splittedMac[i]).byteValue();
        }

        return macBytes;
    }

    /**
     * Checks whether given hardware address of device is valid or not. Valid format of MAC is: XX:XX:XX:XX:XX:XX and XX-XX-XX-XX-XX-XX.
     * @param devMac MAC of device
     * @return true if MAC is valid, else false
     */
    public boolean isMacValid(String devMac){
        String[] splittedMac = devMac.split("[-:]"); //split MAC by - and : to individual parts
        if(splittedMac.length != 6){ //MAC should be splitted into 6 parts
            Log.d(LOG_TAG, "MAC: " + devMac + " is NOT valid");
            return false;
        }else{
            for(int i = 0; i < splittedMac.length; i++){ //go through indiv. parts of MAC - every part should be XX
                if(splittedMac[i].length() != 2){
                    Log.d(LOG_TAG, "MAC: " + devMac + " is NOT valid");
                    return false;
                }
            }
        }

        Log.d(LOG_TAG, "MAC: " + devMac + " is valid");
        return true;
    }
}
