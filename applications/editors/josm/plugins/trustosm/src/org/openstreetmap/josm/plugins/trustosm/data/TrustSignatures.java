// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.trustosm.data;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.openpgp.PGPSignature;
import org.openstreetmap.josm.tools.Logging;

public class TrustSignatures {

    public static final byte SIG_UNKNOWN = 0;
    public static final byte SIG_VALID = 1;
    public static final byte SIG_BROKEN = -1;
    public static final byte ITEM_REMOVED = -2;

    //private final Vector<PGPSignature> signatures = new Vector<PGPSignature>();
    //    private final HashMap<PGPSignature, String> signatureTextMap = new HashMap<PGPSignature, String>();
    private final Map<String, List<PGPSignature>> textsigs = new HashMap<>();
    private byte status;
    private double reputation;

    public TrustSignatures() {
        this.status = SIG_UNKNOWN;
    }

    public TrustSignatures(PGPSignature signature, String sigtext, byte status) {
        this.status = status;
        addSignature(signature, sigtext);
    }

    public int countSigs() {
        //        return signatures.size();
        //return signatureTextMap.size();
        int count = 0;
        for (List<PGPSignature> siglist : textsigs.values()) {
            count += siglist.size();
        }
        return count;
    }

    public void setReputation(double r) {
        reputation = r;
    }

    public double getReputation() {
        return reputation;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public byte getStatus() {
        return status;
    }

    /*    public void setSignatures(Vector<PGPSignature> signatures) {
        this.signatures.addAll(signatures);
    }
     */
    public Vector<PGPSignature> getSignatures() {
        //        return signatures;
        Vector<PGPSignature> sigs = new Vector<>();
        for (List<PGPSignature> siglist : textsigs.values()) {
            sigs.addAll(siglist);
        }
        return sigs;
    }

    public Map<String, List<PGPSignature>> getSignaturesWithText() {
        //        return signatures;
        return textsigs;
    }

    public List<PGPSignature> getSignaturesByPlaintext(String plain) {
        return textsigs.get(plain);
    }

    public void addSignature(PGPSignature signature, String sigtext) {
        //        signatures.add(signature);
        //signatureTextMap.put(signature, sigtext);
        if (textsigs.containsKey(sigtext)) {
            textsigs.get(sigtext).add(signature);
        } else {
            List<PGPSignature> l = new ArrayList<>();
            l.add(signature);
            textsigs.put(sigtext, l);
        }
    }

    /*    public void addSignatures(List<PGPSignature> signatures, String sigtext) {
        textsigs.get(sigtext).addAll(signatures);
    }
     */

    /*
    public PGPSignature getLatestSignature() {
        return signatures.lastElement();
    }
     */
    public String getOnePlainText() {
        Set<String> texts = getAllPlainTexts();
        if (texts.isEmpty()) return "";
        else return texts.iterator().next();
    }

    public Set<String> getAllPlainTexts() {
        return textsigs.keySet();
    }

    public String getSigtext(PGPSignature signature) {
        for (String sigtext : textsigs.keySet()) {
            if (textsigs.get(sigtext).contains(signature)) return sigtext;
        }
        return "";
    }

    public String getArmoredFulltextSignatureAll(String plain) {
        if (textsigs.containsKey(plain)) {
            List<PGPSignature> l = textsigs.get(plain);
            PGPSignature first = l.get(0);
            if (first != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (ArmoredOutputStream aOut = new ArmoredOutputStream(baos)) {
                    aOut.beginClearText(first.getHashAlgorithm());
                    aOut.write(plain.getBytes(Charset.forName("UTF-8")));
                    aOut.write('\n');
                    aOut.endClearText();

                    try (BCPGOutputStream bOut = new BCPGOutputStream(aOut)) {
                        for (PGPSignature sig : l) {
                            sig.encode(bOut);
                        }
                    }

                    return baos.toString("UTF-8");

                } catch (Exception e) {
                    Logging.error(e);
                    return "Error - read console Output";
                }
            }
        }
        return "No sigs available";
    }

    public String getArmoredFulltextSignature(PGPSignature sig) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ArmoredOutputStream aOut = new ArmoredOutputStream(baos)) {
            aOut.beginClearText(sig.getHashAlgorithm());
            aOut.write(getSigtext(sig).getBytes(Charset.forName("UTF-8")));
            aOut.write('\n');
            aOut.endClearText();

            try (BCPGOutputStream bOut = new BCPGOutputStream(aOut)) {
                sig.encode(bOut);
            }

            return baos.toString("UTF-8");
        } catch (Exception e) {
            Logging.error(e);
            return "Error - read console Output";
        }
    }
}
