package com.lentera.silaqserver.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.lentera.silaqserver.R;
import com.lentera.silaqserver.common.Common;
import com.lentera.silaqserver.model.AddonModel;
import com.lentera.silaqserver.model.CartItem;
import com.lentera.silaqserver.model.SizeModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MyOrderDetailAdapter extends RecyclerView.Adapter<MyOrderDetailAdapter.MyViewHolder> {

    Context context;
    List<CartItem> cartItemList;
    Gson gson;

    public MyOrderDetailAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
        gson = new Gson();}

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
        .inflate(R.layout.layout_order_detail_item,parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(cartItemList.get(position).getFoodImage())
                .into(holder.img_food_image);
        holder.txt_food_name.setText(new StringBuilder().append(cartItemList.get(position).getFoodName()));
        holder.txt_food_quantity.setText(new StringBuilder("Jumlah: ").append(cartItemList.get(position).getFoodQuantity()));

        try {
            SizeModel sizeModel = gson.fromJson(cartItemList.get(position).getFoodSize(), new TypeToken<SizeModel>() {
            }.getType());

            if (sizeModel != null)
                holder.txt_size.setText(new StringBuilder("Dimensione: ").append(sizeModel.getName()));
        }catch (IllegalStateException | JsonSyntaxException exception){}

        if (!cartItemList.get(position).getFoodAddon().equals("Default")){
            List<AddonModel> addonModels = gson.fromJson(cartItemList.get(position).getFoodAddon(), new TypeToken<List<AddonModel>>(){}.getType());
            StringBuilder addonString = new StringBuilder();
            if (addonModels != null){
                for (AddonModel addonModel: addonModels)
                    addonString.append(addonModel.getName()).append(",");
                addonString.delete(addonString.length()-1,addonString.length());
                holder.txt_food_add_on.setText(new StringBuilder("Tambahan: ").append(addonString));
            }
        }else
            holder.txt_food_add_on.setText(new StringBuilder("Default"));

    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txt_food_name)
        TextView txt_food_name;
        @BindView(R.id.txt_food_add_on)
        TextView txt_food_add_on;
        @BindView(R.id.txt_size)
        TextView txt_size;

        @BindView(R.id.txt_food_quantity)
        TextView txt_food_quantity;
        @BindView(R.id.img_food_image)
        ImageView img_food_image;


        private Unbinder unbinder;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
        }
    }
}
