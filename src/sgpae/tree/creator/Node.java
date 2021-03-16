package sgpae.tree.creator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;

import sgpae.extractor.data.Interval;
import sgpae.extractor.data.Transaction;

public class Node implements Comparable<Node>
{
	
	private Node parent;
	private final String clef;
	private ArrayList<Interval> interval;
	private ArrayList<Node> children;
	//private HashMap<String,Node> mapChildren;
	private int nbApparition;
	private float score;
	private String target;
	private ArrayList<Transaction> listTrans;
	
	public Node(String target)
	{
		super();
		this.parent = null;
		this.interval = null;
		children = new ArrayList<Node>();
		clef="";
		//mapChildren = new HashMap<String,Node>();
		this.target = target;
		nbApparition=0;
		listTrans = new ArrayList<Transaction>();
	}
	
	public Node(Node parent, Interval interval, Transaction trans, String target)
	{
		super();
		this.parent = parent;
		this.interval=new ArrayList<Interval>();
		this.interval.add(interval);
		this.clef=parent.getClef()+Interval.toKey(this.interval);
		children = new ArrayList<Node>();
		//mapChildren = new HashMap<String,Node>();
		this.target = target;
		nbApparition=0;
		score = 0;
		listTrans = new ArrayList<Transaction>();
		addOne(trans);
	}
	
	public Node(Node parent, ArrayList<Interval> interval, Transaction trans, String target)
	{
		super();
		this.parent = parent;
		this.clef=parent.getClef()+ Interval.toKey(interval);
		this.interval=interval;
		children = new ArrayList<Node>();
		//mapChildren = new HashMap<String,Node>();
		this.target = target;
		nbApparition=0;
		score = 0;
		listTrans = new ArrayList<Transaction>();
		addOne(trans);
	}

	public Node getParent()
	{
		return parent;
	}
	
	public ArrayList<Interval> getInterval()
	{
		return interval;
	}

	public ArrayList<Node> getChildren()
	{
		@SuppressWarnings("unchecked")
		ArrayList<Node> copyChildren=(ArrayList<Node>)children.clone();
		return copyChildren;
	}
	
	public Object findChild(String clef)
	{
		Integer min = 0;
		Integer max = children.size();
		Node actChild = null;
		while(max-min>1)
		{
			int index = (min+max)/2;
			actChild = children.get(index);
			int compare = clef.compareTo(actChild.clef);
			if(compare>0)
			{
				min=index;
			}
			else if(compare<0)
			{
				max=index;
			}
			else
			{
				return actChild;
			}
		}
		return min+1;
	}

	//**V2
	public void addChildren(Node child)
	{
		if(children.size()==0)
		{
			this.children.add(child);
			return;
		}
		Object objIndex=findChild(clef);
		if(!(objIndex instanceof Integer))
		{
			return;
		}
		Integer index = (Integer)objIndex;
		this.children.add(index,child);
	}
	//*/
	
	/**classic
	public void addChildren(Node child)
	{
		this.children.add(child);
		String clef = Interval.toKey(child.interval);
		Collections.sort(this.children);
		//this.mapChildren.put(clef, child);
	}
	//*/

	public void addOne(Transaction trans)
	{
		score += (Float)(trans.getValue(target).getValue());
		nbApparition++;
		listTrans.add(trans);
	}
	
	public void addNode(Node otherNode)
	{
		/*
		for(Node child : children)
		{
			System.out.println(child.interval.toString() + "->" + child.nbApparition);
		}
		System.out.println("*-*-*-*-*");
		for(Node child : otherNode.children)
		{
			System.out.println(child.interval.toString() + "->" + child.nbApparition);
		}
		System.out.println("************");
		//*/
		if(		((interval==null) && (otherNode.interval==null) ) ||
				(Interval.toKey(interval).equals(Interval.toKey(otherNode.interval))))
		{
			nbApparition+=otherNode.nbApparition;
			Hashtable<String, Node> hash = new Hashtable<String, Node>();
			int i = 0;
			outerLoop :
			for(Node otherchild : otherNode.children)
			{
				String otherChildString = Interval.toKey(otherchild.interval);
				Node child = hash.get(otherChildString);
				if(child!=null)
				{
					child.addNode(otherchild);
					continue outerLoop;
				}
				for(; i < children.size(); i++)
				{
					child = children.get(i);
					String childString = Interval.toKey(child.interval);
					hash.put(childString, child);
					if(childString.equals(otherChildString))
					{
						child.addNode(otherchild);
						continue outerLoop ;
					}
				}
				addChildren(otherchild);
			}
			
		}
		/*for(Node child : children)
		{
			System.out.println(child.interval + "->" + child.nbApparition);
		}
		*/
	}
	
	public String toString()
	{
		return this.toString(0);
	}
	public String toString(int level)
	{
		
		String str = "";
		for(int i = 0 ; i < level ; i++)
		{
			str+="| ";
		}
		str+=Interval.toKey(interval)+" : " + nbApparition + ", " + score + "\n";
		//str+=Interval.toKey(interval)+ "\n";
		for(Node child : children)
		{
			if(child!=null)
			{
				str+=child.toString(level+1);
			}
		}
		return str;
	}

	/*public Node getPathTo()
	{
		Node actNode = this;
		int nbApparition = actNode.nbApparition;
		Node ancNode=null;
		while(actNode.interval!=null)
		{
			Node newNode = new Node(null, actNode.interval);
			newNode.nbApparition = nbApparition;
			if(ancNode!=null)
			{
				ancNode.parent = newNode;
				newNode.children.add(ancNode);
			}
			ancNode = newNode;
			actNode = actNode.parent;
			
		}
		return ancNode;
	}
	*/
	
	public int getNbApparition()
	{
		return nbApparition;
	}
	
	public float getScore()
	{
		return score;
	}

	public ArrayList<Transaction> getTransaction()
	{
		return (ArrayList<Transaction>) listTrans.clone();
	}

	public String getClef() {
		return clef;
	}

	@Override
	public int compareTo(Node arg0)
	{
		return this.clef.compareTo(arg0.clef);
	}

//	public Node getChild(String key1)
//	{
//		try
//		{
//			return mapChildren.get(key1);
//		}
//		catch(Exception e)
//		{
//			return null;
//		}
//	}
}
