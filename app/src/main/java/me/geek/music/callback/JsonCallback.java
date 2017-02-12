package me.geek.music.callback;

import com.google.gson.Gson;
import com.zhy.http.okhttp.callback.Callback;

import java.io.IOException;

import okhttp3.Response;

/**
 * @Author Geek-Lizc(394925542@qq.com)
 */
public abstract class JsonCallback<T> extends Callback<T> {
    private Class<T> mClass;
    private Gson mGson;

    public JsonCallback(Class<T> clazz){
        mClass = clazz;
        mGson = new Gson();
    }

    @Override
    public T parseNetworkResponse(Response response) throws IOException {
        try {
            String jsonString = response.body().string();
            return mGson.fromJson(jsonString, mClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

