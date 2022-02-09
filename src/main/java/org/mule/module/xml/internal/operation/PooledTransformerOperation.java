/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.internal.operation;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.mule.module.xml.api.EntityExpansion.NEVER;
import static org.mule.runtime.core.api.util.ExceptionUtils.extractOfType;

import org.mule.module.xml.api.EntityExpansion;
import org.mule.module.xml.internal.error.TransformationException;
import org.mule.module.xml.internal.util.LocalEntityResolver;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.lifecycle.Startable;
import org.mule.runtime.api.lifecycle.Stoppable;
import org.mule.runtime.core.api.util.func.CheckedFunction;
import org.mule.runtime.core.api.util.xmlsecurity.XMLSecureFactories;
import org.mule.runtime.extension.api.annotation.param.ConfigOverride;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.time.Duration;
import java.util.Optional;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PoolUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.xml.sax.EntityResolver;

/**
 * Base class for operations which pool transformers or resources in order to increase performance by
 * pre compiling transformers and other resources which are expensive to create
 *
 * @param <K> the type of the pool keys
 * @param <T> the type of the pool values
 * @since 1.0
 */
public abstract class PooledTransformerOperation<K, T> implements Initialisable, Startable, Stoppable {

  private static final int MIN_IDLE_POOL_COUNT = 1;
  private static final int MAX_IDLE_POOL_COUNT = 32;

  protected DocumentBuilderFactory documentBuilderFactory;
  protected EntityResolver entityResolver = new LocalEntityResolver();
  private LoadingCache<K, GenericObjectPool<T>> transformerPools;

  /**
   * Defines how to treat entity expansion. Setting a value different than NEVER renders the application
   * vulnerable to XXE and/or DoS attacks
   */
  @Parameter
  @ConfigOverride
  @Placement(tab = "Security")
  @Summary("Set to NEVER to prevent XXE and DoS attacks")
  protected EntityExpansion expandEntities = NEVER;

  @Override
  public final void initialise() throws InitialisationException {
    try {
      documentBuilderFactory =
          XMLSecureFactories
              .createWithConfig(expandEntities.isAcceptExternalEntities(), expandEntities.isExpandInternalEntities())
              .getDocumentBuilderFactory();
      documentBuilderFactory.setNamespaceAware(true);

      doInitialise();

    } catch (Exception e) {
      throw new InitialisationException(e, this);
    }
  }

  protected void doInitialise() throws InitialisationException {}



  @Override
  public final void start() throws MuleException {
    transformerPools = CacheBuilder.newBuilder()
        .expireAfterAccess(5, MINUTES)
        .removalListener((RemovalListener<K, GenericObjectPool<T>>) notification -> notification.getValue().close())
        .build(new CacheLoader<K, GenericObjectPool<T>>() {

          @Override
          public GenericObjectPool<T> load(K key) throws Exception {
            return createPool(createPooledObjectFactory(key));
          }
        });
  }

  @Override
  public final void stop() throws MuleException {
    transformerPools.invalidateAll();
  }

  protected abstract BasePooledObjectFactory<T> createPooledObjectFactory(K key);

  protected <V> V withTransformer(K key, CheckedFunction<T, V> func) {
    GenericObjectPool<T> pool = transformerPools.getUnchecked(key);
    T transformer = null;
    try {
      transformer = pool.borrowObject();
      return func.apply(transformer);
    } catch (Exception e) {
      Optional<ModuleException> cause = extractOfType(e, ModuleException.class);
      if (cause.isPresent()) {
        throw cause.get();
      }

      throw new TransformationException(e.getMessage(), e);
    } finally {
      if (transformer != null) {
        pool.returnObject(transformer);
      }
    }
  }

  private <T> GenericObjectPool<T> createPool(BasePooledObjectFactory<T> factory) {
    return new GenericObjectPool<>(PoolUtils.synchronizedPooledFactory(factory), defaultPoolConfig());
  }

  private GenericObjectPoolConfig defaultPoolConfig() {
    GenericObjectPoolConfig config = new GenericObjectPoolConfig();
    config.setMinIdle(MIN_IDLE_POOL_COUNT);
    config.setMaxIdle(MAX_IDLE_POOL_COUNT);
    config.setMaxTotal(MAX_IDLE_POOL_COUNT);
    config.setBlockWhenExhausted(true);
    config.setTimeBetweenEvictionRunsMillis(MINUTES.toMillis(5));
    config.setTestOnBorrow(false);
    config.setTestOnReturn(testOnReturn());
    config.setTestWhileIdle(false);
    config.setTestOnCreate(false);
    config.setJmxEnabled(false);
    config.setMinEvictableIdleTime(Duration.ofMillis(-1));
    return config;
  }

  protected boolean testOnReturn() {
    return false;
  }
}
