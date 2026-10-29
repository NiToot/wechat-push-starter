package com.pt.vx.utils;




import com.pt.vx.config.MainConfig;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


/**
 * @Author: PengTao
 * @Date: 2022/10/4 23:35
 **/
@Slf4j
public class HttpUtil {



    /**
     *
     * @param url get请求地址
     * @param paramsMap 请求参数
     * @param charset 字符集
     * @param timeout 超时时间
     * @param recount 重试次数
     * @return 结果
     */
    public static String get(String url, Map<String,Object> paramsMap, Charset charset,int timeout,int recount){
        try {
            return cn.hutool.http.HttpUtil.createGet(url).form(paramsMap).charset(charset).timeout(timeout).execute().body();
        }catch (Exception e){
            if(recount > 0){
               return get(url,paramsMap,charset,timeout,--recount);
            }else {
               log.warn("GET请求失败（已重试），异常类型：{}", e.getClass().getSimpleName());
            }
        }
        return null;
    }

    public static String get(String url, Map<String,Object> paramsMap, Charset charset,int timeout){
        return get(url,paramsMap,charset,timeout,MainConfig.httpRetryCount);
    }

    public static String get(String url, Charset charset,int timeout){
       return get(url,new HashMap<>(),charset,timeout);
    }

    public static String get(String url, Map<String,Object> paramsMap){
        return get(url, paramsMap,StandardCharsets.UTF_8,MainConfig.httpRetryTime);
    }

    public static String get(String url){
        return get(url, new HashMap<>());
    }

    /**
     *
     * @param url post请求地址
     * @param params 请求参数
     * @param charset 字符集
     * @param timeout 超时时间
     * @param recount 重试次数
     * @return 结果
     */
    public static String post(String url, String params, Charset charset, int timeout,int recount){
        try {
            return cn.hutool.http.HttpUtil.createPost(url)
                    .body(params, "application/json")
                    .charset(charset)
                    .timeout(timeout)
                    .execute()
                    .body();
        }
        catch (Exception e){
            if(recount > 0){
                return post(url,params,charset,timeout,--recount);
            }else {
                log.warn("POST请求失败，异常类型：{}", e.getClass().getSimpleName());
            }
        }
        return null;
    }


    public static String post(String url,String params, Charset charset, int timeout){
        return post(url,params,charset,timeout,MainConfig.httpRetryCount);
    }

    public static String post(String url,String params){
      return post(url,params, StandardCharsets.UTF_8,MainConfig.httpRetryTime);
    }

}
