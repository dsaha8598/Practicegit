package com.udajahaja;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

import org.assertj.core.util.Arrays;
import org.hibernate.internal.build.AllowSysOut;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UdajahajaUserApplicationTests {

	@Test
	void contextLoads() {
	}

	public static void main(String args[]) throws IOException {
/*		PDDocument document = new PDDocument();
	PDPage page = new PDPage();
	document.addPage(page);

	PDPageContentStream contentStream = new PDPageContentStream(document, page);

	contentStream.setFont(PDType1Font.COURIER, 12);
	contentStream.beginText();
	contentStream.showText("Hello World");
	contentStream.endText();
	contentStream.close();

	document.save("//home//dipak//Desktop//pdfBoxHelloWorld.pdf");
	document.close();*/
		Scanner sc=new Scanner(System.in);
		System.out.println("Enter First String");
		String s1=sc.next();
		System.out.println("Enter second string");
		String s2=sc.next();
		String[] charArray=s1.split("");
		String[] charArray2=s2.split("");
		List<String> list1=new ArrayList<>();
		List<String> list2=new ArrayList<>();
		
		Stream.of(charArray).forEach(n->list1.add(n));
		Stream.of(charArray2).forEach(n->list2.add(n));
		
		List<String> list3=new ArrayList<>();
		list3.addAll(list1);
		
		list1.removeAll(list2);
		list2.removeAll(list3);
		
		System.out.print("Output 1 ");
		for(String element:list1) {
			System.out.print(element);
		}
		System.out.println("\n Output 2 ");
		for(String element:list2) {
			System.out.print(element);
		}

		}
		
}
