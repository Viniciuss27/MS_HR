package vinix.kafka.consumer;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import vinix.exceptions.ResourceNotFoundException;

@Configuration
public class KafkaConsumerConfig {

  @Bean
  DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
    var recoverer = new DeadLetterPublishingRecoverer(template, (record, ex)
            -> new TopicPartition(record.topic() + ".DLT", record.partition()));
    var backOff = new FixedBackOff(1000l, 3l);// 3 retries de 1s em 1s

    DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
    handler.addNotRetryableExceptions(ResourceNotFoundException.class);

    return handler;
  }
}
