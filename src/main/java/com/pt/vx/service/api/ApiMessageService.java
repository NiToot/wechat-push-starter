package com.pt.vx.service.api;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.pt.vx.config.KeyConfig;
import com.pt.vx.pojo.KeyDTO;
import com.pt.vx.pojo.User;
import com.pt.vx.utils.HttpUtil;
import com.pt.vx.utils.MessageLanguage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.security.SecureRandom;
import java.util.*;

/** 通用版仅保留无需账号密钥的公开古诗接口。 */
@Slf4j
public class ApiMessageService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String[] LOCAL_MESSAGES = {
        "海上生明月，天涯共此时。", "愿君多采撷，此物最相思。", "明月松间照，清泉石上流。"
    };
    // Original English micro-poems; no translation API or third-party credentials required.
    private static final String[] ENGLISH_MESSAGES = {
        "Dawn lifts the mist; a quiet hope takes wing.",
        "Rain leaves the leaves; the morning learns to shine.",
        "Across the dusk, one window keeps a little sun."
    };
    public static List<KeyDTO> keyDTOS = new ArrayList<>(Collections.singletonList(KeyConfig.KEY_POEM));
    public static String getLocalMessage() {
        String[] messages = MessageLanguage.english() ? ENGLISH_MESSAGES : LOCAL_MESSAGES;
        return messages[RANDOM.nextInt(messages.length)];
    }
    public KeyDTO getRandomKey() { return KeyConfig.KEY_POEM.isOpen() ? KeyConfig.KEY_POEM : null; }
    public String getApiMessage(KeyDTO key, User user) {
        if (MessageLanguage.english()) return getLocalMessage();
        if (key == null || !KeyConfig.KEY_POEM.equalsKey(key)) return getLocalMessage();
        try {
            JSONObject poem = JSONUtil.parseObj(HttpUtil.get("https://v1.jinrishici.com/all.json"));
            String content = poem.getStr("content");
            if (StringUtils.isBlank(content) || content.length() > 20
                    || content.contains("\n") || content.contains("\r")
                    || StringUtils.isBlank(poem.getStr("author"))
                    || StringUtils.isBlank(poem.getStr("origin"))) return getLocalMessage();
            return content;
        } catch (RuntimeException e) {
            log.warn("诗句接口不可用，使用本地短诗");
            return getLocalMessage();
        }
    }
}