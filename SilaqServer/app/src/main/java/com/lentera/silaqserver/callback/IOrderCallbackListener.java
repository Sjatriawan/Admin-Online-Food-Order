package com.lentera.silaqserver.callback;

import com.lentera.silaqserver.model.CategoryModel;
import com.lentera.silaqserver.model.OrderModel;

import java.util.List;

public interface IOrderCallbackListener {
    void onOrderLoadSuccess(List<OrderModel> orderModelList);
    void onOrderLoadFailed(String message);
}
