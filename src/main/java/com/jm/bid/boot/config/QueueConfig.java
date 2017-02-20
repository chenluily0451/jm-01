package com.jm.bid.boot.config;

import com.yimei.cloudservice.queue.ali.AliQueueProvider;
import com.yimei.cloudservice.queue.api.Queue;
import com.yimei.cloudservice.queue.api.QueueProvider;
import com.yimei.cloudservice.queue.file.FileQueueProvider;
import com.yimei.cloudservice.queue.redis.RedisQueueProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Created by xl on 16/6/20.
 */

@Configuration
public class QueueConfig {
    private static final Logger logger = LoggerFactory.getLogger(QueueConfig.class);

    @Value("${notification.mailQueue}")
    private String queueName;

    // 生产与采用aliyun队列
    @Profile("aliqueue")
    static class ProfileAliQueueProvider {
        @Value("${aliyun.mns.Endpoint}")
        private String endPoint;

        @Value("${aliyun.mns.AccessId}")
        private String accessId;

        @Value("${aliyun.mns.AccessKey}")
        private String accessKey;

        @Bean
        QueueProvider queueProvider() {
            return new AliQueueProvider(endPoint, accessId, accessKey);
        }
    }

    // 生产与采用aliyun队列
    @Profile("redisqueue")
    static class ProfileRedisQueueProvider {
        @Value("${redisqueue.hostname}")
        private String redisHost;

        @Value("${redisqueue.port}")
        private int redisPort;

        @Bean
        QueueProvider queueProvider() {
            return new RedisQueueProvider(redisHost, redisPort);
        }
    }

    // 其他环境采用文件队列
    @Profile("filequeue")
    static class ProfileFileQueueProvider {

        @Value("${filequeue.root}")
        private String root;

        @Bean
        public QueueProvider queueProvider() {
            return new FileQueueProvider(root);
        }
    }

    @Bean
    public Queue getQueue(QueueProvider queueProvider){
        return queueProvider.getQueue(queueName);
    }
}
