package sgpae.manifest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

import javax.management.BadAttributeValueExpException;

import sgpae.Subgroup;
import sgpae.extractor.Extractor;
import sgpae.extractor.SelectorExtractionThread;
import sgpae.extractor.data.DataSet;
import sgpae.extractor.data.Interval;
import sgpae.extractor.data.Transaction;
import sgpae.extractor.sg.InformationExtractor;
import sgpae.tree.creator.FpTree;
import sgpae.tree.creator.FpTreeCreator;
import sgpae.tree.creator.Node;
import sgpae.tree.creator.SubTreeCreatorThread;

public class Manifest
{
	/**Objet dataset contenant toute les informations de la base de donnée*/
	public static DataSet dataset;
	
	//TODO : Définir ce truc
	public static int counterTest = 0;
	
	/**Objet final pour définir la variable debugMod, i.e. pas d'affichage de mode débug*/
	public static final int DEBUG_FALSE = 0;
	/**Objet final pour définir la variable debugMod, i.e. affichage du texte uniquement dans le mode débug*/
	public static final int DEBUG_PART = 1;
	/**Objet final pour définir la variable debugMod, i.e. affichage du texte, du nom du fichier et du numéro de ligne dans le mode débug*/
	public static final int DEBUG_TRUE = 2;
	
	/**Permet l'affichage des texte à afficher en mode debug via la fonction debugPrint*/
	public static int debugMod=DEBUG_FALSE;
	
	private static Hashtable<String, ArrayList<ArrayList<Interval>>> tableSelector = new Hashtable<String, ArrayList<ArrayList<Interval>>>();
	
	private static String debugText(String str)
	{
		if(debugMod==DEBUG_TRUE)
		{
			StackTraceElement getLine = Thread.currentThread().getStackTrace()[2];
			str = "<"+getLine.getFileName() + " - Line " + getLine.getLineNumber() + "> : " + str;
		}
		return str;
	}
	
	public static void debugPrint(String str, int debugMod, boolean erreur)
	{
		if(debugMod!=DEBUG_FALSE)
		{
			str = debugText(str);
			if(erreur)
			{
				System.err.println(str);
			}
			else
			{
				System.out.println(str);
			}
		}
	}
	
	public static void debugPrint(String str, int debugMod)
	{
		debugPrint(str, debugMod, false);
	}
	
	public static void debugPrint(String str)
	{
		debugPrint(str, debugMod);
	}
	
	public static void debugPrint(Object obj)
	{
		debugPrint(obj.toString());
	}
	
	/**
	 * 
	 * @param args
	 * @throws BadAttributeValueExpException
	 * @throws InterruptedException 
	 * @throws IOException 
	 * 
	 * The first parameter need to be the path to the data file
	 * The second one have to be the label of the target attribute
	 * The third attribute need to be a threshold beta to make the 1-intervalles
	 * The fourth parameters have to be the number of core
	 * 
	 * data/Body.csv heart_rate 0.5 4
	 * data/SubGroupTest.csv Tonnage 0.5 4
	 */
	
	public static void main(String[] args) throws BadAttributeValueExpException, InterruptedException, IOException
	{
		
		StackTraceElement getLine = Thread.currentThread().getStackTrace()[1];
		
		/** Seuil beta que doivent dépasser les 1-intervals*/
		float beta = 0;
		
		/**Label de la target variable*/
		String target = null;
		/**Nombre de coeur utilisés*/
		int nbCore;
		
		//Si les arguments sont incorect, utilisation des valeurs par défauts
		if(args.length!=4)
		{
			System.err.println("Wrong number of arguments");
			System.err.println("Format of the arguments : <datafile.csv> <target> <beta> <nbCore>");
			System.exit(-1);
//			target = "heart_rate";
//			dataset = Extractor.retrieveData("data/Body.csv");			
//			dataset.compileData("heart_rate");
//			beta=0.5f;
//			nbCore = 1;
		}
		//Sinon utilisation normale des paramètres
		else
		{
			target = args[1];
			dataset = Extractor.retrieveData(args[0]);
			dataset.compileData(args[1]);
			beta=Float.parseFloat(args[2]);
			nbCore = Integer.parseInt(args[3]);
		}
		
		/**Liste des sélecteur (ou 1 interval)*/
		ArrayList<Interval> selectors = make1Intervals(beta,target);
		
		selectors = Interval.filterSimilar(selectors);
		
		
			
		/**FP-Tree généré pour la recherche de sous-groupe*/
		FpTree tree = FpTreeCreator.makeTreeV2(selectors, dataset.getTransactions(), target, 1,dataset);
		//System.out.println(tree);
		int NbBest=3;
		ArrayList<InformationExtractor> tops = new ArrayList<InformationExtractor>();
		/**Hashtable contenant, pour chaque sous-group*/
		Hashtable<String, Subgroup> counter = new Hashtable<String, Subgroup>();
		
		for(int numAttribut = 0 ; numAttribut < dataset.getNbAttributs(); numAttribut++)
		{
			for(String selector : tree.getComplexSelector(numAttribut))
			{
				ArrayList<Node> listNode = tree.getSelectorPos(selector);
				InformationExtractor sg = new InformationExtractor(selector, listNode);
				addToTop(sg, tops, NbBest);
				//TODO comparer au bests
				if(sg.getBestScore()>0)
				{
					ArrayList<InformationExtractor> listSubGroup = new ArrayList<InformationExtractor>();
					listSubGroup.add(sg);
					
					for(int level = numAttribut-1;level>-1; level--)
					{
						String actualLabel = Manifest.dataset.getAttributs(level);
						int maxListSubGroup=listSubGroup.size();
						for(InformationExtractor actSg : (ArrayList<InformationExtractor>)listSubGroup.clone())
						{
							if(actSg.getBestScore()<tops.get(NbBest-1).getScore())
							{
								listSubGroup.remove(actSg);
								continue;
							}
							ArrayList<String> listClef = new ArrayList<String>();
							HashMap<String, ArrayList<Node>> nodeMap=new HashMap<String, ArrayList<Node>>();
							for(Node actNode : actSg.listNode)
							{
								Node ancestor = actNode.getParent();
								while(ancestor.getInterval()!=null && !ancestor.getInterval().get(0).getAttribut().equals(actualLabel))
								{
									ancestor=ancestor.getParent();
								}
								if(ancestor.getInterval()!=null)
								{
									String clef = Interval.toKey(ancestor.getInterval())+" | "+actSg.getClef();
									ArrayList<Node> newNodes = nodeMap.get(clef);
									if(newNodes==null)
									{
										newNodes=new ArrayList<Node>();
										nodeMap.put(clef, newNodes);
										listClef.add(clef);
									}
									newNodes.add(actNode);
								}
							}
							for(String clef : listClef)
							{
								ArrayList<Node> actNodes = nodeMap.get(clef);
								InformationExtractor newSg = new InformationExtractor(clef, actNodes);
								addToTop(newSg, tops, NbBest);
								if(newSg.getBestScore()>tops.get(NbBest-1).getScore())
								{
									listSubGroup.add(newSg);
								}
							}
							Manifest.class.getClass();
						}
					}
					
				}
			}
		}
		for(InformationExtractor extr : tops)
		{
			System.out.println(extr.getClef()+ " : " +extr.getScore());
		}
	}
	
