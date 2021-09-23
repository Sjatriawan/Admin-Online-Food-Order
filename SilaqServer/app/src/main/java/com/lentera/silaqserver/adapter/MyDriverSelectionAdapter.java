package com.lentera.silaqserver.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lentera.silaqserver.R;
import com.lentera.silaqserver.callback.IRecyclerClickListener;
import com.lentera.silaqserver.model.DriverModel;
import com.lentera.silaqserver.ui.driver.DriverViewModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyDriverSelectionAdapter extends RecyclerView.Adapter<MyDriverSelectionAdapter.MyViewHolder>{

    private Context context;
    List<DriverModel> driverModelList;
    private ImageView lastCheckedImageView=null;
    private DriverModel selectedDriver;

    public MyDriverSelectionAdapter(Context context, List<DriverModel> driverModelList) {
        this.context = context;
        this.driverModelList = driverModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_driver_selected,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.txt_name.setText(new StringBuilder(driverModelList.get(position).getName()));
        holder.txt_phone.setText(new StringBuilder(driverModelList.get(position).getPhone()));
        holder.setRecyclerClickListener(new IRecyclerClickListener() {
            @Override
            public void onItemClickListener(View view, int pos) {
                if(lastCheckedImageView != null)
                    lastCheckedImageView.setImageResource(0);
                holder.img_checked.setImageResource(R.drawable.fui_ic_check_circle_black_128dp);
                lastCheckedImageView = holder.img_checked;
                selectedDriver = driverModelList.get(pos);
            }
        });
    }

    public DriverModel getSelectedDriver() {
        return selectedDriver;
    }

    @Override
    public int getItemCount() {
        return driverModelList.size();}

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Unbinder unbinder;

        @BindView(R.id.txt_name)
        TextView txt_name;
        @BindView(R.id.txt_phone)
        TextView txt_phone;
        @BindView(R.id.img_checked)
        ImageView img_checked;

        IRecyclerClickListener iRecyclerClickListener;

        public void setRecyclerClickListener(IRecyclerClickListener iRecyclerClickListener) {
            this.iRecyclerClickListener = iRecyclerClickListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder= ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            iRecyclerClickListener.onItemClickListener(v,getAdapterPosition());
        }
    }
}
