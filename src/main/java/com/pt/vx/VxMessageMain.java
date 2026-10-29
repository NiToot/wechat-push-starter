package com.pt.vx;


import cn.hutool.core.collection.CollectionUtil;
import com.pt.vx.config.WechatConfig;
import com.pt.vx.pojo.User;
import com.pt.vx.service.vx.MessageService;
import com.pt.vx.utils.ThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.TimeZone;

/**
 * github action对私有项目每个月有2000分钟的免费额度
 */
@Slf4j
public class  VxMessageMain {

    public static void main(String[] args) {
        long star = System.currentTimeMillis();
        try {

            TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
            System.setProperty("sun.net.http.retryPost", "false");
            WechatConfig.validate();
            if (args.length == 1 && "--check-config".equals(args[0])) {
                System.out.println("配置检查通过；未调用外部接口，不发送消息。");
                return;
            }
            if (args.length > 0) throw new IllegalArgumentException("仅支持无参数发送或 --check-config 检查配置");
            MessageService messageService = new MessageService();
            List<User> userList =   WechatConfig.userList;
            int failed = 0;
            int succeeded = 0;
            if(CollectionUtil.isNotEmpty(userList)){
                for (int i = 0; i < userList.size(); i++) {
                    User user = userList.get(i);
                    try {
                        messageService.sendMessage(user);
                        succeeded++;
                        log.info("收件人 {}：微信接口已接受消息", user.getUserName());
                    } catch (RuntimeException e) {
                        failed++;
                        log.error("收件人 {} 发送失败：{}", user.getUserName(), e.getMessage());
                    }
                    if(i < userList.size() - 1){
                        try {
                            Thread.sleep(1234);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new IllegalStateException("发送任务被中断", e);
                        }
                    }
                }
            }
            log.info("推送汇总：接口接受 {} 人，失败 {} 人", succeeded, failed);
            if (failed > 0) throw new IllegalStateException("推送失败人数：" + failed);
        }finally {
            ThreadPoolUtil.pool.shutdown();
            long end = System.currentTimeMillis();
            log.info("程序结束，耗时：{} ms",end-star);
        }
    }
}
