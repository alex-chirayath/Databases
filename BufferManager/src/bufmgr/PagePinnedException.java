package bufmgr;

/**
 * Created by alchemist on 10/24/16.
 */
import chainexception.ChainException;

public class PagePinnedException extends ChainException
{

	  public PagePinnedException(Exception e, String name)
	  
	  { 
	    super(e, name); 
	  }
}