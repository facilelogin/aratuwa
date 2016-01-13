package org.wso2.carbon.security.login;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.SerializedName;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "claim")
public class ClaimValue {

	@XmlAttribute(name = "claim_uri")
	@SerializedName("claim_uri")
	private String name;
	@XmlAttribute(name = "value")
	@SerializedName("value")
	private List<String> value = new ArrayList<String>();

	public ClaimValue() {
	}

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
