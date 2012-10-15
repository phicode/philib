package ch.bind.philib.cache.lru.newimpl;

public interface LruRoot {

	void setHead(LruNode head);
	void setTail(LruNode tail);
	
	LruNode getHead();
	LruNode getTail();
}
