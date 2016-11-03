package bufmgr;


/**
 * Created by alchemist on 10/24/16.
 */

import java.util.ArrayList;
import java.util.Date;

public class ReplacerLRFU
{
    private ArrayList<ReplacementCandidate> replacementCandidates;
    private int numBufs;
    private String replaceArg;
    private int reqIndex;


	public ReplacerLRFU(String replaceArg, int numBufs)
	{
	    this.numBufs = numBufs;
	    this.replaceArg = replaceArg;
	    replacementCandidates = new ArrayList<ReplacementCandidate>();
	}

	public void addReplacementCandidate(int frameNumber)
	{
	    replacementCandidates.add((new ReplacementCandidate(frameNumber)));
	}

	public int getReplacement(Descriptor bufDescr[])
	{

        return  myReplacementX(bufDescr);

	}


	public void removeAndUpdate(int frameNumber)
	{
		int i=0;
		while( i < replacementCandidates.size()  )
		{
			if(replacementCandidates.get(i).getFrameNumber() == frameNumber)
				replacementCandidates.remove(i);

			i++;
		}

	}




	public int myReplacementX(Descriptor bufDescr[])		//finds the frame with minimum crf
	{
		int frame_to_return=-1;
		long crf;
		Date d=new Date();
		long min_crf=d.getTime();//myCRFValue(bufDescr[x].getaccesTime());


		//System.out.print("Candidate frames are ");

		for (int i = 0; i< replacementCandidates.size(); i++)
		{
			int framenumber= replacementCandidates.get(i).getFrameNumber();
		//	System.out.print(framenumber+"  ");
			crf=myCRFValue(bufDescr[framenumber].get_accesTime());
			//System.out.println("Min CRF is"+min_crf+"  crf is"+crf);
			if(min_crf>crf)
			{
				//System.out.println("Min CRF updated");
				min_crf=crf;
				frame_to_return=framenumber;
				
			}
		}
		
		return frame_to_return;
	}	
	
	public long myCRFValue(ArrayList<Long> accesstimes)     //gets the crf value
	{
		long sum=0;
		long last_time=accesstimes.get(accesstimes.size()-1);


		if(accesstimes.size()==1)
			return  accesstimes.get(0);

		for(int i=0;i<accesstimes.size()-1;i++)
		{
			sum+= 1/(last_time-accesstimes.get(i)+1);
		}
		
		return sum;
	}
	
	



}