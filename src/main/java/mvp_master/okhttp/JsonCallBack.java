package mvp_master.okhttp;

import com.squareup.okhttp.Request;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Author:  梁铖城
 * Email:   1038127753@qq.com
 * Date:    2015年12月10日22:54:49
 * Description: json回调
 */
public abstract class JsonCallBack<T> {

    /**
     * Called when the request could not be executed due to cancellation, a
     * connectivity problem or timeout. Because networks can fail during an
     * exchange, it is possible that the remote server accepted the request
     * before the failure.
     */
    public abstract void onFailure(Request request, Exception e);

    /**
     * Called when the HTTP response was successfully returned by the remote
     * server. The callback may proceed to read the response body with {@link
     * Response#body}. The response is still live until its response body is
     * closed with {@code response.body().close()}. The recipient of the callback
     * may even consume the response body on another thread.
     *
     * <p>Note that transport-layer success (receiving a HTTP response code,
     * headers and body) does not necessarily ind     * success: {@code response} may still indicate an unhappy HTTP response
     icate application-layer
     * code like 404 or 500.
     * @param object
     */
    public abstract void onResponse(T object) throws IOException;

    Class<T> getType(){
        Type type=((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return (Class<T>)type;
    }
}
