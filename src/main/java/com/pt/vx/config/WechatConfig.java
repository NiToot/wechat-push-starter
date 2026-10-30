package com.pt.vx.config;

import com.pt.vx.pojo.BirthDay;
import com.pt.vx.pojo.User;
import java.util.ArrayList;
import java.util.List;

/** 新账号只需填写此文件中的收件人资料；密钥通过环境变量传入。 */
public class WechatConfig {
    public static final String VxAppId = env("WECHAT_APP_ID");
    public static final String VxAppSecret = env("WECHAT_APP_SECRET");
    public static final List<User> userList = new ArrayList<>();

    static {
        userList.add(getUser(
            "", // 1. 关注本测试号后生成的 OpenID
            "", // 2. 在自己的测试号后台创建的模板 ID
            "", // 3. 昵称
            "", // 4. 详细地区：省、市、区县
            ""  // 5. 城市名称
            // 默认不计算生日，直接使用 templates/basic.txt。
            // 需要生日时，在城市参数后加逗号，再添加：
            // new BirthDay(1995, 6, 19, false, false) // 虚构示例：公历生日
        ));
        // 多人发送：复制上面的 userList.add(...)，每人填写自己的 OpenID 和模板 ID。
    }

    public static void validate() {
        validateValues(VxAppId, VxAppSecret, WeatherConfig.weatherSourceKey, userList);
    }

    static void validateValues(String appId, String secret, String weatherKey, List<User> users) {
        com.pt.vx.utils.MessageLanguage.english(); // Validate before any network or send.
        require(appId, "WECHAT_APP_ID");
        require(secret, "WECHAT_APP_SECRET");
        if (WeatherConfig.OPEN) require(weatherKey, "AMAP_KEY");
        if (users == null || users.isEmpty()) throw new IllegalArgumentException("至少配置一位收件人");
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (user == null) throw new IllegalArgumentException("收件人配置不能为空");
            require(user.getVx(), "收件人" + (i + 1) + "的OpenID");
            require(user.getTemplateId(), "收件人" + (i + 1) + "的模板ID");
            if (!seen.add(user.getVx())) throw new IllegalArgumentException("收件人OpenID重复，请检查复制的配置");
            if (user.getBirthDays() != null) for (BirthDay date : user.getBirthDays()) {
                try {
                    if (date == null) throw new IllegalArgumentException("空日期");
                    if (date.isChineseFlag()) {
                        if (date.getYear() < 1900 || date.getYear() > 2099 || date.getMonth() < 1 || date.getMonth() > 12 || date.getDay() < 1 || date.getDay() > 30)
                            throw new IllegalArgumentException("农历日期超出范围");
                        cn.hutool.core.date.ChineseDate lunar = new cn.hutool.core.date.ChineseDate(date.getYear(), date.getMonth(), date.getDay(), false);
                        java.time.LocalDate converted = java.time.LocalDate.of(lunar.getGregorianYear(), lunar.getGregorianMonthBase1(), lunar.getGregorianDay());
                        cn.hutool.core.date.ChineseDate checked = new cn.hutool.core.date.ChineseDate(converted);
                        if (checked.getChineseYear() != date.getYear() || checked.getMonth() != date.getMonth() || checked.getDay() != date.getDay() || checked.isLeapMonth())
                            throw new IllegalArgumentException("该农历日期不存在");
                    } else java.time.LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
                } catch (RuntimeException e) {
                    throw new IllegalArgumentException("收件人" + (i + 1) + "的日期无效，请检查年月日和公历/农历设置", e);
                }
            }
            if (WeatherConfig.OPEN) {
                require(user.getAddress(), "收件人" + (i + 1) + "的详细地区");
                require(user.getCity(), "收件人" + (i + 1) + "的城市");
            }
        }
    }

    private static void require(String value, String name) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("请填写配置：" + name);
    }

    private static String env(String name) {
        String value = System.getenv(name);
        return value == null ? "" : value.trim();
    }

    private static User getUser(String openId, String templateId, String name,
                                String address, String city, BirthDay... dates) {
        User user = new User();
        user.setVx(openId); user.setTemplateId(templateId); user.setUserName(name);
        user.setAddress(address); user.setCity(city); user.setBirthDays(dates);
        return user;
    }
}