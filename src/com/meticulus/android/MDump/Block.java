package com.meticulus.android.MDump;

public class Block {

	String _Blockpath = "";
	int _Sizeinbytes = 0;
	public Block(String Blockpath){
		_Blockpath = Blockpath;
	}
	public void setSize(int sizeinbytes){
		_Sizeinbytes = sizeinbytes;
	}
	public String getBlockName(){
		return _Blockpath;
	}
	public String getFileSize(){
		return String.valueOf(_Sizeinbytes) +" kb";
	}
	@Override
	public String toString(){
		return getBlockName();
	}
}
