// Zhenwei Gu and Peng Gao

import java.util.ArrayList;
import java.util.HashMap;


enum TransactionType{ ReadOnly, Regular }; 

public class Transaction {
	
  TransactionType type; 

  String name;
  
  //time when started, to indicate age
  int StartTime;   

  // after initialized 
  HashMap<String, Integer> snapshot = new HashMap<String,Integer>();
  
  // to keep trace of all locks
  ArrayList<Lock> lockTable = new ArrayList<Lock>(); 

  Lock getLock(String the_v_name) {  
	// to find lock on the_v_name
    for (Lock lock : this.lockTable) {
      if (lock.the_var_name.equals(the_v_name)) {
        return lock;
      }
    }
    return null;
  }
  boolean hasSLock (String v_name) { 
	// check if has a shared lock on variable 
    for (Lock lock : this.lockTable) {
      if (lock.the_type == LockType.shared && lock.the_var_name.equals(v_name)) {
        return true; 
      }
    }
    return false;
  }

  Lock getSLock (String the_v_name) { 
	// to get the shared-lock on variable 
    for (Lock the_lock : this.lockTable) {
      if (the_lock.the_type == LockType.shared && the_lock.the_var_name.equals(the_v_name)) {
        return the_lock; 
      }
    }
    return null;
  }

  Lock getEXLock (String the_v_name) { 
	// to get the ex-lock 
    for (Lock lock : this.lockTable) {
      if (lock.the_type == LockType.exclusive && lock.the_var_name.equals(the_v_name)) {
        return lock; 
      }
    }
    return null;
  }

  boolean hasEXLock (String the_v_name) { 
	// check if has a ex lock on variable 
    for (Lock lock : this.lockTable) {
      if (lock.the_type == LockType.exclusive && lock.the_var_name.equals(the_v_name)) {
        return true; 
      }
    }
    return false;
  }

  Transaction(String the_name, TransactionType the_transaction, int the_startTime) {
    this.type = the_transaction;
    this.name = the_name;
    this.StartTime = the_startTime;
  }

  int getStartTime() {
    return this.StartTime;
  }

  void abort() {
    // abort 
  
  }

  void commit(){
    // release locks, 
  }
}
