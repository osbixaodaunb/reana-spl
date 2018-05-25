package parsing.activitydiagrams;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ActivitySort {
	private ADReader adReader;
	private List<Activity> activities;
	Queue<Activity> queue;
	
	public ActivitySort(ADReader adr) {
		this.adReader = adr;
		activities = adReader.getActivities();
		queue = new LinkedList<Activity>();
	}
	
	public void orderActivities() {
		queue = new LinkedList<Activity>();
		Activity target, temp = null;

		int j = -1;
		int i = 0;
		stackUpActivities(i, j, temp);

		while (!queue.isEmpty()) {
			for (Edge e : queue.element().getOutgoing()) {
				target = e.getTarget();
				if (!target.isOrdered()) { // nao esta ordenado
					if (!activities.get(i).equals(target)) { // ordem errada
						j = -1;
						sort(target, j, i);
					}
					target.setOrdered(true); // marca ordem target certa
					i++;
					queue.add(target); // poe target na fila
				}
			}
			queue.poll(); // tira o primeiro da fila
		}
	}

	private void sort(Activity target, int j, int i) {
		Activity temp;
		for (Activity a : activities) {
			j++;
			if (a.equals(target)) { // j:posicao do target
									// i:posicao atual ordenacao
				temp = activities.get(i);
				activities.set(i, activities.get(j));
				activities.set(j, temp);
				break;
			}
		}
	}
	
	public void stackUpActivities(int i, int j, Activity temp) {
		for (Activity a : activities) {
			j++;
			if (a.getType().equals(ActivityType.INITIAL_NODE)) {
				if (!activities.get(i).equals(a)) { // PEGAR EXEMPLO P
															// TESTAR AQUI blz
					temp = activities.get(i);
					activities.set(i, activities.get(j));
					activities.set(j, temp);
				}
				a.setOrdered(true);
				i++;
				queue.add(a);
				break;
			}
		}
		
	}
}
