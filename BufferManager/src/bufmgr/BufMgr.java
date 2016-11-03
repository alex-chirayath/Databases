package bufmgr;


/**
 * Created by alchemist on 10/24/16.
 */

import chainexception.ChainException;
import diskmgr.*;
import global.GlobalConst;
import global.Minibase;
import global.Page;
import global.PageId;

import java.io.IOException;
import java.util.Hashtable;

public class BufMgr implements GlobalConst
{
	  byte bufPool[][];
	  Descriptor bufDescr[];
	  ReplacerLRFU replacerLRFU;
	  int descriptorCounter;
	  int numBufs;
	  String replacementPolicy;
	  Hashtable<Integer,Integer> hashx=new Hashtable<>();
	  HashTableModified hashTable;

	/**
	 * Create the BufMgr object.
	 * Allocate pages (frames) for the buffer pool in main memory and
	 * make the buffer manage aware that the replacement policy is
	 * specified by replacerArg (e.g., LH, Clock, LRU, MRU, LRFU, etc.).
	 *
	 * @param numbufs number of buffers in the buffer pool
	 * @param lookAheadSize number of pages to be looked ahead, you can ignore that parameter
	 * @param replacementPolicy Name of the replacement policy, that parameter will be set to "LRFU"
	 */
	public BufMgr(int numbufs,int lookAheadSize, String replacementPolicy)
	{
	    descriptorCounter = 0;
	    this.numBufs = numbufs;
	    this.replacementPolicy = replacementPolicy;
		bufPool = new byte[numBufs][GlobalConst.PAGE_SIZE];
		bufDescr = new Descriptor[numBufs]; // frame number to page number ; HOLDS PAGE NUMBER
		hashTable = new HashTableModified(); // page number to frame number
		replacerLRFU = new ReplacerLRFU(replacementPolicy,numBufs);
	}




	/**
	 * Pin a page.
	 * First check if this page is already in the buffer pool.
	 * If it is, increment the pin_count and return a pointer to this
	 * page.
	 * If the pin_count was 0 before the call, the page was a
	 * replacement candidate, but is no longer a candidate.
	 * If the page is not in the pool, choose a frame (from the
	 * set of replacement candidates) to hold this page, read the
	 * page (using the appropriate method from diskmgr package) and pin it.
	 * Also, must write out the old page in chosen frame if it is dirty
	 * before reading new page.__ (You can assume that emptyPage==false for
	 * this assignment.)
	 *
	 * @param pageno page number in the Minibase.
	 * @param page the pointer pointing to the page.
	 * @param emptyPage true (empty page); false (non-empty page)
	 */

