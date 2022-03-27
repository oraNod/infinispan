package org.infinispan.query.impl;

import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.IndexStartupMode;
import org.infinispan.configuration.cache.IndexStorage;
import org.infinispan.query.Indexer;
import org.infinispan.search.mapper.mapping.SearchMapping;

public final class IndexStartupActivator {

   private final SearchMapping mapping;
   private final Indexer indexer;
   private final Configuration configuration;

   public static IndexStartupActivator create(SearchMapping mapping, Indexer indexer, Configuration configuration) {
      return new IndexStartupActivator(mapping, indexer, configuration);
   }

   private IndexStartupActivator(SearchMapping mapping, Indexer indexer, Configuration configuration) {
      this.mapping = mapping;
      this.indexer = indexer;
      this.configuration = configuration;
   }

   public void run() {
      IndexStartupMode startupMode = computeFinalMode();

      if (IndexStartupMode.PURGE.equals(startupMode)) {
         mapping.scopeAll().workspace().purge();
      } else if (IndexStartupMode.REINDEX.equals(startupMode)) {
         indexer.run();
      }
   }

   private IndexStartupMode computeFinalMode() {
      IndexStartupMode startupMode = configuration.indexing().startupMode();
      if (!IndexStartupMode.AUTO.equals(startupMode)) {
         return startupMode;
      }

      boolean dataIsVolatile = !configuration.persistence().stores()
            .stream().filter(s -> !s.purgeOnStartup()).findFirst().isPresent();
      boolean indexesAreVolatile = IndexStorage.LOCAL_HEAP.equals(configuration.indexing().storage());

      if (dataIsVolatile && !indexesAreVolatile) {
         return IndexStartupMode.PURGE;
      }

      if (!dataIsVolatile && indexesAreVolatile) {
         return IndexStartupMode.REINDEX;
      }

      // if both (data and indexes) are volatile or not volatile they should be already aligned
      return IndexStartupMode.NONE;
   }
}
