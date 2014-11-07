package org.nulleins.formats.iso8583.schema;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.nulleins.formats.iso8583.FieldTemplate;
import org.nulleins.formats.iso8583.MessageFactory;
import org.nulleins.formats.iso8583.MessageTemplate;
import org.nulleins.formats.iso8583.types.BitmapType;
import org.nulleins.formats.iso8583.types.CharEncoder;
import org.nulleins.formats.iso8583.types.ContentType;
import org.nulleins.formats.iso8583.types.MTI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MessageConfig {
  private final MessageFactory factory;
  private MessageConfig(final String configFile) {
    factory = buildFactory(ConfigFactory.load(configFile));
  }

  public static MessageFactory configure(final String configFile) {
    return new MessageConfig(configFile).factory;
  }

  private MessageFactory buildFactory(final Config config) {
    final Config schema = config.getConfig("schema");
    final String id = schema.getString("id");
    final String header = schema.getString("header");
    final ContentType contentType = ContentType.valueOf(schema.getString("contentType").toUpperCase());
    final String description = schema.getString("description");
    final BitmapType bitmapType = BitmapType.valueOf(schema.getString("bitmapType").toUpperCase());
    schema.getString("contentType");
    final Optional<CharEncoder> charset;
    if ( schema.hasPath("charset")) {
      charset = Optional.of(new CharEncoder(schema.getString("charset")));
    } else {
      charset = Optional.absent();
    }
    schema.getString("charset");
    final List<MessageTemplate> messages = FluentIterable.from(schema.getConfigList("messages"))
        .transform(getMessageTemplate(header, bitmapType)).toList();
    final MessageFactory result = MessageFactory.Builder()
        .id(id)
        .header(header)
        .charset(charset.orNull())
        .description(description)
        .bitmapType(bitmapType)
        .contentType(contentType)
        .build();
    result.addMessages(messages);
    return result;
  }

  private Function<Config, MessageTemplate> getMessageTemplate(final String header, final BitmapType bitmapType) {
    return new Function<Config, MessageTemplate>() {
      @Override
      public MessageTemplate apply(final Config input) {
        final MTI mti = MTI.create(input.getString("type"));
        final MessageTemplate template = MessageTemplate.create(header,mti,bitmapType);
        if ( input.hasPath("name")) {
          template.setName(input.getString("name"));
        }
        template.setFields(getFields(template, input.getObject("fields").unwrapped()));
        return template;
      }
    };
  }

  private Map<Integer,FieldTemplate> getFields(final MessageTemplate template, final Map<String, Object> fieldList) {
    final Map<Integer, FieldTemplate> result = new HashMap<>();
    for(final Map.Entry<String,Object> entry : fieldList.entrySet()) {
      final int fieldNumber = Integer.valueOf(entry.getKey());
      result.put(fieldNumber, getFieldDefinition(fieldNumber, template, (Map<String, Object>) entry.getValue()));
    }
    return result;
  }

  private FieldTemplate getFieldDefinition(final int fieldNumber, final MessageTemplate template, final Map<String, Object> fieldConfig) {
    return FieldTemplate.localBuilder(template).get()
        .f(fieldNumber)
        .name((String) fieldConfig.get("name"))
        .desc((String) fieldConfig.get("desc"))
        .type((String) fieldConfig.get("type"))
        .dim((String) fieldConfig.get("dim"))
        .build();
  }
}
