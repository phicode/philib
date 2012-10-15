package ch.bind.philib.cache.lru.newimpl;

public interface LruNode {

	void setNext(LruNode next);

	void setPrev(LruNode Prev);

	LruNode getNext();

	LruNode getPrev();
}