	public void pinPage(PageId pageno, Page page, boolean emptyPage) throws BufferPoolExceededException
	{
//			for (int name: hashTable.keySet()){
//
//				int key =name;
//				int value = hashTable.get(key);
//				System.out.println(key + " " + value);
//
//
//			}


		if(hashTable.containsKey(pageno.pid))
		{
			int frameNumber = hashTable.get(pageno.pid);
			int pinCount = bufDescr[frameNumber].get_pinCount();

			if (pinCount == 0)								//remove from replacement candidates list
				replacerLRFU.removeAndUpdate(frameNumber);

			bufDescr[frameNumber].set_pinCount(bufDescr[frameNumber].get_pinCount()+1);

			bufDescr[frameNumber].add_accessTime();						//adding access time for this case



			// add to pool
			page.setpage(bufPool[frameNumber]);
			//System.out.println("Pin count for Already Pinned Page" +pageno.pid+ "is "+ bufDescr[frameNumber].getPinCount());

		}

		// if not in the buffer pool and there is still empty place in the pool
		else if (descriptorCounter < numBufs)
		{
			Page pg = new Page();
			try {	Minibase.DiskManager.read_page(pageno,pg);	}	// read the page
		    catch (InvalidPageNumberException | FileIOException | IOException e)
			{	e.printStackTrace();}

			page.setpage(pg.getpage());

		 // add to pool
		    bufPool[descriptorCounter] = page.getpage();

		   Descriptor x  = new Descriptor(new PageId(pageno.pid),1,false);	 // add to descirptor


		   		x.add_accessTime();											//to add accesstime to descriptor page
		     bufDescr[descriptorCounter]=x;



            hashTable.put(pageno.pid,descriptorCounter);  // add to hashtable

			hashx.put(pageno.pid,descriptorCounter);

			//System.out.println("Pin count for Pinned Page" +pageno.pid+ "is "+ bufDescr[descriptorCounter].getPinCount());
		    descriptorCounter++;
		}

		//Case to Replace
		else if(descriptorCounter>=numBufs)
		{
			if(getNumUnpinned()==0)
			throw new BufferPoolExceededException(new Exception(),"bufmgr.BufferPoolExceededException");


		  int frameNumber = replacerLRFU.getReplacement(bufDescr);//frame to replace

			//System.out.println("The frame to be replaced this " + frameNumber);


			if(frameNumber==-1)				//all pages are pinned and none to replace
			{
				throw new BufferPoolExceededException(new Exception(),"bufmgr.BufferPoolExceededException");
			}

		  if(frameNumber != -1) // there are frames to replace
		  {
    		   // flush page
    		   if(bufDescr[frameNumber].check_dirty())  flushPage(bufDescr[frameNumber].get_pageId());

    		    // remove page to be replaced from the hash map
    		   hashTable.remove(bufDescr[frameNumber].get_pageNumber());
				hashx.remove(bufDescr[frameNumber].get_pageNumber())	;

    		   Page pg = new Page();
    		   try {	Minibase.DiskManager.read_page(pageno,pg);	}	// read the page
               catch (InvalidPageNumberException | FileIOException | IOException e)
               {	e.printStackTrace();}
    		   page.setpage(pg.getpage());
    		   bufPool[frameNumber] = page.getpage(); // add to pool


    		   bufDescr[frameNumber].reset_page(new PageId(pageno.pid),1,false);	 // update to descirptor


    		   bufDescr[frameNumber].add_accessTime();								//add access time

    		   hashTable.put(pageno.pid,frameNumber);  // add to hashtable
			   hashx.put(pageno.pid,frameNumber);


		   }

		}


	}

	/**
	 * Unpin a page specified by a pageId.
	 * This method should be called with dirty==true if the client has
	 * modified the page.
	 * If so, this call should set the dirty bit for this frame.
	 * Further, if pin_count>0, this method should decrement it.
	 *If pin_count=0 before this call, throw an exception to report error.
	 *(For testing purposes, we ask you to throw
	 * an exception named PageUnpinnedException in case of error.)
	 *
	 * @param pageno page number in the Minibase.
	 * @param dirty the dirty bit of the frame
	 */
	public void unpinPage(PageId pageno, boolean dirty) throws HashEntryNotFoundException, PageUnpinnedException
	{


		if(hashTable.containsKey(pageno.pid))
	    {
    	    int frameNumber = hashTable.get(pageno.pid);
    	    if(dirty)   bufDescr[frameNumber].set_dirty(dirty);
    	    if(bufDescr[frameNumber].get_pinCount() > 0)
    	    {
    	        bufDescr[frameNumber].set_pinCount(bufDescr[frameNumber].get_pinCount()-1);

    	        if(bufDescr[frameNumber].get_pinCount() == 0)
    	        {
    	            replacerLRFU.addReplacementCandidate(frameNumber);
				}

			//	System.out.print("Pin count for Page after Unpinned " +pageno.pid+ "is"+ bufDescr[frameNumber].getPinCount() );

			}
    	    else
    	    {
    	    	//System.out.println("Pin_Count = 0 before Calling unpin method..");
    	    	throw new PageUnpinnedException(new Exception() , "bufmgr.PageUnpinnedExcpetion");
    	    }
	    }
	    else
	    {
	       // System.out.println("Page is not in the pool.");
	        throw new HashEntryNotFoundException(new Exception(),"bufmgr.HashEntryNotFoundException");
	    }

	}

	/**
	 * Allocate new pages.
	 * Call DB object to allocate a run of new pages and
	 * find a frame in the buffer pool for the first page
	 * and pin it. (This call allows a client of the Buffer Manager
	 * to allocate pages on disk.) If buffer is full, i.e., you
	 * can't find a frame for the first page, ask DB to deallocate
	 * all these pages, and return null.
	 *
	 * @param firstpage the address of the first page.
	 * @param howmany total number of allocated new pages.
	 *
	 * @return the first page id of the new pages.__ null, if error.
	 */

