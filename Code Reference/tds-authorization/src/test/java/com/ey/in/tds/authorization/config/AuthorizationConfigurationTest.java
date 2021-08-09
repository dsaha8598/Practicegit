package com.ey.in.tds.authorization.config;

import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import com.ey.in.tds.AuthorizationApplication;

public class AuthorizationConfigurationTest {

	private final Logger logger = LoggerFactory.getLogger(AuthorizationApplication.class);

	@Test
	public void jedisConnectionFactoryTest() {
		try {
			RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(
					"ci-tfo-dv-redis001.redis.cache.windows.net", 6379);
			configuration.setPassword("GIyb4o2pNITDq7sfN12XniIkpkdlJxCU4y8VbsSETIQ=");
			JedisClientConfiguration jedisClientConfiguration = JedisClientConfiguration.builder().useSsl().build();
			JedisConnectionFactory factory = new JedisConnectionFactory(configuration, jedisClientConfiguration);
			factory.afterPropertiesSet();
			RedisConnection redisConnection = factory.getConnection();
			redisConnection.set("test".getBytes(), "test-value".getBytes());
			Assert.assertEquals("test-value", new String(redisConnection.get("test".getBytes(StandardCharsets.UTF_8))));
		} catch (Exception e) {
			logger.error("Exception occurred :", e);
			Assert.fail("Exception while communicating with Redis");
		}
	}
}