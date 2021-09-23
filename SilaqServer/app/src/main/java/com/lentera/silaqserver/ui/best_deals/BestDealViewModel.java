package com.lentera.silaqserver.ui.best_deals;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lentera.silaqserver.callback.IBestDealCallbackListener;
import com.lentera.silaqserver.common.Common;
import com.lentera.silaqserver.model.BestDealModel;

import java.util.ArrayList;
import java.util.List;

public class BestDealViewModel extends ViewModel implements IBestDealCallbackListener {
    private MutableLiveData<String> messageError = new MutableLiveData<>();
    private MutableLiveData<List<BestDealModel>> bestDealListMutable;
    private IBestDealCallbackListener bestDealCallbackListener;

    public BestDealViewModel(){
        bestDealCallbackListener=this;
    }

    public MutableLiveData<List<BestDealModel>> getBestDealListMutable(){
        if (bestDealListMutable == null)
            bestDealListMutable = new MutableLiveData<>();
        loadBestDeals();
        return bestDealListMutable;
    }

    public void loadBestDeals() {
        List<BestDealModel> temp = new ArrayList<>();
        DatabaseReference bestDealRef = FirebaseDatabase.getInstance().
                getReference(Common.BEST_DEALS);
        bestDealRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot bestdealsSnapShot:snapshot.getChildren()){
                    BestDealModel bestDealModel = bestdealsSnapShot.getValue(BestDealModel.class);
                    bestDealModel.setKey(bestdealsSnapShot.getKey());
                    temp.add(bestDealModel);
                }
                bestDealCallbackListener.onListBestDealLoadSuccess(temp);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                bestDealCallbackListener.onListBestDealLoadFailed(error.getMessage());
            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onListBestDealLoadSuccess(List<BestDealModel> bestDealModels) {
        bestDealListMutable.setValue(bestDealModels);
    }

    @Override
    public void onListBestDealLoadFailed(String message) {
        messageError.setValue(message);
    }
}
