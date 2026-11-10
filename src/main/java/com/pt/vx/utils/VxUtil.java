package com.pt.vx.utils;


import cn.hutool.json.JSONUtil;
import com.pt.vx.config.MainConfig;
import com.pt.vx.pojo.TokenInfo;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
public class VxUtil {


    private static String tokenCache;
    private static LocalDateTime getTime;
    private static int tokenLifetimeSeconds;
    private static String cachedAppId;
    private static String cachedAppSecret;

    public static TokenInfo getToken(String appId,String appSecret){
        String TOKEN_URL ="https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
        String result = HttpUtil.get(String.format(TOKEN_URL, appId, appSecret));
        TokenInfo tokenInfo;
        try {
            tokenInfo = JSONUtil.toBean(result, TokenInfo.class);
        } catch (RuntimeException e) {
            // Do not include the response body or a cause containing credentials in logs.
            throw new IllegalStateException("微信 token 响应格式无效");
        }
        if(Objects.nonNull(tokenInfo) ){
            String errCode = tokenInfo.getErrcode();
            if("-1".equals(errCode)){
                 log.warn("微信系统繁忙，此时稍候再试<<<pt提示：请等一会会再来>>>");
            }else if("40001".equals(errCode)){
                log.warn("微信调用错误！<<<pt提示：AppSecret错误或者 AppSecret 不属于这个公众号，请检查一下你的微信AppID和appSecret(刷新一下测试平台，看看是否数据是否一致)>>>");
            }else if("40002".equals(errCode)){
                log.warn("微信调用错误！<<<pt提示：请确保grant_type字段值为client_credential");
            }else if("40164".equals(errCode)){
                log.warn("微信调用错误！<<<pt提示：调用接口的 IP 地址不在白名单中，请在接口 IP 白名单中进行设置。>>>");
            }else if("89503".equals(errCode)){
                log.warn("微信调用错误！<<<pt提示：此 IP 调用需要管理员确认,请联系管理员>>>");
            }else if("89501".equals(errCode)){
                log.warn("微信调用错误！<<<pt提示：此 IP 正在等待管理员确认,请联系管理员>>>");
            }else if("89506".equals(errCode)){
                log.warn("微信调用错误！<<<pt提示：24小时内该 IP 被管理员拒绝调用两次，24小时内不可再使用该 IP 调用>>>");
            }else if("89507".equals(errCode)){
                log.warn("微信调用错误！<<<pt提示：1小时内该 IP 被管理员拒绝调用一次，1小时内不可再使用该 IP 调用>>>");
            }
        }
        return tokenInfo;
    }


    public static void sendMessage(String message,String appId,String appSecret){
        String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s";
        String accessToken = accessToken(appId, appSecret);
        // A timeout can happen after WeChat accepted the message; retrying could duplicate it.
        String result = HttpUtil.post(String.format(url,accessToken),message,
                java.nio.charset.StandardCharsets.UTF_8,MainConfig.httpRetryTime,0);
        if (result == null || result.trim().isEmpty()) throw new IllegalStateException("微信发送接口无响应");
        cn.hutool.json.JSONObject response;
        Integer errcode;
        try {
            response = JSONUtil.parseObj(result);
            errcode = response.getInt("errcode");
        } catch (RuntimeException e) {
            throw new IllegalStateException("微信发送接口响应格式无效；发送结果不确定，请先核实收件情况");
        }
        if (!Integer.valueOf(0).equals(errcode)) {
            throw new IllegalStateException("微信发送失败，错误码：" + errcode);
        }
        log.info("微信接口已接受消息，msgid：{}", response.getStr("msgid"));
    }

    private static synchronized String accessToken(String appId, String appSecret) {
        if (tokenCache != null && Objects.equals(cachedAppId, appId)
                && Objects.equals(cachedAppSecret, appSecret) && !isTokenOver()) {
            return tokenCache;
        }
        LocalDateTime requestedAt = LocalDateTime.now();
        TokenInfo token = getToken(appId, appSecret);
        if (token == null || token.getAccess_token() == null || token.getAccess_token().trim().isEmpty()
                || (token.getErrcode() != null && !"0".equals(token.getErrcode()))) {
            throw new IllegalStateException("获取微信 token 失败，请检查凭据和接口权限");
        }
        int lifetime;
        try {
            lifetime = Integer.parseInt(token.getExpires_in());
            if (lifetime <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            throw new IllegalStateException("微信 token 缺少有效的 expires_in，已停止发送");
        }
        // Update the cache only after the complete response has passed validation.
        tokenCache = token.getAccess_token();
        cachedAppId = appId;
        cachedAppSecret = appSecret;
        getTime = requestedAt;
        tokenLifetimeSeconds = lifetime;
        return tokenCache;
    }

    private static boolean isTokenOver() {
        LocalDateTime now = LocalDateTime.now();
        return getTime == null || now.isBefore(getTime)
                || !now.isBefore(getTime.plusSeconds(Math.max(0L, tokenLifetimeSeconds - 60L)));
    }

}
