package com.lentera.silaqserver.ui.food_list;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lentera.silaqserver.EventBus.AddSizeEditEvent;
import com.lentera.silaqserver.EventBus.ChangeMenuCLick;
import com.lentera.silaqserver.EventBus.ToastEvent;
import com.lentera.silaqserver.R;
import com.lentera.silaqserver.SizeAddonEditActivity;
import com.lentera.silaqserver.adapter.MyFoodListAdapter;
import com.lentera.silaqserver.common.Common;
import com.lentera.silaqserver.common.MySwiperHelper;
import com.lentera.silaqserver.model.FoodModel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class FoodListFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST =1234;
    private  ImageView img_food;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private android.app.AlertDialog dialog;


    private FoodListViewModel foodListViewModel;
    private List<FoodModel> foodModelList;


    Unbinder unbinder;
    @BindView(R.id.recycler_food_list)
    RecyclerView recycler_food_list;

    LayoutAnimationController layoutAnimationController;
    MyFoodListAdapter adapter;
    private Uri imageUri= null;


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.food_list_search, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);


        SearchManager searchManager =  (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        //event
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startSearchFood(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        //hapus text
        ImageView closeButton = (ImageView)searchView.findViewById(R.id.search_close_btn);
        closeButton.setOnClickListener(v -> {
            EditText ed= (EditText)searchView.findViewById(R.id.search_src_text);
            //hapus text
            ed.setText("");
            //hapus query
            searchView.setQuery("", false);

            searchView.onActionViewCollapsed();

            menuItem.collapseActionView();

            foodListViewModel.getMutableLiveDataFoodList().setValue(Common.categorySelected.getFoods());


        });
    }

    private void startSearchFood(String query) {
        List<FoodModel> resultFood = new ArrayList<>();
        for(int i=0;i<Common.categorySelected.getFoods().size();i++) {
            FoodModel foodModel = Common.categorySelected.getFoods().get(i);
            if (foodModel.getName().toLowerCase().contains(query.toLowerCase()))
            {
                resultFood.add(foodModel);
            }
        }
            foodListViewModel.getMutableLiveDataFoodList().setValue(resultFood);

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        foodListViewModel =
                ViewModelProviders.of(this).get(FoodListViewModel.class);
        View root = inflater.inflate(R.layout.fragment_foodlist, container, false);

        unbinder = ButterKnife.bind(this,root);
        initViews();

        foodListViewModel.getMutableLiveDataFoodList().observe(this, foodModels -> {
            if(foodModels != null){
                foodModelList = foodModels;
                adapter = new MyFoodListAdapter(getContext(),foodModelList);
                recycler_food_list.setAdapter(adapter);
                recycler_food_list.setLayoutAnimation(layoutAnimationController);
            }
        });
        return root;
    }

    private void initViews() {

        setHasOptionsMenu(true);

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        ((AppCompatActivity)getActivity())
                .getSupportActionBar()
                .setTitle("Daftar menu "+ Common.categorySelected.getName());
        recycler_food_list.setHasFixedSize(true);
        recycler_food_list.setLayoutManager(new LinearLayoutManager(getContext()));
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);

        //get size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;


        MySwiperHelper mySwiperHelper =  new MySwiperHelper(getContext(),recycler_food_list,width/6) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(),"Hapus",30,0, Color.parseColor("#560027"),
                        pod -> {
                            if(foodModelList != null)
                                Common.selectedFood =  foodModelList.get(pod);
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Hapus")
                                    .setMessage("Apakah anda ingin menghapus item ini")
                                    .setNegativeButton("Batal", ((dialog, which) -> dialog.dismiss()))
                                    .setPositiveButton("Hapus",((dialog, which) -> {
                                        FoodModel foodModel =adapter.getItemAtPosition(pod);
                                        if(foodModel.getPositionInList() == -1)
                                            Common.categorySelected.getFoods().remove(pod);
                                        else
                                            Common.categorySelected.getFoods().remove(foodModel.getPositionInList());
                                        updateFood(Common.categorySelected.getFoods(),Common.ACTION.DELETE);
                                    }));
                            AlertDialog deleteDialog = builder.create();
                            deleteDialog.show();
                        }));

                buf.add(new MyButton(getContext(),"Update",30,0, Color.parseColor("#9b0000"),
                        pod -> {
                            FoodModel foodModel = adapter.getItemAtPosition(pod);
                            if (foodModel.getPositionInList() == -1)
                                showUpdateDialog(pod, foodModel);
                            else
                            showUpdateDialog(foodModel.getPositionInList(), foodModel);
                        }));

                buf.add(new MyButton(getContext(),"Size",30,0, Color.parseColor("#12005e"),
                        pod -> {
                            FoodModel foodModel = adapter.getItemAtPosition(pod);
                            if(foodModel.getPositionInList() == -1)
                                Common.selectedFood = foodModelList.get(pod);
                            else
                                Common.selectedFood = foodModel;
                            startActivity(new Intent(getContext(), SizeAddonEditActivity.class));

                            //ubah posisi
                            if(foodModel.getPositionInList() ==-1)
                                EventBus.getDefault().postSticky(new AddSizeEditEvent(false,pod));
                            else
                                EventBus.getDefault().postSticky(new AddSizeEditEvent(false,foodModel.getPositionInList()));
                        }));
                buf.add(new MyButton(getContext(),"Tambahan",30,0, Color.parseColor("#336699"),
                        pod -> {
                            FoodModel foodModel = adapter.getItemAtPosition(pod);
                            if(foodModel.getPositionInList() == -1)
                                Common.selectedFood = foodModelList.get(pod);
                            else
                                Common.selectedFood = foodModel;
                            startActivity(new Intent(getContext(), SizeAddonEditActivity.class));
                            if(foodModel.getPositionInList() ==-1)
                                EventBus.getDefault().postSticky(new AddSizeEditEvent(true,pod));
                            else
                                EventBus.getDefault().postSticky(new AddSizeEditEvent(true,foodModel.getPositionInList()));


                        }));
            }
        };

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() ==R.id.action_create )
            showAddDialog();
        return super.onOptionsItemSelected(item);
    }
    private void showAddDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Tambahkan");
        builder.setMessage("Masukkan informasi");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_food, null);
        EditText edt_food_name= (EditText) itemView.findViewById(R.id.edt_food_name);
        EditText edt_food_price= (EditText) itemView.findViewById(R.id.edt_food_price);
        EditText edt_description= (EditText) itemView.findViewById(R.id.edt_food_description);
        img_food =(ImageView) itemView.findViewById(R.id.img_food_image);

        Random rand = new Random();

        //set data

        Glide.with(getContext()).load(R.drawable.ic_menu_gallery).into(img_food);

        //event
        img_food.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Pilih Gambar"),PICK_IMAGE_REQUEST);

        });

        builder.setNegativeButton("Batal",((dialog1, which) -> dialog1.dismiss()))
                .setPositiveButton("Tambah",((dialog1, which) -> {
                    FoodModel updateFood =new FoodModel();
                    updateFood.setName(edt_food_name.getText().toString());
                    updateFood.setDescription(edt_description.getText().toString());
                    updateFood.setPrice(TextUtils.isEmpty(edt_food_price.getText()) ? 0:
                            Long.parseLong(edt_food_price.getText().toString()));
                    updateFood.setId("menu"+rand.nextInt());

                    if(imageUri != null){
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
                                updateFood.setImage(uri.toString());
                                if (Common.categorySelected.getFoods()==null)
                                    Common.categorySelected.setFoods(new ArrayList<>());
                                Common.categorySelected.getFoods().add(updateFood);
                                updateFood(Common.categorySelected.getFoods(),Common.ACTION.CREATE);
                            });
                        }).addOnProgressListener(taskSnapshot -> {
                            double progress =(100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            dialog.setMessage(new StringBuilder("Mengupload: ").append(progress).append("%"));
                        });
                    }else{
                        if (Common.categorySelected.getFoods()==null)
                            Common.categorySelected.setFoods(new ArrayList<>());
                        Common.categorySelected.getFoods().add(updateFood);
                        updateFood(Common.categorySelected.getFoods(),Common.ACTION.CREATE);
                    }
                }));

        builder.setView(itemView);
        AlertDialog updateDialog = builder.create();
        updateDialog.show();
    }


    private void showUpdateDialog(int pod, FoodModel foodModel) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Update");
        builder.setMessage("Masukkan informasi");

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_food, null);
        EditText edt_food_name= (EditText) itemView.findViewById(R.id.edt_food_name);
        EditText edt_food_price= (EditText) itemView.findViewById(R.id.edt_food_price);
        EditText edt_description= (EditText) itemView.findViewById(R.id.edt_food_description);
        img_food =(ImageView) itemView.findViewById(R.id.img_food_image);

        //set data
        edt_food_name.setText(new StringBuilder("")
        .append(foodModel.getName()));

        edt_food_price.setText(new StringBuilder("")
                .append(foodModel.getPrice()));
        edt_description.setText(new StringBuilder("")
                .append(foodModel.getDescription()));

        Glide.with(getContext()).load(foodModel.getImage()).into(img_food);

        //event
        img_food.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Pilih Gambar"),PICK_IMAGE_REQUEST);

        });

        builder.setNegativeButton("Batal",((dialog1, which) -> dialog1.dismiss()))
                .setPositiveButton("Update",((dialog1, which) -> {
                    FoodModel updateFood =foodModel;
                    updateFood.setName(edt_food_name.getText().toString());
                    updateFood.setDescription(edt_description.getText().toString());
                    updateFood.setPrice(TextUtils.isEmpty(edt_food_price.getText()) ? 0:
                            Long.parseLong(edt_food_price.getText().toString()));
                    if(imageUri != null){
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
                                updateFood.setImage(uri.toString());
                                Common.categorySelected.getFoods().set(pod,updateFood);
                                updateFood(Common.categorySelected.getFoods(),Common.ACTION.UPDATE);
                            });
                        }).addOnProgressListener(taskSnapshot -> {
                            double progress =(100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            dialog.setMessage(new StringBuilder("Mengupload: ").append(progress).append("%"));
                        });
                    }else{
                        Common.categorySelected.getFoods().set(pod,updateFood);
                        updateFood(Common.categorySelected.getFoods(),Common.ACTION.UPDATE );
                    }
                }));

        builder.setView(itemView);
        AlertDialog updateDialog = builder.create();
        updateDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK){
            if(data != null && data.getData() !=null){
                imageUri = data.getData();
                img_food.setImageURI(imageUri);
            }
        }
    }

    private void updateFood(List<FoodModel> foods, Common.ACTION action) {
        Map<String,Object> updateData = new HashMap<>();
        updateData.put("foods",foods);

        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())
                .updateChildren(updateData)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        foodListViewModel.getMutableLiveDataFoodList();
                        EventBus.getDefault().postSticky(new ToastEvent(action, true));
                    }

        });
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new ChangeMenuCLick(true));
        super.onDestroy();
    }
}
