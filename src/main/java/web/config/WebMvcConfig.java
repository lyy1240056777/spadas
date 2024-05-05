package web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import web.Utils.FileProperties;
import web.interceptor.AuthenticationInterceptor;

/**
 * @author Tian Qi Qing
 * @version 1.0
 * @date 2022/02/28/23:08
 **/
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    //@Autowired ?????
    private final FileProperties fileProperties;

    @Autowired
    public WebMvcConfig(FileProperties fileProperties) {
        this.fileProperties = fileProperties;
    }

    static final String ORIGINS[] = new String[]{"GET", "POST", "PUT", "DELETE"};

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // ?????????????????????
                .allowedOriginPatterns("*") // ????????????? ???localhost??????????????????????????localhost?127.0.0.1????
                .allowCredentials(true) // ??????????
                .allowedMethods(ORIGINS) // ????????????????
                .maxAge(3600); // ???????1??? ???????
    }
    /**
     * ??????
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(fileProperties.getStaticUri() + "**")
                .addResourceLocations("file:" + fileProperties.getBaseUri());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthenticationInterceptor())
                .addPathPatterns("/spadas/api/buy");
    }
}
