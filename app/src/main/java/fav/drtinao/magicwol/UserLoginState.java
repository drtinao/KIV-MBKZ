package fav.drtinao.magicwol;

/**
 * Enum defines available user login states in application.
 * NOT_LOGGED_IN - user is not logged in; can use only functions related to LAN
 * ORION_CLASSIC_USER_LOG - normal user which is successfully authenticated via Orion login; can manage LAN + his / her university equipment which is assigned to user by admin
 * ORION_ADMINISTRATOR_LOG - privileged user which is successfully authenticated via Orion login; can manage LAN + all computer equipment
 * ORION_ERR_NOT_FOUND - user not found in database of Orion logins
 * ORION_ERR_PASS - user entered wrong Orion password
 * (Role is assigned by server, which is resistant to Android-side application changes).
 */
public enum UserLoginState {
    NOT_LOGGED_IN,
    ORION_CLASSIC_USER_LOG,
    ORION_ADMINISTRATOR_LOG,
    ORION_ERR_NOT_FOUND,
    ORION_ERR_PASS
}
