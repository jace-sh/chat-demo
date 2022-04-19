package jace.shim.chatdemo.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.lettuce.core.ClientOptions
import io.lettuce.core.SocketOptions
import io.lettuce.core.cluster.ClusterClientOptions
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions
import io.lettuce.core.resource.ClientResources
import io.lettuce.core.resource.DefaultClientResources
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisClusterConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration


@Configuration
@EnableConfigurationProperties(RedisProperties::class)
class RedisConfig {
    @Bean
    fun reactiveRedisTemplate(
        redisConnectionFactory: ReactiveRedisConnectionFactory,
        objectMapper: ObjectMapper,
    ): ReactiveRedisTemplate<String, Any> {

        val stringSerializer = StringRedisSerializer()
        val jacksonSerializer = GenericJackson2JsonRedisSerializer(objectMapper)
        val serializationContext = RedisSerializationContext
            .newSerializationContext<String, Any>()
            .key(stringSerializer)
            .value(jacksonSerializer)
            .hashKey(stringSerializer)
            .hashValue(jacksonSerializer)
            .build()

        return ReactiveRedisTemplate(redisConnectionFactory, serializationContext)
    }

    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
    }

    @Bean
    fun lettuceClientResources(): DefaultClientResources? {
        return DefaultClientResources.create()
    }

    @Bean
    fun redisConnectionFactory(
        redisProperties: RedisProperties,
        clientResources: ClientResources,
    ): ReactiveRedisConnectionFactory {
        val configuration = RedisClusterConfiguration(
            redisProperties.cluster.nodes
        )
        val clientConfiguration = lettuceClientConfiguration(clientResources)
        val factory = LettuceConnectionFactory(configuration, clientConfiguration)
        factory.validateConnection = false // true인 경우 command 실행시마다 sync로 작성하여 성능저하 원인이 됨 (async, non-blocking환경에서)
        factory.afterPropertiesSet()

        return factory
    }

    private fun lettuceClientConfiguration(clientResources: ClientResources): LettuceClientConfiguration {
        val socketOptions = SocketOptions.builder()
            .connectTimeout(Duration.ofSeconds(2))
            .build()
        val topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
            .enablePeriodicRefresh(Duration.ofSeconds(30))
            .enableAllAdaptiveRefreshTriggers()
            .build()
        val clientOptions: ClientOptions = ClusterClientOptions.builder()
            .socketOptions(socketOptions)
            .topologyRefreshOptions(topologyRefreshOptions)
            .build()
        return LettuceClientConfiguration.builder()
            .clientResources(clientResources)
            .clientOptions(clientOptions)
            .commandTimeout(Duration.ofSeconds(3))
            .build()
    }
}