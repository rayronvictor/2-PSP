package br.ufrn.peripateticosACS;

import java.util.ArrayList;
import java.util.List;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ACS acs = new ACS(10, 5, 1.f, 3.f, 0.9f, 0.1f, 0.1f);
		acs.start();
		
//		ACS acs = new ACS(10, 50, 3.0f, 0.9f, 0.1f, 0.1f);
//		List<Node> nodes = acs.loadNodes();
//		
//		int[] arr = {3, 73, 59, 41, 89, 30, 68, 2, 35, 23, 21, 70, 76, 91, 94, 95, 50, 62, 83, 72, 86, 5, 43, 56, 71, 38, 39, 28, 88, 98, 58, 34, 90, 25, 17, 8, 22, 75, 54, 6, 80, 77, 65, 31, 47, 67, 55, 42, 20, 64, 79, 13, 15, 27, 85, 1, 53, 40, 12, 49, 18, 29, 46, 24, 32, 7, 82, 78, 9, 26, 61, 37, 16, 51, 63, 44, 66, 48, 84, 11, 52, 87, 96, 97, 81, 45, 33, 100, 74, 57, 36, 14, 10, 92, 19, 99, 93, 4, 60, 69, 3};
//		double total = 0.0;
//
//		for(int i = 0; i < arr.length-1; i++) {
//			total += acs.calculateDistance(nodes.get(arr[i]-1), nodes.get(arr[i+1]-1));
//		}
//		System.out.println(total);
//		for(int i = 0; i < arr.length; i++) {
//			for(int j = i+1; j < arr.length; j++) {
//				if(arr[i] == arr[j]) {
//					System.out.print("value: " + arr[i]);
//					System.out.println(", i: " + i + ", j: " + j);
//				}
//			}
//		}
	}

}
