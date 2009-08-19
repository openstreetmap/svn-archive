package org.openstreetmap.labelling.util;

import java.util.Random;

public class RandomSource
{
	private static final Random RANDOM = new Random(2323232323L);

	private RandomSource()
	{
	}

	public static Random getRandom()
	{
		return RANDOM;
	}
}
