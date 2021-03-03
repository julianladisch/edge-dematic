package org.folio.ed.util;

import static org.folio.ed.util.StagingDirectorMessageHelper.MSG_TYPE_SIZE;
import static org.folio.ed.util.StagingDirectorMessageHelper.resolveMessageType;

import org.springframework.core.serializer.Deserializer;
import org.springframework.core.serializer.Serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class StagingDirectorSerializerDeserializer implements Serializer<String>, Deserializer<String> {
  @Override
  public String deserialize(InputStream inputStream) throws IOException {
    MessageTypes messageType = resolveMessageType(parseString(inputStream, MSG_TYPE_SIZE));
    return messageType.getCode() + parseString(inputStream, messageType.getPayloadLength());
  }

  @Override
  public void serialize(String s, OutputStream outputStream) throws IOException {
    outputStream.write(s.getBytes(StandardCharsets.UTF_8));
    outputStream.flush();
  }

  private String parseString(InputStream inputStream, int length) throws IOException {
    StringBuilder builder = new StringBuilder();

    int bite;
    for (int i = 0; i < length; ++i) {
      bite = inputStream.read();
      checkClosure(bite);
      builder.append((char)bite);
    }

    return builder.toString();
  }

  private void checkClosure(int bite) throws IOException {
    if (bite < 0) {
      throw new IOException("Socket closed during message assembly");
    }
  }
}
