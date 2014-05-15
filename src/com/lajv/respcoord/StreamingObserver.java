package com.lajv.respcoord;

import com.lajv.NetworkNode;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;

public class StreamingObserver implements Control {

	private static final String RESPCOORD_PROT = "respcoord";

	private String prefix;

	public StreamingObserver(String prefix) {
		this.prefix = prefix;
		System.err.println(prefix);
	}

	@Override
	public boolean execute() {

		if (CommonState.getTime() == 0)
			return false;

		printSegmentLists();

		return false;
	}

	private void printSegmentLists() {
		int pid = Configuration.lookupPid(RESPCOORD_PROT);
		int numPeers = Network.size();

		double segmentsFromServerTotal = 0;
		double segmentsFromPeersTotal = 0;
		double requestsToPeersTotal = 0;
		double cacheHitsTotal = 0;
		double cacheMissesTotal = 0;

		for (int i = 0; i < numPeers; i++) {

			NetworkNode n = (NetworkNode) Network.get(i);
			ResponsibilityCoordinatorProtocol rcp = (ResponsibilityCoordinatorProtocol) n
					.getProtocol(pid);

			segmentsFromServerTotal += rcp.getSegmentsFromServer();
			segmentsFromPeersTotal += rcp.getSegmentsFromPeers();
			requestsToPeersTotal += rcp.getRequestsToPeers();
			cacheHitsTotal += rcp.getCacheHits();
			cacheMissesTotal += rcp.getCacheMisses();

		}

		System.out.println("#########################" + numPeers
				+ "###############################");
		System.err
				.println("Offload from server:\t\t"
						+ (float) ((segmentsFromPeersTotal / (segmentsFromServerTotal + segmentsFromPeersTotal)) * 100)
						+ " %");
		System.err.println("segmentsFromServerTotal:\t" + segmentsFromServerTotal);
		System.err.println("segmentsFromPeersTotal:\t" + segmentsFromPeersTotal);
		System.err.println("requestsToPeersTotal:\t" + requestsToPeersTotal);
		System.err.println("cacheHitsTotal:\t\t" + (cacheHitsTotal));
		System.err.println("cacheMissesTotal:\t\t" + (cacheMissesTotal));
		System.err.println("Requests to peers to get a segment on avg:\t\t" + requestsToPeersTotal
				/ cacheMissesTotal);
	}

}
