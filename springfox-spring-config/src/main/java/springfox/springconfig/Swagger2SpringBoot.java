/*
 *
 *  Copyright 2015 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package springfox.springconfig;

import com.fasterxml.classmate.TypeResolver;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.async.DeferredResult;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ApiKeyVehicle;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import springfox.petstore.controller.PetController;

import java.util.List;

import static com.google.common.collect.Lists.*;
import static springfox.documentation.schema.AlternateTypeRules.*;

@SpringBootApplication
@EnableSwagger2//<1>
@ComponentScan(basePackageClasses = {
    PetController.class
})//<2>
public class Swagger2SpringBoot {

  public static void main(String[] args) {
    ApplicationContext ctx = SpringApplication.run(Swagger2SpringBoot.class, args);
  }


  @Bean
  public Docket petApi() {
    return new Docket(DocumentationType.SWAGGER_2)//<3>
        .select()//<4>
          .apis(RequestHandlerSelectors.any())//<5>
          .paths(PathSelectors.any())//<6>
          .build()//<7>
        .pathMapping("/")//<8>
        .directModelSubstitute(LocalDate.class,
            String.class)//<9>
        .genericModelSubstitutes(ResponseEntity.class)
        .alternateTypeRules(
            newRule(typeResolver.resolve(DeferredResult.class,
                    typeResolver.resolve(ResponseEntity.class, WildcardType.class)),
                typeResolver.resolve(WildcardType.class)))//<10>
        .useDefaultResponseMessages(false)//<11>
        .globalResponseMessage(RequestMethod.GET,//<12>
            newArrayList(new ResponseMessageBuilder()
                .code(500)
                .message("500 message")
                .responseModel(new ModelRef("Error"))//<13>
                .build()))
        .securitySchemes(newArrayList(apiKey()))//<14>
        .securityContexts(newArrayList(securityContext()))//<15>
        .enableUrlTemplating(true)//<21>
        .globalOperationParameters(//<22>
            newArrayList(new ParameterBuilder() 
                .name("someGlobalParameter")
                .description("Description of someGlobalParameter")
                .modelRef(new ModelRef("string"))
                .parameterType("query")
                .required(true)
                .build()))
        ;
  }

  @Autowired
  private TypeResolver typeResolver;

  private ApiKey apiKey() {
    return new ApiKey("mykey", "api_key", "header");//<16>
  }

  private SecurityContext securityContext() {
    return SecurityContext.builder()
        .securityReferences(defaultAuth())
        .forPaths(PathSelectors.regex("/anyPath.*"))//<17>
        .build();
  }

  List<SecurityReference> defaultAuth() {
    AuthorizationScope authorizationScope
        = new AuthorizationScope("global", "accessEverything");
    AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
    authorizationScopes[0] = authorizationScope;
    return newArrayList(
        new SecurityReference("mykey", authorizationScopes));//<18>
  }

  @Bean
  SecurityConfiguration security() {
    return new SecurityConfiguration(//<19>
        "test-app-client-id",
        "test-app-client-secret",
        "test-app-realm",
        "test-app",
        "apiKey",
        ApiKeyVehicle.HEADER, //<23>
        "api_key", //<24>
        "," /*scope separator*/);
  }

  @Bean
  UiConfiguration uiConfig() {
    return new UiConfiguration(//<20>
        "validatorUrl",// url
        "none",       // docExpansion          => none | list
        "alpha",      // apiSorter             => alpha
        "schema",     // defaultModelRendering => schema
        false,        // enableJsonEditor      => true | false
        true);        // showRequestHeaders    => true | false
  }
}
