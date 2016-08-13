package fr.sonline.enacmyedt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Melvin on 10/05/2016.
 */
public class CLSAdaptater extends ArrayAdapter<CLSStruct> {

    public CLSAdaptater(Context context, List<CLSStruct> classes) {
        super(context, 0, classes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_classes, parent, false);
        }
        ClassViewHolder viewh = (ClassViewHolder) convertView.getTag();
        if (viewh == null) {
            viewh = new ClassViewHolder();
            viewh.classe = (TextView) convertView.findViewById(R.id.classe);
            convertView.setTag(viewh);
        }

        CLSStruct classtr = getItem(position);

        viewh.classe.setText(classtr.name);


        return convertView;
    }

    private class ClassViewHolder {
        public TextView classe;
        public Button validation;
    }
}