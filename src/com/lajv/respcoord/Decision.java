package com.lajv.respcoord;

import java.util.LinkedList;

import com.lajv.NodeWrapper;

public class Decision {

	private int segment;
	private int rf;
	private LinkedList<NodeWrapper> responsiblePeers;
	
	public Decision(LinkedList<NodeWrapper> responsiblePeers, int rf) {
		this.responsiblePeers = responsiblePeers;
		this.rf = rf;
	}

	public int getSegment() {
		return segment;
	}
	
	public int getRf() {
		return rf;
	}
	
	public LinkedList<NodeWrapper> getResponsiblePeers() {
		return responsiblePeers;
	}
}
