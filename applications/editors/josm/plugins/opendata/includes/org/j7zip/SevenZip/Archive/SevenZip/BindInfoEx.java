package org.j7zip.SevenZip.Archive.SevenZip;
import org.j7zip.Common.RecordVector;
import org.j7zip.SevenZip.Archive.Common.BindInfo;



class BindInfoEx extends BindInfo {
    
    RecordVector<MethodID> CoderMethodIDs = new RecordVector<MethodID>();
    
    public void Clear() {
        super.Clear(); // CBindInfo::Clear();
        CoderMethodIDs.clear();
    }
}
