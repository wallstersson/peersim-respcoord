package com.lajv.respcoord;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.lajv.NetworkNode;
import com.lajv.NodeWrapper;
import com.lajv.closepeer.ClosePeerProtocol;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;

public class ResponsibilityCoordinatorProtocol implements CDProtocol {

	/*
	 * Variables for streaming statistics
	 */
	private int segmentsResponsible = 0;
	private int segmentsFallback = 0;
	private int segmentsFromPeers = 0;
	private int requestsToPeers = 0;
	private int cacheHits = 0;
	private int cacheMisses = 0;

	private int currentSegment;
	private HashMap<Integer, Decision> decisions;
	private List<NodeWrapper> peers;

	private static final String CLOSEPEER_PROT = "closepeer_prot";

	private String prefix;

	private static int bitrate = 1;
	private static int highestRf = 16;

	public ResponsibilityCoordinatorProtocol(String prefix) {
		this.prefix = prefix;
		currentSegment = 0;
		decisions = new HashMap<>();
	}

	@Override
	public Object clone() {
		ResponsibilityCoordinatorProtocol rcp = null;

		try {
			rcp = (ResponsibilityCoordinatorProtocol) super.clone();
		} catch (CloneNotSupportedException e) {
		} // never happens

		rcp.currentSegment = 0;
		rcp.decisions = new HashMap<>();
		return rcp;
	}

	@Override
	public void nextCycle(Node node, int protocolID) {
		// System.err.println("ResponsibilityCoordinatorProtocol: " + node.getID()
		// + " calculating resp for segment " + segment);
		currentSegment++;

		if (peers == null) {
			int closePeerPid = Configuration.getPid(prefix + "." + CLOSEPEER_PROT);
			ClosePeerProtocol closePeerProt = (ClosePeerProtocol) node.getProtocol(closePeerPid);
			peers = closePeerProt.getPeers();
		}

		Decision calculatedDecision;
		LinkedList<NodeWrapper> responsiblePeers;

		if (decisions.containsKey(currentSegment)) {
			calculatedDecision = decisions.get(currentSegment);
			responsiblePeers = calculatedDecision.getResponsiblePeers();

		} else {
			calculatedDecision = calculateResponsibility(node, currentSegment);
			decisions.put(currentSegment, calculatedDecision);
			responsiblePeers = calculatedDecision.getResponsiblePeers();
		}

		for (NodeWrapper n : responsiblePeers) {
			if (n.node == node) {
				segmentsResponsible++;
				return;
			}
		}

		while (!responsiblePeers.isEmpty()) {
			double randRespValue = CommonState.r.nextDouble()
					* responsiblePeers.getLast().responsibilityValue;
			NodeWrapper selectedPeer = null;

			for (NodeWrapper n : responsiblePeers) {
				if (randRespValue <= n.responsibilityValue) {
					selectedPeer = n;
				}
			}
			responsiblePeers.remove(selectedPeer);

			ResponsibilityCoordinatorProtocol responsiblePeerProtocol = (ResponsibilityCoordinatorProtocol) selectedPeer.node
					.getProtocol(protocolID);

			requestsToPeers++;

			if (responsiblePeerProtocol.fetchSegment(selectedPeer.node, currentSegment)) {
				segmentsFromPeers++;
				cacheHits++;
				// System.err.println(node.getID() + " HIT");
				return;
			} else {
				cacheMisses++;
				// System.err.println(node.getID() + " MISS");
			}

			calculateRespValue(responsiblePeers);
		}

		// Fallback
		segmentsFallback++;
	}

	public boolean fetchSegment(Node node, int segmentNum) {
		if (peers == null) {
			int closePeerPid = Configuration.getPid(prefix + "." + CLOSEPEER_PROT);
			ClosePeerProtocol closePeerProt = (ClosePeerProtocol) node.getProtocol(closePeerPid);
			peers = closePeerProt.getPeers();
		}

		Decision calculatedDecision;

		if (decisions.containsKey(segmentNum)) {
			calculatedDecision = decisions.get(segmentNum);

		} else {
			calculatedDecision = calculateResponsibility(node, currentSegment);
			decisions.put(segmentNum, calculatedDecision);
		}

		for (NodeWrapper n : calculatedDecision.getResponsiblePeers()) {
			if (n.node == node) {
				return true;
			}
		}

		return false;
	}

