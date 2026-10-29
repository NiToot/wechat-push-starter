package com.pt.vx.utils;

import com.pt.vx.OfflineTest;
import org.junit.Test;
import org.junit.Before;
import org.mockito.MockedStatic;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

public class VxUtilTest extends OfflineTest {
    @Test public void uncertainSendOutcomeMustNotRetryThePost() {
        cn.hutool.http.HttpRequest request=mock(cn.hutool.http.HttpRequest.class,RETURNS_SELF);
        when(request.body(anyString(),anyString())).thenReturn(request);
        when(request.charset(any(java.nio.charset.Charset.class))).thenReturn(request);
        when(request.timeout(anyInt())).thenReturn(request);
        when(request.execute()).thenThrow(new cn.hutool.core.io.IORuntimeException("fixture read timeout"));
        try(MockedStatic<HttpUtil> http=mockStatic(HttpUtil.class,CALLS_REAL_METHODS);
            MockedStatic<cn.hutool.http.HttpUtil> transport=mockStatic(cn.hutool.http.HttpUtil.class)) {
            http.when(()->HttpUtil.get(anyString())).thenReturn("{\"access_token\":\"fixture-token\",\"expires_in\":7200}");
            transport.when(()->cn.hutool.http.HttpUtil.createPost(anyString())).thenReturn(request);
            assertThrows(IllegalStateException.class,()->VxUtil.sendMessage("{}","fixture-app","fixture-secret"));
            verify(request,times(1)).execute();
        }
    }
    @Before public void clearTokenCache() throws Exception {
        for(String name:new String[]{"tokenCache","getTime"}) {
            java.lang.reflect.Field field=VxUtil.class.getDeclaredField(name);
            field.setAccessible(true);field.set(null,null);
        }
    }
    @Test public void templateErrorCannotBeReportedAsSuccess() {
        try(MockedStatic<HttpUtil> http=mockStatic(HttpUtil.class)) {
            http.when(()->HttpUtil.get(anyString())).thenReturn("{\"access_token\":\"fixture-token\",\"expires_in\":7200}");
            http.when(()->HttpUtil.post(anyString(),anyString(),eq(java.nio.charset.StandardCharsets.UTF_8),anyInt(),eq(0))).thenReturn("{\"errcode\":40037,\"errmsg\":\"invalid template_id\"}");
            IllegalStateException e=assertThrows(IllegalStateException.class,()->VxUtil.sendMessage("{}","fixture-app","fixture-secret"));
            assertTrue(e.getMessage().contains("40037"));
        }
    }
    @Test public void subscriptionErrorCannotBeReportedAsSuccess() {
        try(MockedStatic<HttpUtil> http=mockStatic(HttpUtil.class)) {
            http.when(()->HttpUtil.get(anyString())).thenReturn("{\"access_token\":\"fixture-token\",\"expires_in\":7200}");
            http.when(()->HttpUtil.post(anyString(),anyString(),eq(java.nio.charset.StandardCharsets.UTF_8),anyInt(),eq(0))).thenReturn("{\"errcode\":43004,\"errmsg\":\"require subscribe\"}");
            IllegalStateException e=assertThrows(IllegalStateException.class,()->VxUtil.sendMessage("{}","fixture-app","fixture-secret"));
            assertTrue(e.getMessage().contains("43004"));
        }
    }
    @Test public void missingTokenFailsWithoutSending() {
        try(MockedStatic<HttpUtil> http=mockStatic(HttpUtil.class)) {
            http.when(()->HttpUtil.get(anyString())).thenReturn("{\"errcode\":40001}");
            assertThrows(IllegalStateException.class,()->VxUtil.sendMessage("{}","fixture-app","fixture-secret"));
            http.verify(()->HttpUtil.post(anyString(),anyString(),eq(java.nio.charset.StandardCharsets.UTF_8),anyInt(),eq(0)),never());
        }
    }
    @Test public void missingResultCodeIsNotSuccess() {
        try(MockedStatic<HttpUtil> http=mockStatic(HttpUtil.class)) {
            http.when(()->HttpUtil.get(anyString())).thenReturn("{\"access_token\":\"fixture-token\",\"expires_in\":7200}");
            http.when(()->HttpUtil.post(anyString(),anyString(),eq(java.nio.charset.StandardCharsets.UTF_8),anyInt(),eq(0))).thenReturn("{}");
            assertThrows(IllegalStateException.class,()->VxUtil.sendMessage("{}","fixture-app","fixture-secret"));
        }
    }
    @Test public void acceptedMessagesReuseValidToken() {
        try(MockedStatic<HttpUtil> http=mockStatic(HttpUtil.class)) {
            http.when(()->HttpUtil.get(anyString())).thenReturn("{\"access_token\":\"fixture-token\",\"expires_in\":7200}");
            http.when(()->HttpUtil.post(anyString(),anyString(),eq(java.nio.charset.StandardCharsets.UTF_8),anyInt(),eq(0))).thenReturn("{\"errcode\":0,\"errmsg\":\"ok\",\"msgid\":1}");
            VxUtil.sendMessage("{}","fixture-app","fixture-secret");
            VxUtil.sendMessage("{}","fixture-app","fixture-secret");
            http.verify(()->HttpUtil.get(anyString()),times(1));
        }
    }
}
