
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
/**This solution mainly uses hashmap whose implementation provides constant time performance for basic operations such as get 
 * and containKeys.
 * The second mostly used is LinkedList which provides O(1) performance for operations
 * such as add, removeLast, peekLast  for linkedList implements Deque which is bi-directional.
 * The third is HashSet which is similar to HashMap in time performance.
 * 
 * @author Fan
 *
 */
public class Db {
	private HashMap<String, LinkedList<Integer>> db;
	private HashMap<Integer,HashSet<String>> transac_helper;
	private HashMap<Integer, Integer> value_freq;
	private int num_of_transac;
	public Db() {
		//db stores variables and the history values of each variable.
		db = new HashMap<String, LinkedList<Integer>>();
		//transac_helper stores transaction numbers and the corresponding variables involved 
		transac_helper = new HashMap<Integer,HashSet<String>>();
		//value_freq stores values of variables and the number of variables storing the same value.
		value_freq = new HashMap<Integer, Integer>();
		num_of_transac = 0;
	}
	public void assign(String s, Integer n) {
		update(s, n);
	}
	public void unset(String s) {
		update(s, null);
	}
	//always peek the last value whether or not in transaction
	public Integer get(String s) {
		if(db.containsKey(s)) return db.get(s).peekLast();
		else return null;
	}
	//return the number of variables with a same value.
	public Integer get_value_freq(Integer n) {
		int ret = 0;
		ret += value_freq.containsKey(n) ? value_freq.get(n) : 0;
		return ret;
	}
	public void begin() {
		num_of_transac += 1;
	}
	//cancel the operations in a transaction by setting the involved variables to their second last values in history
	//then update the values hashMap.
	public boolean rollback(){
		if(!isTransacOpen()) return false;
		if(transac_helper.containsKey(num_of_transac)) {
			for(String s : transac_helper.get(num_of_transac)) {
				Integer n = db.get(s).removeLast();
				if(db.get(s).size() == 0) db.remove(s);
				update_value_freq(n, get(s));
			}
		}
		num_of_transac -= 1;
		return true;
	}
	//save only the last value in the history value list for each variable
	public boolean commit(){
		if(!isTransacOpen()) return false;
		num_of_transac = 0;
		for(String s : db.keySet()) {
			db.put(s, new LinkedList<Integer>(Arrays.asList(db.get(s).peekLast())));
		}
		transac_helper = new HashMap<Integer,HashSet<String>>();
		return true;
	}
	//process the command
	//TODO: Use lambda to process the commands.
	public boolean command_process(String cmd) {
		String[] command = cmd.split("\\s");
		int len = command.length;
		String op = command[0].toUpperCase();
		if(op.equals("END")) return false;
		else if(op.equals("SET") && len == 3) assign(command[1], Integer.parseInt(command[2])); 
		else if(op.equals("GET") && len == 2) display(get(command[1]));
		else if(op.equals("UNSET") && len == 2) unset(command[1]);
		else if(op.equals("NUMEQUALTO") && len == 2) display(get_value_freq(Integer.parseInt(command[1])));
		else if(op.equals("BEGIN") && len == 1) begin();
		else if(op.equals("ROLLBACK") && len == 1) {
			if(!rollback()) System.out.println("NO TRANSACTION");}
		else if(op.equals("COMMIT") && len == 1) {
			if(!commit()) System.out.println("NO TRANSACTION");
		}
		else System.out.println("INVALID COMMAND");
		return true;
	}
	private void display(Integer n) {
		if(n != null) System.out.println(n);
		else System.out.println("NULL");
	}
	//update the value frequency, do not save the frequency of null.
	private void update_value_freq(Integer n1, Integer n2) {
		HashMap<Integer, Integer>  freq = value_freq;
		if(n1 != null) {
			if(freq.containsKey(n1)) freq.put(n1, freq.get(n1) - 1);
			else freq.put(n1, -1);
		}
		if(n2 != null) {
			if(freq.containsKey(n2)) freq.put(n2, freq.get(n2) + 1); 
			else freq.put(n2,1);
		}
	}
	private boolean isTransacOpen() {
		return num_of_transac > 0;
	}
	//edit, insert and remove a variable in database.
	private void update(String s, Integer n) {
		Integer curval = get(s);
		if(curval == n) return;
		if(!isTransacOpen() && n == null) db.remove(s);
		else {
			if(db.containsKey(s)) db.get(s).add(n);
			else db.put(s, new LinkedList<Integer>(Arrays.asList(n)));
			if(transac_helper.containsKey(num_of_transac)) transac_helper.get(num_of_transac).add(s);
			else transac_helper.put(num_of_transac, new HashSet<String>(Arrays.asList(s)));
		}
		update_value_freq(curval, n);
	}
	//test client
	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		String command = input.nextLine();
		Db db = new Db();
		while(db.command_process(command)) {
			command = input.nextLine();
		}
		input.close();
	}
}
