package bufmgr;


/**
 * Created by alchemist on 10/24/16.
 */

import global.PageId;

import java.util.ArrayList;
import java.util.Date;

public class Descriptor 
{
	  int pageNumber;
	  PageId pgId;
	  int pinCount;
	  boolean dirty;
	  ArrayList<Long> accesstime;
	
	public Descriptor(PageId pgId, int pinCount , boolean dirty)
	{
		this.pgId = pgId;
		this.pageNumber = pgId.pid;
		this.pinCount = pinCount;
		this.dirty = dirty;
		ArrayList<Long> access_time=new ArrayList<Long>();
		this.accesstime=access_time;
	
		
	}
	
	public void reset_page(PageId pgId, int pinCount , boolean dirty)
	{
		this.pgId = pgId;
		this.pageNumber = pgId.pid;
		this.pinCount = pinCount;
		this.dirty = dirty;
		ArrayList<Long> access_time=new ArrayList<Long>();
		this.accesstime=access_time;
		
	}
	
	public void add_accessTime()
	{
	       Date d=new Date();
	       accesstime.add(d.getTime());
	}
	
	public ArrayList<Long> get_accesTime()
	{
		return accesstime;
	}
	
	
	
	
	

	public int get_pinCount()
	{
		return pinCount;
	}

	public void set_pinCount(int count){
		this.pinCount=count;
	}

	public PageId get_pageId() {
		return pgId;
	}

	public int get_pageNumber()
	{
		return pageNumber;
	}

	public boolean check_dirty()
	{
		return dirty;
	}

	public void set_dirty(boolean dirty)
	{
		this.dirty = dirty;
	}

	
}
