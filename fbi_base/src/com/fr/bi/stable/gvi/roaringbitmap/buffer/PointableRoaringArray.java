package com.fr.bi.stable.gvi.roaringbitmap.buffer;

import java.io.DataOutput;
import java.io.IOException;

/**
 * Generic interface for the array underlying roaring bitmap classes.
 * 
 */
public interface PointableRoaringArray extends Cloneable {
    /**
     * Create an independent copy of the underlying array
     * 
     * @return a copy
     */
    PointableRoaringArray clone();

    /**
     * 
     * 
     * @param x
     *            16-bit key
     * @return matching container
     */
    MappeableContainer getContainer(short x);

    /**
     * @param i
     *            index
     * @return matching container
     */
    MappeableContainer getContainerAtIndex(int i);

    /**
     * @return a ContainerPointer to iterator over the array
     */
    MappeableContainerPointer getContainerPointer();

    /**
     * @param x
     *            16-bit key
     * @return corresponding index
     */
    int getIndex(short x);

    /**
     * @param i
     *            the index
     * @return 16-bit key at the index
     */
    short getKeyAtIndex(int i);

    /**
     * @return number of keys
     */
    int size();
    
    /**
    * Serialize.
    * 
    * The current bitmap is not modified.
    * 
    * @param out
    *            the DataOutput stream
    * @throws java.io.IOException
    *             Signals that an I/O exception has occurred.
    */
   public void serialize(DataOutput out) throws IOException;
   
   /**
    * @return the size that the data structure occupies on disk
    */
   public int serializedSizeInBytes();
}