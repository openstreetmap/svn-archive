package de.altenstein.osm;

public interface LicenseConstants {	
	/**
	 *  node: all users agreed -> 0
	 */
	public static final int NODE_AGREED = 0;
	
	/**
	 * node: some agreed, some not -> 1
	 */
	public static final int NODE_SOME_AGREED = 1;
	
	/**
	 * node: no single user agreed -> 2
	 */
	public static final int NODE_ALL_DISAGREED = 2;
	
	/**
	 * node: special number for case 1: all nodes of all ways agreed -> 3
	 */
	public static final int NODE_ALL_NODES_OF_ALL_WAYS_AGREED = 3;
	
	/**
	 * way: all users agreed (way and nodes) -> 0
	 */
	public static final int WAY_AGREED = 0;
	
	/**
	 * way: some agreed, some not -> 1
	 */
	public static final int WAY_SOME_AGREED = 1;
	
	/**
	 * way: no single user agreed -> 2
	 */
	public static final int WAY_ALL_DISAGREED = 2;
}
