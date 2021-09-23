package com.lentera.silaqserver.ui.driver;

import android.app.AlertDialog;
import android.widget.Button;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lentera.silaqserver.callback.IDriverLoadCallbackListener;
import com.lentera.silaqserver.common.Common;
import com.lentera.silaqserver.model.DriverModel;
import com.lentera.silaqserver.model.OrderModel;

import java.util.ArrayList;
import java.util.List;

public class DriverViewModel extends ViewModel implements IDriverLoadCallbackListener {
    private MutableLiveData<String> messageError = new MutableLiveData<>();
    private MutableLiveData<List<DriverModel>> driverModelMutableList;
    private IDriverLoadCallbackListener driverLoadCallbackListener;

    public DriverViewModel() {
        driverLoadCallbackListener = this;
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    public MutableLiveData<List<DriverModel>> getDriverModelMutableList() {
        if (driverModelMutableList == null){
            driverModelMutableList = new MutableLiveData<>();
            loadDriver();
        }
        return driverModelMutableList;
    }

    private void loadDriver() {
        List<DriverModel> tempList = new ArrayList<>();
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference(Common.DRIVER);
        driverRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot driverSnapshot:snapshot.getChildren()){

                    DriverModel driverModel = driverSnapshot.getValue(DriverModel.class);
                    driverModel.setKey(driverSnapshot.getKey());
                    tempList.add(driverModel);
                }
                driverLoadCallbackListener.onDriverLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                driverLoadCallbackListener.onDriverLoadFailed(error.getMessage());
            }
        });
    }

    @Override
    public void onDriverLoadSuccess(List<DriverModel> driverModelList) {
        if (driverModelMutableList != null)
            driverModelMutableList.setValue(driverModelList);
    }

    @Override
    public void onDriverLoadSuccess(int pos, OrderModel orderModel, List<DriverModel> driverModels, AlertDialog dialog, Button btn_ok, Button btn_cancel, RadioButton rdi_shipping, RadioButton rdi_shipped, RadioButton rdi_cancelled, RadioButton rdi_delete, RadioButton rdi_restore_placed) {

    }

    @Override
    public void onDriverLoadFailed(String message) {
        messageError.setValue(message);
    }
}
