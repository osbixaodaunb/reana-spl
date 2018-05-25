package parsing.activitydiagrams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import parsing.ProbabilityEnergyTimeProfile;
import parsing.ProbabilityEnergyTimeProfileReader;
import parsing.exceptions.InvalidTagException;

public class EdgeRetriever {
	private ADReader adReader;
	private List<Edge> edges;
	
	public EdgeRetriever(ADReader adr) {
		this.adReader = adr;
	}
	
	public void retrieveEdges(org.w3c.dom.Node node) throws InvalidTagException {
		NodeList childNodes = node.getChildNodes();
		this.edges = new ArrayList<Edge>();

		for (int s = 0; s < childNodes.getLength(); s++) {
			addEdge(childNodes, s);
		}
		resetEdgesByID();
	}

	private void addEdge(NodeList childNodes, int s) throws InvalidTagException {
		NamedNodeMap childNodeAttributes = childNodes.item(s).getAttributes();
		if (childNodes.item(s).getNodeName().equals("edge")) {
			
			Edge newEdge = (Edge) adReader.createObject(childNodeAttributes);

			setEdgeSourceNTarget(childNodeAttributes, newEdge);

			setEdgeGuard(childNodes, s, newEdge);
			ProbabilityEnergyTimeProfile profile = ProbabilityEnergyTimeProfileReader.retrieveProbEnergyTime(
					newEdge.getId(), adReader.getDoc());
			if (profile.hasProbability()) {
			    newEdge.setProbability(profile.getProbability());
			}
			this.edges.add(newEdge);
		}
	}

	private void setEdgeSourceNTarget(NamedNodeMap childNodeAttributes, Edge newEdge) {

		newEdge.setSource(adReader.getActivitiesByID().get(adReader.getItemByName(
				childNodeAttributes, "source").getTextContent()));
		newEdge.setTarget(adReader.getActivitiesByID().get(adReader.getItemByName(
				childNodeAttributes, "target").getTextContent()));
	}

	private void setEdgeGuard(NodeList childNodes, int s, Edge newEdge) {
		NodeList infoNodes = childNodes.item(s).getChildNodes();
		for (int i = 0; i < infoNodes.getLength(); i++) {
			if (infoNodes.item(i).getNodeName().equals("guard")) {
				NodeList guardNodes = infoNodes.item(i).getChildNodes();
				searchThroughGuardNodes(newEdge, guardNodes);
				break;
			}
		}
	}

	private void searchThroughGuardNodes(Edge newEdge, NodeList guardNodes) {
		for (int j = 0; j < guardNodes.getLength(); j++) {
			if (guardNodes.item(j).getNodeName().equals("body")) {
				newEdge.setGuard(guardNodes.item(j).getTextContent());
				break;
			}
		}
	}
	
	public void resetEdgesByID() {
		this.adReader.setEdgesByID(new HashMap<String, Edge>());
		for (Edge e : edges) {
			this.adReader.getEdgesByID().put(e.getId(), e);
		}
	}

}
