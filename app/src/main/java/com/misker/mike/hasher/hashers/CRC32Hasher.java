package com.misker.mike.hasher.hashers;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

class CRC32Hasher implements Hasher {
    @Override
    public String hash(InputStream inputStream) {
        byte[] buffer = new byte[128];
        try (CheckedInputStream cis = new CheckedInputStream(inputStream, new CRC32())) {
            //noinspection StatementWithEmptyBody
            while (cis.read(buffer) >= 0) ;
            long checksum = cis.getChecksum().getValue();
            return Long.toHexString(checksum);
        } catch (IOException ioe) {
            throw new RuntimeException("There was a problem reading the CheckedInputStream for the CRC32 algorithm", ioe);
        }
    }
}
