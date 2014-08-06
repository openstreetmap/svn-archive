package org.j7zip.SevenZip.Archive.SevenZip;

import org.j7zip.Common.BoolVector;
import org.j7zip.Common.IntVector;
import org.j7zip.Common.LongVector;
import org.j7zip.Common.ObjectVector;

class ArchiveDatabase {
    public LongVector PackSizes = new LongVector();
    public BoolVector PackCRCsDefined = new BoolVector();
    public IntVector PackCRCs = new IntVector();
    public ObjectVector<Folder> Folders = new ObjectVector<>();
    public IntVector NumUnPackStreamsVector = new IntVector();
    public ObjectVector<FileItem> Files = new ObjectVector<>();
    
    void Clear() {
        PackSizes.clear();
        PackCRCsDefined.clear();
        PackCRCs.clear();
        Folders.clear();
        NumUnPackStreamsVector.clear();
        Files.clear();
    }
}