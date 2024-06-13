package it.gov.pagopa.bizeventsservice.repository.redis;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


@Component
public class RedisRepository {

  @Autowired
  @Qualifier("object")
  private RedisTemplate<String, byte[]> redisTemplateObj;

  public void save(String key, byte[] value, long ttl) {
    redisTemplateObj.opsForValue().set(key, value, Duration.ofMinutes(ttl));
  }

  public byte[] get(String key) {
    return redisTemplateObj.opsForValue().get(key);
  }

  public void remove(String key) {
    redisTemplateObj.delete(key);
  }
}
