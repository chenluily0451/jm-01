package com.jm.bid.boot.config;

import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Predicate;
import com.jm.bid.admin.account.dto.AdminDTO;
import com.jm.bid.boot.annotation.CurrentAdmin;
import com.jm.bid.user.account.dto.UserDTO;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.schema.AlternateTypeRules.newRule;

/**
 * Created by xiangyang on 16/7/4.
 */
@Configuration
@EnableSwagger2
@ComponentScan(basePackages = "com.jm.bid.user.account.controller")
public class SwaggerConfiguration {

    @Autowired
    private TypeResolver typeResolver;

    @Bean
    public Docket adminApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("admin-restful-api")
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(or(adminOnlyEndpoints()))
                .build()
                .apiInfo(adminApiInfo())
                .pathMapping("/")
                .directModelSubstitute(LocalDate.class, String.class)
                .directModelSubstitute(LocalDateTime.class, String.class)
                .genericModelSubstitutes(ResponseEntity.class)
                .ignoredParameterTypes(CurrentAdmin.class, AdminDTO.class)
                .alternateTypeRules(
                        newRule(typeResolver.resolve(DeferredResult.class,
                                typeResolver.resolve(ResponseEntity.class, WildcardType.class)),
                                typeResolver.resolve(WildcardType.class)))
                .useDefaultResponseMessages(false)
                .tags(
                        new Tag("jm-admin-login", "admin登陆相关"),
                        new Tag("jm-admin-tender", "admin发布竞价相关"),
                        new Tag("jm-admin-customer", "admin审核客户相关"),
                        new Tag("jm-admin-accountSetting", "用户账户设置相关"),
                        new Tag("jm-admin-trading", "admin退款设置相关")
                );
    }

    @Bean
    public Docket userApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("user-restful-api")
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(userOnlyEndpoints())
                .build()
                .apiInfo(userApiInfo())
                .pathMapping("/")
                .directModelSubstitute(LocalDate.class, String.class)
                .directModelSubstitute(LocalDateTime.class, String.class)
                .genericModelSubstitutes(ResponseEntity.class)
                .ignoredParameterTypes(UserDTO.class)
                .alternateTypeRules(
                        newRule(typeResolver.resolve(DeferredResult.class,
                                typeResolver.resolve(ResponseEntity.class, WildcardType.class)),
                                typeResolver.resolve(WildcardType.class)))
                .useDefaultResponseMessages(false)
                .tags(
                        new Tag("jm-user-login", "用户登陆相关"),
                        new Tag("jm-user-register", "用户注册相关"),
                        new Tag("jm-user-regainPwd", "用户找回密码相关"),
                        new Tag("jm-user-accountSetting", "用户账户设置相关"),
                        new Tag("jm-user-account", "用户功能相关"),
                        new Tag("jm-finance-cashAccount", "资金账户相关"),
                        new Tag("jm-finance-asset", "资产-绑定银行卡相关"),
                        new Tag("jm-user-bid", "报价相关")
                );
    }


    private Predicate<String> adminOnlyEndpoints() {
        return new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return input.contains("/admin");
            }
        };
    }


    private Predicate<String> userOnlyEndpoints() {
        return new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return !input.contains("/admin");
            }
        };
    }


    private ApiInfo userApiInfo() {
        return new ApiInfoBuilder()
                .title("晋煤前台用户接口文档")
                .description("后端路由地址&RESTFUL API地址")
                .termsOfServiceUrl("zhangxiangyang@yimei180.com")
                .version("1.0")
                .build();
    }

    private ApiInfo adminApiInfo() {
        return new ApiInfoBuilder()
                .title("晋煤admin用户接口文档")
                .description("后端路由地址&RESTFUL API地址")
                .termsOfServiceUrl("zhangxiangyang@yimei180.com")
                .version("1.0")
                .build();
    }
}
