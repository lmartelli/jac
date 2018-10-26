package org.objectweb.jac.aspects.gui;

import java.util.Collection;

/**
 * This class is just a wrappable delegator used to display
 * collections. */

public class CollectionWrapper 
{
   /** The displayed collection. */
   public Collection collection;
   /**
    * Construct a new delegator.
    *
    * @param c the delegated collection */
   public CollectionWrapper(Collection c) {
      this.collection = c;
   }
   /**
    * Gets the delegated collection. */
   public Collection getCollection() {
      return collection;
   }
}
