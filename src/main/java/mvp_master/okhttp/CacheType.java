package mvp_master.okhttp;

/**
 * Author:  梁铖城
 * Email:   1038127753@qq.com
 * Date:    2015年12月10日22:52:26
 * Description:
 */
public enum CacheType {

    //只网络
    ONLY_NETWORK,
    //只缓存
    ONLY_CACHED,
    //缓存、网络
    CACHED_ELSE_NETWORK,
    //网络、缓存
    NETWORK_ELSE_CACHED
}
