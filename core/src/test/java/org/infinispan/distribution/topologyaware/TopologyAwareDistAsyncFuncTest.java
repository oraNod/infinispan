package org.infinispan.distribution.topologyaware;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.distribution.DistAsyncFuncTest;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.test.fwk.TestCacheManagerFactory;
import org.infinispan.test.fwk.TransportFlags;
import org.testng.annotations.Test;

/**
 * @author Mircea.Markus@jboss.com
 * @since 4.2
 */
@Test (groups = "functional", testName = "distribution.topologyaware.TopologyAwareDistAsyncFuncTest")
public class TopologyAwareDistAsyncFuncTest extends DistAsyncFuncTest {

   @Override
   public Object[] factory() {
      return new Object[]{new TopologyAwareDistAsyncFuncTest()};
   }

   @Override
   protected EmbeddedCacheManager addClusterEnabledCacheManager(TransportFlags flags) {
      int index = cacheManagers.size();
      String rack;
      String machine;
      switch (index) {
         case 0 : {
            rack = "r0";
            machine = "m0";
            break;
         }
         case 1 : {
            rack = "r1";
            machine = "m0";
            break;
         }
         case 2 : {
            rack = "r1";
            machine = "m0";
            break;
         }
         case 3 : {
            rack = "r1";
            machine = "m1";
            break;
         }
         default : {
            throw new RuntimeException("Bad!");
         }
      }
      GlobalConfigurationBuilder gcb = GlobalConfigurationBuilder.defaultClusteredBuilder();
      gcb.transport().rackId(rack).machineId(machine);
      EmbeddedCacheManager cm = TestCacheManagerFactory.createClusteredCacheManager(gcb, getDefaultClusteredCacheConfig(CacheMode.DIST_ASYNC), flags);
      cacheManagers.add(cm);
      return cm;
   }
}
