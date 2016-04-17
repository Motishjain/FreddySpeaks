package com.admin.freddyspeaks;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.admin.adapter.SelectedRewardsBoxAdapter;
import com.admin.constants.AppConstants;
import com.admin.database.DBHelper;
import com.admin.database.Question;
import com.admin.database.SelectedReward;
import com.admin.tasks.SetRandomQuestionsTask;
import com.admin.util.DialogBuilderUtil;
import com.admin.webservice.RestEndpointInterface;
import com.admin.webservice.RetrofitSingleton;
import com.admin.webservice.response_objects.QuestionResponse;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RewardConfigurationActivity extends AppCompatActivity {

    RecyclerView bronzeRewardsRecyclerView, silverRewardsRecyclerView, goldRewardsRecyclerView;
    Dao<SelectedReward, Integer> selectedRewardDao;
    Dao<Question, Integer> questionDao;
    QueryBuilder<SelectedReward, Integer> selectedRewardQueryBuilder;
    SelectedRewardsBoxAdapter bronzeRewardsAdapter, silverRewardsAdapter, goldRewardsAdapter;
    String outletCode;
    boolean editMode;
    List<SelectedReward> bronzeSelectedRewardList,silverSelectedRewardList,goldSelectedRewardList;
    Button rewardsConfigureNextButton;
    ImageView activityBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_configuration);

        bronzeRewardsRecyclerView = (RecyclerView)findViewById(R.id.bronzeRewardsRecyclerView);
        silverRewardsRecyclerView = (RecyclerView) findViewById(R.id.silverRewardsRecyclerView);
        goldRewardsRecyclerView = (RecyclerView) findViewById(R.id.goldRewardsRecyclerView);
        rewardsConfigureNextButton = (Button) findViewById(R.id.rewardsConfigureNextButton);
        activityBackButton = (ImageView) findViewById(R.id.activityBackButton);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        outletCode = sharedPreferences.getString("outletCode", null) ;

        Bundle extras = getIntent().getExtras();
        if(extras!=null) {
            editMode = extras.getBoolean("editMode",false);
        }

        if(editMode) {
            rewardsConfigureNextButton.setVisibility(View.INVISIBLE);
            activityBackButton.setVisibility(View.VISIBLE);
        }
        else {
            rewardsConfigureNextButton.setVisibility(View.VISIBLE);
            activityBackButton.setVisibility(View.GONE);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        bronzeRewardsRecyclerView.setLayoutManager(layoutManager);

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        silverRewardsRecyclerView.setLayoutManager(layoutManager);

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        goldRewardsRecyclerView.setLayoutManager(layoutManager);

        try {
            questionDao = OpenHelperManager.getHelper(this, DBHelper.class).getCustomDao("Question");
            selectedRewardDao = OpenHelperManager.getHelper(this, DBHelper.class).getCustomDao("SelectedReward");
            selectedRewardQueryBuilder = selectedRewardDao.queryBuilder();
            updateScreen();

        } catch (Exception e) {
            Log.e("RewardConfiguration", "Unable to fetch selected rewards");
        }

    }

    public void rewardConfigNext(View v) {
        if(!editMode) {
            fetchQuestionsAndMove();
        }
        else {
            Intent homePage = new Intent(RewardConfigurationActivity.this, HomePageActivity.class);
            startActivity(homePage);
        }
    }

    public void addBronzeRewards(View v) {
        Intent selectRewardsPopup = new Intent(RewardConfigurationActivity.this, RewardSelectionActivity.class);
        String rewardCategory = AppConstants.BRONZE_CD;
        selectRewardsPopup.putExtra("rewardCategory", rewardCategory);
        startActivityForResult(selectRewardsPopup, 1);
    }

    public void addSilverRewards(View v) {
        Intent selectRewardsPopup = new Intent(RewardConfigurationActivity.this, RewardSelectionActivity.class);
        String rewardCategory = AppConstants.SILVER_CD;
        selectRewardsPopup.putExtra("rewardCategory", rewardCategory);
        startActivityForResult(selectRewardsPopup, 2);
    }

    public void addGoldRewards(View v) {
        Intent selectRewardsPopup = new Intent(RewardConfigurationActivity.this, RewardSelectionActivity.class);
        String rewardCategory = AppConstants.GOLD_CD;
        selectRewardsPopup.putExtra("rewardCategory", rewardCategory);
        startActivityForResult(selectRewardsPopup, 3);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(data!=null) {
            boolean rewardsSelected = data.getBooleanExtra("rewardsSelected", false);

            if (rewardsSelected) {
                if (requestCode == 1) {
                    updateBronzeRewardList();
                } else if (requestCode == 2) {
                    updateSilverRewardList();
                } else if (requestCode == 3) {
                    updateGoldRewardList();
                }
            }
        }
    }

    public void updateBronzeRewardList() {
        try {
            selectedRewardQueryBuilder.reset();
            selectedRewardQueryBuilder.where().eq("rewardCategory", AppConstants.BRONZE_CD);

            bronzeSelectedRewardList = selectedRewardQueryBuilder.query();

            if(bronzeRewardsRecyclerView.getAdapter()==null) {
                bronzeRewardsAdapter = new SelectedRewardsBoxAdapter(R.layout.selected_reward_item, bronzeSelectedRewardList, new SelectedRewardsBoxAdapter.OnAdapterInteractionListener() {
                    @Override
                    public void removeSelectedReward(final int position) {
                        DialogBuilderUtil.showPromptDialog(RewardConfigurationActivity.this, "Confirm", "Delete?", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    SelectedReward rewardToBeRemoved = bronzeSelectedRewardList.get(position);
                                    DeleteBuilder<SelectedReward, Integer> selectedRewardDeleteBuilder = selectedRewardDao.deleteBuilder();
                                    selectedRewardDeleteBuilder.where().eq("id",rewardToBeRemoved.getId());
                                    selectedRewardDeleteBuilder.delete();
                                    bronzeSelectedRewardList.remove(position);
                                    bronzeRewardsAdapter.notifyDataSetChanged();
                                }
                                catch (SQLException e) {
                                    Log.e("RewardConfiguration","Unable to delete bronze reward");
                                }
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                    }
                });
                bronzeRewardsRecyclerView.setAdapter(bronzeRewardsAdapter);
            }
            else {
                bronzeRewardsAdapter.setSelectedRewardList(bronzeSelectedRewardList);
                bronzeRewardsAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateSilverRewardList() {
        try {
            selectedRewardQueryBuilder.reset();
            selectedRewardQueryBuilder.where().eq("rewardCategory", AppConstants.SILVER_CD);

            silverSelectedRewardList  = selectedRewardQueryBuilder.query();
            if(silverRewardsRecyclerView.getAdapter()==null) {
                silverRewardsAdapter = new SelectedRewardsBoxAdapter(R.layout.selected_reward_item, silverSelectedRewardList, new SelectedRewardsBoxAdapter.OnAdapterInteractionListener() {
                    @Override
                    public void removeSelectedReward(final int position) {
                        DialogBuilderUtil.showPromptDialog(RewardConfigurationActivity.this, "Confirm", "Delete?", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    SelectedReward rewardToBeRemoved = silverSelectedRewardList.get(position);
                                    DeleteBuilder<SelectedReward, Integer> selectedRewardDeleteBuilder = selectedRewardDao.deleteBuilder();
                                    selectedRewardDeleteBuilder.where().eq("id",rewardToBeRemoved.getId());
                                    selectedRewardDeleteBuilder.delete();
                                    silverSelectedRewardList.remove(position);
                                    silverRewardsAdapter.notifyDataSetChanged();
                                }
                                catch (SQLException e) {
                                    Log.e("RewardConfiguration","Unable to delete bronze reward");
                                }
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                    }
                });
                silverRewardsRecyclerView.setAdapter(silverRewardsAdapter);
            }
            else {
                silverRewardsAdapter.setSelectedRewardList(silverSelectedRewardList);
                silverRewardsAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateGoldRewardList() {
        try {
            selectedRewardQueryBuilder.reset();
            selectedRewardQueryBuilder.where().eq("rewardCategory", AppConstants.GOLD_CD);

            goldSelectedRewardList = selectedRewardQueryBuilder.query();
            if(goldRewardsRecyclerView.getAdapter()==null) {
                goldRewardsAdapter = new SelectedRewardsBoxAdapter(R.layout.selected_reward_item, goldSelectedRewardList, new SelectedRewardsBoxAdapter.OnAdapterInteractionListener() {
                    @Override
                    public void removeSelectedReward(final int position) {
                        DialogBuilderUtil.showPromptDialog(RewardConfigurationActivity.this, "Confirm", "Delete?", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    SelectedReward rewardToBeRemoved = goldSelectedRewardList.get(position);
                                    DeleteBuilder<SelectedReward, Integer> selectedRewardDeleteBuilder = selectedRewardDao.deleteBuilder();
                                    selectedRewardDeleteBuilder.where().eq("id",rewardToBeRemoved.getId());
                                    selectedRewardDeleteBuilder.delete();
                                    goldSelectedRewardList.remove(position);
                                    goldRewardsAdapter.notifyDataSetChanged();
                                }
                                catch (SQLException e) {
                                    Log.e("RewardConfiguration","Unable to delete bronze reward");
                                }
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                    }
                });
                goldRewardsRecyclerView.setAdapter(goldRewardsAdapter);
            }
            else {
                goldRewardsAdapter.setSelectedRewardList(goldSelectedRewardList);
                goldRewardsAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateScreen() {
        updateBronzeRewardList();
        updateSilverRewardList();
        updateGoldRewardList();
    }

    void fetchQuestionsAndMove() {
        RestEndpointInterface restEndpointInterface = RetrofitSingleton.newInstance();
        Call<List<QuestionResponse>> fetchQuestionsCall = restEndpointInterface.fetchQuestions(AppConstants.OUTLET_TYPE);
        fetchQuestionsCall.enqueue(new Callback<List<QuestionResponse>>() {
            @Override
            public void onResponse(Call<List<QuestionResponse>> call, Response<List<QuestionResponse>> response) {
                List<QuestionResponse> questionResponseList = response.body();
                try {
                    for (QuestionResponse questionResponse : questionResponseList) {
                        Question dbQuestion = new Question();
                        dbQuestion.setQuestionId(questionResponse.get_id());
                        dbQuestion.setName(questionResponse.getQuestionName());
                        dbQuestion.setQuestionType(questionResponse.getQuestionType());
                        dbQuestion.setRatingValues(android.text.TextUtils.join(",", questionResponse.getRatingValues()));
                        dbQuestion.setEmoticonIds(android.text.TextUtils.join(",", questionResponse.getEmoticonIds()));
                        questionDao.create(dbQuestion);
                    }
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("areQuestionsFetched", true);
                    editor.commit();
                    SetRandomQuestionsTask setRandomQuestionsTask = new SetRandomQuestionsTask(RewardConfigurationActivity.this, new SetRandomQuestionsTask.CallBackListener() {
                        @Override
                        public void onTaskCompleted() {
                            Intent homePage = new Intent(RewardConfigurationActivity.this, HomePageActivity.class);
                            startActivity(homePage);
                        }
                    });
                    setRandomQuestionsTask.execute();
                } catch (SQLException e) {
                    Log.e("RewardConfiguration", "Unable to fetch questions");
                }
            }

            @Override
            public void onFailure(Call<List<QuestionResponse>> call, Throwable t) {
                Log.e("Reward Configuration","Unable to fetch questions");
            }
        });
    }

    public void closeActivity(View v) {
        this.finish();
    }
}