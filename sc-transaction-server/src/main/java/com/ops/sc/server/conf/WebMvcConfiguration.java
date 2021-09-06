package com.ops.sc.server.conf;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.ops.sc.server.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.BufferedImageHttpMessageConverter;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


@EnableWebMvc
@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {

    @Value("${skip.deploy:true}")
    private Boolean skipDeploy;

    @Value(value = "${auth.url:#{null}}")
    private String authUrl;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (skipDeploy) {
            registry.addInterceptor(getAuthenticationInterceptor()).addPathPatterns("/sc*");
        }
    }

    @Bean
    public AuthInterceptor getAuthenticationInterceptor() {
        return new AuthInterceptor(authUrl);
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(getByteArrayHttpMessageConverter());
        converters.add(getBufferedImageHttpMessageConverter());
        converters.add(getStringHttpMessageConverter());
        converters.add(getFastJsonHttpMessageConverter());
    }

    /**
     * spring消息转换器
     *
     * @return
     */
    @Bean
    public ByteArrayHttpMessageConverter getByteArrayHttpMessageConverter() {
        return new ByteArrayHttpMessageConverter();
    }

    /**
     * spring消息转换器
     *
     * @return
     */
    @Bean
    public BufferedImageHttpMessageConverter getBufferedImageHttpMessageConverter() {
        return new BufferedImageHttpMessageConverter();
    }

    /**
     * 解决@Responcebody中文乱码问题
     *
     * @return
     */
    @Bean
    public StringHttpMessageConverter getStringHttpMessageConverter() {
        return new StringHttpMessageConverter(Charset.forName("UTF-8"));
    }

    @Bean
    public FastJsonConfig getFastJsonConfig() {
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setCharset(Charset.forName("UTF-8"));
        fastJsonConfig.setSerializerFeatures(SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.WriteDateUseDateFormat, SerializerFeature.PrettyFormat,
                SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.WriteNullListAsEmpty, SerializerFeature.DisableCircularReferenceDetect);
        return fastJsonConfig;
    }


    @Bean
    public FastJsonHttpMessageConverter getFastJsonHttpMessageConverter() {
        List<MediaType> mediaTypeList = new ArrayList<>();
        mediaTypeList.add(new MediaType(MediaType.TEXT_HTML, Charset.forName("UTF-8")));
        mediaTypeList.add(MediaType.APPLICATION_JSON);
        mediaTypeList.add(new MediaType("application", "vnd.spring-boot.actuator.v2+json"));

        FastJsonHttpMessageConverter fastJsonHttpMessageConverter = new FastJsonHttpMessageConverter();
        fastJsonHttpMessageConverter.setDefaultCharset(Charset.forName("UTF-8"));
        fastJsonHttpMessageConverter.setSupportedMediaTypes(mediaTypeList);
        fastJsonHttpMessageConverter.setFastJsonConfig(getFastJsonConfig());
        return fastJsonHttpMessageConverter;
    }

}