	private static void addToTop(InformationExtractor newSg, ArrayList<InformationExtractor> tops, int NbBest)
	{
		if(tops.size()<NbBest || newSg.getScore()>tops.get(NbBest-1).getScore())
		{
			for(int i = 0; i < tops.size();i++)
			{
				if(tops.get(i).getScore()<newSg.getScore())
				{
					tops.add(i, newSg);
					while(tops.size()>NbBest)
					{
						tops.remove(NbBest);
					}
					return;
				}
			}
			tops.add(newSg);
		}
	}
	
	public static ArrayList<Interval> combinedSelectors(ArrayList<Interval> rawSelectors)
	{
		class labeledInterval
		{
			public String label;
			public Interval interval;
			
			public Interval convertToInteval()
			{
				return interval;
			}
			
			public ArrayList<Interval> convertToInteval(ArrayList<labeledInterval> labeledList)
			{
				ArrayList<Interval> unlabeled = new ArrayList<Interval>();
				for(labeledInterval labeled : labeledList)
				{
					unlabeled.add(labeled.convertToInteval());
				}
				return unlabeled;
			}
		}
		/***/
		
		HashSet<String> selectorList = new HashSet<String>();
		ArrayList<labeledInterval> refinedSelectors = new ArrayList<labeledInterval>();
		for(int i=0;i<rawSelectors.size();i++)
		{
			
		}
		ArrayList<Interval> combinedSelectors = new ArrayList<Interval>();
		return combinedSelectors;
	}
	
	public static void newScoreCalculator()
	{
		debugPrint("Place restance");
		debugPrint(Runtime.getRuntime().freeMemory()/(1024.0*1024*1024) + "/" + Runtime.getRuntime().totalMemory()/(1024.0*1024*1024));
	}
	
	public static FpTree makeTree(ArrayList<Interval> selectors, ArrayList<Transaction> transactions, String target, int nbCore) throws InterruptedException
	{
		FpTree tree = new FpTree(selectors, target,dataset);
		ArrayList<SubTreeCreatorThread> listThread = new ArrayList<SubTreeCreatorThread>();
		for(int i = 0; i < nbCore; i++)
		{
			int indexMin = i*dataset.getNbTransactions()/nbCore;
			int indexMax = (i+1)*dataset.getNbTransactions()/nbCore;
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
	
	public static ArrayList<Interval> make1Intervals(float beta, String target) throws InterruptedException
	{
		int nbAttr = dataset.getNbAttributs();
		
		ArrayList<SelectorExtractionThread> extracThreads = new ArrayList<SelectorExtractionThread>();
		
		ArrayList<Interval> selectors = new ArrayList<Interval>();
		
		for(int i = 0; i < nbAttr; i++)
		{
			String attribut = dataset.getAttributs(i);
			
			debugPrint("The Attribut" + attribut);
			
			if(!attribut.equals(target))
			{
				Interval inter = new Interval(attribut,dataset,Manifest.dataset.getTarget());
				SelectorExtractionThread extracThread = new SelectorExtractionThread(inter,(int)(dataset.getNbPositiveTransactions()*beta));
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
			debugPrint("Test");
			for(Interval inter : extractThread.selectors)
			{
				debugPrint("inter :  " + inter);
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
		counterTest++;
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
		float totalMemory = Runtime.getRuntime().totalMemory()/(1024.0f*1024*1024);
		float freeMemory = Runtime.getRuntime().freeMemory()/(1024.0f*1024*1024);
		

		if(freeMemory < 1)
		{
			debugPrint("");
		}
		
		
		debugPrint(counterTest + " : " + freeMemory +"/"+totalMemory);
		counterTest--;
		return list;
		
	}
	
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

	
	public static void printWithLine(String str)
	{
		debugPrint(str);
	}
}
