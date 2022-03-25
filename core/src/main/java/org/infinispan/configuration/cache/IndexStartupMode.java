package org.infinispan.configuration.cache;

import java.util.Arrays;

/**
 * Allows to define some mass indexing operations (e.g.: purge or reindex) to trigger when the cache starts.
 * These actions are usually needed to keep data and indexes aligned (consistent).
 *
 * @author Fabio Massimo Ercoli &lt;fabiomassimo.ercoli@gmail.com&gt;
 */
public enum IndexStartupMode {

   /**
    * Purge will be triggered at cache startup time.
    */
   PURGE("purge"),

   /**
    * Reindex will be triggered at cache startup time.
    */
   REINDEX("reindex"),

   /**
    * With this configuration Infinispan will try to run the right action to align cache data and indexes.
    * Purge will be triggered if the cache data is volatile and indexes are not.
    * Reindex will be triggered if the cache data is not volatile and indexes are.
    */
   AUTO("auto"),

   /**
    * No mass-indexing operation is triggered at cache startup time. This is the default.
    */
   NONE("none");

   private final String token;

   IndexStartupMode(String token) {
      this.token = token;
   }

   public static IndexStartupMode get(String value) {
      return Arrays.stream(IndexStartupMode.values())
            .filter(i -> i.token.equals(value))
            .findFirst()
            .orElse(IndexStartupMode.NONE);
   }
}
