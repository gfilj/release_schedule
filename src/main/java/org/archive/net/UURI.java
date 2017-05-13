package org.archive.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import org.apache.commons.httpclient.URIException;
import org.archive.url.UsableURI;
import com.esotericsoftware.kryo.CustomSerialization;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serialize.StringSerializer;

/**
 * UURI实例化
 * 
 * @author handongming
 *
 */
public class UURI extends UsableURI implements CustomSerialization {

    private static final long serialVersionUID = -8946640480772772310L;

    public UURI(String fixup, boolean b, String charset) throws URIException {
        super(fixup, b, charset);
    }

    public UURI(UsableURI base, UsableURI relative) throws URIException {
        super(base, relative);
    }
    
    protected UURI() {
        super();
    }

    @Override
    public void writeObjectData(Kryo kryo, ByteBuffer buffer) {
        StringSerializer.put(buffer, toCustomString());
    }

    @Override
    public void readObjectData(Kryo kryo, ByteBuffer buffer) {
        try {
            parseUriReference(StringSerializer.get(buffer), true);
        } catch (URIException e) {
            e.printStackTrace();
        }
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeUTF(toCustomString());
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        parseUriReference(stream.readUTF(), true);
    }
}
