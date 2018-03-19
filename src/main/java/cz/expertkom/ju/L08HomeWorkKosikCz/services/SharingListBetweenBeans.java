package cz.expertkom.ju.L08HomeWorkKosikCz.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class SharingListBetweenBeans {
	
	private List <People> listPeoples = new ArrayList<People>();
	
	private class People {
		private String name ="";
		private float weight = 0f;
		public People(String name, float weight) {
			super();
			this.name = name;
			this.weight = weight;
		}
		@Override
		public String toString() {
			return "People [name=" + name + ", weight=" + weight + "]";
		}
		
	}
	
	public void addPeople (String name, float weight) {
		listPeoples.add(new People(name, weight));
	}
	
	public void showAllPeoples() {
		for (People p: listPeoples) {
			System.out.println(p.toString());
		}
	}

}
