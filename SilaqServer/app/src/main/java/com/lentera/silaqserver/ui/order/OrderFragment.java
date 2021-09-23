 package com.lentera.silaqserver.ui.order;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import android.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.lentera.silaqserver.EventBus.ChangeMenuCLick;
import com.lentera.silaqserver.EventBus.LoadOrderEvent;
import com.lentera.silaqserver.R;
import com.lentera.silaqserver.adapter.MyDriverSelectionAdapter;
import com.lentera.silaqserver.adapter.MyOrderAdapter;
import com.lentera.silaqserver.callback.IDriverLoadCallbackListener;
import com.lentera.silaqserver.common.BottomSheetOrderFragment;
import com.lentera.silaqserver.common.Common;
import com.lentera.silaqserver.common.MySwiperHelper;
import com.lentera.silaqserver.model.DriverModel;
import com.lentera.silaqserver.model.DrivingOrderModel;
import com.lentera.silaqserver.model.FCMSendData;
import com.lentera.silaqserver.model.OrderModel;
import com.lentera.silaqserver.model.TokenModel;
import com.lentera.silaqserver.remote.IFCMService;
import com.lentera.silaqserver.remote.RetrofitFCMClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class OrderFragment extends Fragment implements IDriverLoadCallbackListener {
    @BindView(R.id.recycler_order)
    RecyclerView recycler_order;
    @BindView(R.id.txt_order_filter)
    TextView txt_order_filter;

    RecyclerView recycler_driver;

    Unbinder unbinder;
    LayoutAnimationController layoutAnimationController;
    MyOrderAdapter adapter;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IFCMService ifcmService;

    private IDriverLoadCallbackListener driverLoadCallbackListener;


    private OrderViewModel orderViewModel;
    private MyDriverSelectionAdapter myDriverSelectedAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        orderViewModel =
                ViewModelProviders.of(this).get(OrderViewModel.class);
        View root = inflater.inflate(R.layout.fragment_order, container, false);

        unbinder = ButterKnife.bind(this, root);
        initViews();
        orderViewModel.getMessageError().observe(this, s -> {
            Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
        });
        orderViewModel.getOrderModelMutableLiveData().observe(this, orderModels -> {
            if (orderModels != null) {
                adapter = new MyOrderAdapter(getContext(), orderModels);
                recycler_order.setAdapter(adapter);
                recycler_order.setLayoutAnimation(layoutAnimationController);

                updateTextCounter();

            }
        });
        return root;
    }

    private void initViews() {

        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

        driverLoadCallbackListener = this;

        setHasOptionsMenu(true);
        recycler_order.setHasFixedSize(true);
        recycler_order.setLayoutManager(new LinearLayoutManager(getContext()));

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;


        MySwiperHelper mySwiperHelper =  new MySwiperHelper(getContext(),recycler_order,width/6) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(),"Tujuan",30,0, Color.parseColor("#560027"),
                        pod -> {
                            Dexter.withActivity(getActivity())
                                    .withPermission(Manifest.permission.CALL_PHONE)
                                    .withListener(new PermissionListener() {
                                        @Override
                                        public void onPermissionGranted(PermissionGrantedResponse response) {
                                            OrderModel orderModel = adapter.getItemAtPosition(pod);
                                            Intent intent = new Intent();
                                            intent.setAction(Intent.ACTION_DIAL);
                                            intent.setData(Uri.parse(new StringBuilder("tel: ")
                                            .append(orderModel.getUserPhone()).toString()));
                                            startActivity(intent);
                                        }

                                        @Override
                                        public void onPermissionDenied(PermissionDeniedResponse response) {
                                            Toast.makeText(getContext(), ""+response, Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                                        }
                                    }).check();
                        }));

                buf.add(new MyButton(getContext(),"Panggil",30,0, Color.parseColor("#9b0000"),
                        pod -> {
                             }));

                buf.add(new MyButton(getContext(),"Hapus",30,0, Color.parseColor("#12005e"),
                        pod -> {
                        AlertDialog.Builder  builder = new AlertDialog.Builder(getContext())
                                .setTitle("Hapus")
                                .setMessage("Apakah anda yakin ingin menghapus pesanan ini?")
                                .setNegativeButton("Batal", (dialog, which) -> dialog.dismiss())
                                .setPositiveButton("Hapus", (dialog, which) -> {
                                    OrderModel orderModel = adapter.getItemAtPosition(pod);
                                    FirebaseDatabase.getInstance()
                                            .getReference(Common.ORDER_REF)
                                            .child(orderModel.getKey())
                                            .removeValue()
                                            .addOnFailureListener(e -> Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                                            .addOnSuccessListener(aVoid -> {
                                                adapter.removeItem(pod);
                                                adapter.notifyItemRemoved(pod);
                                                updateTextCounter();
                                                dialog.dismiss();
                                                Toast.makeText(getContext(), "Pesanan telah dihapus", Toast.LENGTH_SHORT).show();
                                            });
                                });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                            Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                            negativeButton.setTextColor(Color.GRAY);

                            Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                            negativeButton.setTextColor(Color.RED );
                        }));
                buf.add(new MyButton(getContext(),"Edit",30,0, Color.parseColor("#336699"),
                        pod -> {
                            showEditDialog(adapter.getItemAtPosition(pod), pod);
                        }));
            }
        };

    }

    private void showEditDialog(OrderModel orderModel, int pod) {
        View layout_dialog;
        AlertDialog.Builder builder;
        if(orderModel.getOrderStatus() == 0){
            layout_dialog = LayoutInflater.from(getContext())
                    .inflate(R.layout.layout_dialog_shipping, null);

            recycler_driver = layout_dialog.findViewById(R.id.recycler_drivers);

            builder = new AlertDialog.Builder(getContext(),android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
                    .setView(layout_dialog);
        }else if (orderModel.getOrderStatus() == -1) {
            layout_dialog = LayoutInflater.from(getContext())
                    .inflate(R.layout.layout_dialog_cancelled, null);
            builder = new AlertDialog.Builder(getContext()).setView(layout_dialog);
        }else {
            layout_dialog = LayoutInflater.from(getContext())
                    .inflate(R.layout.layout_dialog_shipped, null);
            builder = new AlertDialog.Builder(getContext()).setView(layout_dialog);
        }
        Button btn_ok = (Button) layout_dialog.findViewById(R.id.btn_ok);
        Button btn_cancel = (Button) layout_dialog.findViewById(R.id.btn_cancel);

        RadioButton rdi_shipping = (RadioButton) layout_dialog.findViewById(R.id.rdi_shipping);
        RadioButton rdi_shipped = (RadioButton) layout_dialog.findViewById(R.id.rdi_shipped);
        RadioButton rdi_cancelled = (RadioButton) layout_dialog.findViewById(R.id.rdi_cancelled);
        RadioButton rdi_delete = (RadioButton) layout_dialog.findViewById(R.id.rdi_delete);
        RadioButton rdi_restore_placed = (RadioButton) layout_dialog.findViewById(R.id.rdi_restore_placed);

        TextView txt_status = (TextView)layout_dialog.findViewById(R.id.txt_status);

        //set data
        txt_status.setText(new StringBuilder("Order Status: ")
        .append(Common.convertStatusToString(orderModel.getOrderStatus())));

        AlertDialog dialog = builder.create();

        if (orderModel.getOrderStatus() == 0)
            loaDriverList(pod,orderModel,dialog,btn_ok,btn_cancel,rdi_shipping
            ,rdi_shipped,rdi_cancelled,rdi_delete,rdi_restore_placed);
        else
            showDialog(pod,orderModel,dialog,btn_ok,btn_cancel,rdi_shipping
                    ,rdi_shipped,rdi_cancelled,rdi_delete,rdi_restore_placed);


    }

    private void loaDriverList(int pod, OrderModel orderModel, AlertDialog dialog, Button btn_ok, Button btn_cancel, RadioButton rdi_shipping, RadioButton rdi_shipped, RadioButton rdi_cancelled, RadioButton rdi_delete, RadioButton rdi_restore_placed) {
        List<DriverModel> tempList = new ArrayList<>();
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference(Common.DRIVER);
        Query driverActive = driverRef.orderByChild("active").equalTo(true);
        driverActive.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot driverSnapshot:snapshot.getChildren()){
                    DriverModel driverModel = driverSnapshot.getValue(DriverModel.class);
                    driverModel.setKey(driverSnapshot.getKey());
                    tempList.add(driverModel);
                }
                driverLoadCallbackListener.onDriverLoadSuccess(pod,orderModel,tempList,
                        dialog,
                        btn_ok,btn_cancel,
                        rdi_shipping,rdi_shipped,rdi_cancelled,rdi_delete,rdi_restore_placed);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                driverLoadCallbackListener.onDriverLoadFailed(error.getMessage());
            }
        });
    }

    private void showDialog(int pod, OrderModel orderModel, AlertDialog dialog, Button btn_ok, Button btn_cancel, RadioButton rdi_shipping, RadioButton rdi_shipped, RadioButton rdi_cancelled, RadioButton rdi_delete, RadioButton rdi_restore_placed) {

        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);
        btn_cancel.setOnClickListener(view -> dialog.dismiss());
        btn_ok.setOnClickListener(v -> {
            dialog.dismiss();
            if(rdi_cancelled != null && rdi_cancelled.isChecked()){

                updateOrder(pod, orderModel,-1);
                dialog.dismiss();

            }else if(rdi_shipping != null && rdi_shipping.isChecked()) {
//                updateOrder(pod, orderModel, 1);
                DriverModel driverModel =null;
                if (myDriverSelectedAdapter != null){
                    driverModel = myDriverSelectedAdapter.getSelectedDriver();
                    if (driverModel != null){

                        createDrivingOrder(pod,driverModel, orderModel,dialog);

                    }else
                        Toast.makeText(getContext(), "Pilih driver", Toast.LENGTH_SHORT).show();

                }


            }else if(rdi_shipped != null && rdi_shipped.isChecked()) {
                updateOrder(pod, orderModel, 2);
                dialog.dismiss();

            }else if(rdi_restore_placed != null && rdi_restore_placed.isChecked()) {
                updateOrder(pod, orderModel, 0);
                dialog.dismiss();
            }else if(rdi_delete != null && rdi_delete.isChecked()) {
                deleteOrder(pod, orderModel);
                dialog.dismiss();
            }

        });
    }

    private void createDrivingOrder(int pod,DriverModel driverModel, OrderModel orderModel, AlertDialog dialog) {
        DrivingOrderModel drivingOrder = new DrivingOrderModel();
        drivingOrder.setDriverPhone(driverModel.getPhone());
        drivingOrder.setDriverName(driverModel.getName());
        drivingOrder.setOrderModel(orderModel);
        drivingOrder.setStartTrip(false);
        drivingOrder.setCurrentLat(-1);
        drivingOrder.setCurrentLng(-1);

        FirebaseDatabase.getInstance()
                .getReference(Common.DRIVING_ORDER_REF)
                .push()
                .setValue(drivingOrder)
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dialog.dismiss();
                FirebaseDatabase.getInstance()
                        .getReference(Common.TOKEN_REF)
                        .child(driverModel.getKey())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()){
                                    TokenModel tokenModel = snapshot.getValue(TokenModel.class);
                                    Map<String, String> notiData = new HashMap<>();
                                    notiData.put(Common.NOTI_TITLE,"Kamu memiliki pesan untuk diantarkan");
                                    notiData.put(Common.NOTI_CONTENT,new StringBuilder("Pesanan anda dari")
                                            .append(orderModel.getUserPhone()).toString());

                                    FCMSendData sentData = new FCMSendData(tokenModel.getToken(), notiData);

                                    compositeDisposable.add(ifcmService.sendNotification(sentData)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(fcmResponse -> {
                                                dialog.dismiss();
                                                if(fcmResponse.getSuccess() == 1){
                                                   updateOrder(pod,orderModel,1);
                                                }else  {
                                                    Toast.makeText(getContext(), "Update berhasil tapi gagal mengirim notif", Toast.LENGTH_SHORT).show();
                                                }

                                            }, throwable -> {
                                                dialog.dismiss();
                                                Toast.makeText(getContext(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                            }));

                                }else {
                                    dialog.dismiss();
                                    Toast.makeText(getContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                dialog.dismiss();
                                Toast.makeText(getContext(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }

    private void deleteOrder(int pod, OrderModel orderModel) {
        if (!TextUtils.isEmpty(orderModel.getKey())) {

            FirebaseDatabase.getInstance()
                    .getReference(Common.ORDER_REF)
                    .child(orderModel.getKey())
                    .removeValue()
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show())
                    .addOnSuccessListener(aVoid -> {
                        adapter.removeItem(pod);
                        adapter.notifyItemRemoved(pod);
                        updateTextCounter();
                        Toast.makeText(getContext(), "Hapus berhasil", Toast.LENGTH_SHORT).show();

                    });
        }
    }

    private void updateOrder(int pos, OrderModel orderModel, int status){
        if (!TextUtils.isEmpty(orderModel.getKey())){
            Map<String, Object>updateData = new HashMap<>();
            updateData.put("orderStatus", status);

            FirebaseDatabase.getInstance()
                    .getReference(Common.ORDER_REF)
                    .child(orderModel.getKey())
                    .updateChildren(updateData)
                    .addOnFailureListener(e -> Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show())
                    .addOnSuccessListener(aVoid -> {

                        android.app.AlertDialog dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
                        dialog.show();


                        FirebaseDatabase.getInstance()
                                .getReference(Common.TOKEN_REF)
                                .child(orderModel.getUserId())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()){
                                            TokenModel tokenModel = snapshot.getValue(TokenModel.class);
                                            Map<String, String> notiData = new HashMap<>();
                                            notiData.put(Common.NOTI_TITLE,"Pesanan telah diupdate");
                                            notiData.put(Common.NOTI_CONTENT,new StringBuilder("Pesanan anda").append(orderModel.getKey())
                                                    .append("telah diupdate")
                                                    .append(Common.convertStatusToString(status)).toString());

                                            FCMSendData sentData = new FCMSendData(tokenModel.getToken(), notiData);

                                            compositeDisposable.add(ifcmService.sendNotification(sentData)
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(fcmResponse -> {
                                                        dialog.dismiss();
                                                        if(fcmResponse.getSuccess() == 1){
                                                            Toast.makeText(getContext(), "Update berhasil", Toast.LENGTH_SHORT).show();
                                                        }else  {
                                                            Toast.makeText(getContext(), "Update berhasil tapi gagal mengirim notif", Toast.LENGTH_SHORT).show();
                                                        }

                                                    }, throwable -> {
                                                        dialog.dismiss();
                                                        Toast.makeText(getContext(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }));

                                        }else {
                                            dialog.dismiss();
                                            Toast.makeText(getContext(), "Token tidak ditemukan", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        dialog.dismiss();
                                        Toast.makeText(getContext(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                        adapter.removeItem(pos);
                        adapter.notifyItemRemoved(pos);
                        updateTextCounter();

                    });
        }else {
            Toast.makeText(getContext(), "Pesanan harus kosong", Toast.LENGTH_SHORT).show();
        }

    }

    private void updateTextCounter() {
        txt_order_filter.setText(new StringBuilder("Pesanan (")
                .append(adapter.getItemCount())
                .append(")"));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.order_filter_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter:
                BottomSheetOrderFragment bottomSheetOrderFragment = BottomSheetOrderFragment.getInstance();
                bottomSheetOrderFragment.show(getActivity().getSupportFragmentManager(), "OrderFilter");
                break;
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(toString()))
            EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        if (EventBus.getDefault().hasSubscriberForEvent(LoadOrderEvent.class))
            EventBus.getDefault().removeStickyEvent(LoadOrderEvent.class);
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new ChangeMenuCLick(true));
        super.onDestroy();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onLoadOrderEvent(LoadOrderEvent event){
        orderViewModel.loadOrderByStatus(event.getStatus());
    }

    @Override
    public void onDriverLoadSuccess(List<DriverModel> driverModelList) {
    }

    @Override
    public void onDriverLoadSuccess(int pos, OrderModel orderModel, List<DriverModel> driverModels, android.app.AlertDialog dialog, Button btn_ok, Button btn_cancel, RadioButton rdi_shipping, RadioButton rdi_shipped, RadioButton rdi_cancelled, RadioButton rdi_delete, RadioButton rdi_restore_placed) {
        if (recycler_driver!= null){
            recycler_driver.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            recycler_driver.setLayoutManager(layoutManager);
            recycler_driver.addItemDecoration(new DividerItemDecoration(getContext(),layoutManager.getOrientation()));

            myDriverSelectedAdapter = new MyDriverSelectionAdapter(getContext(), driverModels);
            recycler_driver.setAdapter(myDriverSelectedAdapter);
        }
        showDialog(pos,orderModel,dialog,btn_ok,btn_cancel,rdi_shipping,rdi_shipped,rdi_cancelled,rdi_delete,rdi_restore_placed);
    }

    @Override
    public void onDriverLoadFailed(String message) {
        Toast.makeText(getContext(), ""+message, Toast.LENGTH_SHORT).show();
    }
}
