package com.lentera.silaqserver.ui.driver;

import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.app.Dialog;
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

import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.lentera.silaqserver.EventBus.ChangeMenuCLick;
import com.lentera.silaqserver.EventBus.UpdateDriverEvent;
import com.lentera.silaqserver.R;
import com.lentera.silaqserver.adapter.MyDriverAdapter;
import com.lentera.silaqserver.common.Common;
import com.lentera.silaqserver.model.DriverModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

public class DriverFragment extends Fragment {

    private DriverViewModel mViewModel;

    private Unbinder unbinder;

    @BindView(R.id.recycler_driver)
    RecyclerView recycler_driver;

    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyDriverAdapter adapter;
    List<DriverModel> driverModelList;

    public static DriverFragment newInstance() {
        return new DriverFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.driver_fragment, container, false);
        mViewModel = ViewModelProviders.of(this).get(DriverViewModel.class);

        unbinder = ButterKnife.bind(this,itemView);
        initViews();
        mViewModel.getMessageError().observe(this,s ->{
            Toast.makeText(getContext(), ""+s, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        mViewModel.getDriverModelMutableList().observe(this,driver -> {
            dialog.dismiss();
            driverModelList = driver;
            adapter = new MyDriverAdapter(getContext(), driverModelList);
            recycler_driver.setAdapter(adapter);
            recycler_driver.setLayoutAnimation(layoutAnimationController);

        });
        return itemView;
    }

    private void initViews() {
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_driver.setLayoutManager(layoutManager);
        recycler_driver.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        if (EventBus.getDefault().hasSubscriberForEvent(UpdateDriverEvent.class))
            EventBus.getDefault().removeStickyEvent(UpdateDriverEvent.class);
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new ChangeMenuCLick(true));
        super.onDestroy();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUpdateDriverActive(UpdateDriverEvent event){
        Map<String,Object> updateData = new HashMap<>();
        updateData.put("active", event.isActive());
        FirebaseDatabase.getInstance()
                .getReference(Common.DRIVER)
                .child(event.getDriverModel().getKey())
                .updateChildren(updateData)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Update status ke"+event.isActive(), Toast.LENGTH_SHORT).show();
                });
    }
}
