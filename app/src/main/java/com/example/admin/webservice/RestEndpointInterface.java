package com.example.admin.webservice;

import com.example.admin.webservice.request_objects.OutletRequest;
import com.example.admin.webservice.response_objects.PostServiceResponse;
import com.example.admin.webservice.response_objects.QuestionResponse;
import com.example.admin.webservice.response_objects.RewardResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

/**
 * Created by Admin on 3/16/2016.
 */
public interface RestEndpointInterface {
    @GET("fetch_rewards.php")
    Call<List<RewardResponse>> fetchRewards(@Query("outletType") String outletType);

    @GET("fetch_questions.php")
    Call<List<QuestionResponse>> fetchQuestions(@Query("outletType") String outletType);

    @POST("register_outlet.php")
    Call<PostServiceResponse> registerOutlet(OutletRequest outletRequest);


}
