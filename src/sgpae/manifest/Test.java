package sgpae.manifest;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import javax.management.BadAttributeValueExpException;

import sgpae.extractor.Extractor;
import sgpae.extractor.SelectorExtractionThread;
import sgpae.extractor.data.DataSet;
import sgpae.extractor.data.Interval;
import sgpae.extractor.data.Transaction;
import sgpae.extractor.data.Valeur;
import sgpae.manifest.Manifest.Couple;
import sgpae.tree.creator.FpTree;
import sgpae.tree.creator.Node;

public class Test
{
	public static void main(String[] args) throws BadAttributeValueExpException, FileNotFoundException, InterruptedException
	{
		System.out.println("Test");
		int min = 17;
		int max = 19;
		int index = (min+max)/2;
		char car= (char)('a'+(index-1));
		String string = car+"";
		System.out.println(index);
		System.out.println(string);
		System.out.println("r".compareTo(string));
		
		//testTransaction();
		
		//testDataSetExtraction();
		
		//testInterval();
		
		//testErosion();
	
		//testThreadErosion();
		
		//testTree();
		
		//testFusion();
		
		//testSimilarite();
		
	}
	
	private static void testSimilarite() throws BadAttributeValueExpException, FileNotFoundException, InterruptedException
	{
		System.out.println("-------------------------------------------------------------\n"
				+ "Test Similarite\n"
				+ "-------------------------------------------------------------");
		
		DataSet dataset;
		
		float beta;
		String target;
		int nbCore;
		target = "heart_rate";
		dataset = Extractor.retrieveData("data/Body.csv");			
		dataset.compileData("heart_rate");
		beta=0.4f;
		nbCore = 1;
		
		Manifest.dataset = dataset;
		
		ArrayList<Interval> selectors = Manifest.make1Intervals(beta,target);
		
		/*
		for(int i = 0; i < selectors.size();i++)
		{
			Interval inter = selectors.get(i);
			
			System.out.println("index : " + i + " -> " + inter + " : " + inter.getNbElement());
		}
		*/
		
		Interval test1 =  selectors.get(0);
		Interval test2 =  selectors.get(1);
		
		System.out.println(test1);
		
		System.out.println(test2);
		
		System.out.println(test1.isSimilar(test2));
		
		System.out.println("nb selector avant : " + selectors.size());
		
		for(int i = 0; i < selectors.size(); i++)
		{
			Interval selector = selectors.get(i);
			for(int j = i+1; j < selectors.size();)
			{
				Interval candidat = selectors.get(j);
				if(candidat.isSimilar(selector))
				{
					selectors.remove(j);
					continue;
				}
				j++;
			}
		}
		
		System.out.println("nb selector après : " + selectors.size());
		
	}
	
	private static void testFusion() throws BadAttributeValueExpException, FileNotFoundException, InterruptedException
	{
		System.out.println("-------------------------------------------------------------\n"
				+ "Test Interval\n"
				+ "-------------------------------------------------------------");
		Interval inter = new Interval("Pluie");
		Interval inter2 = new Interval("Température");
		DataSet set = Extractor.retrieveData("data/SubGroupTest.csv");
		set.compileData("Tonnage");
		for(int i = 0; i < set.getNbPositiveTransactions(); i++)
		{
			Transaction trans = set.getPositiveTransactions(i);
			inter.addTransaction(trans);
			inter2.addTransaction(trans);
		}
		
		ArrayList<Interval> listInter = inter.makeGeration1();

		for(Interval i : listInter)
		{
			System.out.println(i);	
		}
		
		Interval test1=listInter.get(0).childFromInnerRight();
		Interval test2=listInter.get(3);
		//Interval test3=listInter.get(0).childFromExternLeft().childFromInnerRight().childFromInnerRight();
		Interval test3=listInter.get(2).childFromExtenRight().childFromExtenRight();
		
		Couple<Float, Float> res = Interval.subfusion(5, 10, 5, 10);
		
		System.out.println(res);
		
		System.out.println("-*-*-*-*-*-*-*-*-*-*-*");
		
		System.out.println(test1);
		
		System.out.println(test2);
		
		System.out.println(test1.fusion(test2));
		
		System.out.println(test3);
		
		System.out.println(test3.fusion(test1.fusion(test2)));
		
	}
	
