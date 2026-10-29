package com.pt.vx.service.weather;

import com.pt.vx.OfflineTest;
import com.pt.vx.pojo.WeatherFuture;
import com.pt.vx.utils.HttpUtil;
import org.junit.Test;
import org.mockito.MockedStatic;
import java.util.List;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

public class GaoDeWeatherServiceTest extends OfflineTest {
    private GaoDeWeatherService service() {
        return new GaoDeWeatherService() {
            @Override public String getCityCode(String address, String city) { return "510184"; }
        };
    }
    @Test(timeout=5000) public void parsesOfficialForecastsArrayAndKeepsEveryDay() throws Exception {
        String json = "{\"status\":\"1\",\"infocode\":\"10000\",\"forecasts\":[{\"casts\":["
            + "{\"date\":\"2026-10-28\",\"week\":\"3\",\"dayweather\":\"晴\",\"daytemp\":\"22\",\"daywind\":\"北\",\"daypower\":\"≤3\",\"nightweather\":\"多云\",\"nighttemp\":\"12\",\"nightwind\":\"北\",\"nightpower\":\"≤3\"},"
            + "{\"date\":\"2026-10-29\",\"week\":\"4\",\"dayweather\":\"雨\",\"daytemp\":\"18\",\"daywind\":\"南\",\"daypower\":\"4\",\"nightweather\":\"阴\",\"nighttemp\":\"10\",\"nightwind\":\"南\",\"nightpower\":\"3\"}]}]}";
        try (MockedStatic<HttpUtil> http = mockStatic(HttpUtil.class)) {
            http.when(() -> HttpUtil.get(anyString(), anyMap())).thenReturn(json);
            List<WeatherFuture> result = service().getWeatherFuture("fixture-address", "fixture-city");
            assertEquals(2, result.size());
            assertEquals("晴", result.get(0).getWeatherDay().getInfo());
            assertEquals("12", result.get(0).getWeatherNight().getTemperature());
            assertEquals("2026-10-29", result.get(1).getDate());
            assertEquals("雨", result.get(1).getWeatherDay().getInfo());
        }
    }
    @Test public void rejectsMissingForecastsInsteadOfTreatingItAsSuccess() throws Exception {
        try (MockedStatic<HttpUtil> http = mockStatic(HttpUtil.class)) {
            http.when(() -> HttpUtil.get(anyString(), anyMap())).thenReturn("{\"status\":\"1\",\"forecasts\":[]}");
            Exception error = assertThrows(Exception.class, () -> service().getWeatherFuture("a", "c"));
            assertTrue(error.getMessage().contains("预报"));
        }
    }
}
