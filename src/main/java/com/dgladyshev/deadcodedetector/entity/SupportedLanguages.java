package com.dgladyshev.deadcodedetector.entity;

public enum SupportedLanguages {

	JAVA("java"),
	ADA("ada"),
	FORTRAN("fortran"),
	C_PLUS_PLUS("c++"); //partial support

	private String name;

	SupportedLanguages(String name) {
		this.name = name.toLowerCase();
	}

	public String getName() {
		return name;
	}
}
