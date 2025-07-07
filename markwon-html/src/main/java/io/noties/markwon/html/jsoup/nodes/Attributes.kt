package io.noties.markwon.html.jsoup.nodes

import io.noties.markwon.html.jsoup.helper.Normalizer.lowerCase
import io.noties.markwon.html.jsoup.helper.Validate.isFalse
import io.noties.markwon.html.jsoup.helper.Validate.isTrue
import io.noties.markwon.html.jsoup.helper.Validate.notNull
import kotlin.math.min

/**
 * The attributes of an Element.
 *
 *
 * Attributes are treated as a map: there can be only one value associated with an attribute key/name.
 *
 *
 *
 * Attribute name and value comparisons are  generally **case sensitive**. By default for HTML, attribute names are
 * normalized to lower-case on parsing. That means you should use lower-case strings when referring to attributes by
 * name.
 *
 *
 * @author Jonathan Hedley, jonathan@hedley.net
 */
class Attributes : Iterable<Attribute>, Cloneable {
    private var size = 0 // number of slots used (not capacity, which is keys.length
    var keys: Array<String?> = Empty
    var vals: Array<String?> = Empty

    // check there's room for more
    private fun checkCapacity(minNewSize: Int) {
        isTrue(minNewSize >= size)
        val curSize = keys.size
        if (curSize >= minNewSize) return

        var newSize: Int = if (curSize >= InitialCapacity) size * GrowthFactor else InitialCapacity
        if (minNewSize > newSize) newSize = minNewSize

        keys = copyOf(keys, newSize)
        vals = copyOf(vals, newSize)
    }

    fun indexOfKey(key: String): Int {
        notNull(key)
        for (i in 0..<size) {
            if (key == keys[i]) return i
        }
        return NotFound
    }

    private fun indexOfKeyIgnoreCase(key: String): Int {
        notNull(key)
        for (i in 0..<size) {
            if (key.equals(keys[i], ignoreCase = true)) return i
        }
        return NotFound
    }

    /**
     * Get an attribute value by key.
     *
     * @param key the (case-sensitive) attribute key
     * @return the attribute value if set; or empty string if not set (or a boolean attribute).
     * @see .hasKey
     */
    fun get(key: String): String? {
        val i = indexOfKey(key)
        return if (i == NotFound) EmptyString else checkNotNull(vals[i])
    }

    /**
     * Get an attribute's value by case-insensitive key
     *
     * @param key the attribute name
     * @return the first matching attribute value if set; or empty string if not set (ora boolean attribute).
     */
    fun getIgnoreCase(key: String): String? {
        val i = indexOfKeyIgnoreCase(key)
        return if (i == NotFound) EmptyString else checkNotNull(vals[i])
    }

    // adds without checking if this key exists
    private fun add(key: String?, value: String?) {
        checkCapacity(size + 1)
        keys[size] = key
        vals[size] = value
        size++
    }

    /**
     * Set a new attribute, or replace an existing one by key.
     *
     * @param key   case sensitive attribute key
     * @param value attribute value
     * @return these attributes, for chaining
     */
    fun put(key: String, value: String?): Attributes {
        val i = indexOfKey(key)
        if (i != NotFound) vals[i] = value
        else add(key, value)
        return this
    }

    fun putIgnoreCase(key: String, value: String?) {
        val i = indexOfKeyIgnoreCase(key)
        if (i != NotFound) {
            vals[i] = value
            if (keys[i] != key)  // case changed, update
                keys[i] = key
        } else add(key, value)
    }

    /**
     * Set a new boolean attribute, remove attribute if value is false.
     *
     * @param key   case **insensitive** attribute key
     * @param value attribute value
     * @return these attributes, for chaining
     */
    fun put(key: String, value: Boolean): Attributes {
        if (value) putIgnoreCase(key, null)
        else remove(key)
        return this
    }

    /**
     * Set a new attribute, or replace an existing one by key.
     *
     * @param attribute attribute with case sensitive key
     * @return these attributes, for chaining
     */
    fun put(attribute: Attribute): Attributes {
        notNull(attribute)
        put(attribute.key!!, attribute.value)
        attribute.parent = this
        return this
    }

