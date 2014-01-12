package org.j7zip.SevenZip.Archive.SevenZip;

import org.j7zip.Common.LongVector;

class InArchiveInfo
{
    public byte ArchiveVersion_Major;
    //public byte ArchiveVersion_Minor;
    
    //public long StartPosition;
    public long StartPositionAfterHeader;
    public long DataStartPosition;
    public long DataStartPosition2;    
    LongVector FileInfoPopIDs = new LongVector();
    
    void Clear()
    {
        FileInfoPopIDs.clear();
    }    
}