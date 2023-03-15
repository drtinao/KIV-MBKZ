package fav.drtinao.magicwol.restapi.registration;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface RegistrationAPIInterface {
    @POST("/registration/postAddDeviceReq")
    Call<ResponseBody> postAddDeviceReq(@Field("dev_name") String devName, @Field("dev_mac") String devMAC, @Field("dev_seg") String devSeg);

    @POST("/registration/postAddDeviceAdmin")
    Call<ResponseBody> postAddDeviceAdmin(@Field("dev_name") String devName, @Field("dev_mac") String devMAC, @Field("dev_seg") String devSeg);

    @GET("/registration/getDevRegState")
    Call<ResponseBody> getDevRegState(@Field("dev_mac") String devMAC);
}