    // removes and shifts up
    private fun remove(index: Int) {
        isFalse(index >= size)
        val shifted = size - index - 1
        if (shifted > 0) {
            System.arraycopy(keys, index + 1, keys, index, shifted)
            System.arraycopy(vals, index + 1, vals, index, shifted)
        }
        size--
        keys[size] = null // release hold
        vals[size] = null
    }

    /**
     * Remove an attribute by key. **Case sensitive.**
     *
     * @param key attribute key to remove
     */
    fun remove(key: String) {
        val i = indexOfKey(key)
        if (i != NotFound) remove(i)
    }

    /**
     * Remove an attribute by key. **Case insensitive.**
     *
     * @param key attribute key to remove
     */
    fun removeIgnoreCase(key: String) {
        val i = indexOfKeyIgnoreCase(key)
        if (i != NotFound) remove(i)
    }

    /**
     * Tests if these attributes contain an attribute with this key.
     *
     * @param key case-sensitive key to check for
     * @return true if key exists, false otherwise
     */
    fun hasKey(key: String): Boolean {
        return indexOfKey(key) != NotFound
    }

    /**
     * Tests if these attributes contain an attribute with this key.
     *
     * @param key key to check for
     * @return true if key exists, false otherwise
     */
    fun hasKeyIgnoreCase(key: String): Boolean {
        return indexOfKeyIgnoreCase(key) != NotFound
    }

    /**
     * Get the number of attributes in this set.
     *
     * @return size
     */
    fun size(): Int {
        return size
    }

    /**
     * Add all the attributes from the incoming set to this set.
     *
     * @param incoming attributes to add to these attributes.
     */
    fun addAll(incoming: Attributes) {
        if (incoming.size() == 0) return
        checkCapacity(size + incoming.size)

        for (attr in incoming) {
            // todo - should this be case insensitive?
            put(attr)
        }
    }

    override fun iterator(): Iterator<Attribute> {
        return object : Iterator<Attribute> {
            var i: Int = 0

            override fun hasNext(): Boolean {
                return i < size
            }

            override fun next(): Attribute {
                val `val` = vals[i]
                val attr = Attribute(keys[i]!!, `val` ?: "", this@Attributes)
                i++
                return attr
            }
        }
    }

    //    /**
    //     Get the attributes as a List, for iteration.
    //     @return an view of the attributes as an unmodifialbe List.
    //     */
    //    public List<Attribute> asList() {
    //        ArrayList<Attribute> list = new ArrayList<>(size);
    //        for (int i = 0; i < size; i++) {
    /*            Attribute attr = vals[i] == null ?
    * /                    new BooleanAttribute(keys[i]) : // deprecated class, but maybe someone still wants it
    * /                    new Attribute(keys[i], vals[i], Attributes.this);
    * /            list.add(attr); */
    //            list.add(new Attribute(keys[i], vals[i], Attributes.this));
    //        }
    //        return Collections.unmodifiableList(list);
    //    }
    //    /**
    //     * Retrieves a filtered view of attributes that are HTML5 custom data attributes; that is, attributes with keys
    //     * starting with {@code data-}.
    //     * @return map of custom data attributes.
    //     */
    //    public Map<String, String> dataset() {
    //        return new Dataset(this);
    //    }
    //    /**
    //     Get the HTML representation of these attributes.
    //     @return HTML
    //     @throws SerializationException if the HTML representation of the attributes cannot be constructed.
    //     */
    //    public String html() {
    //        StringBuilder accum = new StringBuilder();
    //        try {
    //            html(accum, (new Document("")).outputSettings()); // output settings a bit funky, but this html() seldom used
    //        } catch (IOException e) { // ought never happen
    //            throw new SerializationException(e);
    //        }
    //        return accum.toString();
    //    }
    //
    //    final void html(final Appendable accum, final Document.OutputSettings out) throws IOException {
    //        final int sz = size;
    //        for (int i = 0; i < sz; i++) {
    //            // inlined from Attribute.html()
    //            final String key = keys[i];
    //            final String val = vals[i];
    //            accum.append(' ').append(key);
    //
    //            // collapse checked=null, checked="", checked=checked; write out others
    //            if (!Attribute.shouldCollapseAttribute(key, val, out)) {
    //                accum.append("=\"");
    //                Entities.escape(accum, val == null ? EmptyString : val, out, true, false, false);
    //                accum.append('"');
    //            }
    //        }
    //    }
    //
    //    @Override
    //    public String toString() {
    //        return html();
    //    }
    /**
     * Checks if these attributes are equal to another set of attributes, by comparing the two sets
     *
     * @param other attributes to compare with
     * @return if both sets of attributes have the same content
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as Attributes

        if (size != that.size) return false
        if (!keys.contentEquals(that.keys)) return false
        return vals.contentEquals(that.vals)
    }

    /**
     * Calculates the hashcode of these attributes, by iterating all attributes and summing their hashcodes.
     *
     * @return calculated hashcode
     */
    override fun hashCode(): Int {
        var result = size
        result = 31 * result + keys.contentHashCode()
        result = 31 * result + vals.contentHashCode()
        return result
    }

