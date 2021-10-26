package com.example.httpurlconnection_get_test.ViewControler.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.httpurlconnection_get_test.Model.StreetLight;
import com.example.httpurlconnection_get_test.R;

import java.util.ArrayList;

// Todo:- Adapter는 View와Control에 관한 것을 둘 다 가지고 있다.
public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    ArrayList<StreetLight> items = new ArrayList<StreetLight>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.data_item, viewGroup, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        StreetLight item = items.get(position);
        viewHolder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(StreetLight item) {
        items.add(item);
    }

    public void setItems(ArrayList<StreetLight> items) {
        this.items = items;
    }

    public StreetLight getItem(int position) {
        return items.get(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.textView);
        }

        public void setItem(StreetLight item) {
            textView.setText(item.street_light_id);
        }

    }

}
