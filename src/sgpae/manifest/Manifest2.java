package sgpae.manifest;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.management.BadAttributeValueExpException;

import sgpae.Subgroup;
import sgpae.extractor.Extractor;
import sgpae.extractor.SelectorExtractionThread;
import sgpae.extractor.data.DataSet;
import sgpae.extractor.data.Interval;
import sgpae.extractor.data.Transaction;
import sgpae.manifest.Manifest2.Couple;
import sgpae.tree.creator.FpTree;
import sgpae.tree.creator.Node;
import sgpae.tree.creator.SubTreeCreatorThread;

public class Manifest2
{
	public static DataSet dataset;
	
	/**
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 * @throws BadAttributeValueExpException
	 * @throws InterruptedException 
	 * 
	 * The first parameter need to be the path to the data file
	 * The second one have to be the label of the target attribute
	 * The third attribute need to be a threshold beta to make the 1-intervalles
	 * The fourth parameters have to be the number of core
	 */
	
	public static void main(String[] args) throws FileNotFoundException, BadAttributeValueExpException, InterruptedException
	{
		float beta;
		String target;
		int nbCore;
		if(args.length!=4)
		{
			/*
			target = "Tonnage";
			dataset = Extractor.retrieveData("data/Body.csv");			
			dataset.compileData("Tonnage");
			beta=0.5f;
			nbCore = 1;
			*/
			target = "heart_rate";
			dataset = Extractor.retrieveData("data/Body.csv");			
			dataset.compileData("heart_rate");
			beta=0.8f;
			nbCore = 1;
		}
		else
		{
			target = args[1];
			dataset = Extractor.retrieveData(args[0]);
			dataset.compileData(args[1]);
			beta=Float.parseFloat(args[2]);
			nbCore = Integer.parseInt(args[3]);
		}
		
		ArrayList<Interval> selectors = make1Intervals(beta,target);
		
		/*
		for(Interval inter : selectors)
		{
			System.out.println(inter + " : " + inter.getNbElement());
		}
		//*/
		
		FpTree tree = makeTree(selectors, dataset.getTransactions(), target, nbCore);
		//System.out.println(tree);
		ArrayList<String> listElement = new ArrayList<String>();
		ArrayList<String> actList = new ArrayList<String>();
		Hashtable<String, Couple<Float, Integer>> counter = new Hashtable<String, Couple<Float, Integer>>();
//		finish(selectors, tree, listElement, counter);
	}
	
//	public static void finish(ArrayList<Interval> selectors, FpTree fptree, ArrayList<String> listElement, Hashtable<String, Couple<Float, Integer>> counter)
//	{
//		ArrayList<Subgroup> subgroups = new ArrayList<Subgroup>();
//		for(int i = selectors.size()-1; i >= 0; i--)
//		{
//			Interval selector = selectors.get(i);
//			//System.out.println("Selector : " + selector.toString() + "("+i+"/"+selectors.size()+")");
//			String intervalStr = selector.toString();
//			int nbNode = fptree.getSelectorPos(intervalStr).size();
//			//System.out.println(nbNode);
//			for(Node actNode : fptree.getSelectorPos(intervalStr))
//			{
//				int nbApparition = actNode.getNbApparition();
//				float score = actNode.getScore();
//				ArrayList<Interval> listOnBranch = new ArrayList<Interval>();
//				Node parent = actNode.getParent();
//				while(parent.getInterval()!=null)
//				{
//					listOnBranch.add(0,parent.getInterval());
//					parent=parent.getParent();
//				}
//				/*
//				if(i<90)
//				{
//					System.out.println(selectors.size());
//					continue;
//				}
//				//*/
//				//System.out.println(listOnBranch.size());
//				ArrayList<String> actList = new ArrayList<String>();
//				for(ArrayList<Interval> inters : Manifest2.addScoreOfBranch(listOnBranch, 0, actList))
//				{
//					String key = "";
//					int count1=0;
//					for(Interval inter : inters)
//					{
//						if(inter.getAttribut()==selector.getAttribut())
//						{
//							try
//							{
//								key+=inter.fusion(selector).get(0);
//								count1+=1;
//							}
//							catch(IndexOutOfBoundsException e)
//							{
//							}
//						}
//						else
//						{
//							key+=inter;
//						}
//					}
//					if(count1==0)
//					{
//						key+=selector;
//					}
//					
//					//System.out.println("  "+ key + " : " + score + "/" + nbApparition);
//					Couple<Float,Integer> count = counter.get(key);
//					if(count==null)
//					{
//						count = new Couple<Float, Integer>(0.0f, 0);
//						counter.put(key,count);
//						listElement.add(key);
//					}
//					count.valeur1+=score;
//					count.valeur2+=nbApparition;
//				}
//			}
//			System.err.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\naaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\naaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\naaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\naaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\naaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n");
//			
//			for(String subGroup : listElement)
//			{
//				int nbApparition = counter.get(subGroup).valeur2;
//				float scoreSum = counter.get(subGroup).valeur1;
//				float score = scoreSum/nbApparition;
//				System.out.println(subGroup + " : " + score);
//			}
//		}
//	}
	
