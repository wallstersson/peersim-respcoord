package com.lajv.respcoord;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.lajv.NetworkNode;
import com.lajv.NodeWrapper;
import com.lajv.closepeer.ClosePeerProtocol;
import com.lajv.cyclon.CyclonProtocol;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;

public class ClosePeerObserver implements Control {

	private static final String CLOSEPEER_PROT = "cpp";

	private String prefix;

	public ClosePeerObserver(String prefix) {
		this.prefix = prefix;
		System.err.println(prefix);
	}

	@Override
	public boolean execute() {

		if (CommonState.getTime() == 0)
			return false;

		calculateSimilarity();

		return false;
	}

	private void calculateSimilarity() {
		DecimalFormat df = new DecimalFormat("0.0");
		
		int pid = Configuration.getPid(prefix + "." + CLOSEPEER_PROT);
		int numPeers = Network.size();
		double networkSumSimilarity = 0;
		// Loop over all nodes in network
		for (int i = 0; i < numPeers; i++) {
			double pairSumSimilarity = 0.0;

			NetworkNode n = (NetworkNode) Network.get(i);
			ClosePeerProtocol cpp = (ClosePeerProtocol) n.getProtocol(pid);

			List<NodeWrapper> peerNeighbours = cpp.getPeers();

			// Loop over all closePeer neighbours
			for (NodeWrapper otherPeer : peerNeighbours) {
				int similarNodes = 0;
				List<NodeWrapper> otherNeighbours = ((ClosePeerProtocol) otherPeer.node
						.getProtocol(pid)).getPeers();

				// Check if my peers exists in closePeer list of otherPeer
				for (NodeWrapper peerNodeWrapper : peerNeighbours) {
					for (NodeWrapper otherNodeWrapper : otherNeighbours) {
						if (peerNodeWrapper.node == otherNodeWrapper.node || otherNodeWrapper.node == n) {
							similarNodes++;
							break;
						}
					}

				}
				double pairSimilarity = (double) similarNodes / peerNeighbours.size();
				// System.err.println((similarNodes / myCacheSize) * 100 + "%");
				pairSumSimilarity += pairSimilarity;
			}
			double avgNodeSimilarity = (double) (pairSumSimilarity / peerNeighbours.size());
//			System.err.println("Sim: " + avgNodeSimilarity * 100 + " %");
			networkSumSimilarity += avgNodeSimilarity;
		}
		double networkSimilarity = (double) networkSumSimilarity / Network.size();
		System.err.println("Network similarity: " + df.format(networkSimilarity * 100) + " %");

	}
}
