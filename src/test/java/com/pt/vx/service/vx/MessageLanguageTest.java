package com.pt.vx.service.vx;

import com.pt.vx.OfflineTest;
import com.pt.vx.config.*;
import com.pt.vx.pojo.*;
import com.pt.vx.service.api.ApiMessageService;
import com.pt.vx.service.weather.WeatherService;
import java.lang.reflect.*;
import java.time.LocalDate;
import java.util.*;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

public class MessageLanguageTest extends OfflineTest {
    private void language(String value) throws Exception {
        MainConfig.messageLanguage = value;
    }
    @After public void resetLanguage() throws Exception {
        language("zh");
    }
    private Map<String, DataInfo> build(boolean failWeather) throws Exception {
        MessageService service = new MessageService();
        WeatherService weather = mock(WeatherService.class);
        if (failWeather) when(weather.getWeatherFuture(anyString(), anyString())).thenThrow(new Exception("offline fixture"));
        else {
            List<WeatherFuture> forecasts = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                WeatherFuture forecast = new WeatherFuture();
                forecast.setDate(LocalDate.now().plusDays(i).toString());
                BaseWeather value = new BaseWeather();
                value.setInfo(i == 0 ? "晴" : "雷阵雨并伴有冰雹");
                value.setTemperature(i == 0 ? "22" : "-3");
                value.setWind("东北"); value.setPower("1-3");
                forecast.setWeatherDay(value); forecast.setWeatherNight(value);
                forecasts.add(forecast);
            }
            when(weather.getWeatherFuture(anyString(), anyString())).thenReturn(forecasts);
        }
        Field field = MessageService.class.getDeclaredField("weatherService");
        field.setAccessible(true); field.set(service, weather);
        if (!com.pt.vx.utils.MessageLanguage.english()) {
            ApiMessageService poem = mock(ApiMessageService.class);
            when(poem.getRandomKey()).thenReturn(KeyConfig.KEY_POEM);
            when(poem.getApiMessage(any(), any())).thenReturn("海上生明月，天涯共此时。");
            Field source = MessageService.class.getDeclaredField("apiMessageService");
            source.setAccessible(true); source.set(service, poem);
        }
        User user = new User(); user.setUserName("Alex"); user.setAddress("fixture"); user.setCity("fixture");
        user.setBirthDays(new BirthDay[0]);
        Method build = MessageService.class.getDeclaredMethod("buildMessageContext", User.class);
        build.setAccessible(true);
        return (Map<String, DataInfo>) build.invoke(service, user);
    }
    @Test public void chinesePayloadKeepsExistingOutput() throws Exception {
        language("zh");
        Map<String, DataInfo> data = build(false);
        assertEquals("晴，22摄氏度，东北风1-3级", data.get("weatherTodayDay").getValue());
        assertEquals("海上生明月，天涯共此时。", data.get("randomInfo").getValue());
        assertEquals(com.pt.vx.utils.DateUtil.getWeek(LocalDate.now(), MainConfig.chineseWeek), data.get("week").getValue());
        assertEquals("天气暂不可用", build(true).get("weatherTodayDay").getValue());
    }
    @Test public void allWeekdaysUseEnglishRegardlessOfChineseWeekOption() throws Exception {
        language("en");
        String[] expected = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (int i = 0; i < 7; i++) assertEquals(expected[i], com.pt.vx.utils.MessageLanguage.week(LocalDate.of(2030, 5, 20).plusDays(i)));
    }
    @Test public void unusualConditionsAndWindHaveSafeEnglishFallbacks() throws Exception {
        language("en");
        assertEquals("Unknown conditions", com.pt.vx.utils.MessageLanguage.condition("新天气现象"));
        assertEquals("Variable", com.pt.vx.utils.MessageLanguage.wind("旋转不定"));
        assertEquals("Calm", com.pt.vx.utils.MessageLanguage.wind("无风向"));
        assertEquals("NW", com.pt.vx.utils.MessageLanguage.wind("西北风"));
        assertEquals("Unspecified", com.pt.vx.utils.MessageLanguage.wind("未知"));
        assertEquals("<=3", com.pt.vx.utils.MessageLanguage.power("≤3级"));
        assertEquals("unspecified", com.pt.vx.utils.MessageLanguage.power("未知"));
        assertEquals("Light to moderate rain", com.pt.vx.utils.MessageLanguage.condition("小雨-中雨"));
    }
    @Test public void englishPayloadIncludesCompleteWeatherAndWeekday() throws Exception {
        language("en");
        Map<String, DataInfo> data = build(false);
        assertEquals(LocalDate.now().getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, Locale.ENGLISH), data.get("week").getValue());
        assertEquals("Sunny, 22 Celsius, NE wind force 1-3", data.get("weatherTodayDay").getValue());
        assertEquals("Thunderstorms with hail, -3 Celsius, NE wind force 1-3", data.get("weatherTomorrowNight").getValue());
        for (DataInfo value : data.values()) assertFalse("Chinese leaked: " + value.getValue(), value.getValue().matches("(?s).*[\\p{IsHan}].*"));
    }
    @Test public void englishFailureIsReadableAndPoemStaysEnglish() throws Exception {
        language("en");
        Map<String, DataInfo> data = build(true);
        assertEquals("Weather unavailable", data.get("weatherTodayDay").getValue());
        assertEquals("Weather unavailable", data.get("weatherTomorrowNight").getValue());
        assertFalse(data.get("randomInfo").getValue().isEmpty());
        assertTrue(data.get("randomInfo").getValue().length() <= 100);
        assertTrue(data.get("randomInfo").getValue().matches("[\\x20-\\x7E]+"));
    }
    @Test public void englishPoetryWorksWithoutAnyNetwork() throws Exception {
        language("en");
        Set<String> poems = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            String poem = new ApiMessageService().getApiMessage(KeyConfig.KEY_POEM, new User());
            assertTrue(poem.matches("[\\x20-\\x7E]+")); poems.add(poem);
        }
        assertTrue("Expected a random pool", poems.size() > 1);
    }
}