    public override fun clone(): Attributes {
        val clone: Attributes
        try {
            clone = super.clone() as Attributes
        } catch (e: CloneNotSupportedException) {
            throw RuntimeException(e)
        }
        clone.size = size
        keys = copyOf(keys, size)
        vals = copyOf(vals, size)
        return clone
    }

    /**
     * Internal method. Lowercases all keys.
     */
    fun normalize() {
        for (i in 0..<size) {
            keys[i] = lowerCase(keys[i])
        }
    } //    private static class Dataset extends AbstractMap<String, String> {
    //        private final Attributes attributes;
    //
    //        private Dataset(Attributes attributes) {
    //            this.attributes = attributes;
    //        }
    //
    //        @Override
    //        public Set<Entry<String, String>> entrySet() {
    //            return new EntrySet();
    //        }
    //
    //        @Override
    //        public String put(String key, String value) {
    //            String dataKey = dataKey(key);
    //            String oldValue = attributes.hasKey(dataKey) ? attributes.get(dataKey) : null;
    //            attributes.put(dataKey, value);
    //            return oldValue;
    //        }
    //
    //        private class EntrySet extends AbstractSet<Map.Entry<String, String>> {
    //
    //            @Override
    //            public Iterator<Map.Entry<String, String>> iterator() {
    //                return new DatasetIterator();
    //            }
    //
    //            @Override
    //            public int size() {
    //                int count = 0;
    //                Iterator iter = new DatasetIterator();
    //                while (iter.hasNext())
    //                    count++;
    //                return count;
    //            }
    //        }
    //
    //        private class DatasetIterator implements Iterator<Map.Entry<String, String>> {
    //            private Iterator<Attribute> attrIter = attributes.iterator();
    //            private Attribute attr;
    //            public boolean hasNext() {
    //                while (attrIter.hasNext()) {
    //                    attr = attrIter.next();
    //                    if (attr.isDataAttribute()) return true;
    //                }
    //                return false;
    //            }
    //
    //            public Entry<String, String> next() {
    //                return new Attribute(attr.getKey().substring(dataPrefix.length()), attr.getValue());
    //            }
    //
    //            public void remove() {
    //                attributes.remove(attr.getKey());
    //            }
    //        }
    //    }
    //    private static String dataKey(String key) {
    //        return dataPrefix + key;
    //    }

    companion object {
        //    protected static final String dataPrefix = "data-";
        private const val InitialCapacity =
            4 // todo - analyze Alexa 1MM sites, determine best setting

        // manages the key/val arrays
        private const val GrowthFactor = 2
        private val Empty = arrayOf<String?>()
        const val NotFound: Int = -1
        private const val EmptyString = ""

        // simple implementation of Arrays.copy, for support of Android API 8.
        private fun copyOf(orig: Array<String?>, size: Int): Array<String?> {
            val copy = arrayOfNulls<String>(size)
            System.arraycopy(orig, 0, copy, 0, min(orig.size, size))
            return copy
        }

        // we track boolean attributes as null in values - they're just keys. so returns empty for consumers
        fun checkNotNull(`val`: String?): String {
            return `val` ?: EmptyString
        }
    }
}