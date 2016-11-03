package bufmgr;
/**
 * Created by alchemist on 10/24/16.
 */


import chainexception.ChainException;

public class PageUnpinnedException extends ChainException
{
	  public PageUnpinnedException(Exception e, String name)
	  
	  { 
	    super(e, name); 
	  }
}