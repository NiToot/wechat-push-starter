package com.pt.vx.utils;

import com.pt.vx.config.MainConfig;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

/** Local presentation only; keep original weather data for advisory rules. */
public final class MessageLanguage {
    private MessageLanguage() { }
    public static boolean english() {
        if (!"zh".equals(MainConfig.messageLanguage) && !"en".equals(MainConfig.messageLanguage))
            throw new IllegalArgumentException("messageLanguage must be zh or en");
        return "en".equals(MainConfig.messageLanguage);
    }
    public static String text(String zh, String en) { return english() ? en : zh; }
    public static String week(LocalDate date) {
        return english() ? date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                : DateUtil.getWeek(date, MainConfig.chineseWeek);
    }
    private static final Map<String, String> CONDITIONS = new HashMap<>();
    static {
        String[][] pairs = {
            {"晴", "Sunny"}, {"少云", "Mostly sunny"}, {"晴间多云", "Partly cloudy"},
            {"多云", "Cloudy"}, {"阴", "Overcast"}, {"有风", "Windy"}, {"平静", "Calm"},
            {"微风", "Light breeze"}, {"和风", "Moderate breeze"}, {"清风", "Fresh breeze"},
            {"强风/劲风", "Strong breeze"}, {"疾风", "Near gale"}, {"大风", "Gale"},
            {"烈风", "Strong gale"}, {"风暴", "Storm"}, {"狂爆风", "Violent storm"},
            {"飓风", "Hurricane"}, {"热带风暴", "Tropical storm"},
            {"霾", "Haze"}, {"中度霾", "Moderate haze"}, {"重度霾", "Heavy haze"}, {"严重霾", "Severe haze"},
            {"阵雨", "Showers"}, {"雷阵雨", "Thunderstorms"}, {"雷阵雨并伴有冰雹", "Thunderstorms with hail"},
            {"小雨", "Light rain"}, {"中雨", "Moderate rain"}, {"大雨", "Heavy rain"},
            {"暴雨", "Torrential rain"}, {"大暴雨", "Severe torrential rain"}, {"特大暴雨", "Extreme torrential rain"},
            {"强阵雨", "Heavy showers"}, {"强雷阵雨", "Severe thunderstorms"}, {"极端降雨", "Extreme rainfall"},
            {"毛毛雨/细雨", "Drizzle"}, {"雨", "Rain"}, {"小雨-中雨", "Light to moderate rain"},
            {"中雨-大雨", "Moderate to heavy rain"}, {"大雨-暴雨", "Heavy to torrential rain"},
            {"暴雨-大暴雨", "Torrential to severe torrential rain"},
            {"大暴雨-特大暴雨", "Severe to extreme torrential rain"},
            {"雨雪天气", "Rain and snow"}, {"雨夹雪", "Sleet"}, {"阵雨夹雪", "Sleet showers"}, {"冻雨", "Freezing rain"},
            {"雪", "Snow"}, {"阵雪", "Snow showers"}, {"小雪", "Light snow"}, {"中雪", "Moderate snow"},
            {"大雪", "Heavy snow"}, {"暴雪", "Blizzard"}, {"小雪-中雪", "Light to moderate snow"},
            {"中雪-大雪", "Moderate to heavy snow"}, {"大雪-暴雪", "Heavy snow to blizzard"},
            {"浮尘", "Suspended dust"}, {"扬沙", "Blowing sand"}, {"沙尘暴", "Sandstorm"},
            {"强沙尘暴", "Severe sandstorm"}, {"龙卷风", "Tornado"}, {"雾", "Fog"},
            {"浓雾", "Dense fog"}, {"强浓雾", "Very dense fog"}, {"轻雾", "Mist"},
            {"大雾", "Heavy fog"}, {"特强浓雾", "Extremely dense fog"},
            {"热", "Hot"}, {"冷", "Cold"}, {"未知", "Unknown conditions"}
        };
        for (String[] pair : pairs) CONDITIONS.put(pair[0], pair[1]);
    }
    public static String condition(String value) {
        return english() ? CONDITIONS.getOrDefault(value, "Unknown conditions") : value;
    }
    public static String wind(String value) {
        if (!english()) return value;
        if (value == null) return "Unspecified";
        switch (value.replace("风", "").trim()) {
            case "东北": return "NE"; case "东": return "E"; case "东南": return "SE";
            case "南": return "S"; case "西南": return "SW"; case "西": return "W";
            case "西北": return "NW"; case "北": return "N";
            case "无向": return "Calm"; case "旋转不定": return "Variable";
            default: return "Unspecified";
        }
    }
    public static String power(String value) {
        if (!english() || value == null) return value;
        String normalized = value.replace("级", "").replace("≤", "<=").trim();
        return normalized.matches("[0-9<>=. -]+") ? normalized : "unspecified";
    }
    public static String advisory(String value) {
        if (!english()) return value;
        switch (value) {
            case "天气天气有点小冷，注意保暖哦~": return "It is cold. Keep warm.";
            case "天气热起来咯，不要穿太多咯~": return "It is hot. Dress lightly.";
            case "出门记得带伞哦！": return "Remember your umbrella.";
            default: return value; // User-authored notes must be configured in the desired language.
        }
    }
}
