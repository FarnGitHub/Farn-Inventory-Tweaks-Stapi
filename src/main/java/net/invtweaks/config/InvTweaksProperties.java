package net.invtweaks.config;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class InvTweaksProperties extends Properties {
	private static final long serialVersionUID = 1L;
	private final List keys = new LinkedList();

	public Enumeration keys() {
		return Collections.enumeration(new LinkedHashSet(this.keys));
	}

	public Object put(String key, Object value) {
		this.keys.add(key);
		return super.put(key, value);
	}

	public void sortKeys() {
		Collections.sort(this.keys);
	}
}
