package fav.drtinao.magicwol;

/**
 * Represents device in LAN which belong to device list and can be added into SQLite database.
 */
public class LANDevRowDB {
    private int id; //id of LAN device
    private String name; //user given name of device
    private String mac; //MAC of device
    private String ip; //IP address of device (could be omitted by user)
    private String hostname; //hostname of device (could be omitted by user)
    private int lanListsId; //key to lan_lists table

    /**
     * Constructor takes values which are required for creation of entry containing details about device in DB.
     * @param id id of the LAN device
     * @param name user given name of the device
     * @param mac MAC of the device
     * @param ip IP address of the device (could be omitted by user)
     * @param hostname hostname of device (could be omitted by user)
     * @param lanListsId key to lan_lists table
     */
    public LANDevRowDB(int id, String name, String mac, String ip, String hostname, int lanListsId){
        this.id = id;
        this.name = name;
        this.mac = mac;
        this.ip = ip;
        this.hostname = hostname;
        this.lanListsId = lanListsId;
    }

    /**
     * The constructor can be used to manually init required items via setters.
     */
    public LANDevRowDB(){
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac.replace("-", ":");
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getLanListsId() {
        return lanListsId;
    }

    public void setLanListsId(int lanListsId) {
        this.lanListsId = lanListsId;
    }
}
