package sgpae.extractor.sg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import sgpae.extractor.data.Transaction;
import sgpae.manifest.Manifest;
import sgpae.tree.creator.Node;

public class InformationExtractor
{
	private final String clef;
	public ArrayList<Node> listNode  = new ArrayList<Node>();
	public ArrayList<Transaction> listTrans = new ArrayList<Transaction>();
	private Double score;
	private Double bestScore;
	
	public InformationExtractor(String clef, ArrayList<Node> listNode)
	{
		this.clef=clef;
		this.listNode=(ArrayList<Node>) listNode.clone();
		ExtractInfo();
	}
	
	public void ExtractInfo()
	{
		HashSet<Integer> hashTransaction = new HashSet<Integer>();
		int nbTransaction = 0;
		double sum = 0;
		for(Node node : listNode)
		{
			for(Transaction trans : node.getTransaction())
			{
				int id = trans.getID();
				if(!hashTransaction.contains(id))
				{
					listTrans.add(trans);
					Collections.sort(listTrans);
					sum+=(float)trans.getValue(Manifest.dataset.getTarget()).getValue();
					nbTransaction++;
					hashTransaction.add(id);
				}
			}
		}
		double globalMean = Manifest.dataset.getMean();
		score = makeScore(nbTransaction,sum,globalMean);
		double bestPossibleScore = score;
		nbTransaction = 0;
		sum = 0;
		for(Transaction trans : listTrans)
		{
			float transVal=(float)trans.getValue(Manifest.dataset.getTarget()).getValue();
			sum+=transVal;
			nbTransaction++;
			double tmpScore=makeScore(nbTransaction,sum,globalMean);
			if(tmpScore>bestPossibleScore)
			{
				bestPossibleScore=tmpScore;
			}
		}
		bestScore=bestPossibleScore;
	}
	
	private double makeScore(int nbTransaction, double sum, double globalMean)
	{
		double mean = sum/nbTransaction;
		
		return Math.sqrt(nbTransaction)*(mean-globalMean); 
	}
	
	public double getScore()
	{
		return score;
	}
	
	public double getBestScore()
	{
		return bestScore;
	}
	
	public String getClef()
	{
		return clef;
	}
}
