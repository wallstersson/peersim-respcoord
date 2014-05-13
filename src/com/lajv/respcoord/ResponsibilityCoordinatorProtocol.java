package com.lajv.respcoord;

import java.util.ArrayList;
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
import peersim.core.Node;

public class ResponsibilityCoordinatorProtocol implements CDProtocol {

	private int segment;
	private HashMap<Integer, Decision> decisions;
	private List<NodeWrapper> peers;

	private static final String CLOSEPEER_PROT = "closepeer_prot";

	private String prefix;

	private static int bitrate = 1;
	private static int highestRf = 16;

	public ResponsibilityCoordinatorProtocol(String prefix) {
		this.prefix = prefix;
		segment = 0;
		decisions = new HashMap<>();
	}

	@Override
	public Object clone() {
		ResponsibilityCoordinatorProtocol rcp = null;

		try {
			rcp = (ResponsibilityCoordinatorProtocol) super.clone();
		} catch (CloneNotSupportedException e) {
		} // never happens

		rcp.segment = 0;
		rcp.decisions = new HashMap<>();
		return rcp;
	}

	@Override
	public void nextCycle(Node node, int protocolID) {
		// System.err.println("ResponsibilityCoordinatorProtocol: " + node.getID()
		// + " calculating resp for segment " + segment);

		if (peers == null) {
			int closePeerPid = Configuration.getPid(prefix + "." + CLOSEPEER_PROT);
			ClosePeerProtocol closePeerProt = (ClosePeerProtocol) node.getProtocol(closePeerPid);
			peers = closePeerProt.getPeers();
		}

		Decision calculatedDecision = calculateResponsibility(node, segment);
		// decisions.put(segment, calculatedDecision);
		segment++;
	}

	public void fetchSegment() {

	}

	private Decision calculateResponsibility(Node node, int segmentNumber) {
		int rf = highestRf * 2;
		double totalUpload = 0;

		NodeWrapper me = new NodeWrapper(node);
		me.uploadCapacity = ((NetworkNode) node).location.getUploadCapacity();

		peers.add(me);

		LinkedList<NodeWrapper> responsiblePeers = new LinkedList<NodeWrapper>();

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
			requiredUploadCapacity = bitrate * peers.size();

			for (NodeWrapper n : peers) {

				if (n.node.getID() % rf == segmentNumber % rf || n.uploadCapacity >= superPeerLimit) {
					totalUploadCapacity += n.uploadCapacity;
					requiredUploadCapacity -= bitrate;
					responsiblePeers.add(n);
				}
			}
			if (totalUploadCapacity >= requiredUploadCapacity) {
				break;
			}
		}

		// To limit number of printouts while debugging
		if (node.getID() == 1) {
			System.err.println("avgUpload: " + avgUpload);
			System.err.println("superPeerLimit: " + superPeerLimit);
			System.err.println("segmentNumber: " + segmentNumber);
			System.err.println("rf: " + rf);
			System.err.println("totalUploadCapacity: " + totalUploadCapacity);
			System.err.println("requiredUploadCapacity: " + requiredUploadCapacity);
		}

		// Sort resp peers
		Collections.sort(responsiblePeers, new UploadCapacityComparator());

		// System.err.println(responsiblePeers);
		// Calculate resp value

		peers.remove(me);

		return new Decision(responsiblePeers, rf);
	}
}

class UploadCapacityComparator implements Comparator<NodeWrapper> {
	@Override
	public int compare(NodeWrapper cp1, NodeWrapper cp2) {
		return (int) Math.ceil(cp1.uploadCapacity - cp2.uploadCapacity);
	}
}
