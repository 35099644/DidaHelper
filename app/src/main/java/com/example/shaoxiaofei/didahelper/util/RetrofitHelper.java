package com.example.shaoxiaofei.didahelper.util;

import android.support.annotation.NonNull;
import android.util.Log;

import com.example.shaoxiaofei.didahelper.bean.Greeting;
import com.example.shaoxiaofei.didahelper.bean.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 * Created by shaoxiaofei on 28/12/2017.
 */

public class RetrofitHelper {
    private static final String TAG = "RetrofitHelper";
    private final static String baseUrl = "http://192.168.1.4:5000/";
    private Retrofit retrofit;

    /**
     * from retrofit 2.0 on ,url should be ended with "/",just like this
     * "http://192.168.0.5:8877/"
     * */
    public RetrofitHelper(){
        initRetrofit(baseUrl);
    }
    private void initRetrofit(String baseUrl) {
        /*init ok http*/
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .retryOnConnectionFailure(true)
                .connectTimeout(15, TimeUnit.SECONDS)
//                .addNetworkInterceptor(null)
                .build();

        Gson gson = new GsonBuilder()
                //custom gson
                .setDateFormat("yyyy-MM-dd hh:mm:ss")
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
    }

    public interface BlogService {
        @GET("cmd/{id}")
        Call<Greeting> getById(@Path("id") int id);//@path是url占位符

        @GET
        Call<Greeting> getDirectly(@Url String url);//直接使用url来访问

        @GET("dida/user")
        Call<Greeting> getByIdField(@Query("id") int id);//--> http://baseurl/group/users?id=groupId

        @GET("dida/user")
        Call<Greeting> getByMutiFields(@QueryMap(encoded = true) Map<String, String> options);

        @FormUrlEncoded//通过表单提交
        @POST("dida/user")
        Call<Greeting> postMap(@FieldMap Map<String, String> options);

        @POST("dida/user")
        Call<Greeting> postEntity(@Body User User);//使用@body就不能使用@FormUrlEncoded 但是目测现在还不成功

        @FormUrlEncoded//通过表单提交
        @POST("dida/user")
        Call<Greeting> postFields(@Field("phone") String first, @Field("immei") String last);


        @Multipart
        @POST("upload/log")
        Call<Greeting> upload(@Part("description") RequestBody description,
                            @Part MultipartBody.Part file);

    }

    public void postMap(Map<String,String> data,Callback<Greeting> callback){
        BlogService service = retrofit.create(BlogService.class);
        Call<Greeting> call = service.postMap(data);
        call.enqueue(callback);
    }

    public void postEntity(User user,Callback<Greeting> callback) {
        BlogService service = retrofit.create(BlogService.class);
        Call<Greeting> call = service.postEntity(user);
        call.enqueue(callback);
    }

    public void getByIdField(int id) {
        BlogService service = retrofit.create(BlogService.class);
        Call<Greeting> call = service.getByIdField(id);
        call.enqueue(new Callback<Greeting>() {
            @Override
            public void onResponse(Call<Greeting> call, Response<Greeting> response) {
                Log.d(TAG, "onResponse: " + response.body().getGreeting());
            }

            @Override
            public void onFailure(Call<Greeting> call, Throwable t) {
                t.printStackTrace();
            }
        });

    }


    public void unloadFile() {
        File file = new File("/sdcard/fengdongsifang-log.txt");
        if (file.exists()) Log.e(TAG, "unloadFile: file exists");

        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), file);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("log", file.getName(), requestFile);

        // add another part within the multipart request
        String descriptionString = "hello, this is description speaking";
        RequestBody description =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), descriptionString);

        BlogService service = retrofit.create(BlogService.class);
        Call<Greeting> call = service.upload(description, body);
        // 用法和OkHttp的call如出一辙
        // 不同的是如果是Android系统回调方法执行在主线程
        call.enqueue(new Callback<Greeting>() {
            @Override
            public void onResponse(Call<Greeting> call, Response<Greeting> response) {
                Log.d(TAG, "onResponse: " + response.body().getGreeting());
            }

            @Override
            public void onFailure(Call<Greeting> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
