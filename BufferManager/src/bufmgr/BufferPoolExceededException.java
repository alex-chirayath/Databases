package bufmgr;

import chainexception.ChainException;

/**
 * Created by alchemist on 10/24/16.
 */

public class BufferPoolExceededException extends ChainException
{

  public BufferPoolExceededException(Exception e, String name)
  
  { 
    super(e, name); 
  }


}




