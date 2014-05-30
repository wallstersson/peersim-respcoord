package com.lajv.respcoord;

import java.text.DecimalFormat;

import com.lajv.NetworkNode;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;

public class StreamingObserver implements Control {

	private static final String RESPCOORD_PROT = "respcoord";
	
	/**
	 * The cycle in which printing starts
	 * 
	 * @config
	 */
	private static final String PAR_START_CYLE = "start_cycle";

	private String prefix;
	
	private final int start_cycle;

	public StreamingObserver(String prefix) {
		this.prefix = prefix;
		System.err.println(prefix);
		start_cycle = Configuration.getInt(prefix + "." + PAR_START_CYLE, 0);
	}

	@Override
	public boolean execute() {

		if (CommonState.getTime() == 0)
			return false;
		
		if (CommonState.getTime() < start_cycle)
			return false;

		printSegmentLists();

		return false;
	}

	private void printSegmentLists() {
		int pid = Configuration.lookupPid(RESPCOORD_PROT);
		int numPeers = Network.size();

		double segmentsResponsibleTotal = 0;
		double segmentsFallbackTotal = 0;
		double segmentsFromPeersTotal = 0;
		double requestsToPeersTotal = 0;
		double cacheHitsTotal = 0;
		double cacheMissesTotal = 0;

		for (int i = 0; i < numPeers; i++) {

			NetworkNode n = (NetworkNode) Network.get(i);
			ResponsibilityCoordinationProtocol rcp = (ResponsibilityCoordinationProtocol) n
					.getProtocol(pid);

			segmentsResponsibleTotal += rcp.getSegmentsResponsible();
			segmentsFallbackTotal += rcp.getSegmentsFallback();
			segmentsFromPeersTotal += rcp.getSegmentsFromPeers();
			requestsToPeersTotal += rcp.getRequestsToPeers();
			cacheHitsTotal += rcp.getCacheHits();
			cacheMissesTotal += rcp.getCacheMisses();

		}

		DecimalFormat df = new DecimalFormat("0.0");

		double offloadFromServer = (segmentsFromPeersTotal / (segmentsResponsibleTotal
				+ segmentsFallbackTotal + segmentsFromPeersTotal));

		double segmentsFetchedFromServer = segmentsResponsibleTotal + segmentsFallbackTotal;

		System.out.println("#########################" + numPeers
				+ "###############################");

		System.err.println("Offload from server:\t\t" + df.format(offloadFromServer * 100) + " %");

		// System.err.println("Segments from server:\t\t" + segmentsFetchedFromServer);
		System.err.println("Segments from server");
		System.err.println("\tResponsible:\t\t"
				+ df.format((segmentsResponsibleTotal / segmentsFetchedFromServer) * 100) + "%");
		System.err.println("\tFallback:\t\t"
				+ df.format((segmentsFallbackTotal / segmentsFetchedFromServer) * 100) + "%");
		// System.err.println("requestsToPeersTotal:\t\t" + requestsToPeersTotal);
		// System.err.println("cacheHitsTotal:\t\t\t" + (cacheHitsTotal));
		// System.err.println("cacheMissesTotal:\t\t" + (cacheMissesTotal));
		System.err.println("Requests until hit avg:\t\t"
				+ df.format((requestsToPeersTotal / cacheMissesTotal)));
	}
}
