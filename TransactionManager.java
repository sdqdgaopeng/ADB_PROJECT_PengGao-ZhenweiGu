// Zhenwei Gu and Peng Gao

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class TransactionManager {

  ArrayList<Transaction> transList = new ArrayList<Transaction>(); 

  // list of aborted transactions
  ArrayList<String> abortList = new ArrayList<String>(); 

  // waiting-list of pending commands
  ArrayList<String> waitList = new ArrayList<String> ();

  // maps from a transaction to pending-operations
  HashMap<String,Integer> pendingOperations = new HashMap<String,Integer>(); 


  Transaction getTransaction (String the_t_name) {
    for (Transaction t : this.transList) {
      if (t.name.equals(the_t_name)) {
        return t; 
      }
    }
    return null;
  }

  void begin(String the_name, int startingTime, HashMap<String, Integer> the_snapshot) {
    Transaction t = new Transaction(the_name, TransactionType.Regular, startingTime);

    Iterator<String> keySetIterator = the_snapshot.keySet().iterator();
    while(keySetIterator.hasNext()){
      String key = keySetIterator.next();
      t.snapshot.put(key, the_snapshot.get(key));
    }

    this.transList.add(t);
    this.pendingOperations.put(the_name, 0);
    System.out.println("Regular transaction "+t.name+" begins at time "+t.StartTime+". ");
  }

  void beginRO(String the_name, int startingTime, HashMap<String, Integer> the_snapshot) {
    Transaction t = new Transaction(the_name, TransactionType.ReadOnly, startingTime);

    Iterator<String> the_keySetIterator = the_snapshot.keySet().iterator();
    while(the_keySetIterator.hasNext()){
      String key = the_keySetIterator.next();
      t.snapshot.put(key, the_snapshot.get(key));
    }

    this.transList.add(t);
    this.pendingOperations.put(the_name, 0);
    System.out.println("RO transaction "+t.name+" begins at time "+t.StartTime+". ");
  }

  void abort(Transaction the_transaction) {
    // abort 
 
  }

  boolean Aborted (String the_t_name) {
    if (abortList.contains(the_t_name)) {
      return true;
    }else {
      return false;
    }
  }

  void commit(Transaction the_transaction) {
    the_transaction.commit();
  }
}
