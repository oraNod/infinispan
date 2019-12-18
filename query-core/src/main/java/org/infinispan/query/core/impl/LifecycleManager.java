package org.infinispan.query.core.impl;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.commons.marshall.AdvancedExternalizer;
import org.infinispan.commons.util.AggregatedClassLoader;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.factories.ComponentRegistry;
import org.infinispan.factories.GlobalComponentRegistry;
import org.infinispan.factories.annotations.InfinispanModule;
import org.infinispan.lifecycle.ModuleLifecycle;
import org.infinispan.objectfilter.impl.ReflectionMatcher;
import org.infinispan.query.core.impl.continuous.ContinuousQueryResult;
import org.infinispan.query.core.impl.continuous.IckleContinuousQueryCacheEventFilterConverter;
import org.infinispan.query.core.impl.eventfilter.IckleCacheEventFilterConverter;
import org.infinispan.query.core.impl.eventfilter.IckleFilterAndConverter;
import org.infinispan.registry.InternalCacheRegistry;
import org.infinispan.registry.InternalCacheRegistry.Flag;

/**
 * @author anistor@redhat.com
 * @since 10.1
 */
@InfinispanModule(name = "query-core", requiredModules = "core")
public class LifecycleManager implements ModuleLifecycle {

   @Override
   public void cacheStarting(ComponentRegistry cr, Configuration cfg, String cacheName) {
      InternalCacheRegistry icr = cr.getGlobalComponentRegistry().getComponent(InternalCacheRegistry.class);
      if (!icr.isInternalCache(cacheName) || icr.internalCacheHasFlag(cacheName, Flag.QUERYABLE)) {
         AdvancedCache<?, ?> cache = cr.getComponent(Cache.class).getAdvancedCache();
         ClassLoader aggregatedClassLoader = makeAggregatedClassLoader(cr.getGlobalComponentRegistry().getGlobalConfiguration().classLoader());
         cr.registerComponent(new ReflectionMatcher(aggregatedClassLoader), ReflectionMatcher.class);
         cr.registerComponent(new QueryEngine<>(cache), QueryEngine.class);
      }
   }

   @Override
   public void cacheStarted(ComponentRegistry cr, String cacheName) {
   }

   /**
    * Create a class loader that delegates loading to an ordered set of class loaders.
    *
    * @param globalClassLoader the cache manager's global ClassLoader from GlobalConfiguration
    * @return the aggregated ClassLoader
    */
   private ClassLoader makeAggregatedClassLoader(ClassLoader globalClassLoader) {
      // use an ordered set to deduplicate them
      Set<ClassLoader> classLoaders = new LinkedHashSet<>(6);

      // add the cache manager's CL
      if (globalClassLoader != null) {
         classLoaders.add(globalClassLoader);
      }

      // add Infinispan's CL
      classLoaders.add(AggregatedClassLoader.class.getClassLoader());

      // TODO [anistor]
      // add Hibernate Search's CL
      //classLoaders.add(ClassLoaderService.class.getClassLoader());

      // add this module's CL
      classLoaders.add(getClass().getClassLoader());

      // add the TCCL
      try {
         ClassLoader tccl = Thread.currentThread().getContextClassLoader();
         if (tccl != null) {
            classLoaders.add(tccl);
         }
      } catch (Exception e) {
         // ignored
      }

      // add the system CL
      try {
         ClassLoader syscl = ClassLoader.getSystemClassLoader();
         if (syscl != null) {
            classLoaders.add(syscl);
         }

      } catch (Exception e) {
         // ignored
      }

      return new AggregatedClassLoader(classLoaders);
   }

   @Override
   public void cacheStopping(ComponentRegistry cr, String cacheName) {
   }

   @Override
   public void cacheStopped(ComponentRegistry cr, String cacheName) {
   }

   @Override
   public void cacheManagerStarting(GlobalComponentRegistry gcr, GlobalConfiguration globalCfg) {
      gcr.registerComponent(new QueryCache(), QueryCache.class);

      Map<Integer, AdvancedExternalizer<?>> externalizerMap = globalCfg.serialization().advancedExternalizers();
      externalizerMap.put(ExternalizerIds.ICKLE_FILTER_AND_CONVERTER, new IckleFilterAndConverter.IckleFilterAndConverterExternalizer());
      externalizerMap.put(ExternalizerIds.ICKLE_FILTER_RESULT, new IckleFilterAndConverter.FilterResultExternalizer());
      externalizerMap.put(ExternalizerIds.ICKLE_CACHE_EVENT_FILTER_CONVERTER, new IckleCacheEventFilterConverter.Externalizer());
      externalizerMap.put(ExternalizerIds.ICKLE_CONTINUOUS_QUERY_CACHE_EVENT_FILTER_CONVERTER, new IckleContinuousQueryCacheEventFilterConverter.Externalizer());
      externalizerMap.put(ExternalizerIds.ICKLE_CONTINUOUS_QUERY_RESULT, new ContinuousQueryResult.Externalizer());
   }

   @Override
   public void cacheManagerStopped(GlobalComponentRegistry gcr) {
   }
}