	private static void testTree() throws FileNotFoundException, BadAttributeValueExpException, InterruptedException
	{
		System.out.println("-------------------------------------------------------------\n"
				+ "Test Fp-Tree\n"
				+ "-------------------------------------------------------------");
		
		
		float beta;
		String target;
		target = "Tonnage";
		DataSet dataset = Extractor.retrieveData("data/SubGroupTest.csv");			
		dataset.compileData("Tonnage");
		beta=0.5f;
		
		Manifest.dataset = dataset;
		
		ArrayList<Interval> selectors = Manifest.make1Intervals(0.5f,target);
		
		
		FpTree fptree= new FpTree(selectors, target,dataset);
		FpTree fptree1= new FpTree(selectors, target,dataset);
		FpTree fptree2= new FpTree(selectors, target,dataset);
		
		/*
		for(Interval inter : selectors)
		{
			System.out.println(inter + " : " + inter.getNbElement());
		}
		//*/
		
		int nbTrans = dataset.getNbTransactions();
		
		for(int index = 0; index < nbTrans ; index++ )
		{
			Transaction trans = dataset.getTransactions(index);
			fptree.addTransaction(trans);
			if(index<5)
			{
				fptree1.addTransaction(trans);				
			}
			else
			{
				fptree2.addTransaction(trans);
			}
//			System.out.println(trans);
//			System.out.println(fptree);
//			System.out.println("\n\n\n");
		}
		
		/*
		System.out.println(fptree1);
		System.out.println(fptree2);
		System.out.println(fptree);
		//*/
		fptree1.addTree(fptree2);
		//System.out.println(fptree1);
		ArrayList<String> listElement = new ArrayList<String>();
		Hashtable<String, Couple<Float, Integer>> counter = new Hashtable<String, Couple<Float, Integer>>();
		
		/*
		
		for(int i = 0; i < selectors.size(); i++)
		{
			Interval selector = selectors.get(i);
			//System.out.println("Selector : " + selector.toString() + "("+i+"/"+selectors.size()+")");
			String intervalStr = selector.toString();
			int nbNode = fptree.getSelectorPos(intervalStr).size();
			for(Node actNode : fptree.getSelectorPos(intervalStr))
			{
				int nbApparition = actNode.getNbApparition();
				float score = actNode.getScore();
				ArrayList<String> listOnBranch = new ArrayList<String>();
				Node parent = actNode.getParent();
				while(parent.getInterval()!=null)
				{
					listOnBranch.add(0,parent.getInterval().toString());
					parent=parent.getParent();
				}
				for(String str : Manifest.addScoreOfBranch(listOnBranch, 0, new ArrayList<String>()))
				{
					String key = str+intervalStr;
					//System.out.println("  "+ key + " : " + score + "/" + nbApparition);
					Couple<Float,Integer> count = counter.get(key);
					if(count==null)
					{
						count = new Couple<Float, Integer>(0.0f, 0);
						counter.put(key,count);
						listElement.add(key);
					}
					count.valeur1+=score;
					count.valeur2+=nbApparition;
				}
			}
		}
		System.out.println(listElement.size());
		for(String subGroup : listElement)
		{
			int nbApparition = counter.get(subGroup).valeur2;
			float scoreSum = counter.get(subGroup).valeur1;
			float score = (float) (Math.sqrt(nbApparition)*( scoreSum/nbApparition - dataset.getMean()) );
			System.out.println(subGroup + " : " + score);
		}
		
		/*Node node = fptree.getSelectorPos("[19;19 U 22;32]").get(0);
		
		//System.out.println(node.getScore() + " " + node.getNbApparition());
		Node parent = node.getParent();
		
		while(parent.getInterval()!=null)
		{
			node = parent;
			parent=parent.getParent();
		}
		System.out.println("------------------");
		System.out.println(node.getInterval());
		*/
		/*
		for(int i = 0; i < fptree.getSelectorPos("[960;1740]").size() ; i ++)
		{
			Node node = fptree.getSelectorPos("[960;1740]").get(i);
			int nbNew = node.getNbApparition();
			while(node.getInterval()!=null)
			{
				String strInterval = node.getInterval().toString();
				Integer nbElement = counter.get(strInterval);
				if(nbElement==null)
				{
					listElement.add(strInterval);
					counter.put(strInterval, 0);
					nbElement=0;
				}
				counter.put(strInterval, nbElement+nbNew);
				node = node.getParent();
			}
		}
		
		for(String str : listElement)
		{
			System.out.println(str + " -> " +  counter.get(str));
		}
		//*/
		
		
	}
	
	private static void testThreadErosion() throws FileNotFoundException, BadAttributeValueExpException, InterruptedException
	{
		System.out.println("-------------------------------------------------------------\n"
				+ "Test Thread Erosion\n"
				+ "-------------------------------------------------------------");
		
		
		int nbThread = 4;
		String target = "Tonnage";
		ArrayList<SelectorExtractionThread> extracThreads = new ArrayList<SelectorExtractionThread>();
		
		DataSet set = Extractor.retrieveData("data/SubGroupTest.csv");
		set.compileData(target);
		int nbAttr = set.getNbAttributs();
		ArrayList<Interval> selectors;
		for(int i = 0; i < nbAttr; i++)
		{
			String attribut = set.getAttributs(i);
			if(!attribut.equals(target))
			{
				Interval inter = new Interval(attribut,set,"Tonnage");
				SelectorExtractionThread extracThread = new SelectorExtractionThread(inter,5);
				extracThreads.add(extracThread);
				extracThread.start();
			}
		}
		for(SelectorExtractionThread extractThread : extracThreads)
		{
			extractThread.join();
		}
		
		for(SelectorExtractionThread extractThread : extracThreads)
		{
			for(Interval inter : extractThread.selectors)
			{
				System.out.println(inter.getNbElement());
			}
		}
	}
	