	public static FpTree makeTree(ArrayList<Interval> selectors, ArrayList<Transaction> transactions, String target, int nbCore) throws InterruptedException
	{
		FpTree tree = new FpTree(selectors, target,dataset);
		ArrayList<SubTreeCreatorThread> listThread = new ArrayList<SubTreeCreatorThread>();
		for(int i = 0; i < nbCore; i++)
		{
			int indexMin = i*dataset.getNbTransactions()/nbCore;
			int indexMax = (i+1)*dataset.getNbTransactions()/nbCore;
			SubTreeCreatorThread thread = new SubTreeCreatorThread(indexMin, indexMax, selectors, target, transactions,dataset);
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
	
	public static ArrayList<Interval> make1Intervals(float beta, String target) throws InterruptedException
	{
		int nbAttr = dataset.getNbAttributs();
		
		ArrayList<SelectorExtractionThread> extracThreads = new ArrayList<SelectorExtractionThread>();
		
		ArrayList<Interval> selectors = new ArrayList<Interval>();
		
		for(int i = 0; i < nbAttr; i++)
		{
			String attribut = dataset.getAttributs(i);
			if(!attribut.equals(target))
			{
				Interval inter = new Interval(attribut,dataset,"Tonnage");
				SelectorExtractionThread extracThread = new SelectorExtractionThread(inter,1);
				extracThreads.add(extracThread);
				extracThread.start();
			}
		}
		//TODO Adapter les threads
		for(SelectorExtractionThread extractThread : extracThreads)
		{
			extractThread.join();
		}
		
		for(SelectorExtractionThread extractThread : extracThreads)
		{
			for(Interval inter : extractThread.selectors)
			{
				int nbElements = inter.getNbElement();
				if(selectors.size()==0)
				{
					selectors.add(inter);
				}
				else
				{
					int place = 0;
					for(Interval inter2 : selectors)
					{
						if(inter2.getNbElement()<inter.getNbElement())
						{
							break;
						}
						place++;
					}
					selectors.add(place,inter);
				}
			}
		}
		
		return selectors;
	}
	
	/**
	 * @return Une liste de listes d'intervalles. Chaque sous-list correspond à un sous-groupe
	 */
	public static ArrayList<ArrayList<Interval>> addScoreOfBranch(ArrayList<Interval> intervals, int index, ArrayList<String> actList)
	{
		/**List a retourner*/
		ArrayList<ArrayList<Interval>> list = new ArrayList<ArrayList<Interval>>();
		//*
		/**Si on a pas fini la list intervals ...*/
		if(index<intervals.size())
		{
			/**Interval actuel*/
			Interval actInter = intervals.get(index);
			//System.out.println(intervals.get(index));
			/**On parcours l'ensemble des sous-groupes possibles et on ajoute des duplicata avec le nouvel interval*/
			for(ArrayList<Interval> subList : addScoreOfBranch(intervals,index+1,actList))
			{
				ArrayList<Interval> newList = new ArrayList<Interval>();
				/**On procède à une fusion des éléments ayant le même attribut*/
				int count=0;
				for(Interval interval : subList)
				{
					if(interval.getAttribut().equals(actInter.getAttribut()))
					{
						newList.addAll(actInter.fusion(interval));
						count++;
					}
					else
					{
						newList.add(interval);
					}
				}
				/**Si il n'y a aucune fusion, on ajouter le nouvel élement*/
				if(count==0)
				{
					newList.add(actInter);
				}
				list.add(newList);
				list.add(subList);
			}
		}
		else
		{
			list.add(new ArrayList<Interval>());
		}
		
		//System.out.println(list.size());
		//*/
		return list;
	}
	
	/*
	public static ArrayList<String> addScoreOfBranch(ArrayList<String> intervals, int index, ArrayList<String> actList)
	{
		ArrayList<String> list = new ArrayList<String>();
		//*
		if(index<intervals.size())
		{
			//System.out.println(intervals.get(index));
			for(String str : addScoreOfBranch(intervals,index+1,actList))
			{
				list.add(str);
				list.add(intervals.get(index) + str);
			}
		}
		else
		{
			list.add("");
		}
		//System.out.println(list.size());
		//*
		return list;
	}
	//*/
	
	public static class Couple<T,U>
	{
		public T valeur1;
		public U valeur2;
		
		public Couple(T valeur1, U valeur2)
		{
			this.valeur1 = valeur1;
			this.valeur2 = valeur2;
		}
		
		@Override
		public String toString()
		{
			return "<" + valeur1 + "," + valeur2 + ">";
		}
	}
}
