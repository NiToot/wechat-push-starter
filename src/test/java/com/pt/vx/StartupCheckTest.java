package com.pt.vx;
import com.pt.vx.config.WechatConfig;
import com.pt.vx.service.vx.MessageService;
import org.junit.*;
import org.mockito.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
public class StartupCheckTest extends OfflineTest {
    @After public void restorePool(){com.pt.vx.utils.ThreadPoolUtil.pool=new java.util.concurrent.ThreadPoolExecutor(3,5,1,java.util.concurrent.TimeUnit.SECONDS,new java.util.concurrent.ArrayBlockingQueue<>(100));}
    @Test public void checkConfigModeNeverConstructsSendingService(){
        try(MockedStatic<WechatConfig> config=mockStatic(WechatConfig.class);MockedConstruction<MessageService> sends=mockConstruction(MessageService.class)){
            VxMessageMain.main(new String[]{"--check-config"});config.verify(WechatConfig::validate);assertEquals(0,sends.constructed().size());
        }
    }
    @Test public void invalidConfigStopsBeforeConstructingSendingService(){
        try(MockedStatic<WechatConfig> config=mockStatic(WechatConfig.class);MockedConstruction<MessageService> sends=mockConstruction(MessageService.class)){
            config.when(WechatConfig::validate).thenThrow(new IllegalArgumentException("fixture missing value"));
            assertThrows(IllegalArgumentException.class,()->VxMessageMain.main(new String[0]));assertEquals(0,sends.constructed().size());
        }
    }
}