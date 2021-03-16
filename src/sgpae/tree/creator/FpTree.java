package sgpae.tree.creator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import sgpae.Subgroup;
import sgpae.extractor.data.DataSet;
import sgpae.extractor.data.Interval;
import sgpae.extractor.data.Transaction;
import sgpae.manifest.Manifest;

public class FpTree
{
	private final Node racine;
	
	private final ArrayList<Interval> selectors;
	
	private final ArrayList<String>[] complexSelectors;
	
	private final Hashtable<String, ArrayList<Node>> selectorsPos;

	private final String target;
	
	public FpTree(ArrayList<Interval> selectors, String target,DataSet dataset)
	{
		super();
		this.racine = new Node(target);
		this.selectors = selectors;
		
		int nbAttributs=dataset.getNbAttributs();
		
		this.complexSelectors = new ArrayList[nbAttributs];
		for(int i =0;i<nbAttributs;i++)
		{
			complexSelectors[i] = new ArrayList<String>();
		}
		selectorsPos = new Hashtable<String, ArrayList<Node>>();
		/*
		for(Interval selector : selectors)
		{
			String label = selector.toString();
			ArrayList<Node> newListNode = new ArrayList<Node>();
			selectorsPos.put(label, newListNode);
		}
		*/
		this.target = target;
	}
	
	/**SemiBrute Method*/
	public void addTransactionV2_1(Transaction trans)
	{
		DataSet dataset = Manifest.dataset;
		ArrayList<Node> actNodes = new ArrayList<Node>();
		actNodes.add(racine);
		Hashtable<String, ArrayList<ArrayList<Interval>>> listInterval = createCombi(trans,selectors);
		for(int numAttribut = 0; numAttribut < dataset.getNbAttributs();numAttribut++)
		{
			//System.out.println("nbAttribut : "+numAttribut+"/"+dataset.getNbAttributs());
			String attribut = dataset.getAttributs(numAttribut);
			ArrayList<ArrayList<Interval>> attrListInterval = listInterval.get(attribut);
			
			if(attrListInterval==null || attrListInterval.size()==0)
			{
				continue;
			}
			
			ArrayList<Node> newNodes = new ArrayList<Node>();
			int nbAttrListInterval = 0;
			int maxAttrListInterval = attrListInterval.size();
			
			//System.out.println("Parcours de la liste des interval de cet attribut");
			for(ArrayList<Interval> s : attrListInterval)
			{
				//System.out.println("nbAttr:"+numAttribut+"/"+dataset.getNbAttributs() + " | nbCombi" +nbAttrListInterval+"/"+maxAttrListInterval);
				nbAttrListInterval++;
				String key1=Interval.toKey(s);
				int nbNode = 0;
				int maxNode = actNodes.size();
				//System.out.println("Parcours des noeuds");
				for(Node actNode : actNodes)
				{
					nbNode++;
					//System.out.println("Node :" + nbNode + "/" + maxNode);
					//Node sonNode = Node.mapNode.get(actNode.getClef()+key1);
					Node sonNode = null;
					/*
					loop4:
					for(Node child : actNode.getChildren())
					{
						String key2=Interval.toKey(child.getInterval());
						if(key1.equals(key2))
						{
							sonNode=child;
							child.addOne(trans);
							break loop4;
						}
					}
					//*/
					
					Object indexNode = actNode.findChild(actNode.getClef()+key1);
					
					//if(sonNode!=null)
					if(indexNode instanceof Node)
					{
						sonNode=(Node)indexNode;
						sonNode.addOne(trans);
					}
					else
					{
						Integer index = (Integer)indexNode;
						sonNode=new Node(actNode, s, trans, target);
						actNode.addChildren(sonNode);
						ArrayList<Node> listNode = selectorsPos.get(key1);
						if(listNode==null)
						{
							listNode=new ArrayList<Node>();
							selectorsPos.put(key1, listNode);
							complexSelectors[numAttribut].add(key1);
						}
						listNode.add(sonNode);
					}
					newNodes.add(sonNode);
				}
			}
			//actNodes.addAll(newNodes);
			actNodes=newNodes;
		}
	}
	
