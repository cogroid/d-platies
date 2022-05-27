/* CD
 *
 * Modified by Dinh Thoai Tran <progrocus@gmail.com>
 *
 * Ref: https://github.com/google/gson/blob/gson-parent-2.8.5/gson/src/main/java/com/google/gson/internal/bind/ObjectTypeAdapter.java
 *
 * CD
 */

/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cogroid.platies.internal.bind;

import com.cogroid.platies.Platies;
import com.cogroid.platies.TypeAdapter;
import com.cogroid.platies.TypeAdapterFactory;
import com.cogroid.platies.internal.LinkedTreeMap;
import com.cogroid.platies.reflect.TypeToken;
import com.cogroid.platies.stream.JsonReader;
import com.cogroid.platies.stream.JsonToken;
import com.cogroid.platies.stream.JsonWriter;
import com.cogroid.platies.stream.TagJsonReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adapts types whose static type is only 'Object'. Uses getClass() on
 * serialization and a primitive/Map/List on deserialization.
 */
public final class ObjectTypeAdapter extends TypeAdapter<Object> {
  public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
    @SuppressWarnings("unchecked")
    @Override public <T> TypeAdapter<T> create(Platies platies, TypeToken<T> type) {
      if (type.getRawType() == Object.class) {
        return (TypeAdapter<T>) new ObjectTypeAdapter(platies);
      }
      return null;
    }
  };

  private final Platies platies;

  ObjectTypeAdapter(Platies platies) {
    this.platies = platies;
  }

  @Override public Object read(JsonReader in) throws IOException {
    boolean taggable = (in instanceof TagJsonReader);
    TagJsonReader tagJsonReader = null;
    if (taggable) {
      tagJsonReader = (TagJsonReader)in;
    }
    JsonToken token = in.peek();
    switch (token) {
    case BEGIN_ARRAY:
      List<Object> list = new ArrayList<Object>();
      in.beginArray();
      while (in.hasNext()) {
        list.add(read(in));
      }
      in.endArray();
      return list;

    case BEGIN_OBJECT:
      Map<String, Object> map = new LinkedTreeMap<String, Object>();
      in.beginObject();
      while (in.hasNext()) {
        String fld = in.nextName();
        if (taggable) fld = tagJsonReader.tagField(fld);
        map.put(fld, read(in));
      }
      in.endObject();
      return map;

    case STRING:
      String val = in.nextString();
      if (taggable) return tagJsonReader.tagValue(val);
      return val;

    case NUMBER:
      return in.nextDouble();

    case BOOLEAN:
      return in.nextBoolean();

    case NULL:
      in.nextNull();
      return null;

    default:
      throw new IllegalStateException();
    }
  }

  @SuppressWarnings("unchecked")
  @Override public void write(JsonWriter out, Object value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }

    TypeAdapter<Object> typeAdapter = (TypeAdapter<Object>) platies.getAdapter(value.getClass());
    if (typeAdapter instanceof ObjectTypeAdapter) {
      out.beginObject();
      out.endObject();
      return;
    }

    typeAdapter.write(out, value);
  }
}
