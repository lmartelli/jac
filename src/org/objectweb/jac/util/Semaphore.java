/*
  Based on some code found with Google, somewhere in the Internet

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA */

package org.objectweb.jac.util;

import org.apache.log4j.Logger;



/**
 * This is a simple implementation of the well-known semaphore synchronization feature.
 *
 * <p>It allows a given number of thread to be blocked waiting for a
 * resource to be freed by the threads that are currently using it. */

public class Semaphore {
    static Logger logger = Logger.getLogger("semaphore");

    /** The available resources count. */ 
    protected int count = 0;
    private int waitingCount = 0;
   
    /**
     * The semaphore constructor that allows the programmer to assign a
     * number of resources item that are greater than 0 or 1 (meaning
     * that several threads (n exactly) can use the semaphore at the
     * same time).
     * 
     * @param n the number of resource items */

    public Semaphore(int n) { 
        this.count = n; 
    }

    /**
     * The default constructor for this semaphore equivalent to
     * <code>Semaphore(0)</code>.
     *
     * <p>This constructor means that no resources are available by
     * default. Thus, the first thread (t1) that will acquire the
     * semaphore will eventually by blocked until another thread (t2)
     * releases a resource on this semaphore.
     *
     * <p>This use of the semaphore feature is quite strange regarding
     * resources (since the deblocking thread (t2) releases a resource
     * that it never acquired!!) but it can be very usefull for
     * synchronization especially if t1 waits for a result that is
     * being calculating by t2 and that is not yet available. */

    public Semaphore() {
    }

    /**
     * Gets the number of threads that are currently blocked on this
     * semaphore.
     *
     * <p>This method is an indication but is not safe since a thread
     * can be released meantime you evaluate its result.
     *
     * @return the waiting threads number at the moment you have asked
     * it */

    public int getWaitingCount() {
        return waitingCount;
    }

    public int getCount() {
        return count;
    }

    /**
     * Acquires a resource on this semaphore with no timeout.
     *
     * <p>If no resources are available anymore (count == 0), then the
     * thread that performed this call is blocked until another thread
     * releases a resource.</p>
     *
     * @return true if the resource could be aquired.
     *
     * @see #acquire(long)
     * @see #release() 
     */
    public boolean acquire() {
        return acquire(0);
    }

    /**
     * Acquires a resource on this semaphore.
     *
     * <p>If no resources are available anymore (count == 0), then the
     * thread that performed this call is blocked until another thread
     * releases a resource.
     *
     * @param timeout milliseconds to wait for. 0 means wait for ever.
     * @return true if the resource could be aquired.
     *
     * @see #acquire()
     * @see #release() 
     */
    public synchronized boolean acquire(long timeout) {
        waitingCount++;
        while(count == 0) {
            logger.debug(this+" acquire, waiting;  count = "+count);
            try {
                wait(timeout);
                // I think this is not correct in the general case but
                // should work if there's only on thread waiting.
                if (count==0) 
                    return false;
            } catch (InterruptedException e) {
                //keep trying
            }
        }
        logger.debug(this+" acquired, count = "+count+"(--)");
        count--;
        waitingCount--;
        return true;
    }

    /**
     * Releases a resource on this semaphore.
     *
     * <p>If one or several threads are blocked, waiting for a resource
     * to be available, then this call will necessaraly makes one of
     * them take the semaphore's monitor (the others, if any will block
     * again). There is absolutly no way of knowing which thread will
     * take the monitor first.
     *
     * @see #acquire() 
     */
    public synchronized void release() {
        logger.debug(this+" release, count = "+count+"(++)");
        count++;
        notify(); //alert a thread that's blocking on this semaphore
    }
}
