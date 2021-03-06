package com.admin.crystalrating;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.admin.adapter.SelectedRewardsBoxAdapter;
import com.admin.constants.AppConstants;
import com.admin.database.DBHelper;
import com.admin.database.Question;
import com.admin.database.SelectedReward;
import com.admin.dialogs.CustomDialogFragment;
import com.admin.tasks.SetRandomQuestionsTask;
import com.admin.view.CustomProgressDialog;
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

public class RewardConfigurationActivity extends AppCompatActivity
        implements CustomDialogFragment.CustomDialogListener {

    RecyclerView bronzeRewardsRecyclerView;
    Dao<SelectedReward, Integer> selectedRewardDao;
    Dao<Question, Integer> questionDao;
    QueryBuilder<SelectedReward, Integer> selectedRewardQueryBuilder;
    SelectedRewardsBoxAdapter bronzeRewardsAdapter;
    String outletCode;
    boolean editMode;
    List<SelectedReward> bronzeSelectedRewardList;
    Button rewardsConfigureNextButton;
    private DialogFragment dialogDelete;

    //Delete element:
    private SelectedRewardsBoxAdapter adapterFromDel;
    private List<SelectedReward> listFromDel;
    private int positionFromDel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_configuration);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){public void onClick(View v){closeActivity(v);}});

        bronzeRewardsRecyclerView = (RecyclerView) findViewById(R.id.bronzeRewardsRecyclerView);
        rewardsConfigureNextButton = (Button) findViewById(R.id.rewardsConfigureNextButton);


        dialogDelete = CustomDialogFragment.newInstance(R.layout.dialog_rewards_delete,this);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        outletCode = sharedPreferences.getString("outletCode", null);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            editMode = extras.getBoolean("editMode", false);
        }

        if (editMode) {
            rewardsConfigureNextButton.setVisibility(View.INVISIBLE);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            rewardsConfigureNextButton.setText("Configure Later");
            ViewGroup.LayoutParams params = rewardsConfigureNextButton.getLayoutParams();
            params.width = params.WRAP_CONTENT;
            rewardsConfigureNextButton.setLayoutParams(params);
            rewardsConfigureNextButton.setVisibility(View.VISIBLE);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        bronzeRewardsRecyclerView.setLayoutManager(layoutManager);

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        try {
            questionDao = OpenHelperManager.getHelper(this, DBHelper.class).getCustomDao("Question");
            selectedRewardDao = OpenHelperManager.getHelper(this, DBHelper.class).getCustomDao("SelectedReward");
            selectedRewardQueryBuilder = selectedRewardDao.queryBuilder();
            updateScreen();

        } catch (Exception e) {
            Log.e("RewardConfiguration", "Unable to fetch selected rewards");
        }

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        return super.onCreateDialog(id);
    }

    public void rewardConfigNext(View v) {
        if (!editMode) {
            fetchQuestionsAndMove();
        } else {
            this.finish();
        }
    }

    public void addBronzeRewards(View v) {
        Intent selectRewardsPopup = new Intent(this, RewardSelectionActivity.class);
        String rewardCategory = AppConstants.BRONZE_CD;
        selectRewardsPopup.putExtra("rewardCategory", rewardCategory);
        startActivityForResult(selectRewardsPopup, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if (data != null) {
            boolean rewardsSelected = data.getBooleanExtra("rewardsSelected", false);

            if (rewardsSelected) {
                if (requestCode == 1) {
                    updateBronzeRewardList();
                }
            }
        }
    }

    public void updateBronzeRewardList() {
        try {
            selectedRewardQueryBuilder.reset();
            selectedRewardQueryBuilder.where().eq("rewardCategory", AppConstants.BRONZE_CD);

            bronzeSelectedRewardList = selectedRewardQueryBuilder.query();

            if (bronzeRewardsRecyclerView.getAdapter() == null) {
                bronzeRewardsAdapter = new SelectedRewardsBoxAdapter(R.layout.selected_reward_item, bronzeSelectedRewardList, new SelectedRewardsBoxAdapter.OnAdapterInteractionListener() {
                    @Override
                    public void removeSelectedReward(int position) {
                        prepareforDeleting(bronzeRewardsAdapter, bronzeSelectedRewardList, position);
                    }
                });
                bronzeRewardsRecyclerView.setAdapter(bronzeRewardsAdapter);
            } else {
                bronzeRewardsAdapter.setSelectedRewardList(bronzeSelectedRewardList);
                bronzeRewardsAdapter.notifyDataSetChanged();
            }
            if (!editMode){
                updateNextButtonName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateScreen() {
        updateBronzeRewardList();
    }

    void fetchQuestionsAndMove() {
        final ProgressDialog progressDialog = CustomProgressDialog.createCustomProgressDialog(this);
        progressDialog.setMessage("Loading Rewards...");
        progressDialog.show();

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
                        dbQuestion.setDisplayRank(questionResponse.getDisplayRank());
                        dbQuestion.setShowNA(questionResponse.getShowNA());
                        dbQuestion.setRatingValues(android.text.TextUtils.join(";", questionResponse.getRatingValues()));
                        dbQuestion.setEmoticonIds(android.text.TextUtils.join(";", questionResponse.getEmoticonIds()));
                        dbQuestion.setQuestionInputType(questionResponse.getQuestionInputType());
                        questionDao.create(dbQuestion);
                    }
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("areQuestionsFetched", true);
                    editor.commit();
                    SetRandomQuestionsTask setRandomQuestionsTask = new SetRandomQuestionsTask(RewardConfigurationActivity.this,null);
                    setRandomQuestionsTask.execute();

                    progressDialog.dismiss();
                    Intent homePage = new Intent(RewardConfigurationActivity.this, HomePageActivity.class);
                    homePage.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(homePage);
                } catch (SQLException e) {
                    Log.e("RewardConfiguration", "Unable to fetch questions");
                }
            }

            @Override
            public void onFailure(Call<List<QuestionResponse>> call, Throwable t) {
                Log.e("Reward Configuration", "Unable to fetch questions");
                progressDialog.dismiss();
                Toast.makeText(RewardConfigurationActivity.this,"Not able to connect to server! Please try again later.",Toast.LENGTH_LONG).show();
            }
        });
    }

    public void closeActivity(View v) {
        if (editMode) {
            this.finish();
        }
    }

    private void prepareforDeleting(SelectedRewardsBoxAdapter adapter, List<SelectedReward> list, int position){
        dialogDelete.show(getSupportFragmentManager(), "");

        adapterFromDel = adapter;
        listFromDel = list;
        positionFromDel = position;
    }

    @Override
    public void onDialogPositiveClick() {
        if (listFromDel != null && adapterFromDel != null) {
            try {
                SelectedReward rewardToBeRemoved = listFromDel.get(positionFromDel);
                DeleteBuilder<SelectedReward, Integer> selectedRewardDeleteBuilder = selectedRewardDao.deleteBuilder();
                selectedRewardDeleteBuilder.where().eq("id", rewardToBeRemoved.getId());
                selectedRewardDeleteBuilder.delete();
                listFromDel.remove(positionFromDel);
                if (!editMode){
                    updateNextButtonName();}
                adapterFromDel.notifyDataSetChanged();

            } catch (SQLException e) {
                Log.e("RewardConfiguration", "Unable to delete bronze reward");
            }
        }
        dialogDelete.dismiss();
    }

    @Override
    public void onDialogNegativeClick() {
        adapterFromDel = null;
        listFromDel = null;
        dialogDelete.dismiss();
    }

    public void updateNextButtonName() {
        ViewGroup.LayoutParams params = rewardsConfigureNextButton.getLayoutParams();
        params.width = params.WRAP_CONTENT;
        rewardsConfigureNextButton.setLayoutParams(params);
        rewardsConfigureNextButton.setVisibility(View.VISIBLE);

        if((bronzeSelectedRewardList==null || bronzeSelectedRewardList.size()==0)) {
            rewardsConfigureNextButton.setText("Configure Later");

        }
        else{
            rewardsConfigureNextButton.setText("Get Started");
        }
    }
}
