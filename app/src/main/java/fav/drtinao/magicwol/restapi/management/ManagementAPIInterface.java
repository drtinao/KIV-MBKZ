package fav.drtinao.magicwol.restapi.management;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.PATCH;

public interface ManagementAPIInterface {
    @GET("/management/getScheduledTasks")
    Call<ResponseBody> getScheduledTasks(@Field("dev_id") String devId);

    @GET("/management/getUserDevices")
    Call<ResponseBody> getUserDevices();

    @PATCH("/management/postEditDev")
    Call<ResponseBody> patchEditDev();
}
