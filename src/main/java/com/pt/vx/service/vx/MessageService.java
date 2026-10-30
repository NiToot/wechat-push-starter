package com.pt.vx.service.vx;

import cn.hutool.json.JSONUtil;
import com.pt.vx.config.KeyConfig;
import com.pt.vx.config.MainConfig;
import com.pt.vx.config.WeatherConfig;
import com.pt.vx.config.WechatConfig;
import com.pt.vx.pojo.BirthDay;
import com.pt.vx.pojo.DataInfo;
import com.pt.vx.pojo.User;
import com.pt.vx.pojo.VxMessageDto;
import com.pt.vx.pojo.BaseWeather;
import com.pt.vx.pojo.KeyDTO;
import com.pt.vx.pojo.WeatherFuture;
import com.pt.vx.pojo.WeatherOtherInfo;
import com.pt.vx.service.api.ApiMessageService;
import com.pt.vx.service.weather.WeatherAdapter;
import com.pt.vx.service.weather.WeatherService;
import com.pt.vx.utils.DateUtil;
import com.pt.vx.utils.MessageLanguage;
import com.pt.vx.utils.ThreadPoolUtil;
import com.pt.vx.utils.VxUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MessageService {

    private VxMessageDto dto;

    private final WeatherService weatherService = new WeatherAdapter();

    private final SecureRandom random = new SecureRandom();

    private final ApiMessageService apiMessageService = new ApiMessageService();


    public void sendMessage(User user) {
        log.info("开始处理用户：{}", user.getUserName());
        if (Objects.equals(MainConfig.messageMode, 0) || Objects.isNull(dto)) {
            dto = new VxMessageDto();
            Map<String, DataInfo> context = buildMessageContext(user);
            dto.setTemplate_id(user.getTemplateId());
            dto.setData(context);
        }
        dto.setTouser(user.getVx());
        String message = JSONUtil.toJsonStr(dto);
        VxUtil.sendMessage(message, WechatConfig.VxAppId, WechatConfig.VxAppSecret);
    }

    private Map<String, DataInfo> buildMessageContext(User user) {
        ConcurrentHashMap<String, DataInfo> map = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> apiInfoList = buildApiInfoFuture(map, user);


        CompletableFuture<Void> weekFuture = buildWeekFuture(map);
        apiInfoList.add(weekFuture);

        CompletableFuture<Void> birthFuture = buildBirthFuture(map, user);
        apiInfoList.add(birthFuture);

        if (WeatherConfig.OPEN) {
            setMap(map, KeyConfig.KEY_WEATHER_INFO, MessageLanguage.text("天气暂时获取失败，出门前记得查看天气。", "Weather unavailable; check before going out."));
            setShortWeatherFallbacks(map);
            CompletableFuture<List<BaseWeather>> weatherFuture = buildWeatherFuture(map, user);
            CompletableFuture<Void> buildWeatherInfoFuture = buildWeatherOtherInfoFuture(weatherFuture,birthFuture,map);
            apiInfoList.add(buildWeatherInfoFuture);
        }

        CompletableFuture<Void> all = CompletableFuture.allOf(apiInfoList.toArray(new CompletableFuture[apiInfoList.size()]));
        try {
            all.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("消息组装被中断，取消发送", e);
        } catch (java.util.concurrent.ExecutionException e) {
            throw new IllegalStateException("消息字段组装失败，取消发送", e.getCause());
        }
        return map;
    }

    private List<CompletableFuture<Void>> buildApiInfoFuture(Map<String, DataInfo> map, User user) {
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        if (MainConfig.randomApiMessageMode) {
            CompletableFuture<Void> randomInfo = CompletableFuture.runAsync(() -> {
                String apiMessage = null;
                try {
                    KeyDTO randomKey = apiMessageService.getRandomKey();
                    apiMessage = apiMessageService.getApiMessage(randomKey, user);
                } catch (RuntimeException e) {
                    log.warn("随机文案获取失败，使用本地文案");
                }
                if (StringUtils.isBlank(apiMessage)) apiMessage = ApiMessageService.getLocalMessage();
                setMap(map, KeyConfig.KEY_RANDOM_INFO, apiMessage);

            }, ThreadPoolUtil.pool);
            futureList.add(randomInfo);
        } else {
            ApiMessageService.keyDTOS.stream().filter(KeyDTO::isOpen).forEach(keyDTO -> {
                CompletableFuture<Void> infoFuture = CompletableFuture.runAsync(() -> {
                    String apiMessage = apiMessageService.getApiMessage(keyDTO, user);
                    setMap(map, keyDTO, apiMessage);
                }, ThreadPoolUtil.pool);
                futureList.add(infoFuture);
            });
        }
        return futureList;
    }

    private CompletableFuture<Void> buildBirthFuture(Map<String, DataInfo> map, User user) {
        return CompletableFuture.runAsync(() -> {
            setMap(map, KeyConfig.KEY_USER_NAME, StringUtils.defaultIfBlank(user.getUserName(), MessageLanguage.text("朋友", "Friend")));
            BirthDay[] birthDays = user.getBirthDays();
            if (birthDays == null) return;
            for (int i = 0; i < birthDays.length; i++) {
                BirthDay birthDay = birthDays[i];
                boolean chineseFlag = birthDay.isChineseFlag();
                boolean countFlag = birthDay.isCountFlag();
                String day = null;
                if (countFlag) {
                    String passDayChinese = getPassDay(birthDay, chineseFlag);
                    day = passDayChinese;
                } else {
                    String nextChineseBirthDay = getNextBirthDay(birthDay, chineseFlag);
                    day = nextChineseBirthDay;
                    if ("0".equals(nextChineseBirthDay)) {
                        //倒计时额外信息
                        if (!Objects.equals(MainConfig.otherInfoMode, 0)) {
                            if (StringUtils.isNotBlank(birthDay.getInfo())) {
                                setMapExist(map, KeyConfig.KEY_OTHER_INFO, birthDay.getInfo());
                            }
                        }
                    }
                }
                setMap(map, KeyConfig.KEY_BIRTHDAY, day, i);
                if (i == 0) setMap(map, KeyConfig.KEY_BIRTHDAY_LEFT, day);
                if (i == 1) setMap(map, KeyConfig.KEY_PARTNER_BIRTHDAY_LEFT, day);
                if (i == 2) setMap(map, KeyConfig.KEY_LOVE_DAYS, day);
                if (i == 3) setMap(map, KeyConfig.KEY_ANNIVERSARY_LEFT, day);

            }
        }, ThreadPoolUtil.pool);
    }




    private CompletableFuture<Void> buildWeekFuture(Map<String, DataInfo> map) {
        return CompletableFuture.runAsync(() -> {
            LocalDate today = LocalDate.now();
            for (int i = 0; i < 5; i++) {
                LocalDate now = today.plusDays(i);
                String week = MessageLanguage.week(now);
                String date = now.toString();
                setMap(map, KeyConfig.KEY_DATE, date, i);
                setMap(map, KeyConfig.KEY_WEEK, week, i);
            }
        }, ThreadPoolUtil.pool);
    }

    private CompletableFuture<List<BaseWeather>> buildWeatherFuture(Map<String, DataInfo> map, User user) {
        return CompletableFuture.supplyAsync(() -> {
            List<BaseWeather> list = new ArrayList<>();
            try {
                StringBuilder summary = new StringBuilder();
                if (Objects.equals(0, WeatherConfig.getWeatherType)) {
                    //实时天气
                    BaseWeather weatherNow = weatherService.getWeatherNow(user.getAddress(), user.getCity());
                    summary.append(weatherLine(MessageLanguage.text("当前天气", "Current weather"), weatherNow));
                    setMap(map, KeyConfig.KEY_WEATHER_NOW, MessageLanguage.condition(weatherNow.getInfo()));
                    setMap(map, KeyConfig.KEY_HUMIDITY_NOW, weatherNow.getHumidity());
                    setMap(map, KeyConfig.KEY_TEMPERATURE_NOW, weatherNow.getTemperature());
                    setMap(map, KeyConfig.KEY_WIND_NOW, MessageLanguage.wind(weatherNow.getWind()));
                    setMap(map, KeyConfig.KEY_POWER_NOW, MessageLanguage.power(weatherNow.getPower()));
                    list.add(weatherNow);
                    //buildWeatherOtherInfo(map, weatherNow);
                } else {
                    //天气预报
                    List<WeatherFuture> weatherFutureList = weatherService.getWeatherFuture(user.getAddress(), user.getCity());
                    if (weatherFutureList == null || weatherFutureList.isEmpty()) throw new IllegalStateException("天气预报为空");
                    for (int i = 0; i < weatherFutureList.size(); i++) {
                        WeatherFuture weather = weatherFutureList.get(i);
                        LocalDate forecastDate = LocalDate.parse(weather.getDate());
                        String label = forecastDate.equals(LocalDate.now()) ? "今天" :
                                forecastDate.equals(LocalDate.now().plusDays(1)) ? "明天" : null;
                        if (label != null) {
                            if (summary.length() > 0) summary.append("\n");
                            summary.append(weatherLine(MessageLanguage.text(label + "白天", forecastDate.equals(LocalDate.now()) ? "Today (day)" : "Tomorrow (day)"), weather.getWeatherDay()));
                            summary.append("\n").append(weatherLine(MessageLanguage.text(label + "晚上", forecastDate.equals(LocalDate.now()) ? "Today (night)" : "Tomorrow (night)"), weather.getWeatherNight()));
                            boolean today = forecastDate.equals(LocalDate.now());
                            setMap(map, today ? KeyConfig.KEY_WEATHER_TODAY_DAY : KeyConfig.KEY_WEATHER_TOMORROW_DAY,
                                    shortWeatherValue(weather.getWeatherDay()));
                            setMap(map, today ? KeyConfig.KEY_WEATHER_TODAY_NIGHT : KeyConfig.KEY_WEATHER_TOMORROW_NIGHT,
                                    shortWeatherValue(weather.getWeatherNight()));
                        }
                        setMap(map, KeyConfig.KEY_POWER_DAY, MessageLanguage.power(weather.getWeatherDay().getPower()), i);
                        setMap(map, KeyConfig.KEY_TEMPERATURE_DAY, weather.getWeatherDay().getTemperature(), i);
                        setMap(map, KeyConfig.KEY_WEATHER_DAY, MessageLanguage.condition(weather.getWeatherDay().getInfo()), i);
                        setMap(map, KeyConfig.KEY_WIND_DAY, MessageLanguage.wind(weather.getWeatherDay().getWind()), i);
                        setMap(map, KeyConfig.KEY_POWER_DAY, MessageLanguage.power(weather.getWeatherDay().getPower()), i);

                        setMap(map, KeyConfig.KEY_POWER_NIGHT, MessageLanguage.power(weather.getWeatherNight().getPower()), i);
                        setMap(map, KeyConfig.KEY_TEMPERATURE_NIGHT, weather.getWeatherNight().getTemperature(), i);
                        setMap(map, KeyConfig.KEY_WEATHER_NIGHT, MessageLanguage.condition(weather.getWeatherNight().getInfo()), i);
                        setMap(map, KeyConfig.KEY_WIND_NIGHT, MessageLanguage.wind(weather.getWeatherNight().getWind()), i);
                        setMap(map, KeyConfig.KEY_POWER_NIGHT, MessageLanguage.power(weather.getWeatherNight().getPower()), i);

                        if (i == 0) {
                            setMap(map, KeyConfig.KEY_SUN_RISE, weather.getSunUp());
                            setMap(map, KeyConfig.KEY_SUN_SET, weather.getSunDown());

                            //  buildWeatherOtherInfo(map, weather.getWeatherDay());
                            //buildWeatherOtherInfo(map, weather.getWeatherNight());
                            list.add(weather.getWeatherDay());
                            list.add(weather.getWeatherNight());
                        }
                    }
                }
                if (summary.length() == 0) throw new IllegalStateException("缺少当前日期的天气");
                setMap(map, KeyConfig.KEY_WEATHER_INFO, summary.toString());
            } catch (Exception e) {
                list.clear();
                setShortWeatherFallbacks(map);
                log.warn("获取天气失败，保留天气提示：{}", e.getClass().getSimpleName());
            }

            return list;
        }, ThreadPoolUtil.pool);
    }

    private String weatherLine(String label, BaseWeather weather) {
        return label + MessageLanguage.text("：", ": ") + weatherValue(weather);
    }

    private String weatherValue(BaseWeather weather) {
        if (weather == null || StringUtils.isBlank(weather.getInfo()) || StringUtils.isBlank(weather.getTemperature())) {
            throw new IllegalStateException("天气字段不完整");
        }
        if (MessageLanguage.english()) {
            String value = MessageLanguage.condition(weather.getInfo()) + ", " + weather.getTemperature() + " Celsius";
            if (StringUtils.isNotBlank(weather.getWind())) {
                value += ", " + MessageLanguage.wind(weather.getWind()) + " wind";
                if (StringUtils.isNotBlank(weather.getPower())) value += " force " + MessageLanguage.power(weather.getPower());
            }
            return value;
        }
        String line = weather.getInfo() + "，" + weather.getTemperature() + "摄氏度";
        if (StringUtils.isNotBlank(weather.getWind())) {
            line += "，" + weather.getWind() + (weather.getWind().endsWith("风") ? "" : "风");
            if (StringUtils.isNotBlank(weather.getPower())) {
                line += weather.getPower() + (weather.getPower().endsWith("级") ? "" : "级");
            }
        }
        return line;
    }

    private String shortWeatherValue(BaseWeather weather) {
        String value = weatherValue(weather).replace('\n', ' ').replace('\r', ' ');
        if (MessageLanguage.english()) return StringUtils.abbreviate(value, 100);
        if (value.length() > 20) value = weather.getInfo() + "，" + weather.getTemperature() + "摄氏度";
        return StringUtils.abbreviate(value, 20);
    }

    private void setShortWeatherFallbacks(Map<String, DataInfo> map) {
        for (KeyDTO key : new KeyDTO[]{KeyConfig.KEY_WEATHER_TODAY_DAY, KeyConfig.KEY_WEATHER_TODAY_NIGHT,
                KeyConfig.KEY_WEATHER_TOMORROW_DAY, KeyConfig.KEY_WEATHER_TOMORROW_NIGHT}) {
            setMap(map, key, MessageLanguage.text("天气暂不可用", "Weather unavailable"));
        }
    }

    private CompletableFuture<Void> buildWeatherOtherInfoFuture(CompletableFuture<List<BaseWeather>> weatherFuture,CompletableFuture<Void> birthFuture,Map<String, DataInfo> map) {
        return weatherFuture.thenAcceptBothAsync(birthFuture, (list, x) -> {
            if (Objects.equals(MainConfig.otherInfoMode, 0)) {
                return;
            }

            if (Objects.equals(MainConfig.otherInfoMode, 1) && map.containsKey(KeyConfig.KEY_OTHER_INFO.getKey())) {
                return;
            }

            list.forEach(weather -> buildWeatherOtherInfo(map,weather));

        }, ThreadPoolUtil.pool);
    }

    /**
     * 构造天气的额外消息
     *
     * @param map
     * @param weather
     */
    private void buildWeatherOtherInfo(Map<String, DataInfo> map, BaseWeather weather) {
        for (WeatherOtherInfo weatherOtherInfo : MainConfig.weatherOtherInfos) {
            String key = weatherOtherInfo.getKey();
            if (StringUtils.isBlank(key)) {
                continue;
            }
            if (Objects.equals(weatherOtherInfo.getType(), 0)) {
                int temperatureKey = Integer.parseInt(key.substring(1));
                    double temperatureNow;
                    try { temperatureNow = Double.parseDouble(weather.getTemperature()); }
                    catch (RuntimeException e) { continue; }
                boolean isGreater = key.startsWith(">") && temperatureNow > temperatureKey;
                boolean isLess = key.startsWith("<") && temperatureNow < temperatureKey;
                boolean isEqual = key.startsWith("=") && temperatureNow == temperatureKey;
                if (isGreater || isLess || isEqual) {
                    setMapExist(map, KeyConfig.KEY_OTHER_INFO, MessageLanguage.advisory(weatherOtherInfo.getMessage()));
                }
            } else if (Objects.equals(weatherOtherInfo.getType(), 1)) {
                String info = weather.getInfo();
                if (info.contains(key)) {
                    setMapExist(map, KeyConfig.KEY_OTHER_INFO, MessageLanguage.advisory(weatherOtherInfo.getMessage()));
                }
            }

        }
    }

    private String getNextBirthDay(BirthDay birthDay, boolean chineseFlag) {
        if (chineseFlag) {
            return DateUtil.getNextChineseBirthDay(birthDay.getMonth(), birthDay.getDay());
        } else {
            return DateUtil.getNextBirthDay(birthDay.getMonth(), birthDay.getDay());
        }
    }

    private String getPassDay(BirthDay birthDay, boolean chineseFlag) {
        if (chineseFlag) {
            return DateUtil.passDayChinese(birthDay.getYear(), birthDay.getMonth(), birthDay.getDay());
        } else {
            return DateUtil.passDay(birthDay.getYear(), birthDay.getMonth(), birthDay.getDay());
        }
    }


    private void setMapExist(Map<String, DataInfo> map, KeyDTO dto, String value) {
        setMapExist(map, dto, value, null);
    }

    private void setMapExist(Map<String, DataInfo> map, KeyDTO dto, String value, Integer id) {
        String key = id == null || id == 0 ? dto.getKey() : dto.getKey() + id;
        DataInfo dataInfo = map.get(key);
        if (Objects.nonNull(dataInfo)) {
            StringBuilder previous = new StringBuilder(dataInfo.getValue());
            for (int part = 1; ; part++) {
                String partKey = key + "Split" + (part == 1 ? "" : part - 1);
                DataInfo chunk = map.get(partKey);
                if (chunk == null) break;
                previous.append(chunk.getValue());
            }
            String newValue = previous.toString();
            if (newValue.contains(value)) {
                return;
            }
            newValue += "\n" + value;
            setMap(map, dto, newValue, id);
        } else {
            setMap(map, dto, value, id);
        }
    }

    private void setMap(Map<String, DataInfo> map, KeyDTO dto, String value) {
        setMap(map, dto, value, null);
    }

    private void setMap(Map<String, DataInfo> map, KeyDTO dto, String value, Integer id) {
        if (StringUtils.isNotBlank(value) && dto.isOpen()) {
            String key = Objects.isNull(id) || Objects.equals(id, 0) ? dto.getKey() : dto.getKey() + id;
            // A replacement must also remove the previous value's trailing chunks.
            map.keySet().removeIf(existing -> existing.equals(key) || existing.matches(java.util.regex.Pattern.quote(key) + "Split[0-9]*"));
            String color = this.getColor(dto.getColor());
            int chunkSize = MainConfig.keyMessageSplit ? 100 : value.length();
            for (int start = 0, part = 0; start < value.length(); start += chunkSize, part++) {
                String partKey = part == 0 ? key : key + "Split" + (part == 1 ? "" : part - 1);
                DataInfo dataInfo = new DataInfo();
                dataInfo.setColor(color);
                dataInfo.setValue(value.substring(start, Math.min(start + chunkSize, value.length())));
                map.put(partKey, dataInfo);
            }
        }
    }

    /**
     * 随机颜色
     *
     * @param color
     * @return
     */
    private String getColor(String color) {
        if (MainConfig.randomMessageColorMode) {
            int i = random.nextInt(MainConfig.randomColors.length);
            return MainConfig.randomColors[i];
        } else {
            return color;
        }
    }
}
