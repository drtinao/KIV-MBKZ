package fav.drtinao.magicwol;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains methods which perform operations related to SQLite database.
 */
public class DatabaseLogic extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "magicwol";
    //const related to lan_device table - START
    private static final String TABLE_LAN_DEV = "lan_devices";
    private static final String COL_LAN_DEV_ID = "id";
    private static final String COL_LAN_DEV_NAME = "name";
    private static final String COL_LAN_DEV_MAC = "mac";
    private static final String COL_LAN_DEV_IP = "ip";
    private static final String COL_LAN_DEV_HOSTNAME = "hostname";
    private static final String COL_LAN_DEV_LAN_LISTS_ID = "lan_lists_id"; //key to lan_lists table
    //const related to lan_device table - END

    //const related to lan_lists table - START
    private static final String TABLE_LAN_LISTS = "lan_lists";
    private static final String COL_LAN_LISTS_ID = "id";
    private static final String COL_LAN_LISTS_NAME = "name";
    private static final String COL_LAN_LISTS_DESC = "desc";
    //const related to lan_lists table - END

    public DatabaseLogic(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Performs operations which lead to creation of all required tables.
     * @param sqLiteDatabase database object
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createLanDevTable = "CREATE TABLE " + TABLE_LAN_DEV + "(" + COL_LAN_DEV_ID + " INTEGER PRIMARY KEY,"
                + COL_LAN_DEV_NAME + " TEXT, " + COL_LAN_DEV_MAC + " TEXT," + COL_LAN_DEV_IP + " TEXT, " + COL_LAN_DEV_HOSTNAME + " TEXT, " + COL_LAN_DEV_LAN_LISTS_ID + " INTEGER)";
        sqLiteDatabase.execSQL(createLanDevTable); //create table TABLE_LAN_DEV which contains details of lan PCs

        String createLanListsTable = "CREATE TABLE " + TABLE_LAN_LISTS + "(" + COL_LAN_LISTS_ID + " INTEGER PRIMARY KEY,"
                + COL_LAN_LISTS_NAME + " TEXT, " + COL_LAN_LISTS_DESC + " TEXT)";
        sqLiteDatabase.execSQL(createLanListsTable); //create table TABLE_LAN_LISTS which contains individual list to which PCs could be gathered
    }

    /**
     * Describes what happens on DB upgrade. All tables should be dropped and then created again.
     * @param sqLiteDatabase database object
     * @param oldVer old version of database
     * @param newVer new version of database
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVer, int newVer) {
        String dropLanDevTable = "DROP TABLE IF EXISTS " + TABLE_LAN_DEV;
        sqLiteDatabase.execSQL(dropLanDevTable);

        String dropLanListsTable = "DROP TABLE IF EXISTS " + TABLE_LAN_LISTS;
        sqLiteDatabase.execSQL(dropLanListsTable);

        onCreate(sqLiteDatabase);
    }

    /**
     * Used for updating row which contains data regarding to one list of LAN devices. Update occurs on row which has same id as given LanListRowDB object.
     * @param lanList object which represents one list of LAN devices
     * @return result of update operation
     */
    public int updateLanList(LANListRowDB lanList){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contVals = new ContentValues();
        contVals.put(COL_LAN_LISTS_NAME, lanList.getName());
        contVals.put(COL_LAN_LISTS_DESC, lanList.getDescription());

        return db.update(TABLE_LAN_LISTS, contVals, COL_LAN_LISTS_ID + " =? ", new String[]{String.valueOf(lanList.getId())});
    }

    /**
     * Removes one list of LAN devices from database. Remove occurs on row which has the same id as id given as param. Also deletes all machines which belong to the list.
     * @param lanListId id of list which should be removed from database
     */
    public void removeLanList(int lanListId){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LAN_DEV, COL_LAN_DEV_LAN_LISTS_ID + " =? ", new String[]{String.valueOf(lanListId)}); //delete all machines which belong to the list first
        db.delete(TABLE_LAN_LISTS, COL_LAN_LISTS_ID + " =? ", new String[]{String.valueOf(lanListId)}); //remove list from DB
        db.close();
    }

    /**
     * Used for adding entry which represents list of LAN devices to database.
     * @param lanList LanListRowDB object which represents one list
     */
    long addLanList(LANListRowDB lanList){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contVals = new ContentValues();
        contVals.put(COL_LAN_LISTS_NAME, lanList.getName());
        contVals.put(COL_LAN_LISTS_DESC, lanList.getDescription());
        long result = db.insert(TABLE_LAN_LISTS, null, contVals);
        db.close();

        return result;
    }

    /**
     * Retrieves LAN list with given id from database and returns LanListRowDB object corresponding to given id.
     * @param id if of LAN list which should be retrieved from database
     * @return LanListRowDB object which represents one LAN list with given id
     */
    public LANListRowDB getLanList(int id){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor dbCursor = db.query(TABLE_LAN_LISTS, new String[]{COL_LAN_LISTS_ID, COL_LAN_LISTS_NAME, COL_LAN_LISTS_DESC}, COL_LAN_LISTS_ID + " =? ", new String[]{String.valueOf(id)}, null, null, null, null);
        if(dbCursor != null){
            dbCursor.moveToFirst();
        }

        LANListRowDB lanListRowDB = new LANListRowDB(Integer.parseInt(dbCursor.getString(0)), dbCursor.getString(1), dbCursor.getString(2));
        dbCursor.close();
        return lanListRowDB;
    }

    /**
     * Retrieves all LAN lists which are present in SQLite database and returns ArrayList with LanListRowDB objects. Each object represents one list with LAN devices.
     * @return ArrayList with LanListRowDB objects
     */
    public List<LANListRowDB> getAllLanLists(){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<LANListRowDB> allLanListsList = new ArrayList<>();

        String selAllQuery = "SELECT * FROM " + TABLE_LAN_LISTS;
        Cursor dbCursor = db.rawQuery(selAllQuery, null);

        if(dbCursor.moveToFirst()){
            do{
                LANListRowDB lanListRowDB = new LANListRowDB();
                lanListRowDB.setId(Integer.parseInt(dbCursor.getString(0)));
                lanListRowDB.setName(dbCursor.getString(1));
                lanListRowDB.setDescription(dbCursor.getString(2));

                allLanListsList.add(lanListRowDB);
            }while(dbCursor.moveToNext());
        }
        dbCursor.close();
        return allLanListsList;
    }

    /**
     * Checks whether list with given name already exists or not.
     * @param listName name of list which is about to be created by user
     * @return true if list with the name exists, else false
     */
    public boolean isLANListPresent(String listName){
        SQLiteDatabase db = this.getWritableDatabase();
        String selAllQuery = "SELECT * FROM " + TABLE_LAN_LISTS;
        Cursor dbCursor = db.rawQuery(selAllQuery, null);
        String curListName;

        if(dbCursor.moveToFirst()){
            do{
                curListName = dbCursor.getString(1);
                if(curListName.equals(listName)){ //list already exists
                    return true;
                }
            }while(dbCursor.moveToNext());
        }
        dbCursor.close();
        return false;
    }

    /**
     * Used for updating row which contains data regarding to one device in LAN. Update occurs on row which has same id as given LanDevRowDB object.
     * @param lanDevRowDB object which represents one device in LAN
     * @return result of update operation
     */
    public int updateLanDev(LANDevRowDB lanDevRowDB){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contVals = new ContentValues();
        contVals.put(COL_LAN_DEV_NAME, lanDevRowDB.getName());
        contVals.put(COL_LAN_DEV_MAC, lanDevRowDB.getMac());
        contVals.put(COL_LAN_DEV_IP, lanDevRowDB.getIp());
        contVals.put(COL_LAN_DEV_HOSTNAME, lanDevRowDB.getHostname());
        contVals.put(COL_LAN_DEV_LAN_LISTS_ID, lanDevRowDB.getLanListsId());

        return db.update(TABLE_LAN_DEV, contVals, COL_LAN_DEV_ID + " =? ", new String[]{String.valueOf(lanDevRowDB.getId())});
    }

    /**
     * Removes one LAN device with specific id from database. Remove occurs on row which has the same id as id given as param.
     * @param lanDevId id of LAN device which should be deleted from database
     */
    public void removeLanDev(int lanDevId){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LAN_DEV, COL_LAN_DEV_ID + " =? ", new String[]{String.valueOf(lanDevId)});
        db.close();
    }

    /**
     * Method adds entry which represents one device in LAN to database.
     * @param lanDevRowDB LanDevRowDB object which represents one device in LAN
     */
    void addLanDev(LANDevRowDB lanDevRowDB){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contVals = new ContentValues();
        contVals.put(COL_LAN_DEV_NAME, lanDevRowDB.getName());
        contVals.put(COL_LAN_DEV_MAC, lanDevRowDB.getMac());
        contVals.put(COL_LAN_DEV_IP, lanDevRowDB.getIp());
        contVals.put(COL_LAN_DEV_HOSTNAME, lanDevRowDB.getHostname());
        contVals.put(COL_LAN_DEV_LAN_LISTS_ID, lanDevRowDB.getLanListsId());

        db.insert(TABLE_LAN_DEV, null, contVals);
        db.close();
    }

    /**
     * Retrieves LAN device with given id from database and returns LanDevRowDB object corresponding to given id.
     * @param id id of LAN device which should be retrieved from database
     * @return LanDevRowDB which represents one LAN device in database
     */
    LANDevRowDB getLanDev(int id){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor dbCursor = db.query(TABLE_LAN_DEV, new String[]{COL_LAN_DEV_ID, COL_LAN_DEV_NAME, COL_LAN_DEV_MAC, COL_LAN_DEV_IP, COL_LAN_DEV_HOSTNAME, COL_LAN_DEV_LAN_LISTS_ID},
                COL_LAN_DEV_ID + " =? ", new String[]{String.valueOf(id)}, null, null, null, null);
        if(dbCursor != null){
            dbCursor.moveToFirst();
        }

        LANDevRowDB lanDevRowDB = new LANDevRowDB();
        lanDevRowDB.setId(Integer.parseInt(dbCursor.getString(0)));
        lanDevRowDB.setName(dbCursor.getString(1));
        lanDevRowDB.setMac(dbCursor.getString(2));
        lanDevRowDB.setIp(dbCursor.getString(3));
        lanDevRowDB.setHostname(dbCursor.getString(4));
        lanDevRowDB.setLanListsId(Integer.parseInt(dbCursor.getString(5)));
        dbCursor.close();
        return lanDevRowDB;
    }

    /**
     * Retrieves all LAN devices which are present in SQLite database and returns ArrayList with LanDevRowDB objects. Each object represents one device in LAN.
     * @return ArrayList with LanDevRowDB objects
     */
    public List<LANDevRowDB> getAllLanDevices(){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<LANDevRowDB> allLanListsList = new ArrayList<>();

        String selAllQuery = "SELECT * FROM " + TABLE_LAN_DEV;
        Cursor dbCursor = db.rawQuery(selAllQuery, null);

        if(dbCursor.moveToFirst()){
            do{
                LANDevRowDB lanDevRowDB = new LANDevRowDB();
                lanDevRowDB.setId(Integer.parseInt(dbCursor.getString(0)));
                lanDevRowDB.setName(dbCursor.getString(1));
                lanDevRowDB.setMac(dbCursor.getString(2));
                lanDevRowDB.setIp(dbCursor.getString(3));
                lanDevRowDB.setHostname(dbCursor.getString(4));
                lanDevRowDB.setLanListsId(Integer.parseInt(dbCursor.getString(5)));

                allLanListsList.add(lanDevRowDB);
            }while(dbCursor.moveToNext());
        }
        dbCursor.close();
        return allLanListsList;
    }

    /**
     * Gets all devices which belong to specific list specified by id given as param.
     * @param lanListId id of list which devices should be retrieved from database
     * @return List with LanDevRowDB objects, each represents one device in list
     */
    public List<LANDevRowDB> getLANDevicesInList(int lanListId){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<LANDevRowDB> LANDevsInList = new ArrayList<>();

        String selInListQuery = "SELECT * FROM " + TABLE_LAN_DEV + " WHERE " + COL_LAN_DEV_LAN_LISTS_ID + " = " + lanListId;
        Cursor dbCursor = db.rawQuery(selInListQuery, null);

        if(dbCursor.moveToFirst()){
            do{
                LANDevRowDB lanDevRowDB = new LANDevRowDB();
                lanDevRowDB.setId(Integer.parseInt(dbCursor.getString(0)));
                lanDevRowDB.setName(dbCursor.getString(1));
                lanDevRowDB.setMac(dbCursor.getString(2));
                lanDevRowDB.setIp(dbCursor.getString(3));
                lanDevRowDB.setHostname(dbCursor.getString(4));
                lanDevRowDB.setLanListsId(Integer.parseInt(dbCursor.getString(5)));

                LANDevsInList.add(lanDevRowDB);
            }while(dbCursor.moveToNext());
        }
        dbCursor.close();
        return LANDevsInList;
    }

    /**
     * Checks whether device with given name OR MAC already exists in database or not.
     * @param devName name of the new device which is about to be created
     * @param devMAC MAC of the new device which is about to be created
     * @param skipDevID ID of device to skip while checking (useful when device is being edited)
     * @return null if device not found, else name of list in which is device located
     */
    public String isLANDevPresent(String devName, String devMAC, int skipDevID){
        devMAC = devMAC.replace("-", ":");

        SQLiteDatabase db = this.getWritableDatabase();
        String selAllQuery = "SELECT * FROM " + TABLE_LAN_DEV;
        Cursor dbCursor = db.rawQuery(selAllQuery, null);
        String curDevName, curDevMAC;
        int curDevLANList;
        int curDevID;
        String foundList = null; //name of list in which is device located

        if(dbCursor.moveToFirst()){
            do{
                curDevID = Integer.parseInt(dbCursor.getString(0));
                curDevName = dbCursor.getString(1);
                curDevMAC = dbCursor.getString(2);
                curDevLANList = Integer.parseInt(dbCursor.getString(5));

                if(curDevName.equalsIgnoreCase(devName) || curDevMAC.equalsIgnoreCase(devMAC)){ //name or MAC match found
                    if(skipDevID != -1 && skipDevID == curDevID){
                        continue;
                    }else{
                        LANListRowDB foundListRowDB = getLanList(curDevLANList);
                        foundList = foundListRowDB.getName();
                        break;
                    }
                }
            }while(dbCursor.moveToNext());
        }
        dbCursor.close();
        return foundList; //device with same name / MAC not yet present
    }
}
