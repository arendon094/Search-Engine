package edu.csula.cs454.crawler;

import java.util.ArrayList;

public class ExampleDocument {
	private double rank;
	private int outGoingLinks;
	private String randomURL;
	
	public ExampleDocument(String randomURL, double rank, int outGoingLinks){
		this.randomURL = randomURL;
		this.rank = rank;
		this.outGoingLinks = outGoingLinks;
	}

	public double getRank() {
		return rank;
	}

	public String getRandomURL() {
		return randomURL;
	}

	public void setRandomURL(String randomURL) {
		this.randomURL = randomURL;
	}

	public void setRank(double rank) {
		this.rank = rank;
	}

	public int getOutGoingLinks() {
		return outGoingLinks;
	}

	public void setOutGoingLinks(int outGoingLinks) {
		this.outGoingLinks = outGoingLinks;
	}
	
	

}
