package com.lajv.respcoord;

import java.text.DecimalFormat;

import com.lajv.closepeer.ClosePeerProtocol;

import peersim.config.Configuration;

import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

public class NetworkSimilarityObserver implements Control {

	// ========================= Parameters ===============================
	// ====================================================================

	/**
	 * The protocol to operate on.
	 * 
	 * @config
	 */
	private static final String PAR_CLOSEPEER_PROT = "protocol";

	// =========================== Fields =================================
	// ====================================================================

	private String prefix;

	// ==================== Constructor ===================================
	// ====================================================================

	public NetworkSimilarityObserver(String prefix) {
		this.prefix = prefix;
		System.err.println(prefix);
	}

	// ======================= Methods ====================================
	// ====================================================================

	@Override
	public boolean execute() {

		calculateSimilarity();

		return false;
	}

	private void calculateSimilarity() {
		DecimalFormat df = new DecimalFormat("0.0");
		int pid = Configuration.getPid(prefix + "." + PAR_CLOSEPEER_PROT);
		double sumNetworkSimilarity = 0;
		for (int i = 0; i < Network.size(); i++) {
			Node n = Network.get(i);
			ClosePeerProtocol cpp = (ClosePeerProtocol) n.getProtocol(pid);
			sumNetworkSimilarity += cpp.nbSimilarity(n, pid);
		}
		double networkSimilarity = (double) sumNetworkSimilarity / Network.size();
		System.err.println("Network similarity: " + df.format(networkSimilarity * 100) + " %");
	}
}
