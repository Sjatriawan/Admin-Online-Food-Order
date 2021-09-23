package com.lentera.silaqserver.ui.best_deals;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lentera.silaqserver.EventBus.ToastEvent;
import com.lentera.silaqserver.R;
import com.lentera.silaqserver.adapter.MyBestDealAdapter;
import com.lentera.silaqserver.adapter.MyCategoriesAdapter;
import com.lentera.silaqserver.common.Common;
import com.lentera.silaqserver.common.MySwiperHelper;
import com.lentera.silaqserver.model.BestDealModel;
import com.lentera.silaqserver.model.CategoryModel;
import com.lentera.silaqserver.ui.category.CategoryViewModel;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class BestDealFragment extends Fragment {

    private BestDealViewModel mViewModel;
    private static final int PICK_IMAGE_REQUEST = 1234;

    Unbinder unbinder;
    @BindView(R.id.recycler_best_deal)
    RecyclerView recycler_best_deal;
    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyBestDealAdapter adapter;

    List<BestDealModel> bestDealsModels;
    ImageView img_best_deal;
    private Uri imageUri= null;

    FirebaseStorage storage;
    StorageReference storageReference;

    public static BestDealFragment newInstance() {
        return new BestDealFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel =
                new ViewModelProvider(this).get(BestDealViewModel.class);
        View root = inflater.inflate(R.layout.best_deal_fragment, container, false);

        unbinder = ButterKnife.bind(this,root);
        initView();
        mViewModel.getMessageError().observe(getViewLifecycleOwner(), s -> {
            Toast.makeText(getContext(),""+s,Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        mViewModel.getBestDealListMutable().observe(getViewLifecycleOwner(),list   ->{
            dialog.dismiss();
            bestDealsModels =list;
            adapter  = new  MyBestDealAdapter(getContext(),bestDealsModels);
            recycler_best_deal.setAdapter(adapter);
            recycler_best_deal.setLayoutAnimation(layoutAnimationController);
        });
        return root;
    }

    private void initView() {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
//        dialog.show();
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        recycler_best_deal.setLayoutManager(layoutManager);
        recycler_best_deal.addItemDecoration(new DividerItemDecoration(getContext(),layoutManager.getOrientation()));

        MySwiperHelper mySwiperHelper =  new MySwiperHelper(getContext(),recycler_best_deal,200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(),"Hapus",30,0, Color.parseColor("#333639"),
                        pod -> {
                            Common.bestDealSelected = bestDealsModels.get(pod);
                            showDeleteDialog();

                        }));

                buf.add(new MyButton(getContext(),"Update",30,0, Color.parseColor("#560027"),
                        pod -> {
                            Common.bestDealSelected = bestDealsModels.get(pod);
                            showUpdateDialog();

                        }));
            }
        };
    }

    private void showDeleteDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Hapus");
        builder.setMessage("Apakah anda ingin menghapus item ini");
        builder.setNegativeButton("Batal", (dialog, which) -> {

        }).setPositiveButton("Ya", (dialog, which) -> {
            deleteBestDeal();
        });

        androidx.appcompat.app.AlertDialog dialog= builder.create();
        dialog.show();
    }

    private void deleteBestDeal() {
        FirebaseDatabase.getInstance()
                .getReference(Common.BEST_DEALS)
                .child(Common.bestDealSelected.getKey())
                .removeValue()
                .addOnFailureListener(e -> { Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(task -> {
            mViewModel.loadBestDeals();
            EventBus.getDefault().postSticky(new ToastEvent( Common.ACTION.DELETE, true));
        });
    }

    private void showUpdateDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Update");
        builder.setMessage("Masukkan informasi");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_category,null);
        EditText edt_category_name = (EditText)itemView.findViewById(R.id.edt_category_name);
        img_best_deal =  (ImageView)itemView.findViewById(R.id.img_category);

        //set data
        edt_category_name.setText(new StringBuilder("").append(Common.bestDealSelected.getName()));
        Glide.with(getContext()).load(Common.bestDealSelected.getImage()).into(img_best_deal);

        //set event
        img_best_deal.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Pilih Gambar"),PICK_IMAGE_REQUEST);

        });

        builder.setNegativeButton("Batal",((dialog1, which) -> dialog1.dismiss()));
        builder.setPositiveButton("Update", (dialog1, which) -> {
            Map<String,Object> updateData = new HashMap<>();
            updateData.put("name",edt_category_name.getText().toString());

            if (imageUri != null){
                dialog.setMessage("Mengunggah....");
                dialog.show();

                String unique_name= UUID.randomUUID().toString();
                StorageReference imageFolder = storageReference.child("images/"+unique_name);

                imageFolder.putFile(imageUri)
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }).addOnCompleteListener(task -> {
                    dialog.dismiss();
                    imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                        updateData.put("image",uri.toString());
                        updateBestDeal(updateData);
                    });
                }).addOnProgressListener(taskSnapshot -> {
                    double progress =(100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    dialog.setMessage(new StringBuilder("Mengupload: ").append(progress).append("%"));
                });
            }else {
                updateBestDeal(updateData);
            }


        });
        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog= builder.create();
        dialog.show();
    }

    private void updateBestDeal(Map<String, Object> updateData) {
        FirebaseDatabase.getInstance()
                .getReference(Common.BEST_DEALS)
                .child(Common.bestDealSelected.getKey())
                .updateChildren(updateData)
                .addOnFailureListener(e -> { Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(task -> {
            mViewModel.loadBestDeals();
            EventBus.getDefault().postSticky(new ToastEvent( Common.ACTION.UPDATE, true));
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK){
            if(data != null && data.getData() !=null){
                imageUri = data.getData();
                img_best_deal.setImageURI(imageUri);
            }

        }
    }

}
