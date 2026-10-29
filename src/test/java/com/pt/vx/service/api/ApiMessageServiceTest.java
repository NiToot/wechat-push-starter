package com.pt.vx.service.api;

import com.pt.vx.OfflineTest;
import com.pt.vx.config.KeyConfig;
import com.pt.vx.pojo.User;
import com.pt.vx.utils.HttpUtil;
import org.junit.Test;
import org.mockito.MockedStatic;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

public class ApiMessageServiceTest extends OfflineTest {
    @Test public void defaultRandomSourceIsPoetry() {
        assertEquals("poem", new ApiMessageService().getRandomKey().getKey());
    }
    @Test public void validPoemIsReturnedWithoutTruncation() {
        try(MockedStatic<HttpUtil> http=mockStatic(HttpUtil.class)) {
            http.when(()->HttpUtil.get("https://v1.jinrishici.com/all.json"))
                .thenReturn("{\"content\":\"海上生明月，天涯共此时。\",\"author\":\"张九龄\",\"origin\":\"望月怀远\"}");
            assertEquals("海上生明月，天涯共此时。",new ApiMessageService().getApiMessage(new com.pt.vx.pojo.KeyDTO("poem","#FFFFFF",true),new User()));
        }
    }
    @Test public void invalidPoemsUseShortLocalPoetry() {
        String[] invalid={"{}","not json","{\"content\":\"海上生明月，天涯共此时。\"}",
            "{\"content\":\""+String.join("",java.util.Collections.nCopies(21,"长"))+"\",\"author\":\"作者\",\"origin\":\"题名\"}"};
        try(MockedStatic<HttpUtil> http=mockStatic(HttpUtil.class)) {
            for(String body:invalid) {
                http.when(()->HttpUtil.get(anyString())).thenReturn(body);
                String result=new ApiMessageService().getApiMessage(new com.pt.vx.pojo.KeyDTO("poem","#FFFFFF",true),new User());
                assertTrue(java.util.Arrays.asList("海上生明月，天涯共此时。","愿君多采撷，此物最相思。","明月松间照，清泉石上流。").contains(result));
                assertTrue(result.length()<=20);
            }
        }
    }
    @Test public void remoteFailureUsesLocalPoem() {
        try(MockedStatic<HttpUtil> http=mockStatic(HttpUtil.class)) {
            http.when(()->HttpUtil.get(anyString())).thenThrow(new IllegalStateException("fixture timeout"));
            String result=new ApiMessageService().getApiMessage(KeyConfig.KEY_POEM,new User());
            assertTrue(result.length()<=20);assertFalse(result.isEmpty());
        }
    }
}