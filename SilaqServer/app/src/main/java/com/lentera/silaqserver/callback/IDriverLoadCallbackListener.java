package com.lentera.silaqserver.callback;

import android.app.AlertDialog;
import android.widget.Button;
import android.widget.RadioButton;

import com.lentera.silaqserver.model.DriverModel;
import com.lentera.silaqserver.model.OrderModel;

import java.util.List;

public interface IDriverLoadCallbackListener {
    void  onDriverLoadSuccess(List<DriverModel> driverModelList);
    void onDriverLoadSuccess(int pos, OrderModel orderModel, List<DriverModel> driverModels,
                             AlertDialog dialog, Button btn_ok, Button btn_cancel,
                             RadioButton rdi_shipping,RadioButton rdi_shipped,RadioButton rdi_cancelled,RadioButton rdi_delete, RadioButton rdi_restore_placed);
    void onDriverLoadFailed(String message);
}
