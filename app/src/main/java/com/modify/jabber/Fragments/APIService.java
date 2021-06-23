package com.modify.jabber.Fragments;

import com.modify.jabber.Notifications.MyResponse;
import com.modify.jabber.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAqyVI_M8:APA91bHh1vwxxvkNtLHP1_jjVdT5hd6LdZWloehbvYRtQJ7VNPNXw8sMpoFTgcZZ_cjcscUdSCGblFkcfjbi5k5Qb2I4SzHxu_io4Y-CU48BZygn1gA3-BA1-yguZ6PjC5s0en6CQou5"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}