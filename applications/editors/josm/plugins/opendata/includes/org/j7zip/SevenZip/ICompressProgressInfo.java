package org.j7zip.SevenZip;

public interface ICompressProgressInfo {
    public static final long INVALID = -1;
    int SetRatioInfo(long inSize, long outSize);
}
