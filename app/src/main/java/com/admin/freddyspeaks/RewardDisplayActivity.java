package com.admin.freddyspeaks;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.admin.constants.AppConstants;
import com.admin.database.DBHelper;
import com.admin.database.Reward;
import com.admin.database.SelectedReward;
import com.admin.database.User;
import com.admin.tasks.FetchRewardImageTask;
import com.admin.tasks.TextToSpeechConversionTask;
import com.admin.util.RewardAllocationUtility;
import com.admin.view.CustomFontTextView;
import com.admin.webservice.RestEndpointInterface;
import com.admin.webservice.RetrofitSingleton;
import com.admin.webservice.request_objects.FeedbackRequest;
import com.admin.webservice.response_objects.PostServiceResponse;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RewardDisplayActivity extends BaseActivity {

    FeedbackRequest feedback;
    Dao<SelectedReward, Integer> selectedRewardDao;
    SelectedReward allocatedReward;
    Dao<User, Integer> userDao;
    CustomFontTextView rewardDisplayExclaimer, rewardDisplayMessage, resultRewardName;
    CustomFontTextView rewardNotFoundExclaimer,rewardNotFoundMessage,thankYouMessage1,thankYouMessage2;
    ImageView resultRewardImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_display);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rewardDisplayExclaimer = (CustomFontTextView) findViewById(R.id.rewardDisplayExclaimer);
        rewardDisplayMessage = (CustomFontTextView) findViewById(R.id.rewardDisplayMessage);
        rewardNotFoundExclaimer = (CustomFontTextView) findViewById(R.id.rewardNotFoundExclaimer);
        rewardNotFoundMessage = (CustomFontTextView) findViewById(R.id.rewardNotFoundMessage);
        thankYouMessage1 = (CustomFontTextView) findViewById(R.id.thankYouMessage1);
        thankYouMessage2 = (CustomFontTextView) findViewById(R.id.thankYouMessage2);
        resultRewardName = (CustomFontTextView) findViewById(R.id.resultRewardName);
        resultRewardImage = (ImageView) findViewById(R.id.resultRewardImage);

        Bundle extras = getIntent().getExtras();
        feedback = (FeedbackRequest)extras.get("feedback");

        try {
            userDao = OpenHelperManager.getHelper(this, DBHelper.class).getCustomDao("User");
            selectedRewardDao = OpenHelperManager.getHelper(this, DBHelper.class).getCustomDao("SelectedReward");
        } catch (Exception e) {
            e.printStackTrace();
        }

        allocateReward();

        if(allocatedReward!=null) {
            Reward rewardResult = allocatedReward.getReward();
            FetchRewardImageTask fetchRewardImageTask = new FetchRewardImageTask(resultRewardImage);
            fetchRewardImageTask.execute(rewardResult.getImage());
            resultRewardName.setText(rewardResult.getName());
            rewardDisplayExclaimer.setText("Congratulations!!!");
            rewardDisplayMessage.setText("We have a reward for you!");
            rewardDisplayExclaimer.setVisibility(View.VISIBLE);
            rewardDisplayMessage.setVisibility(View.VISIBLE);
        }
        else {
            rewardNotFoundExclaimer.setText("Ahh! We couldn't find a reward for you.");
            rewardNotFoundMessage.setText("Duly noted, you will be taken better care of next time :)");
            rewardNotFoundExclaimer.setVisibility(View.VISIBLE);
            rewardNotFoundMessage.setVisibility(View.VISIBLE);
        }
    }

    void allocateReward() {
        allocatedReward = RewardAllocationUtility.allocateReward(feedback.getUserPhoneNumber(),
                Integer.parseInt(feedback.getBillAmount()), selectedRewardDao, userDao);
        if (allocatedReward != null) {
            feedback.setRewardCategory(allocatedReward.getRewardCategory());
            feedback.setRewardId(allocatedReward.getReward().getRewardId());
        }
        submitFeedback();
    }

    void submitFeedback() {
        RestEndpointInterface restEndpointInterface = RetrofitSingleton.newInstance();
        Call<PostServiceResponse> submitFeedbackCall = restEndpointInterface.submitFeedback(feedback);
        submitFeedbackCall.enqueue(new Callback<PostServiceResponse>() {
            @Override
            public void onResponse(Call<PostServiceResponse> call, Response<PostServiceResponse> response) {
                PostServiceResponse postServiceResponse = response.body();

                if (postServiceResponse.isSuccess()) {
                    Log.i("Reward display","Feedback sent successfully");
                }
            }

            @Override
            public void onFailure(Call<PostServiceResponse> call, Throwable t) {
                Log.e("Reward display","Failed to submit feedback");
            }
        });
    }

    public void exit(View v) {
        Intent homePage = new Intent(RewardDisplayActivity.this, HomePageActivity.class);
        startActivity(homePage);
    }
}