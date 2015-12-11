package mvp_master.okhttp;

import android.content.Context;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author:  梁铖城
 * Email:   1038127753@qq.com
 * Date:    2015年12月10日23:01:10
 * Description:
 */
public  class OKHttpUtils<T> {

    private boolean DEBUG= true;
    private OkHttpClient client=null;
    private Gson gson;

    public OkHttpClient getClient(){
        return client;
    }

    private OKHttpUtils(){

    }

    /**
     * OKHttpUtils（构造函数）
     * @param context 上下文
     * @param maxCacheSize 缓存的大小
     * @param cachedDir 缓存的文件夹
     * @param maxCacheAge 最大缓存时间
     * @param cacheType 缓存类型
     * @param netWorkinterceptors 网络拦截器
     * @param interceptors 拦截器
     */
    private OKHttpUtils(Context context,int maxCacheSize,File cachedDir,final int maxCacheAge,
                        CacheType cacheType ,List<Interceptor> netWorkinterceptors, List<Interceptor> interceptors){
        client=new OkHttpClient();
        gson=new Gson();
        if (cachedDir!=null){
            //设置缓存的大小和目录
            client.setCache(new Cache(cachedDir,maxCacheSize));
        }else{
            client.setCache(new Cache(context.getCacheDir(),maxCacheSize));
        }
        //请求拦截器
        Interceptor cacheInterceptor=new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
               Response originalResponse=chain.proceed(chain.request());
                return originalResponse.newBuilder().removeHeader("Pragma")
                        .header("Cache-Control", String.format("max-age=%d", maxCacheAge))
                        .build();
            }
        };
        //添加这个拦截器
        client.networkInterceptors().add(cacheInterceptor);
        if (netWorkinterceptors!=null&&netWorkinterceptors.isEmpty()){
            client.networkInterceptors().addAll(netWorkinterceptors);
        }
        if (interceptors!=null &&interceptors.isEmpty()){
            client.interceptors().addAll(interceptors);
        }
    }

    public OKHttpUtils initDefault(Context context){
        return new Builder(context).build();
    }

    public void get(String url,Callback callback){
        get(url,CacheType.NETWORK_ELSE_CACHED,null,callback);
    }

    public void get(String url,Headers headers,Callback callback){
        get(url,CacheType.NETWORK_ELSE_CACHED,headers,callback);
    }
    public void get(String url,CacheType cacheType,Callback callback){
        get(url, cacheType,null, callback);
    }

    public void get(String url,JsonCallBack callback){
        get(url,CacheType.NETWORK_ELSE_CACHED,null,callback);
    }

    public void get(String url,Headers headers,JsonCallBack callback){
        get(url,CacheType.NETWORK_ELSE_CACHED,headers,callback);
    }
    public void get(String url,CacheType cacheType,JsonCallBack callback){
        get(url, cacheType,null, callback);
    }

    /**
     * get 请求数据
     * @param url  地址
     * @param cacheType 缓存类型
     * @param headers 头部文件
     * @param jsonCallBack json结果回调
     */
    public void get(final String url, final CacheType cacheType, final Headers headers , final JsonCallBack jsonCallBack){
        get(url, cacheType, headers, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                if(jsonCallBack!=null){
                    jsonCallBack.onFailure(request,e);
                }
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if(response.isSuccessful() && jsonCallBack!=null){
                    String jsonString = response.body().string();;
                    if(!TextUtils.isEmpty(jsonString)){
                        Object result = null;
                        try {
                            result =  gson.fromJson(jsonString,jsonCallBack.getType());
                            jsonCallBack.onResponse(result);
                        } catch (JsonSyntaxException e) {
                            jsonCallBack.onFailure(null,new Exception("json string parse error"));
                            e.printStackTrace();
                        }

                    }else{
                        jsonCallBack.onFailure(null,new Exception("json string may be null"));
                    }
                }
            }
        });
    }

    public void get(final String url, final CacheType cacheType, final Headers headers, final Callback callback){
        switch (cacheType){
            case ONLY_NETWORK:
                getDataFromNetwork(url,headers,callback);
                break;
            case ONLY_CACHED:
                getDataFromCached(url,headers,callback);
                break;
            case CACHED_ELSE_NETWORK:
                getDataFromCached(url,headers, new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        getDataFromNetwork(url,headers,callback);
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        if(response.code()==200){
                            callback.onResponse(response);
                        }else{
                            getDataFromNetwork(url,headers,callback);
                        }
                    }
                });
                break;
            case NETWORK_ELSE_CACHED:
                getDataFromNetwork(url,headers, new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        getDataFromCached(url,headers,callback);
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        if(response.code()==200){
                            callback.onResponse(response);
                        }else{
                            getDataFromCached(url,headers,callback);
                        }
                    }
                });
                break;
        }
    }

    /**
     * 从网络获取数据
     * @param url 地址
     * @param headers  头部文件
     * @param callback 回调
     */
    public void getDataFromNetwork(final String url, Headers headers,final Callback callback){
        getData(url, CacheControl.FORCE_NETWORK, headers, callback);
    }

    /**
     * 从缓存中获取数据
     * @param url 地址
     * @param headers 头部文件
     * @param callback 回调
     */
    public void getDataFromCached(String url,Headers headers ,final Callback callback){
        getData(url, CacheControl.FORCE_CACHE, headers, callback);
    }

    /**
     * 获取数据
     * @param url 地址
     * @param cacheControl 缓存
     * @param headers 头部文件
     * @param callback 回调
     */
    public void getData(String url, final CacheControl cacheControl, Headers headers, final Callback callback){
        //初始化请求
        final Request.Builder requestBuilder = new Request.Builder().url(url).cacheControl(cacheControl);
        if(headers!=null){
            //添加头部文件
            requestBuilder.headers(headers);
        }
        requestBuilder.tag(url);
        final Request request = requestBuilder.build();
        getData(request, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                callback.onFailure(request, e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.code() == 504) {
                    if (CacheControl.FORCE_CACHE == cacheControl) {
                        callback.onFailure(request, new IOException("cached not found"));
                        return;
                    }
                }
                callback.onResponse(response);
            }
        });
    }

    /**
     * 获取数据
     * @param request 请求
     * @param callback 回调
     */
    public void getData(Request request,Callback callback){
        client.newCall(request).enqueue(callback);
    }

    public static class Builder{

        private int maxCachedSize= 5 * 1024 * 1024;
        private File cachedDir;
        private Context context;
        private List<Interceptor> networkInterceptors;
        private List<Interceptor> interceptors;
        private int maxCacheAge = 3600 * 12;
        private CacheType cacheType = CacheType.NETWORK_ELSE_CACHED;

        public Builder(Context context){
            this.context=context;
        }

        private Builder(){}

        public OKHttpUtils build(){
            return new OKHttpUtils(context,maxCachedSize,cachedDir,maxCacheAge,cacheType,networkInterceptors,interceptors);
        }

        public Builder cacheType(CacheType cacheType){
            this.cacheType=cacheType;
            return this;
        }

        public Builder cacheDire(File cachedDir){
            this.cachedDir=cachedDir;
            return this;
        }

        public Builder context(Context context) {
            this.context = context;
            return this;
        }

        // TODO: 2015/12/10 拦截器使用可参考这篇文章  <a href="http://www.tuicool.com/articles/Uf6bAnz">http://www.tuicool.com/articles/Uf6bAnz</a>
        public Builder interceptors(List<Interceptor> interceptors) {
            this.interceptors = interceptors;
            return this;
        }

        public Builder maxCachedSize(int maxCachedSize) {
            this.maxCachedSize = maxCachedSize;
            return this;
        }

        public Builder networkInterceptors(List<Interceptor> networkInterceptors) {
            this.networkInterceptors = networkInterceptors;
            return this;
        }

        public Builder maxCacheAge(int maxCacheAge){
            this.maxCacheAge = maxCacheAge;
            return this;
        }
    }

    public void post(String url,Map<String,String> params, Headers headers,String encodedKey, String encodedValue,Callback callback){
        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
        if(params!=null && !params.isEmpty()){
            Set<String> keys = params.keySet();
            for(String key:keys){
                formEncodingBuilder.add(key,params.get(key));
            }
        }
        if(!TextUtils.isEmpty(encodedKey) && !TextUtils.isEmpty(encodedValue)){
            formEncodingBuilder.addEncoded(encodedKey,encodedValue);
        }
        Request.Builder requestBuilder = new Request.Builder().url(url).post(formEncodingBuilder.build());
        if(headers!=null){
            requestBuilder.headers(headers);
        }
        requestBuilder.tag(url);
        post(requestBuilder.build(),callback);
    }

    /**
     * post请求
     * @param url 地址
     * @param params 请求的参数
     * @param headers 头部文件
     * @param encodedKey  键
     * @param encodedValue 值
     * @param jsonCallBack 结果回调
     */
    public void post(String url, Map<String,String> params, Headers headers, String encodedKey, String encodedValue, final JsonCallBack jsonCallBack){
        post(url, params, headers, encodedKey, encodedValue, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                if(jsonCallBack!=null){
                    jsonCallBack.onFailure(request,e);
                }
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if(response.isSuccessful() && jsonCallBack!=null){
                    String jsonString = response.body().string();;
                    if(!TextUtils.isEmpty(jsonString)){
                        Object result = null;
                        try {
                            result =  gson.fromJson(jsonString,jsonCallBack.getType());
                            jsonCallBack.onResponse(result);
                        } catch (JsonSyntaxException e) {
                            jsonCallBack.onFailure(null,new Exception("json string parse error"));
                            e.printStackTrace();
                        }

                    }else{
                        jsonCallBack.onFailure(null,new Exception("json string may be null"));
                    }
                }
            }
        });
    }

    public void post(Request request,Callback callback){
        client.newCall(request).enqueue(callback);
    }

    public void post(String url,Callback callback){
        post(url,null,null,null,null,callback);
    }
    public void post(String url,Map<String,String> params,Callback callback){
        post(url,params,null,null,null,callback);
    }
    public void post(String url,Headers headers,Callback callback){
        post(url,null,headers,null,null,callback);
    }
    public void post(String url,Map<String,String> params, Headers headers,Callback callback){
        post(url, params, headers,null,null, callback);
    }

    public void post(String url,JsonCallBack callback){
        post(url,null,null,null,null,callback);
    }
    public void post(String url,Map<String,String> params,JsonCallBack callback){
        post(url,params,null,null,null,callback);
    }
    public void post(String url,Headers headers,JsonCallBack callback){
        post(url,null,headers,null,null,callback);
    }
    public void post(String url,Map<String,String> params, Headers headers,JsonCallBack callback){
        post(url, params, headers,null,null, callback);
    }

    /**
     * 通过url来取消一个请求  如果使用自定义的Request,传入request的Tag为url才能有效
     * @param url url
     */
    public void cancel(String url){
        try {
            client.cancel(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
