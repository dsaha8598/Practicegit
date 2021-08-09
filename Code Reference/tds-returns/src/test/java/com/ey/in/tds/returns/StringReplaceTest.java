package com.ey.in.tds.returns;

public class StringReplaceTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String lastString="^O^^1^^AAAPA1234A^^1234567890^Praveen Morsa^1.00^0.00^0.00^1.00^^1.00^^^10.00^09122020^09122020^^10.0000^^^^^^^94C^^^^^^^^^^^^^^^";
		String firstString="^DD^1^1^";
		
		for (int i = 1; i < 500; i++) {
			System.out.println((i+3)+firstString+(i)+lastString);
		}
		
		
		
	}

}
