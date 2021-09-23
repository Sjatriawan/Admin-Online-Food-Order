package com.lentera.silaqserver.callback;

import com.lentera.silaqserver.model.BestDealModel;
import com.lentera.silaqserver.model.MostPopularModel;

import java.util.List;

public interface IMostPopularCallbackListener {
    void onListMostPopularLoadSuccess(List<MostPopularModel> mostPopularModels);
    void onListMostPopularLoadFailed(String message);
}
