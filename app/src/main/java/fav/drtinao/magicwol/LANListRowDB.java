package fav.drtinao.magicwol;

import androidx.annotation.NonNull;

/**
 * Represents list of LAN devices which can be added into SQLite database.
 */
public class LANListRowDB {
    private int id; //id of LAN list
    private String name; //name of LAN list
    private String description; //description of LAN list

    /**
     * Constructor takes values which are required for creation of entry containing details about device in DB.
     * @param id id of the LAN list
     * @param name name of the LAN list
     * @param description description of the LAN list
     */
    public LANListRowDB(int id, String name, String description){
        this.id = id;
        this.name = name;
        this.description = description;
    }

    /**
     * The constructor can be used to manually init required items via setters.
     */
    public LANListRowDB(){
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NonNull
    @Override
    public String toString() {
        return this.name;
    }
}