	private static void testErosion() throws BadAttributeValueExpException, FileNotFoundException
	{
		System.out.println("-------------------------------------------------------------\n"
				+ "Test Erosion\n"
				+ "-------------------------------------------------------------");
		DataSet set = Extractor.retrieveData("data/SubGroupTest.csv");
		set.compileData("Tonnage");
		Interval inter = new Interval("Pluie",set,"Tonnage");
//		System.out.println(inter);
		ArrayList<Interval> selectors = new ArrayList<Interval>();
		selectors.add(inter);
		HashSet<String> hash = new HashSet<String>();
		hash.add(inter.toString());
		Interval.EROSION(inter, selectors, 5, hash);
		for(Interval selector : selectors)
		{
			System.out.println(selector);
		}
		
	}
	
	private static void testInterval() throws BadAttributeValueExpException, FileNotFoundException
	{
		System.out.println("-------------------------------------------------------------\n"
				+ "Test Interval\n"
				+ "-------------------------------------------------------------");
		Interval inter = new Interval("Pluie");
		Interval inter2 = new Interval("Température");
		DataSet set = Extractor.retrieveData("data/SubGroupTest.csv");
		set.compileData("Tonnage");
		for(int i = 0; i < set.getNbPositiveTransactions(); i++)
		{
			Transaction trans = set.getPositiveTransactions(i);
			inter.addTransaction(trans);
			inter2.addTransaction(trans);
		}
		
		ArrayList<Interval> listInter = inter.makeGeration1();
		Interval interTest = listInter.get(3);
		
		
		System.out.println(interTest);
		
		for(int i = 0; i < set.getNbPositiveTransactions(); i++)
		{
			Transaction trans = set.getPositiveTransactions(i);
			System.out.println(trans);
			System.out.println(interTest.isTransactionValid(trans));
		}
		
		ArrayList<Interval> interTestChild = interTest.makeChildren();
		
		System.out.println(interTest);
		
		for(Interval testChild : interTestChild)
		{
			System.out.println(testChild);	
		}
		
		System.out.println("...........................................................");
		
		Interval interLeft = inter.childFromInnerRight();
		interLeft = interLeft.childFromInnerRight();
		interLeft = interLeft.childFromInnerRight();
		
		System.out.println(inter.listOfTransaction());
		System.out.println("------------------");
		System.out.println(interLeft.listOfTransaction());
		
		System.out.println("------------------");
		System.out.println(interLeft);
		for(int i = 0 ; i < inter2.getNbElement() ; i++)
		{
			Transaction trans = inter2.getTransaction(i);
			System.out.println(trans + " : " + interLeft.isTransactionValid(trans));
		}
	}
	
	private static void testDataSetExtraction() throws BadAttributeValueExpException, FileNotFoundException
	{
		System.out.println("-------------------------------------------------------------\n"
				+ "Test Dataset Extraction\n"
				+ "-------------------------------------------------------------");
		DataSet set = Extractor.retrieveData("data/SubGroupTest.csv");
		Extractor.retrieveData("data/SubGroupTest.csv");
		int nbTransaction = set.getNbTransactions();
		System.out.println(set.listAttributToString());
		System.out.println("Nombre de transaction : " + nbTransaction);
		set.compileData("Tonnage");
		int nbPos = set.getNbPositiveTransactions();
		System.out.println(nbPos);
		
		for(int i = 0; i < nbPos ; i++)
		{
			System.out.println(set.getPositiveTransactions(i));
		}
	}
	
	private static void testTransaction() throws BadAttributeValueExpException
	{
		System.out.println("-------------------------------------------------------------\n"
				+ "Test Transaction\n"
				+ "-------------------------------------------------------------");
		ArrayList<Valeur> list = new ArrayList<Valeur>();
		ArrayList<Valeur> list2 = new ArrayList<Valeur>();

		//*
		Valeur temp = new Valeur("Temperature", 26);
		list.add(temp);
		//*/
		
		//*
		Valeur temp2 = new Valeur("Temperature", 22);
		list2.add(temp2);
		//*/
		
		//*
		Valeur rain = new Valeur("Rainfall", 1450);
		list.add(rain);
		//*/

		//*
		Valeur rain2 = new Valeur("Rainfall", 850);
		list2.add(rain2);
		//*/
		
		ArrayList<String> attributs = new ArrayList<String>();
		attributs.add("Temperature");
		attributs.add("Rainfall");
		Transaction trans = new Transaction(attributs, list);
		Transaction trans2 = new Transaction(attributs, list2);
		System.out.println(trans);
		System.out.println(trans2);
	}
}
