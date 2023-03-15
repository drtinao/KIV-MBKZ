package fav.drtinao.magicwol.restapi.action;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Defines all available API calls which are related to actions which can be performed.
 */
public interface ActionAPIInterface {
    @POST("/action/postMagicPacket/{devID}")
    Call<ResponseBody> postMagicPacket(@Path(value = "devID", encoded = true) String devId);

    @GET("/action/getAvailability")
    Call<ResponseBody> getAvailability(@Field("dev_id") String devId, @Field("ping_timeout_ms") int pingTimeoutMS);

    @POST("/action/postRunScript")
    Call<ResponseBody> postRunScript(@Field("dev_id") String devId, @Field("script_id") String scriptId);
}