	public PageId newPage(Page firstpage, int howmany) throws ChainException
	{
		//System.out.println("New Page:"+ firstpage);

		if(getNumUnpinned() != 0)
		{
			PageId pgid = new PageId();
		    // allocate page
		    try {	Minibase.DiskManager.allocate_page(pgid,howmany);	}
		    catch (OutOfSpaceException | InvalidRunSizeException | InvalidPageNumberException | FileIOException | DiskMgrException | IOException e)
			{	e.printStackTrace();	}

		    pinPage(pgid,firstpage,false);
		    return pgid;
		}

		else


		return null;

	}

	/**
	 * Used to flush a particular page of the buffer pool to disk.
	 * This method calls the write_page method of the diskmgr package.
	 *
	 * @param pageid the page number in the database.
	 */
	public void freePage(PageId pageid) throws PagePinnedException
	{
		//System.out.println("Freeing Page:"+ pageid);
		if(hashTable.containsKey(pageid.pid))
	    {
	    	if(bufDescr[hashTable.get(pageid.pid)].get_pinCount() <= 1 )
	    	{

	    		if(bufDescr[hashTable.get(pageid.pid)].get_pinCount()!=0)
	    		{
	    			try {	unpinPage(pageid , false );	}
	    	    	catch (HashEntryNotFoundException | PageUnpinnedException e1) {e1.printStackTrace();}
	    		}

	    		bufDescr[hashTable.get(pageid.pid)].set_dirty(false);
	    		hashTable.remove(pageid.pid);
				hashx.remove(pageid.pid);
				descriptorCounter--;


//				System.out.println("Printing hash after free");
//				for (int name:hashTable.keySet()) {
//
//				int key =name;
//				int value = hashTable.get(key);
//				System.out.println(key + " " + value);
//				}

	    		try
				{
					Minibase.DiskManager.deallocate_page(pageid);

				}
		    	catch (ChainException e)
		    	{
					e.printStackTrace();

		    	}
	    	}
	    	else
	    	{
	    		//System.out.println("Page is used by another user.");
	    		throw new PagePinnedException(new Exception(),"bufmgr.PagePinnedException");
	    	}
	    }
	    else
	    {
	        	try {	Minibase.DiskManager.deallocate_page(pageid);	}
				catch (ChainException e)
				{
					e.printStackTrace();

				}

	    }

	}

	/**
	 * Used to flush a particular page of the buffer pool to disk.
	 * This method calls the write_page method of the diskmgr package.
	 *
	 * @param pageid the page number in the database.
	 */
	public void flushPage(PageId pageid)
	{
		//System.out.println("Flushing Page:"+ pageid);

		Page pg = new Page();
	    pg.setpage(bufPool[hashTable.get(pageid.pid)]);
	    try {	Minibase.DiskManager.write_page(pageid,pg);}
	    catch (InvalidPageNumberException | FileIOException | IOException e)
	    {
			e.printStackTrace();
	    }
	    bufDescr[hashTable.get(pageid.pid)].set_dirty(false);
	}

	public void flushAllPages()
	{
//		System.out.println("Flushing All Pages:");
		int i=0;
		while ((i<descriptorCounter))
		{
			if(bufDescr[i].check_dirty())
			{
				Page new_pg = new Page();
				new_pg.setpage(bufPool[hashTable.get(bufDescr[i].get_pageNumber())]);
				try {	Minibase.DiskManager.write_page(bufDescr[i].get_pageId(),new_pg);	}
			    catch (InvalidPageNumberException | FileIOException | IOException e)
			    {	e.printStackTrace();	}
				bufDescr[i].set_dirty(false);
			}
			i++;
		}
	}

	/**
	 * Returns the total number of buffer frames.
	 */
	public int getNumBufs()
	{
		return numBufs;
	}

/**
 * Returns the total number of unpinned buffer frames.
*/
	public int getNumUnpinned()
	{

		//ReplacerLRFU replacer;

		int unpinned = numBufs-descriptorCounter-1;

        for(int i =0 ;i<descriptorCounter; i++)
        {
            if(bufDescr[i].get_pinCount() == 0) {

						unpinned++;

			}
        }

		return unpinned;
	}

}
