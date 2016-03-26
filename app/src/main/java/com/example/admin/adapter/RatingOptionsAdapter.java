package com.example.admin.adapter;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.admin.database.Question;
import com.example.admin.database.Reward;
import com.example.admin.freddyspeaks.R;

import java.util.List;

/**
 * Created by Admin on 3/26/2016.
 */
public class RatingOptionsAdapter extends RecyclerView.Adapter<RatingOptionsAdapter.RatingOptionHolder>{

    private int layoutResourceId;
    private Question question;
    String optionValues[];
    String emoticonIds[];
    boolean selected[];
    int selectedOption;
    int emoticonHeight, emoticonWidth;
    float optionTextSize;


    public RatingOptionsAdapter(int layoutResourceId, Question question) {
        this.layoutResourceId = layoutResourceId;
        this.question = question;
        optionValues = question.getRatingValues().split(",");
        emoticonIds = question.getEmoticonIds().split(",");
        selected = new boolean[question.getRatingValues().length()];
    }

    @Override
    public RatingOptionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutResourceId, null);
        RatingOptionHolder ratingOptionHolder = new RatingOptionHolder(view);
        emoticonWidth = ratingOptionHolder.ratingOptionEmoticon.getLayoutParams().width;
        emoticonHeight = ratingOptionHolder.ratingOptionEmoticon.getLayoutParams().height;
        optionTextSize = ratingOptionHolder.selectedOptionTextView.getTextSize();
        return ratingOptionHolder;
    }

    @Override
    public void onBindViewHolder(final RatingOptionHolder holder, final int position) {
        holder.ratingOptionEmoticon.setImageResource(R.drawable.form_button_shape);
        holder.selectedOptionTextView.setText(optionValues[position]);

        if(selected[position]) {
            holder.ratingOptionEmoticon.getLayoutParams().width = emoticonWidth + 2;
            holder.ratingOptionEmoticon.getLayoutParams().height = emoticonHeight + 2;
            holder.selectedOptionTextView.setTextSize(optionTextSize+2);
            holder.selectedOptionTextView.setTypeface(null, Typeface.BOLD);
        }
        else {
            holder.ratingOptionEmoticon.getLayoutParams().width = emoticonWidth;
            holder.ratingOptionEmoticon.getLayoutParams().height = emoticonHeight;
            holder.selectedOptionTextView.setTextSize(optionTextSize);
            holder.selectedOptionTextView.setTypeface(null, Typeface.NORMAL);
        }
        holder.ratingOptionRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected[selectedOption] = false;
                selectedOption = position;
                question.setSelectedOption((selectedOption+1)+"");
                selected[position] = true;
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return 0;
    }


    static class RatingOptionHolder extends RecyclerView.ViewHolder{

        RadioButton ratingOptionRadioButton;
        ImageView ratingOptionEmoticon;
        TextView selectedOptionTextView;

        public RatingOptionHolder(View view){
            super(view);
            ratingOptionRadioButton = (RadioButton) view.findViewById(R.id.ratingOptionRadioButton);
            ratingOptionEmoticon = (ImageView) view.findViewById(R.id.ratingOptionEmoticon);
            selectedOptionTextView = (TextView) view.findViewById(R.id.selectedOptionTextView);
        }
    }
}