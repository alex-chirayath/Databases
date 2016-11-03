package bufmgr;


/**
 * Created by alchemist on 10/24/16.
 */

import chainexception.ChainException;

public class HashEntryNotFoundException extends ChainException
{

	  public HashEntryNotFoundException(Exception e, String name)
	  
	  { 
	    super(e, name); 
	  }
}