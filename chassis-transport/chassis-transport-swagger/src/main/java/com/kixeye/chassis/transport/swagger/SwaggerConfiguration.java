package com.kixeye.chassis.transport.swagger;

/*
 * #%L
 * Chassis Transport Swagger
 * %%
 * Copyright (C) 2014 KIXEYE, Inc
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fasterxml.classmate.ResolvedType;
import com.google.common.base.Preconditions;
import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.models.alternates.AlternateTypeRule;
import com.mangofactory.swagger.plugin.EnableSwagger;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;
import com.wordnik.swagger.model.ApiInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import java.util.concurrent.Future;

/**
 * Configures Swagger.
 *
 * @author ebahtijaragic
 */
@Configuration
@EnableSwagger
@ComponentScan(basePackageClasses = SwaggerConfiguration.class)
public class SwaggerConfiguration {

    @Autowired
    private SpringSwaggerConfig springSwaggerConfig;

    @Value("${app.version:unknown}")
    private String appVersion;

    @Bean
    public SwaggerSpringMvcPlugin swaggerSpringMvcPlugin() {
        return new SwaggerSpringMvcPlugin(springSwaggerConfig)
                .alternateTypeRules(
                        new GenericAlternateTypeRule(ResponseEntity.class, 0, null, null),
                        new GenericAlternateTypeRule(DeferredResult.class, 0, null, null),
                        new GenericAlternateTypeRule(Future.class, 0, null, null),
                        new GenericAlternateTypeRule(scala.concurrent.Future.class, 0, null, null))
                .apiVersion(appVersion)
                .apiInfo(new ApiInfo(null, null, null, null, null, null));
    }

    public static class GenericAlternateTypeRule extends AlternateTypeRule {

        private Class<?> genericType;
        private int boundTypeIndex;

        public GenericAlternateTypeRule(Class<?> genericType, int boundTypeIndex, ResolvedType original, ResolvedType alternate) {
            super(original, alternate);
            Preconditions.checkNotNull(genericType, "genericType cannot be null");
            Preconditions.checkArgument(boundTypeIndex >=0, "boundTypeIndex must be >= 0");

            this.genericType = genericType;
            this.boundTypeIndex = boundTypeIndex;
        }

        @Override
        public ResolvedType alternateFor(ResolvedType type) {
            return type.getTypeBindings().getBoundType(boundTypeIndex);
        }

        @Override
        public boolean appliesTo(ResolvedType type) {
            return type.getErasedType() == genericType && !type.getTypeBindings().isEmpty() && boundTypeIndex <= type.getTypeBindings().size() - 1;
        }


    }
}
