package com.example.admin.webservice.response_objects;

import com.j256.ormlite.field.DatabaseField;

/**
 * Created by Admin on 3/16/2016.
 */
public class QuestionResponse {
    private String questionId;
    private String questionName;
    private String[] optionValues;
    private String questionType;
    private String questionInputType;

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getQuestionName() {
        return questionName;
    }

    public void setQuestionName(String questionName) {
        this.questionName = questionName;
    }

    public String[] getOptionValues() {
        return optionValues;
    }

    public void setOptionValues(String[] optionValues) {
        this.optionValues = optionValues;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getQuestionInputType() {
        return questionInputType;
    }

    public void setQuestionInputType(String questionInputType) {
        this.questionInputType = questionInputType;
    }
}