/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package io.confluent.examples.streams.avro;

import org.apache.avro.specific.SpecificData;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class WikiFeed extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = -8040319673076936448L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"WikiFeed\",\"namespace\":\"io.confluent.examples.streams.avro\",\"fields\":[{\"name\":\"user\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"is_new\",\"type\":\"boolean\"},{\"name\":\"content\",\"type\":[{\"type\":\"string\",\"avro.java.string\":\"String\"},\"null\"]}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
   private java.lang.String user;
   private boolean is_new;
   private java.lang.String content;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public WikiFeed() {}

  /**
   * All-args constructor.
   * @param user The new value for user
   * @param is_new The new value for is_new
   * @param content The new value for content
   */
  public WikiFeed(java.lang.String user, java.lang.Boolean is_new, java.lang.String content) {
    this.user = user;
    this.is_new = is_new;
    this.content = content;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return user;
    case 1: return is_new;
    case 2: return content;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: user = (java.lang.String)value$; break;
    case 1: is_new = (java.lang.Boolean)value$; break;
    case 2: content = (java.lang.String)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'user' field.
   * @return The value of the 'user' field.
   */
  public java.lang.String getUser() {
    return user;
  }

  /**
   * Sets the value of the 'user' field.
   * @param value the value to set.
   */
  public void setUser(java.lang.String value) {
    this.user = value;
  }

  /**
   * Gets the value of the 'is_new' field.
   * @return The value of the 'is_new' field.
   */
  public java.lang.Boolean getIsNew() {
    return is_new;
  }

  /**
   * Sets the value of the 'is_new' field.
   * @param value the value to set.
   */
  public void setIsNew(java.lang.Boolean value) {
    this.is_new = value;
  }

  /**
   * Gets the value of the 'content' field.
   * @return The value of the 'content' field.
   */
  public java.lang.String getContent() {
    return content;
  }

  /**
   * Sets the value of the 'content' field.
   * @param value the value to set.
   */
  public void setContent(java.lang.String value) {
    this.content = value;
  }

  /**
   * Creates a new WikiFeed RecordBuilder.
   * @return A new WikiFeed RecordBuilder
   */
  public static io.confluent.examples.streams.avro.WikiFeed.Builder newBuilder() {
    return new io.confluent.examples.streams.avro.WikiFeed.Builder();
  }

  /**
   * Creates a new WikiFeed RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new WikiFeed RecordBuilder
   */
  public static io.confluent.examples.streams.avro.WikiFeed.Builder newBuilder(io.confluent.examples.streams.avro.WikiFeed.Builder other) {
    return new io.confluent.examples.streams.avro.WikiFeed.Builder(other);
  }

  /**
   * Creates a new WikiFeed RecordBuilder by copying an existing WikiFeed instance.
   * @param other The existing instance to copy.
   * @return A new WikiFeed RecordBuilder
   */
  public static io.confluent.examples.streams.avro.WikiFeed.Builder newBuilder(io.confluent.examples.streams.avro.WikiFeed other) {
    return new io.confluent.examples.streams.avro.WikiFeed.Builder(other);
  }

  /**
   * RecordBuilder for WikiFeed instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<WikiFeed>
    implements org.apache.avro.data.RecordBuilder<WikiFeed> {

    private java.lang.String user;
    private boolean is_new;
    private java.lang.String content;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(io.confluent.examples.streams.avro.WikiFeed.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.user)) {
        this.user = data().deepCopy(fields()[0].schema(), other.user);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.is_new)) {
        this.is_new = data().deepCopy(fields()[1].schema(), other.is_new);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.content)) {
        this.content = data().deepCopy(fields()[2].schema(), other.content);
        fieldSetFlags()[2] = true;
      }
    }

    /**
     * Creates a Builder by copying an existing WikiFeed instance
     * @param other The existing instance to copy.
     */
    private Builder(io.confluent.examples.streams.avro.WikiFeed other) {
            super(SCHEMA$);
      if (isValidValue(fields()[0], other.user)) {
        this.user = data().deepCopy(fields()[0].schema(), other.user);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.is_new)) {
        this.is_new = data().deepCopy(fields()[1].schema(), other.is_new);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.content)) {
        this.content = data().deepCopy(fields()[2].schema(), other.content);
        fieldSetFlags()[2] = true;
      }
    }

    /**
      * Gets the value of the 'user' field.
      * @return The value.
      */
    public java.lang.String getUser() {
      return user;
    }

    /**
      * Sets the value of the 'user' field.
      * @param value The value of 'user'.
      * @return This builder.
      */
    public io.confluent.examples.streams.avro.WikiFeed.Builder setUser(java.lang.String value) {
      validate(fields()[0], value);
      this.user = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'user' field has been set.
      * @return True if the 'user' field has been set, false otherwise.
      */
    public boolean hasUser() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'user' field.
      * @return This builder.
      */
    public io.confluent.examples.streams.avro.WikiFeed.Builder clearUser() {
      user = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'is_new' field.
      * @return The value.
      */
    public java.lang.Boolean getIsNew() {
      return is_new;
    }

    /**
      * Sets the value of the 'is_new' field.
      * @param value The value of 'is_new'.
      * @return This builder.
      */
    public io.confluent.examples.streams.avro.WikiFeed.Builder setIsNew(boolean value) {
      validate(fields()[1], value);
      this.is_new = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'is_new' field has been set.
      * @return True if the 'is_new' field has been set, false otherwise.
      */
    public boolean hasIsNew() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'is_new' field.
      * @return This builder.
      */
    public io.confluent.examples.streams.avro.WikiFeed.Builder clearIsNew() {
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'content' field.
      * @return The value.
      */
    public java.lang.String getContent() {
      return content;
    }

    /**
      * Sets the value of the 'content' field.
      * @param value The value of 'content'.
      * @return This builder.
      */
    public io.confluent.examples.streams.avro.WikiFeed.Builder setContent(java.lang.String value) {
      validate(fields()[2], value);
      this.content = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'content' field has been set.
      * @return True if the 'content' field has been set, false otherwise.
      */
    public boolean hasContent() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'content' field.
      * @return This builder.
      */
    public io.confluent.examples.streams.avro.WikiFeed.Builder clearContent() {
      content = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    @Override
    public WikiFeed build() {
      try {
        WikiFeed record = new WikiFeed();
        record.user = fieldSetFlags()[0] ? this.user : (java.lang.String) defaultValue(fields()[0]);
        record.is_new = fieldSetFlags()[1] ? this.is_new : (java.lang.Boolean) defaultValue(fields()[1]);
        record.content = fieldSetFlags()[2] ? this.content : (java.lang.String) defaultValue(fields()[2]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  private static final org.apache.avro.io.DatumWriter
    WRITER$ = new org.apache.avro.specific.SpecificDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  private static final org.apache.avro.io.DatumReader
    READER$ = new org.apache.avro.specific.SpecificDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

}
