package com.lentera.silaqserver.ui.most_popular;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.app.AlertDialog;
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
import com.lentera.silaqserver.adapter.MyMostPopularAdapter;
import com.lentera.silaqserver.common.Common;
import com.lentera.silaqserver.common.MySwiperHelper;
import com.lentera.silaqserver.model.BestDealModel;
import com.lentera.silaqserver.model.MostPopularModel;
import com.lentera.silaqserver.ui.best_deals.BestDealViewModel;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class MostPopularFragment extends Fragment {

    private MostPopularViewModel mViewModel;
    private static final int PICK_IMAGE_REQUEST = 1234;

    Unbinder unbinder;
    @BindView(R.id.recycler_most_popular)
    RecyclerView recycler_most_popular;
    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyMostPopularAdapter adapter;

    List<MostPopularModel> mostPopularModels;
    ImageView img_popular;
    private Uri imageUri= null;

    FirebaseStorage storage;
    StorageReference storageReference;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel =
                new ViewModelProvider(this).get(MostPopularViewModel.class);
        View root = inflater.inflate(R.layout.most_popular_fragment, container, false);

        unbinder = ButterKnife.bind(this,root);
        initView();
        mViewModel.getMessageError().observe(getViewLifecycleOwner(), s -> {
            Toast.makeText(getContext(),""+s,Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        mViewModel.getMostPopularListMutable().observe(getViewLifecycleOwner(),list   ->{
            dialog.dismiss();
            mostPopularModels =list;
            adapter  = new MyMostPopularAdapter(getContext(),mostPopularModels);
            recycler_most_popular.setAdapter(adapter);
            recycler_most_popular.setLayoutAnimation(layoutAnimationController);
        });
        return root;
    }

    private void initView() {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        recycler_most_popular.setLayoutManager(layoutManager);
        recycler_most_popular.addItemDecoration(new DividerItemDecoration(getContext(),layoutManager.getOrientation()));

        MySwiperHelper mySwiperHelper =  new MySwiperHelper(getContext(),recycler_most_popular,200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(),"Hapus",30,0, Color.parseColor("#333639"),
                        pod -> {
                            Common.mostPopularSelected = mostPopularModels.get(pod);
                            showDeleteDialog();

                        }));

                buf.add(new MyButton(getContext(),"Update",30,0, Color.parseColor("#560027"),
                        pod -> {
                            Common.mostPopularSelected = mostPopularModels.get(pod);
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
            deleteMostPopular();
        });

        androidx.appcompat.app.AlertDialog dialog= builder.create();
        dialog.show();
    }

    private void deleteMostPopular() {
        FirebaseDatabase.getInstance()
                .getReference(Common.MOST_POPULAR)
                .child(Common.mostPopularSelected.getKey())
                .removeValue()
                .addOnFailureListener(e -> { Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(task -> {
            mViewModel.loadMostPopular();
            EventBus.getDefault().postSticky(new ToastEvent( Common.ACTION.DELETE, true));
        });
    }

    private void showUpdateDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Update");
        builder.setMessage("Masukkan informasi");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_category,null);
        EditText edt_category_name = (EditText)itemView.findViewById(R.id.edt_category_name);
        img_popular =  (ImageView)itemView.findViewById(R.id.img_category);

        //set data
        edt_category_name.setText(new StringBuilder("").append(Common.mostPopularSelected.getName()));
        Glide.with(getContext()).load(Common.mostPopularSelected.getImage()).into(img_popular);

        //set event
        img_popular.setOnClickListener(v -> {
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
                        updateMostPopular(updateData);
                    });
                }).addOnProgressListener(taskSnapshot -> {
                    double progress =(100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    dialog.setMessage(new StringBuilder("Mengupload: ").append(progress).append("%"));
                });
            }else {
                updateMostPopular(updateData);
            }


        });
        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog= builder.create();
        dialog.show();
    }

    private void updateMostPopular(Map<String, Object> updateData) {
        FirebaseDatabase.getInstance()
                .getReference(Common.MOST_POPULAR)
                .child(Common.mostPopularSelected.getKey())
                .updateChildren(updateData)
                .addOnFailureListener(e -> { Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(task -> {
            mViewModel.loadMostPopular();
            EventBus.getDefault().postSticky(new ToastEvent( Common.ACTION.UPDATE, true));
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK){
            if(data != null && data.getData() !=null){
                imageUri = data.getData();
                img_popular.setImageURI(imageUri);
            }

        }
    }

}
