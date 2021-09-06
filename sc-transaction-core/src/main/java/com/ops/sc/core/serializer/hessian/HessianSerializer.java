
package com.ops.sc.core.serializer.hessian;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;
import com.ops.sc.common.anno.LoadLevel;
import com.ops.sc.core.serializer.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


@LoadLevel(name = "HESSIAN")
public class HessianSerializer implements Serializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(HessianSerializer.class);

    @Override
    public <T> byte[] serialize(T t) {
        byte[] stream = null;
        SerializerFactory hessian = HessianSerializerFactory.getInstance();
        try {
            com.caucho.hessian.io.Serializer serializer = hessian.getSerializer(t.getClass());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Hessian2Output output = new Hessian2Output(baos);
            serializer.writeObject(t, output);
            output.close();
            stream = baos.toByteArray();
        } catch (IOException e) {
            LOGGER.error("Hessian encode error:{}", e.getMessage(), e);
        }
        return stream;
    }

    @Override
    public <T> T deserialize(byte[] bytes) {
        T obj = null;
        try (ByteArrayInputStream is = new ByteArrayInputStream(bytes);) {
            Hessian2Input input = new Hessian2Input(is);
            obj = (T) input.readObject();
            input.close();
        } catch (IOException e) {
            LOGGER.error("Hessian decode error:{}", e.getMessage(), e);
        }
        return obj;
    }
}
