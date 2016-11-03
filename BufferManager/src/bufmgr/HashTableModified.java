package bufmgr;

import java.util.Vector;

/**
 * Created by alchemist on 10/24/16.
 */
public class HashTableModified {

	private final int hashtablesize = 127;
	private final int a = 6;
	private final int b = 35;


	private class PagetoFrame {
		public Integer page_id;
		public Integer frame_id;

		public PagetoFrame(int page_id, int frame_id) {
			super();
			this.page_id = page_id;
			this.frame_id = frame_id;
		}

	}

	private class Bucket {
		private Vector<PagetoFrame> entries;

		public Bucket() {
			super();
			entries = new Vector<PagetoFrame>();
		}

		public void addPageFrame(Integer pageId, Integer frameId)

		{
			entries.add(new PagetoFrame(pageId, frameId));
		}
	}

	private class BucketOfBuckets {
		Bucket[] buckets;
	}

	private BucketOfBuckets bucketOfBuckets;

	public HashTableModified() {
		bucketOfBuckets = new BucketOfBuckets();
		bucketOfBuckets.buckets = new Bucket[hashtablesize];

		for (int i = 0; i < hashtablesize; i++) {
			bucketOfBuckets.buckets[i] = new Bucket();
		}
	}

	private int hashFunction(int value)


	{
		int key= (a * value + b) % hashtablesize;
		return key;
	}

	public void put(Integer pid, Integer fid) {
		int index = hashFunction(pid);
		bucketOfBuckets.buckets[index].addPageFrame(pid, fid);
	}


	public void remove(Integer pid) {
		int index = hashFunction(pid);
		int index_element = 0;
		for (PagetoFrame element : bucketOfBuckets.buckets[index].entries) {
			if (element.page_id.equals(pid)) {
				bucketOfBuckets.buckets[index].entries.remove(index_element);
				break;
			}
			index_element++;
		}

	}


	public boolean containsKey(Integer pid) {

		int index_element = hashFunction(pid);
		for (PagetoFrame element : bucketOfBuckets.buckets[index_element].entries) {
			if (element.page_id.equals(pid)) {
				return true;
			}
		}
		return false;
	}

	public Integer get(Integer pid) {
		int index = hashFunction(pid);
		for (PagetoFrame element : bucketOfBuckets.buckets[index].entries) {
			if (element.page_id.equals(pid)) {
				return element.frame_id;
			}
		}
		return null;
	}






}
