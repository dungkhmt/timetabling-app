package openerp.openerpresourceserver.wms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @version 1.0
 * @description: ThreadPoolConfig
 * @author: dung.nguyendinh
 * @date: 4/16/25 2:41 PM
 */
@Configuration
@EnableAsync
public class ThreadPoolConfig {
    @Bean(name = "customExecutor")
    public ThreadPoolTaskExecutor customExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);  // Minimum number of active threads
        executor.setMaxPoolSize(20);  // Maximum number of threads the pool can grow to
        executor.setQueueCapacity(100);  // Queue size for tasks waiting to be executed
        executor.setKeepAliveSeconds(60);  // Time idle threads remain alive before termination
        executor.setThreadNamePrefix("AsyncExecutor-");  // Naming pattern for worker threads
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
