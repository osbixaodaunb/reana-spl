package parsing.activitydiagrams;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import parsing.ProbabilityEnergyTimeProfile;
import parsing.ProbabilityEnergyTimeProfileReader;
import parsing.exceptions.InvalidTagException;

/**
 * @author andlanna
 *
 */
public class ADReader {
    private static final Logger LOGGER = Logger.getLogger(ADReader.class.getName());

	private int index;
	private String name;
	private boolean next;
	private Map<String, Activity> activitiesByID;
	private Map<String, Edge> edgesByID;
	private Document doc;
	private List<Activity> activities;



	public ADReader(File xmlFile, int index) {
		this.index = index;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			this.doc = db.parse(xmlFile);
			this.doc.getDocumentElement().normalize();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}
	}
	
	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}
	
	public Document getDoc() {
		return doc;
	}
	
	public Map<String, Activity> getActivitiesByID() {
		return activitiesByID;
	}

	public void setActivitiesByID(Map<String, Activity> activitiesByID) {
		this.activitiesByID = activitiesByID;
	}

	public List<Activity> getActivities() {
		return activities;
	}

	public void setActivities(List<Activity> activities) {
		this.activities = activities;
	}

	public boolean hasNext() {
		return next;
	}


	/**
	 * retrieveActivities function is responsible for identifying the activity diagrams
	 * represented in a XMI file, identify all the activities in each activity diagram and
	 * create the trace (link) between each activity and its related sequence diagram.
	 * @throws InvalidTagException
	 */
	public void retrieveActivities() throws InvalidTagException {
		List<org.w3c.dom.Node> adList = new ArrayList<org.w3c.dom.Node>();
		NodeList nodes = this.doc.getElementsByTagName("packagedElement");
		searchThroughNodes(adList, nodes);

		verifyIndexSize(adList);

		org.w3c.dom.Node node = adList.get(this.index);
		this.name = node.getAttributes().getNamedItem("name").getTextContent();
		NodeList childNodes = node.getChildNodes();
		this.activities = new ArrayList<Activity>();

		for (int s = 0; s < childNodes.getLength(); s++) {
			if (childNodes.item(s).getNodeName().equals("node")) { 
				addToActivityList(childNodes, s);
			}
		}
		resetActivitiesByID();
		retrieveEdges(node);
		solveActivities(node);
	}

	private void addToActivityList(NodeList childNodes, int s) {
		Activity newActivity;
		final NamedNodeMap childNodeAttributes = childNodes.item(s).getAttributes();
		newActivity = (Activity) createObject(childNodeAttributes);
		final Node behavior = getItemByName(childNodeAttributes,"behavior");
		setActivitySDId(newActivity, behavior);
		
		this.activities.add(newActivity);
	}

	private void verifyIndexSize(List<org.w3c.dom.Node> adList) {
		if ((this.index + 1) == adList.size())
			this.next = false;
		else
			this.next = true;
	}

	private void setActivitySDId(Activity tmp, Node behavior) {
		if (behavior != null) {
			tmp.setSdID(behavior.getTextContent());
		} else {
			tmp.setSdID(null);
		}
	}

	public Object createObject(NamedNodeMap childNodeAttributes) { 
		Object newObject = null;
		final Node xmiId = getItemByName(childNodeAttributes,"xmi:id");
		final Node xmiType = getItemByName(childNodeAttributes, "xmi:type");
		String name = null;
		
		if (childNodeAttributes.getNamedItem("name") != null) {
			name = getItemByName(childNodeAttributes,"name").getTextContent();
		}
		
		if(childNodeAttributes.equals("node")) {
			newObject = new Activity(
					xmiId.getTextContent(),
					name,
					xmiType.getTextContent());
		} else {
			newObject = new Edge(
					xmiId.getTextContent(),
					name,
					xmiType.getTextContent());
		}
		
		
		
		return newObject;
	}

	public Node getItemByName(NamedNodeMap childNodeAttributes, String nodeName ) {
		return childNodeAttributes.getNamedItem(nodeName);
	}

	private void searchThroughNodes(List<org.w3c.dom.Node> adList, NodeList nodes) {
		for (int i = 0; i < nodes.getLength(); i++) {
			NamedNodeMap childNodeAttributes = nodes.item(i).getAttributes();
			addNodeToList(adList, nodes, i,childNodeAttributes);
		}
	}

	private void addNodeToList(List<org.w3c.dom.Node> adList, NodeList nodes, int i,NamedNodeMap childNodeAttributes) {
		org.w3c.dom.Node ad;
		Node node = getItemByName(childNodeAttributes,"xmi:type");
		if (node != null) {
			String xmiType = node.getTextContent();
			if (xmiType != null && xmiType.equals("uml:Activity")) {
				ad = nodes.item(i);
				adList.add(ad);
			}
		}
	}

	public void resetActivitiesByID() {
		this.activitiesByID = new HashMap<String, Activity>();
		for (Activity a : this.activities) {
			this.activitiesByID.put(a.getId(), a);
		}
	}



	/**
	 * The retrieveEdges functions is responsible for identifying all the edges between the activities
	 * represented at an activity diagram. Given an activity diagram the function traverse its XMI
	 * fragment and whenever it finds an edge node, the information needed is extract (id, edge name,
	 * edge type, source and target activities, guards conditions). By the end the function creates a
	 * mapping <id, edge>.
	 * @param node The XMI fragment representing a sequence diagram to be parsed.
	 * @throws InvalidTagException
	 */
	public void retrieveEdges(org.w3c.dom.Node node) throws InvalidTagException {
		EdgeRetriever edgeRetriever = new EdgeRetriever(this);
		edgeRetriever.retrieveEdges(node);
	}


	/**
	 * The solveActivities' role is to represent the activity diagram in memory (by using Activity
	 * and Edge objects. This function is called after retrieveActivities and retrieveEdges functions,
	 * because all the objects needed for creating the representation are avaiable. In the end the
	 * orderActivities function is called to ensure the order which the activities happen.
	 * @param node The node object (i.e. the XMI fragment) representing the whole activity diagram.
	 */
	public void solveActivities(org.w3c.dom.Node node) {
		NodeList elements = node.getChildNodes();
		for (int s = 0; s < elements.getLength(); s++) {
			if (elements.item(s).getNodeName().equals("node")) {
				Activity activity = this.activitiesByID.get(elements.item(s).getAttributes() //TODO: insert variable
						.getNamedItem("xmi:id").getTextContent()); 
				NodeList tmpEdges = elements.item(s).getChildNodes();
				for (int t = 0; t < tmpEdges.getLength(); t++) {
					if (tmpEdges.item(t).getNodeName().equals("incoming")) {//TODO: insert variable
						activity.addIncoming(this.edgesByID.get(tmpEdges.item(t).getAttributes()//TODO: insert method
								.getNamedItem("xmi:idref").getTextContent()));
					} else if (tmpEdges.item(t).getNodeName().equals("outgoing")) {//TODO: insert variable
						activity.addOutgoing(this.edgesByID.get(tmpEdges.item(t).getAttributes()
								.getNamedItem("xmi:idref").getTextContent()));
					}
				}
			}
		}
		orderActivities();
	}




	public void setEdgesByID(Map<String, Edge> edgesByID) {
		this.edgesByID = edgesByID;
	}

	public Map<String, Edge> getEdgesByID() {
		return edgesByID;
	}

	/**
	 * This function is used to order the activities of a sequence diagram.
	 */
	public void orderActivities() {
		int i = 0, j;
		Queue<Activity> queue = new LinkedList<Activity>();
		Activity target, temp;

		j = -1;
		for (Activity a : this.activities) {
			j++;
			if (a.getType().equals(ActivityType.INITIAL_NODE)) {
				if (!this.activities.get(i).equals(a)) { // PEGAR EXEMPLO P
															// TESTAR AQUI blz
					temp = this.activities.get(i);
					this.activities.set(i, this.activities.get(j));
					this.activities.set(j, temp);
				}
				a.setOrdered(true);
				i++;
				queue.add(a);
				break;
			}
		}

		while (!queue.isEmpty()) {
			for (Edge e : queue.element().getOutgoing()) {
				target = e.getTarget();
				if (!target.isOrdered()) { // nao esta ordenado
					if (!this.activities.get(i).equals(target)) { // ordem errada
						j = -1;
						for (Activity a : this.activities) {
							j++;
							if (a.equals(target)) { // j:posicao do target
													// i:posicao atual ordenacao
								temp = this.activities.get(i);
								this.activities.set(i, this.activities.get(j));
								this.activities.set(j, temp);
								break;
							}
						}
					}
					target.setOrdered(true); // marca ordem target certa
					i++;
					queue.add(target); // poe target na fila
				}
			}
			queue.poll(); // tira o primeiro da fila
		}
	}
}
