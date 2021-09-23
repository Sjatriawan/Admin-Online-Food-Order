package com.lentera.silaqserver.callback;

import com.lentera.silaqserver.model.BestDealModel;

import java.util.List;

public interface IBestDealCallbackListener {
    void onListBestDealLoadSuccess(List<BestDealModel>bestDealModels);
    void onListBestDealLoadFailed(String message);
}
