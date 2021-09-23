package com.lentera.silaqserver.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.lentera.silaqserver.EventBus.UpdateDriverEvent;
import com.lentera.silaqserver.R;
import com.lentera.silaqserver.model.DriverModel;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyDriverAdapter extends RecyclerView.Adapter<MyDriverAdapter.MyViewHolder> {

    Context context;
    List<DriverModel> driverModels;

    public MyDriverAdapter(Context context, List<DriverModel> driverModels) {
        this.context = context;
        this.driverModels = driverModels;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView= LayoutInflater.from(context).inflate(R.layout.layout_driver,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txt_name.setText(new StringBuilder(driverModels.get(position).getName()));
        holder.txt_phone.setText(new StringBuilder(driverModels.get(position).getPhone()));
        holder.btn_enable.setChecked(driverModels.get(position).isActive());

        holder.btn_enable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            EventBus.getDefault().postSticky(new UpdateDriverEvent(driverModels.get(position),isChecked));
        });
    }

    @Override
    public int getItemCount() {
        return driverModels.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private Unbinder unbinder;

        @BindView(R.id.txt_name)
        TextView txt_name;
        @BindView(R.id.txt_phone)
        TextView txt_phone;
        @BindView(R.id.btn_enable)
        SwitchCompat btn_enable;



        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
        }
    }
}
