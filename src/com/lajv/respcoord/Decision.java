package com.lajv.respcoord;

import java.text.DecimalFormat;
import java.util.LinkedList;

import com.lajv.NodeWrapper;

public class Decision {

	private int segment;
	private int rf;
	private LinkedList<NodeWrapper> responsiblePeers;
	private LinkedList<NodeWrapper> notResponsiblePeers;
	private double superPeerLimit;
	private double totalUploadCapacity;
	private double requiredUploadCapacity;

	public Decision(int segment, LinkedList<NodeWrapper> responsiblePeers,
			LinkedList<NodeWrapper> notResponsiblePeers, int rf, double superPeerLimit,
			double totalUploadCapacity, double requiredUploadCapacity) {
		this.segment = segment;
		this.responsiblePeers = responsiblePeers;
		this.notResponsiblePeers = notResponsiblePeers;
		this.rf = rf;
		this.superPeerLimit = superPeerLimit;
		this.totalUploadCapacity = totalUploadCapacity;
		this.requiredUploadCapacity = requiredUploadCapacity;
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

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		DecimalFormat df = new DecimalFormat("0.00000000000000");

		sb.append("#################################################################################################\n");
		sb.append("#\t\t\t\tSegment: " + segment + "\tRF: " + rf + "\t\t\t\t\t\t#\n");
		sb.append("#################################################################################################\n");
		sb.append("#\tId\t#\tUpload\t\t\t#\tResponsible\t#\tSuper peer\t#\n");
		sb.append("#################################################################################################\n");

		for (NodeWrapper n : responsiblePeers) {
			sb.append("#\t" + n.node.getID() + "\t#\t" + df.format(n.uploadCapacity) + "\t#\t"
					+ true + "\t\t#\t" + n.superPeer + "\t\t#\n");
		}

		sb.append("#-----------------------------------------------------------------------------------------------#\n");

		for (NodeWrapper n : notResponsiblePeers) {
			sb.append("#\t" + n.node.getID() + "\t#\t" + df.format(n.uploadCapacity) + "\t#\t"
					+ false + "\t\t#\t" + n.superPeer + "\t\t#\n");
		}

		sb.append("#################################################################################################\n");
		sb.append("# superPeerLimit:\t\t" + df.format(superPeerLimit) + "\t\t\t\t\t\t#\n");
		sb.append("# totalUploadCapacity:\t\t" + df.format(totalUploadCapacity) + "\t\t\t\t\t\t#\n");
		sb.append("# requiredUploadCapacity:\t" + requiredUploadCapacity + "\t\t\t\t\t\t\t\t#\n");
		sb.append("#################################################################################################\n");

		return sb.toString();
	}
}
