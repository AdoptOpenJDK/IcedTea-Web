/* VoidPluginCallRequest -- represent Java-to-JavaScript requests
   Copyright (C) 2008  Red Hat

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

IcedTea is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package sun.applet;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

class PluginMessageConsumer {

	private static int MAX_PARALLEL_INITS = 1;

	// Each initialization requires 5 responses (tag, handle, width, proxy, cookie) 
	// before the message stack unlocks/collapses. This works out well because we 
	// want to allow upto 5 parallel tasks anyway
	private static int MAX_WORKERS = MAX_PARALLEL_INITS*4;
	private static int PRIORITY_WORKERS = MAX_PARALLEL_INITS*2;

	private static Hashtable<Integer, PluginMessageHandlerWorker> initWorkers = new Hashtable<Integer, PluginMessageHandlerWorker>(2);

	LinkedList<String> readQueue = new LinkedList<String>();
	private static LinkedList<String> priorityWaitQueue = new LinkedList<String>();
	ArrayList<PluginMessageHandlerWorker> workers = new ArrayList<PluginMessageHandlerWorker>();
	PluginStreamHandler streamHandler = null;
	AppletSecurity as;
	ConsumerThread consumerThread = new ConsumerThread();
	private static ArrayList<Integer> processedIds = new ArrayList<Integer>();

	/** 
	 * Registers a reference to wait for. Responses to registered priority 
	 * references get handled by priority worker if normal workers are busy.
	 *
	 * @param reference The reference to give priority to
	 */
	public static void registerPriorityWait(Long reference) {
	    PluginDebug.debug("Registering priority for reference " + reference);
	    registerPriorityWait("reference " + reference.toString());
	}

	/** 
     * Registers a string to wait for.
     *
     * @param searchString the string to look for in a response
     */
    public static void registerPriorityWait(String searchString) {
        PluginDebug.debug("Registering priority for string " + searchString);
        synchronized (priorityWaitQueue) {
            if (!priorityWaitQueue.contains(searchString))
                priorityWaitQueue.add(searchString);
        }
    }

	/** 
     * Unregisters a priority reference to wait for.
     *
     * @param reference The reference to remove
     */
    public static void unRegisterPriorityWait(Long reference) {
        unRegisterPriorityWait(reference.toString());
    }

    /** 
     * Unregisters a priority string to wait for.
     *
     * @param searchString The string to unregister from the priority list
     */
    public static void unRegisterPriorityWait(String searchString) {
        synchronized (priorityWaitQueue) {
            priorityWaitQueue.remove(searchString);
        }
    }

    /**
     * Returns the reference for this message. This method assumes that 
     * the message has a reference number.
     * 
     * @param The message
     * @return the reference number
     */
    private Long getReference(String[] msgParts) {
        return Long.parseLong(msgParts[3]);
    }
    
	public PluginMessageConsumer(PluginStreamHandler streamHandler) {
		
		as = new AppletSecurity();
		this.streamHandler = streamHandler;
		this.consumerThread.start();
	}

	private String getPriorityStrIfPriority(String message) {

	    synchronized (priorityWaitQueue) {
	        Iterator<String> it = priorityWaitQueue.iterator();

	        while (it.hasNext()) {
	            String priorityStr = it.next();
	            if (message.indexOf(priorityStr) > 0)
	                return priorityStr;
	        }
	    }

	    return null;
	}

	private boolean isInInit(Integer instanceNum) {
	    return initWorkers.containsKey(instanceNum);
	}

	private void addToInitWorkers(Integer instanceNum, PluginMessageHandlerWorker worker) {
        synchronized(initWorkers) {
            initWorkers.put(instanceNum, worker);
        }
	}


	public void notifyWorkerIsFree(PluginMessageHandlerWorker worker) {
	    synchronized (initWorkers) {
	        Iterator<Integer> i = initWorkers.keySet().iterator();
            while (i.hasNext()) {
                Integer key = i.next();
                if (initWorkers.get(key).equals(worker)) {
                    processedIds.add(key);
                    initWorkers.remove(key);
                }
            }
	    }
	    
	    consumerThread.interrupt();
	}

	public void queue(String message) {
	    synchronized(readQueue) {
	        readQueue.addLast(message);
	    }
	    
	    // Wake that lazy consumer thread
	    consumerThread.interrupt();
	}

	protected class ConsumerThread extends Thread { 
	    public void run() {

	        while (true) {

                String message = null;

	            synchronized(readQueue) {
	                message = readQueue.poll();
	            }

	            if (message != null) {

	                String[] msgParts = message.split(" ");


	                String priorityStr = getPriorityStrIfPriority(message);
	                boolean isPriorityResponse = (priorityStr != null);
		
	                //PluginDebug.debug("Message " + message + " (priority=" + isPriorityResponse + ") ready to be processed. Looking for free worker...");
	                final PluginMessageHandlerWorker worker = getFreeWorker(isPriorityResponse);
	                
	                if (worker == null) {
	                    synchronized(readQueue) {
                            readQueue.addLast(message);
                        }

	                    continue; // re-loop to try next msg
	                }

	                if (msgParts[2].equals("tag"))
	                    addToInitWorkers((new Integer(msgParts[1])), worker);

	                if (isPriorityResponse) {
	                    unRegisterPriorityWait(priorityStr);
	                }

                    worker.setmessage(message);
	                worker.interrupt();

	            } else {
	                try {
	                    Thread.sleep(1000);
	                } catch (InterruptedException ie) {}
	            }
	        }
	    }
	}

	private PluginMessageHandlerWorker getFreeWorker(boolean prioritized) {

			for (PluginMessageHandlerWorker worker: workers) {
				if (worker.isFree(prioritized)) {
					PluginDebug.debug("Found free worker (" + worker.isPriority() + ") with id " + worker.getWorkerId());
					// mark it busy before returning
					worker.busy();
					return worker;
				}
			}

			// If we have less than MAX_WORKERS, create a new worker
			if (workers.size() <= MAX_WORKERS) {
			    PluginMessageHandlerWorker worker = null;
			    
			    if (workers.size() < (MAX_WORKERS - PRIORITY_WORKERS)) {
			        PluginDebug.debug("Cannot find free worker, creating worker " + workers.size());
			        worker = new PluginMessageHandlerWorker(this, streamHandler, workers.size(), as, false);
			    } else if (prioritized) {
			        PluginDebug.debug("Cannot find free worker, creating priority worker " + workers.size());
			        worker = new PluginMessageHandlerWorker(this, streamHandler, workers.size(), as, true);
			    } else {
			        return null;
			    }

		        worker.start();
		        worker.busy();
		        workers.add(worker);
		        return worker;

			}
			
			// No workers available. Better luck next time! 
			return null;
	}
	
	private void dumpWorkerStatus() {
		for (PluginMessageHandlerWorker worker: workers) {
			PluginDebug.debug(worker.toString());
		}
	}
}
