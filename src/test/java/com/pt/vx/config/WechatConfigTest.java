package com.pt.vx.config;
import com.pt.vx.OfflineTest;
import com.pt.vx.pojo.User;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class WechatConfigTest extends OfflineTest {
    private User user(){User u=new User();u.setVx("fixture-openid");u.setTemplateId("fixture-template");u.setUserName("示例用户");u.setAddress("示例省示例市示例区");u.setCity("示例市");return u;}
    @Test public void invalidLanguageFailsBeforeCredentialsOrNetwork() {
        String previous = MainConfig.messageLanguage;
        try {
            for (String invalid : new String[]{null, "", "EN", "fr"}) {
                MainConfig.messageLanguage = invalid;
                IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> WechatConfig.validateValues("", "", "", Collections.emptyList()));
                assertTrue(error.getMessage().contains("messageLanguage"));
            }
        } finally { MainConfig.messageLanguage = previous; }
    }
    @Test public void acceptsCompleteConfiguration(){WechatConfig.validateValues("fixture-app","fixture-secret","fixture-key",Collections.singletonList(user()));}
    @Test public void missingAppIdFailsBeforeNetwork(){IllegalArgumentException e=assertThrows(IllegalArgumentException.class,()->WechatConfig.validateValues("","secret","key",Collections.singletonList(user())));assertTrue(e.getMessage().contains("WECHAT_APP_ID"));}
    @Test public void missingSecretFailsBeforeNetwork(){assertThrows(IllegalArgumentException.class,()->WechatConfig.validateValues("app"," ","key",Collections.singletonList(user())));}
    @Test public void missingWeatherKeyFailsBeforeNetwork(){assertThrows(IllegalArgumentException.class,()->WechatConfig.validateValues("app","secret","",Collections.singletonList(user())));}
    @Test public void emptyRecipientsFailBeforeNetwork(){assertThrows(IllegalArgumentException.class,()->WechatConfig.validateValues("app","secret","key",Collections.emptyList()));}
    @Test public void blankOpenIdFailsBeforeNetwork(){User u=user();u.setVx("");assertThrows(IllegalArgumentException.class,()->WechatConfig.validateValues("app","secret","key",Collections.singletonList(u)));}
    @Test public void blankTemplateFailsBeforeNetwork(){User u=user();u.setTemplateId("");assertThrows(IllegalArgumentException.class,()->WechatConfig.validateValues("app","secret","key",Collections.singletonList(u)));}
    @Test public void blankRegionFailsBeforeNetwork(){User u=user();u.setAddress("");assertThrows(IllegalArgumentException.class,()->WechatConfig.validateValues("app","secret","key",Collections.singletonList(u)));}
    @Test public void duplicateRecipientFailsBeforeNetwork(){assertThrows(IllegalArgumentException.class,()->WechatConfig.validateValues("app","secret","key",Arrays.asList(user(),user())));}
    @Test public void invalidDateFailsBeforeNetwork(){User u=user();u.setBirthDays(new com.pt.vx.pojo.BirthDay[]{new com.pt.vx.pojo.BirthDay(2000,13,1,false,false)});assertThrows(IllegalArgumentException.class,()->WechatConfig.validateValues("app","secret","key",Collections.singletonList(u)));}
}