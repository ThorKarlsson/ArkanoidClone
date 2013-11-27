package com.Arkanoid;

public class ObjectReference {
	public int firstIndex;
	public int vertexCount;
	public int openGLPrimitiveType;

	public ObjectReference(int fi, int vc, int pt)
	{
		firstIndex = fi;
		vertexCount = vc;
		openGLPrimitiveType = pt;
	}
}
