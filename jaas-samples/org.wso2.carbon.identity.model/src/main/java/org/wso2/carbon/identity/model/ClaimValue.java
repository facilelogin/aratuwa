package org.wso2.carbon.identity.model;

import java.util.ArrayList;
import java.util.List;

public class ClaimValue {

	private String name;
	private List<String> value = new ArrayList<String>();

	/**
	 * 
	 * @param name
	 * @param value
	 */
	public ClaimValue(String name, List<String> value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getValue() {
		return value;
	}

	public void setValue(List<String> value) {
		this.value = value;
	}
}
