package edu.university.ecs.lab.detection.metrics.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Operation of a microservice
 */
public class Operation {
	private String path;
	private String name;
	private List<Parameter> paramList;
	private List<String> usingTypesList;
	private String responseType;

	public Operation() {
		this.path = null;
		this.name = "OperationDefaultName";
		this.paramList = new ArrayList<>();
		this.usingTypesList = new ArrayList<>();
		this.responseType = "UNDEFINED";
	}

	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<Parameter> getParamList() {
		return paramList;
	}
	
	public void setParamList(List<Parameter> paramList) {
		this.paramList = paramList;
	}
	
	public List<String> getUsingTypesList() {
		return usingTypesList;
	}
	
	public void setUsingTypesList(List<String> usingTypesList) {
		this.usingTypesList = usingTypesList;
	}
	
	public String getResponseType() {
		return responseType;
	}
	
	public void setResponseType(String responseType) {
		this.responseType = responseType;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Operation op = new Operation();
		op.setName(this.name);
		op.setParamList(this.paramList);
		op.setUsingTypesList(this.usingTypesList);
		op.setPath(this.path);
		op.setResponseType(this.responseType);
		return op;
	}

	@Override
	public String toString() {
		return "Operation{" +
				"path='" + path + '\'' +
				", name='" + name + '\'' +
				", paramList=" + paramList +
				", usingTypesList=" + usingTypesList +
				", responseType='" + responseType + '\'' +
				'}';
	}
}