	private void calculateRespValue(LinkedList<NodeWrapper> responsiblePeers) {
		double prevResponsibiltyValue = 0.0;
		for (NodeWrapper n : responsiblePeers) {
			// Can't use floor on respvalue since it is double and will result in 0.0 on small
			// uploads.
			n.responsibilityValue = n.uploadCapacity + prevResponsibiltyValue;
			prevResponsibiltyValue = n.responsibilityValue;
		}
	}

	private Decision calculateResponsibility(Node node, int segmentNumber) {
		int rf = highestRf * 2;
		double totalUpload = 0;

		NodeWrapper me = new NodeWrapper(node);
		me.uploadCapacity = ((NetworkNode) node).location.getUploadCapacity();

		peers.add(me);

		LinkedList<NodeWrapper> responsiblePeers = new LinkedList<NodeWrapper>();
		LinkedList<NodeWrapper> notResponsiblePeers = new LinkedList<NodeWrapper>();

		for (NodeWrapper n : peers) {
			totalUpload += n.uploadCapacity;
		}

		double avgUpload = totalUpload / peers.size();
		double superPeerLimit = avgUpload * 2;
		double totalUploadCapacity = 0;
		double requiredUploadCapacity = 0;

		while (rf > 1) {
			rf /= 2;
			totalUploadCapacity = 0;
			responsiblePeers.clear();
			notResponsiblePeers.clear();
			requiredUploadCapacity = bitrate * peers.size();

			for (NodeWrapper n : peers) {

				if (n.node.getID() % rf == segmentNumber % rf || n.uploadCapacity >= superPeerLimit) {
					totalUploadCapacity += n.uploadCapacity;
					requiredUploadCapacity -= bitrate;
					responsiblePeers.add(n);
					if (n.uploadCapacity >= superPeerLimit)
						n.superPeer = true;
					else
						n.superPeer = false;
				} else {
					notResponsiblePeers.add(n);
					n.superPeer = false;
				}
			}
			if (totalUploadCapacity >= requiredUploadCapacity) {
				break;
			}
		}

		// To limit number of printouts while debugging
		// if (node.getID() == 1) {
		// System.err.println("avgUpload: " + avgUpload);
		// System.err.println("superPeerLimit: " + superPeerLimit);
		// System.err.println("segmentNumber: " + segmentNumber);
		// System.err.println("rf: " + rf);
		// System.err.println("totalUploadCapacity: " + totalUploadCapacity);
		// System.err.println("requiredUploadCapacity: " + requiredUploadCapacity);
		// }

		// Sort resp peers
		// Collections.sort(responsiblePeers, new UploadCapacityComparator());

		// Calculate resp value
		calculateRespValue(responsiblePeers);

		peers.remove(me);

		return new Decision(segmentNumber, responsiblePeers, notResponsiblePeers, rf,
				superPeerLimit, totalUploadCapacity, requiredUploadCapacity);
	}

	public int getSegmentsResponsible() {
		return segmentsResponsible;
	}

	public int getSegmentsFallback() {
		return segmentsFallback;
	}

	public int getSegmentsFromPeers() {
		return segmentsFromPeers;
	}

	public int getRequestsToPeers() {
		return requestsToPeers;
	}

	public int getCacheHits() {
		return cacheHits;
	}

	public int getCacheMisses() {
		return cacheMisses;
	}
}

class UploadCapacityComparator implements Comparator<NodeWrapper> {
	@Override
	public int compare(NodeWrapper cp1, NodeWrapper cp2) {
		return (int) Math.ceil(cp1.uploadCapacity - cp2.uploadCapacity);
	}
}
