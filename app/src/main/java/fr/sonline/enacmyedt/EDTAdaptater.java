package fr.sonline.enacmyedt;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Melvin on 30/04/2016.
 */



public class EDTAdaptater extends ArrayAdapter<EDTCourse> {

    public EDTAdaptater(Context context, List<EDTCourse> courses){
        super(context, 0, courses);
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_course, parent, false);
        }
        CoursViewHolder viewh = (CoursViewHolder) convertView.getTag();
        if(viewh == null){
            viewh = new CoursViewHolder();
            viewh.Classe = (TextView) convertView.findViewById(R.id.Classe);
            viewh.Cours = (TextView) convertView.findViewById(R.id.Cours);
            viewh.Heure = (TextView) convertView.findViewById(R.id.Heure);
            viewh.Date = (TextView) convertView.findViewById(R.id.Date);
            viewh.Dotted = (ImageView) convertView.findViewById(R.id.dotlin);
            viewh.Dotted2 = (ImageView) convertView.findViewById(R.id.dotlin2);
            convertView.setTag(viewh);
        }

        EDTCourse course = getItem(position);


        viewh.Classe.setText(course.getRoom());
        viewh.Cours.setText(course.getName());
        viewh.Heure.setText(course.getFormatTime());
        viewh.Date.setText(course.getDay());

        final CoursViewHolder vh = viewh;
                if (course.getDay() != null && course.getDay().trim().length()>0) {
                    viewh.Date.setVisibility(View.VISIBLE);
                    viewh.Dotted.setVisibility(View.VISIBLE);
                    viewh.Dotted2.setVisibility(View.VISIBLE);
                }
        else{
                    viewh.Date.setVisibility(View.GONE);
                    viewh.Dotted.setVisibility(View.GONE);
                    viewh.Dotted2.setVisibility(View.GONE);
                }


        return convertView;
    }

    private class CoursViewHolder{
        public TextView Heure;
        public TextView Cours;
        public TextView Classe;
        public TextView Date;
        public ImageView Dotted;
        public ImageView Dotted2;

    }
}
