package com.pt.vx.service.vx;
import com.pt.vx.OfflineTest;
import com.pt.vx.config.KeyConfig;
import com.pt.vx.pojo.*;
import com.pt.vx.service.api.ApiMessageService;
import com.pt.vx.service.weather.WeatherService;
import java.lang.reflect.*;
import java.time.LocalDate;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
public class PortableMessageTest extends OfflineTest {
    private Map<String,DataInfo> build(User user)throws Exception{
        MessageService service=new MessageService();
        WeatherService weather=mock(WeatherService.class);
        when(weather.getWeatherFuture(anyString(),anyString())).thenThrow(new Exception("offline fixture"));
        ApiMessageService poem=mock(ApiMessageService.class);
        when(poem.getRandomKey()).thenReturn(KeyConfig.KEY_POEM);
        when(poem.getApiMessage(any(),any())).thenReturn("海上生明月，天涯共此时。");
        for(Object[] entry:new Object[][]{{"weatherService",weather},{"apiMessageService",poem}}){Field f=MessageService.class.getDeclaredField((String)entry[0]);f.setAccessible(true);f.set(service,entry[1]);}
        Method method=MessageService.class.getDeclaredMethod("buildMessageContext",User.class);method.setAccessible(true);
        return (Map<String,DataInfo>)method.invoke(service,user);
    }
    private User user(BirthDay... dates){User u=new User();u.setUserName("示例用户");u.setAddress("示例地区");u.setCity("示例市");u.setBirthDays(dates);return u;}
    @Test public void zeroDatesStillProvideAllBasicTemplateFields()throws Exception{
        Map<String,DataInfo> data=build(user());
        for(String key:new String[]{"userName","date","week","randomInfo","weatherTodayDay","weatherTodayNight","weatherTomorrowDay","weatherTomorrowNight"})assertNotNull(data.get(key));
        assertFalse(data.containsKey("birthdayLeft"));assertEquals(LocalDate.now().toString(),data.get("date").getValue());
    }
    @Test public void birthdayProfileAddsOwnCountdown()throws Exception{assertNotNull(build(user(new BirthDay(1995,6,19,false,false))).get("birthdayLeft"));}
    @Test public void coupleProfileMapsDatesAndTemplatesKeepAllFields()throws Exception{
        User basic=user(); User birthday=user(new BirthDay(1995,6,19,false,false));
        User couple=user(new BirthDay(1995,6,19,false,false),new BirthDay(1996,1,10,true,false),new BirthDay(2020,1,1,false,true),new BirthDay(2020,1,1,false,false));
        int index=0;
        for(User profile:Arrays.asList(basic,birthday,couple)){
            Map<String,DataInfo> data=build(profile);
            String name=new String[]{"basic","birthday","couple"}[index++];
            String template=new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("templates",name+".txt")),java.nio.charset.StandardCharsets.UTF_8);
            java.util.regex.Matcher m=java.util.regex.Pattern.compile("\\{\\{([A-Za-z0-9]+)\\.DATA\\}\\}").matcher(template);
            while(m.find())assertNotNull("Missing field "+m.group(1),data.get(m.group(1)));
            assertTrue(template.indexOf("{{randomInfo.DATA}}")<template.indexOf("{{weatherTodayDay.DATA}}"));
            assertFalse(template.contains("{{otherInfo"));
        }
    }
    @Test public void invalidDateCannotProduceAPartialPayload(){InvocationTargetException e=assertThrows(InvocationTargetException.class,()->build(user(new BirthDay(2000,13,1,false,false))));assertTrue(e.getCause() instanceof IllegalStateException);}
}