	public static Hashtable<String, ArrayList<ArrayList<Interval>>> createCombi(Transaction trans, ArrayList<Interval> selectors)
	{
		//TODO Lister en amont les combinaisons possible et trier les cas similaires
		Hashtable<String, ArrayList<ArrayList<Interval>>> listInterval = new Hashtable<String, ArrayList<ArrayList<Interval>>>();
		HashSet<String> existInterval= new HashSet<String>();
		for(Interval selector : selectors)
		{
			if(selector.isTransactionValid(trans))
			{
				String attribut = selector.getAttribut();
				ArrayList<ArrayList<Interval>> listSelector;
				listSelector = listInterval.get(attribut);
				if(listSelector==null)
				{
					listSelector=new ArrayList<ArrayList<Interval>>();
					listInterval.put(attribut, listSelector);
				}
				int previousNbElements = listSelector.size();
				for(int i = 0; i < previousNbElements;i++)
				{
					ArrayList<Interval> valid = listSelector.get(i);
					ArrayList<Interval> candidat = selector.fusion(valid);
					String key = Interval.toKey(candidat);
					if(!existInterval.contains(key)&&candidat.size()<3)
					{
						existInterval.add(key);
						listSelector.add(candidat);
					}
				}
				ArrayList<Interval> list = new ArrayList<Interval>();
				list.add(selector);
				String key = Interval.toKey(list);
				if(!existInterval.contains(key))
				{
					existInterval.add(key);
					listSelector.add(list);
				}
			}
		}
		System.gc();
		return listInterval;
	}
	
//	public static Hashtable<String, ArrayList<Subgroup>> createCombi(ArrayList<Transaction> transactions, ArrayList<Interval> selectors)
//	{
//		//TODO Lister en amont les combinaisons possible et trier les cas similaires
//		Hashtable<String, ArrayList<Subgroup>> listInterval = new Hashtable<String, ArrayList<Subgroup>>();
//		HashSet<String> existInterval= new HashSet<String>();
//		for(Interval selector : selectors)
//		{
//				String attribut = selector.getAttribut();
//				ArrayList<Subgroup> listSelector;
//				listSelector = listInterval.get(attribut);
//				if(listSelector==null)
//				{
//					listSelector=new ArrayList<Subgroup>();
//					listInterval.put(attribut, listSelector);
//				}
//				int previousNbElements = listSelector.size();
//				for(int i = 0; i < previousNbElements;i++)
//				{
//					Subgroup valid = listSelector.get(i);
//					ArrayList<Interval> candidat = selector.fusion(valid.intervals);
//					Subgroup combi = new Subgroup(candidat, 0, 0);
//					for(Transaction trans : transactions)
//					{
//						//combi.
//					}
//					String key = Interval.toKey(candidat);
//					if(!existInterval.contains(key)&&candidat.size()<3)
//					{
//						existInterval.add(key);
//						listSelector.add(candidat);
//					}
//				}
//				ArrayList<Interval> list = new ArrayList<Interval>();
//				list.add(selector);
//				String key = Interval.toKey(list);
//				if(!existInterval.contains(key))
//				{
//					existInterval.add(key);
//					listSelector.add(list);
//				}
//		}
//		System.gc();
//		return listInterval;
//	}
	
	public void addTransaction(Transaction trans)
	{
		Node actNode = racine;
		for(Interval selector : selectors)
		{
			boolean isValid = selector.isTransactionValid(trans);
			if(isValid)
			{
				boolean exist=false;
				for(Node child : actNode.getChildren())
				{
					if(child.getInterval().equals(selector))
					{
						System.out.println("Treated : "+trans.getID() + " with " + selector);
						child.addOne(trans);
						exist=true;
						actNode=child;
						break;
					}
				}
				if(!exist)
				{
					Node child = new Node(actNode, selector, trans, target);
					actNode.addChildren(child);
					ArrayList<Node> listNode = selectorsPos.get(selector.toString());
					listNode.add(child);
					actNode=child;
				}
			}
			//System.out.println( trans + " -> "+ selector + " : " +  isValid);
		}
	}
	
	
	public void addTree(FpTree otherTree)
	{
		this.racine.addNode(otherTree.racine);
	}
	
	public String selectorsToString()
	{
		String str = "";
		for(Interval inter : selectors)
		{
			System.out.println(inter);
		}
		return str;
	}
	
	public String toString()
	{
		return racine.toString(0);
	}

	public ArrayList<Node> getSelectorPos(String str)
	{
		try
		{
			ArrayList<Node> selectorPos = (ArrayList<Node>) selectorsPos.get(str).clone(); 
			return selectorPos;
		}
		catch(NullPointerException e)
		{
			return null;
		}
	}
	
	public ArrayList<String> getComplexSelector(int numAttribut)
	{
		try
		{
			ArrayList<String> selectorPos = (ArrayList<String>) complexSelectors[numAttribut].clone(); 
			return selectorPos;
		}
		catch(NullPointerException e)
		{
			return null;
		}
	}
}
