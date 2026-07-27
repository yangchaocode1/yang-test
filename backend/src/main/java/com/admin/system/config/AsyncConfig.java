package com.admin.system.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 日志异步线程池 - 用于操作日志、审计日志等IO密集型任务
     * <p>
     * 配置策略：
     * - IO密集型任务：核心线程数 = CPU核心数 * 2
     * - 队列容量适中，避免OOM
     * - 拒绝策略：调用者运行（保证日志不丢失）
     * - 优雅关闭：等待任务完成再关闭
     */
    @Bean(name = "logExecutor")
    public Executor logExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor() {
            @Override
            public void execute(Runnable task) {
                SecurityContext context = SecurityContextHolder.getContext();
                super.execute(() -> {
                    SecurityContextHolder.setContext(context);
                    try {
                        task.run();
                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                });
            }
        };
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors() * 2);
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 4);
        executor.setQueueCapacity(500);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("log-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        log.info("日志异步线程池初始化完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        return executor;
    }

    /**
     * 默认异步线程池 - 用于通用异步任务
     * <p>
     * 配置策略：
     * - 通用任务：核心线程数 = CPU核心数
     * - 队列容量较小，快速消费
     * - 拒绝策略：中止并抛出异常，便于发现问题
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor() {
            @Override
            public void execute(Runnable task) {
                SecurityContext context = SecurityContextHolder.getContext();
                super.execute(() -> {
                    SecurityContextHolder.setContext(context);
                    try {
                        task.run();
                    } finally {
                        SecurityContextHolder.clearContext();
                    }
                });
            }
        };
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("task-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        log.info("通用异步线程池初始化完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        return executor;
    }
}
