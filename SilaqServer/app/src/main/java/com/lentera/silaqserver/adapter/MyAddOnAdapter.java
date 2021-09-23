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
import com.lentera.silaqserver.model.AddonModel;
import com.lentera.silaqserver.model.SelectAddonModel;
import com.lentera.silaqserver.model.SelectSizeModel;
import com.lentera.silaqserver.model.SizeModel;
import com.lentera.silaqserver.model.UpdateAddOnModel;
import com.lentera.silaqserver.model.UpdateSizeModel;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyAddOnAdapter extends RecyclerView.Adapter<MyAddOnAdapter.MyViewHolder> {

    Context context;
    List<AddonModel> addonModels;
    UpdateAddOnModel updateAddOnModel;
    int editPos;

    public MyAddOnAdapter(Context context, List<AddonModel> addonModels) {
        this.context = context;
        this.addonModels = addonModels;
        editPos = -1;
        updateAddOnModel= new UpdateAddOnModel();
    }

    @NonNull
    @Override
    public MyAddOnAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyAddOnAdapter.MyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_size_addon_display,parent, false));
    }

//    @Override
//    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
//
//    }

    @Override
    public void onBindViewHolder(@NonNull MyAddOnAdapter.MyViewHolder holder, int position) {
        holder.txt_name.setText(addonModels.get(position).getName());
        holder.txt_price.setText(String.valueOf(addonModels.get(position).getPrice()));

        //Event
        holder.img_delete.setOnClickListener(v -> {
            addonModels.remove(position);
            notifyItemRemoved(position);
            updateAddOnModel.setAddonModel(addonModels);
            EventBus.getDefault().postSticky(updateAddOnModel);
        });

        holder.setListener((view, pos) -> {
            editPos = position;
            EventBus.getDefault().postSticky(new SelectAddonModel(addonModels.get(pos)));
        });

    }

    @Override
    public int getItemCount() {
        return addonModels.size();
    }

    public void addNewSize(AddonModel addonModel) {
        addonModels.add(addonModel);
        notifyItemInserted(addonModels.size()-1);
        updateAddOnModel.setAddonModel(addonModels);
        EventBus.getDefault().postSticky(updateAddOnModel);
    }

    public void editSize(AddonModel addonModel) {
        if (editPos!= -1){
            addonModels.set(editPos,addonModel);
            notifyItemChanged(editPos);
            //send update
            updateAddOnModel.setAddonModel(addonModels);
            EventBus.getDefault().postSticky(updateAddOnModel);
        }

    }

    public class MyViewHolder extends  RecyclerView.ViewHolder{

        @BindView(R.id.txt_name)
        TextView txt_name;
        @BindView(R.id.txt_price)
        TextView txt_price;
        @BindView(R.id.img_delete)
        ImageView img_delete;

        Unbinder unbinder;
        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public  MyViewHolder(@NonNull View itemView){
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v -> {
                listener.onItemClickListener(v, getAdapterPosition());

            });
        }
    }
}

