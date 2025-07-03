package io.noties.markwon.html.jsoup.nodes;

import androidx.annotation.NonNull;

import java.util.Map;

import io.noties.markwon.html.jsoup.helper.Validate;

/**
 * A single key + value attribute. (Only used for presentation.)
 */
public class Attribute implements Map.Entry<String, String>, Cloneable {
    private String key;
    private String val;
    Attributes parent; // used to update the holding Attributes when the key / value is changed via this interface

    /**
     * Create a new attribute from unencoded (raw) key and value.
     *
     * @param key   attribute key; case is preserved.
     * @param value attribute value
     */
    public Attribute(String key, String value) {
        this(key, value, null);
    }

    /**
     * Create a new attribute from unencoded (raw) key and value.
     *
     * @param key    attribute key; case is preserved.
     * @param val    attribute value
     * @param parent the containing Attributes (this Attribute is not automatically added to said Attributes)
     */
    public Attribute(String key, String val, Attributes parent) {
        Validate.notNull(key);
        this.key = key.trim();
        Validate.notEmpty(key); // trimming could potentially make empty, so validate here
        this.val = val;
        this.parent = parent;
    }

    /**
     * Get the attribute key.
     *
     * @return the attribute key
     */
    public String getKey() {
        return key;
    }

    /**
     * Set the attribute key; case is preserved.
     *
     * @param key the new key; must not be null
     */
    public void setKey(String key) {
        Validate.notNull(key);
        key = key.trim();
        Validate.notEmpty(key); // trimming could potentially make empty, so validate here
        if (parent != null) {
            int i = parent.indexOfKey(this.key);
            if (i != Attributes.NotFound) parent.keys[i] = key;
        }
        this.key = key;
    }

    /**
     * Get the attribute value.
     *
     * @return the attribute value
     */
    public String getValue() {
        return val;
    }

    /**
     * Set the attribute value.
     *
     * @param val the new attribute value; must not be null
     */
    public String setValue(String val) {
        String oldVal = parent.get(this.key);
        if (parent != null) {
            int i = parent.indexOfKey(this.key);
            if (i != Attributes.NotFound) parent.vals[i] = val;
        }
        this.val = val;
        return oldVal;
    }

    @Override
    public boolean equals(Object o) { // note parent not considered
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attribute attribute = (Attribute) o;
        if (key != null ? !key.equals(attribute.key) : attribute.key != null) return false;
        return val != null ? val.equals(attribute.val) : attribute.val == null;
    }

    @Override
    public int hashCode() { // note parent not considered
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (val != null ? val.hashCode() : 0);
        return result;
    }

    @NonNull
    @Override
    public Attribute clone() {
        try {
            return (Attribute) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
