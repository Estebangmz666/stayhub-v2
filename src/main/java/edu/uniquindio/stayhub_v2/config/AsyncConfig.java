package edu.uniquindio.stayhub_v2.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration class for enabling asynchronous processing in the application.
 *
 * <p>This configuration activates Spring's asynchronous execution capabilities,
 * allowing methods annotated with {@link org.springframework.scheduling.annotation.Async}
 * to be executed in separate threads.</p>
 *
 * <p><b>Key Features Enabled:</b></p>
 * <ul>
 *   <li>Asynchronous event listeners for non-blocking operations</li>
 *   <li>Parallel execution of independent tasks</li>
 *   <li>Improved response times for HTTP endpoints</li>
 *   <li>Better resource utilization for I/O bound operations</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * @Async
 * public void processLongRunningTask() {
 *     // This method will execute in a separate thread
 * }
 *
 * @Async
 * @EventListener
 * public void handleAsyncEvent(SomeEvent event) {
 *     // Event processing won't block the main thread
 * }
 * }</pre>
 *
 * <p><b>Default Behavior:</b></p>
 * <ul>
 *   <li>Uses Spring's default SimpleAsyncTaskExecutor</li>
 *   <li>Creates a new thread for each async execution</li>
 *   <li>Threads are not pooled by default</li>
 *   <li>Suitable for development and low-volume scenarios</li>
 * </ul>
 *
 * @author Esteban Gómez León
 * @version 1.0
 * @since 1.0
 * @see org.springframework.scheduling.annotation.EnableAsync
 * @see org.springframework.scheduling.annotation.Async
 * @see org.springframework.context.annotation.Configuration
 */
@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
          ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
          executor.setCorePoolSize(5);
          executor.setMaxPoolSize(10);
          executor.setQueueCapacity(100);
          executor.setThreadNamePrefix("async-");
          executor.initialize();
          return executor;
    }
}