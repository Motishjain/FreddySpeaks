package com.admin.freddyspeaks;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import com.admin.adapter.UserPhoneNumberInputAdapter;
import com.admin.constants.AppConstants;
import com.admin.database.DBHelper;
import com.admin.database.User;
import com.admin.util.DateTimeUtility;
import com.admin.util.ImageUtility;
import com.admin.util.KeyboardUtil;
import com.admin.util.ValidationUtil;
import com.admin.view.CustomFontButton;
import com.admin.webservice.request_objects.FeedbackRequest;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HomePageActivity extends BaseActivity {

    ImageView backgroundRatingImage;
    CustomFontButton getStartedButton;
    TextInputLayout inputUserPhoneNumberLayout;
    AutoCompleteTextView autoCompleteInputUserPhoneNumberText;
    Dao<User, Integer> userDao;
    QueryBuilder<User, Integer> queryBuilder;
    List<User> userList = new ArrayList<>();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        executeDailyTasks();
        String activationStatus = sharedPreferences.getString("activationStatus", null);

        if(activationStatus!=null && activationStatus.equals(AppConstants.SUBSCRIPTION_EXPIRED)){
            Intent subscriptionInfo = new Intent(HomePageActivity.this, SubscriptionInfoActivity.class);
            startActivity(subscriptionInfo);
        }

        try {
            userDao = OpenHelperManager.getHelper(this, DBHelper.class).getCustomDao("User");
            queryBuilder = userDao.queryBuilder();
            userList = queryBuilder.query();
        } catch (Exception e) {
            e.printStackTrace();
        }

        backgroundRatingImage = (ImageView) findViewById(R.id.backgroundRatingImage);
        backgroundRatingImage.setImageBitmap(ImageUtility.getImageBitmap(this,R.drawable.shopping_bg));
        getStartedButton = (CustomFontButton) findViewById(R.id.getStartedButton);
        inputUserPhoneNumberLayout = (TextInputLayout) findViewById(R.id.inputUserPhoneNumberLayout);
        autoCompleteInputUserPhoneNumberText =(AutoCompleteTextView)findViewById(R.id.autoCompleteInputUserPhoneNumberText);

        autoCompleteInputUserPhoneNumberText.addTextChangedListener(userPhoneTextWatcher);

        final UserPhoneNumberInputAdapter adapter = new UserPhoneNumberInputAdapter(this,
                R.layout.userinfo_autosuggest, userList);

        autoCompleteInputUserPhoneNumberText.setAdapter(adapter);

        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAndNext();
            }
        });
    }

    void saveAndNext() {
        if(ValidationUtil.isValidCellNumber(autoCompleteInputUserPhoneNumberText,inputUserPhoneNumberLayout,"Please enter valid Mobile Number")) {
            try {
                KeyboardUtil.hideKeyboard(this);
                queryBuilder.reset();
                queryBuilder.where().eq("phoneNumber", autoCompleteInputUserPhoneNumberText.getText().toString().trim());
                userList = queryBuilder.query();
                if (userList == null || userList.size() == 0) {
                    User newUser = new User();
                    newUser.setPhoneNumber(autoCompleteInputUserPhoneNumberText.getText().toString());
                    userDao.create(newUser);
                }
                String outletCode = sharedPreferences.getString("outletCode", null);
                FeedbackRequest feedback = new FeedbackRequest();
                feedback.setOutletCode(outletCode);
                feedback.setUserPhoneNumber(autoCompleteInputUserPhoneNumberText.getText().toString());

                Intent getRating = new Intent(HomePageActivity.this, GetRatingActivity.class);
                getRating.putExtra("feedback", feedback);
                startActivity(getRating);
            } catch (SQLException e) {
                Log.e("HomePageActivity", "Failed to save user", e);
            }
        }
    }

    TextWatcher userPhoneTextWatcher =new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            checkPhoneNumberCompletion();
        }

        @Override
        public void afterTextChanged(Editable s) {
            checkPhoneNumberCompletion();
        }
    };

    private void checkPhoneNumberCompletion()
    {
        if(autoCompleteInputUserPhoneNumberText.getText().length()==10)
        {
            getStartedButton.setEnabled(true);
        }
        else
        {
            getStartedButton.setEnabled(false);
        }
    }

    private void executeDailyTasks() {
        String dailyTaskExecutedDate = sharedPreferences.getString("dailyTaskExecutedDate", null);
        String currentDate = simpleDateFormat.format(DateTimeUtility.getLocalDate());
        if (dailyTaskExecutedDate == null || !dailyTaskExecutedDate.equals(currentDate)) {
            Intent intent = new Intent("com.admin.freddyspeaks.executedailytasks");
            sendBroadcast(intent);
        }
    }
}