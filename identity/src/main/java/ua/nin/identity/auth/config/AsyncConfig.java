package ua.nin.identity.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name = "appAsyncExecutor")
    public AsyncTaskExecutor appAsyncExecutor() {
        SimpleAsyncTaskExecutor ex = new SimpleAsyncTaskExecutor("async-");
        ex.setVirtualThreads(true);
        return ex;
    }

    @Override
    public Executor getAsyncExecutor() {
        return appAsyncExecutor();
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
                log.error("Async error in {}: {}. {}", method, ex, ex.getMessage());
    }
}