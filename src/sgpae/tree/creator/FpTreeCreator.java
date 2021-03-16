package sgpae.tree.creator;

import java.lang.reflect.Method;
import java.util.ArrayList;

import sgpae.extractor.data.DataSet;
import sgpae.extractor.data.Interval;
import sgpae.extractor.data.Transaction;
import sgpae.manifest.Manifest;

public class FpTreeCreator
{
	private static Method f = setMethod();
	
	private static Method setMethod()
	{
		try
		{
			return FpTreeCreator.class.getMethod("makeTreeV1",ArrayList.class,ArrayList.class,String.class,int.class,DataSet.class);
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static FpTree makeTree(ArrayList<Interval> selectors, ArrayList<Transaction> transactions, String target, int nbCore, DataSet dataset) throws InterruptedException
	{
		return makeTreeV2(selectors, transactions, target, nbCore, dataset);
		/*
		try
		{
			return (FpTree) f.invoke(FpTreeCreator.class,selectors, transactions,target,nbCore);
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		 */
	}
	
	public static FpTree makeTreeV2(ArrayList<Interval> selectors, ArrayList<Transaction> transactions, String target, int nbCore, DataSet dataset) throws InterruptedException
	{
		FpTree tree = new FpTree(selectors, target, dataset);
		for(int i = 0; i < transactions.size();i++)
		{
			Transaction trans = transactions.get(i);
			//System.out.println(i+"/"+transactions.size() + " : " + trans);
			tree.addTransactionV2_1(trans);
		}
		return tree;
	}
	
	public static FpTree makeTreeV1(ArrayList<Interval> selectors, ArrayList<Transaction> transactions, String target, int nbCore, DataSet dataset) throws InterruptedException
	{
		FpTree tree = new FpTree(selectors, target,dataset);
		ArrayList<SubTreeCreatorThread> listThread = new ArrayList<SubTreeCreatorThread>();
		for(int i = 0; i < nbCore; i++)
		{
			int indexMin = i*Manifest.dataset.getNbTransactions()/nbCore;
			int indexMax = (i+1)*Manifest.dataset.getNbTransactions()/nbCore;
			SubTreeCreatorThread thread = new SubTreeCreatorThread(indexMin, indexMax, selectors, target, transactions,dataset);
			System.out.println("limiteThread : [" +indexMin + ";" + indexMax+"]");
			listThread.add(thread);
			thread.start();
		}
		for(SubTreeCreatorThread thread : listThread)
		{
			thread.join();
			tree.addTree(thread.tree);
		}
		return listThread.get(0).tree;
	}
}
