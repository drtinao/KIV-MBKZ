package fav.drtinao.magicwol.restapi.login;

import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Defines all available API calls which are related to user login.
 */
public interface LoginAPIInterface {
    @POST("/auth/login")
    Call<ResponseBody> postLogin(@Body JsonObject orionCreds);

    @GET("/action/getLogout")
    Call<ResponseBody> getLogout();
}
