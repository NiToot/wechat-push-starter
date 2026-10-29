package com.pt.vx.config;

import com.pt.vx.pojo.KeyDTO;

/**
 * 所有内置的模板
 * new KeyDTO("模板key", "颜色", 是否开启);
 * 在微信模板里添加 {{模板key.DATA}}
 */
public class KeyConfig {
    // 语义明确的新模板字段；birthDay 系列保留兼容原有模板。
    public static final KeyDTO KEY_LOVE_DAYS = new KeyDTO("loveDays", "#C0392B", true);
    public static final KeyDTO KEY_ANNIVERSARY_LEFT = new KeyDTO("anniversaryLeft", "#C0392B", true);
    public static final KeyDTO KEY_BIRTHDAY_LEFT = new KeyDTO("birthdayLeft", "#C0392B", true);
    public static final KeyDTO KEY_PARTNER_BIRTHDAY_LEFT = new KeyDTO("partnerBirthdayLeft", "#C0392B", true);
    public static final KeyDTO KEY_WEATHER_INFO = new KeyDTO("weatherInfo", "#1565C0", true);
    public static final KeyDTO KEY_WEATHER_TODAY_DAY = new KeyDTO("weatherTodayDay", "#1565C0", true);
    public static final KeyDTO KEY_WEATHER_TODAY_NIGHT = new KeyDTO("weatherTodayNight", "#1565C0", true);
    public static final KeyDTO KEY_WEATHER_TOMORROW_DAY = new KeyDTO("weatherTomorrowDay", "#1565C0", true);
    public static final KeyDTO KEY_WEATHER_TOMORROW_NIGHT = new KeyDTO("weatherTomorrowNight", "#1565C0", true);
    /**
     * 昵称，在模板里面就是{{userName.DATA}}
     */
    public static final KeyDTO KEY_USER_NAME = new KeyDTO("userName", "#C0392B", true);

    /**
     * 日期计算
     */
    public static final KeyDTO KEY_BIRTHDAY = new KeyDTO("birthDay","#C0392B",true);
    /**
     * 额外提示
     */
    public static final KeyDTO KEY_OTHER_INFO = new KeyDTO("otherInfo","#333333",true);

    /**
     * 日期
     */
    public static final KeyDTO KEY_DATE = new KeyDTO("date","#616161",true);
    /**
     * 星期
     */
    public static final KeyDTO KEY_WEEK = new KeyDTO("week","#616161",true);
    /**
     * 白天天气
     */
    public static final KeyDTO KEY_WEATHER_DAY = new KeyDTO("weatherDay","#1565C0",true);
    /**
     * 白天温度
     */
    public static final KeyDTO KEY_TEMPERATURE_DAY = new KeyDTO("temperatureDay","#1565C0",true);
    /**
     * 白天风向
     */
    public static final KeyDTO KEY_WIND_DAY = new KeyDTO("windDay","#1565C0",true);
    /**
     * 白天风力
     */
    public static final KeyDTO KEY_POWER_DAY = new KeyDTO("powerDay","#1565C0",true);

    /**
     * 晚上天气
     */
    public static final KeyDTO KEY_WEATHER_NIGHT = new KeyDTO("weatherNight","#1565C0",true);
    /**
     * 晚上温度
     */
    public static final KeyDTO KEY_TEMPERATURE_NIGHT = new KeyDTO("temperatureNight","#1565C0",true);

    /**
     * 晚上风向
     */
    public static final KeyDTO KEY_WIND_NIGHT = new KeyDTO("windNight","#1565C0",true);

    /**
     * 晚上风力
     */
    public static final KeyDTO KEY_POWER_NIGHT = new KeyDTO("powerNight","#1565C0",true);
    /**
     * 现在天气
     */
    public static final KeyDTO KEY_WEATHER_NOW = new KeyDTO("weatherNow","#1565C0",true);
    /**
     * 实时天气
     */
    public static final KeyDTO KEY_TEMPERATURE_NOW = new KeyDTO("temperatureNow","#1565C0",true);
    /**
     * 现在风向
     */
    public static final KeyDTO KEY_WIND_NOW = new KeyDTO("windNow","#1565C0",true);
    /**
     * 现在风力
     */
    public static final KeyDTO KEY_POWER_NOW = new KeyDTO("powerNow","#1565C0",true);
    /**
     * 现在湿度
     */
    public static final KeyDTO KEY_HUMIDITY_NOW = new KeyDTO("humidityNow","#1565C0",true);
    /**
     * 日出时间
     * 只有和风天气才有
     */
    public static final KeyDTO KEY_SUN_RISE = new KeyDTO("sunrise","#1565C0",true);

    /**
     * 日落时间
     * 只有和风天气才有
     */
    public static final KeyDTO KEY_SUN_SET = new KeyDTO("sunset","#1565C0",true);

    /**
     * 随机API消息
     */
    public static final KeyDTO KEY_RANDOM_INFO = new KeyDTO("randomInfo","#2E7D32",true);

    /**
     * 情话
     */
    public static final KeyDTO KEY_QING_HUA = new KeyDTO("qingHua","#333333",false);
    public static final KeyDTO KEY_POEM = new KeyDTO("poem","#2E7D32",true);

    /**
     * 随机段子
     */
    public static final KeyDTO KEY_DUAN_ZI = new KeyDTO("duanZi","#333333",false);

    /**
     * 随机毒鸡汤
     */
    public static final KeyDTO KEY_DU_JI_TANG = new KeyDTO("duJiTang","#333333",false);

    /**
     * 随机短句
     */
    public static final KeyDTO KEY_SENTENCE= new KeyDTO("sentence","#333333",false);

    /**
     * 谜语
     */
    public static final KeyDTO KEY_MI_YU= new KeyDTO("miyu","#333333",false);

    /**
     * 星座解析
     * 用户信息填写了BirthDay的话，则会自动解析星座
     */
    public static final KeyDTO KEY_HOROSCOPE= new KeyDTO("horoscope","#333333",false);

    /**
     * 历史的今天发生的3件事情
     */
    public static final KeyDTO KEY_HISTORY_TODAY= new KeyDTO("historyToday","#333333",false);

    /**
     * 新冠
     */
    public static final KeyDTO KEY_XIN_GUAN= new KeyDTO("xinGuan","#333333",false);




}
