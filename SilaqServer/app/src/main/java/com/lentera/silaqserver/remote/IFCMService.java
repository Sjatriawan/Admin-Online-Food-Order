package com.lentera.silaqserver.remote;


import com.lentera.silaqserver.model.FCMResponse;
import com.lentera.silaqserver.model.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({"Authorization:key=AAAAHM-ueEI:APA91bGTAmIcKpETbBm_o0heVG2U-HRhkzI9GTh3DN-HHiaCFrOwUkkgSCsIUAzKlFniQ158Q-9-sCWnmA_78lxQL2LMreFlKakshf_g6AVHsScLv7oKMUlD-2tIDyo65oX9w4XwCap2",
            "Content-Type:application/json"})
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
