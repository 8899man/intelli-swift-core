/**
 *
 */
package com.fr.bi.stable.structure.queue;

import com.fr.bi.common.inter.Delete;
import com.fr.bi.common.inter.Release;

import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Daniel
 *
 */
public class ThreadUnitedQueue<T extends Delete> implements Release {
	private volatile Deque<ConcurrentUUIDObject<T>> queue = new LinkedBlockingDeque<ConcurrentUUIDObject<T>>();
	private volatile boolean isClear = false;
	private Map<Long, ConcurrentUUIDObject<T>> map = new ConcurrentHashMap<Long, ConcurrentUUIDObject<T>>();

	public T get() {
		Long threadId = Thread.currentThread().getId();
		ConcurrentUUIDObject<T> result = map.get(threadId);
		if(result != null){
			return result.get();
		}
		while(queue.isEmpty()){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		result = queue.peek();
		result.access_plus();
		map.put(threadId, result);
		return result.get();
	}

	public void add(T t){
		if(isClear){
			return;
		}
		ConcurrentUUIDObject<T> u = new ConcurrentUUIDObject<T>(t);
		queue.offerFirst(u);
	}

	public void releaseObject(){
		Long threadId = Thread.currentThread().getId();
		ConcurrentUUIDObject<T> result = map.get(threadId);
		if(result != null){
			map.remove(threadId);
			result.access_reduce();
			releaseOffuse();
		}
	}

	public boolean isEmpty(){
		return queue.isEmpty();
	}

	/**
	 *
	 */
	private void releaseOffuse() {
		if(queue.size() > 1){
			new Thread(){
				@Override
				public void run(){
					releaseInThread();
				}
			}.start();
		}
	}

	private void releaseInThread(){
		Iterator<ConcurrentUUIDObject<T>> iter = queue.descendingIterator();
		while(iter.hasNext()){
			ConcurrentUUIDObject<T> o = iter.next();
			if(!iter.hasNext()){
				break;
			}
			if(o.isZeroCount()){
				iter.remove();
				synchronized (o) {
					if(!o.isClear()){
						o.clear();
						o.get().clear();
						o.get().delete();
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.fr.bi.common.inter.Release#clear()
	 */
	@Override
	public void clear() {
		isClear = true;
		map.clear();
		releaseInThread();
		if(!queue.isEmpty()){
			queue.pop().get().clear();
		}
	}

    public int size() {
    	return queue.size();
    }
}