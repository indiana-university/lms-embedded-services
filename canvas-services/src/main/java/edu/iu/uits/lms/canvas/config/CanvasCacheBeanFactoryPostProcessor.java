package edu.iu.uits.lms.canvas.config;

/*-
 * #%L
 * LMS Canvas Services
 * %%
 * Copyright (C) 2015 - 2024 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile({"ehcache", "redis-cache"})
@Slf4j
@Component
public class CanvasCacheBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    protected boolean isThereAPrimaryCacheManagerDefinedAlready(ConfigurableListableBeanFactory beanFactory) {
        String[] cacheManagerBeanNames = beanFactory.getBeanNamesForType(CacheManager.class);

        boolean isThereAPrimaryCacheManager = false;

        for (String cacheManagerBeanName : cacheManagerBeanNames) {
            isThereAPrimaryCacheManager = beanFactory.findAnnotationOnBean(cacheManagerBeanName, Primary.class) != null;

            if (isThereAPrimaryCacheManager) {
                break;
            }
        }

        return isThereAPrimaryCacheManager;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (! isThereAPrimaryCacheManagerDefinedAlready(beanFactory)) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition("CanvasServicesCacheManager");
            beanDefinition.setPrimary(true);

            log.info("No Primary Cache Managers are set as Primary. Setting CanvasServicesCacheManager as Primary");
        }
    }
}