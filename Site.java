// Zhenwei Gu and Peng Gao

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;



public class Site {

  int id;

  //map from variable-name to variable-object
  HashMap<String, Variable> variables = new HashMap<String, Variable>();  
  
  //map from variable-name to its lock, if a variable maps to shared-locks, the lock list is more than 1, 
  //if a variable maps to a exclusive-lock, the lock list is 1. 
  
  HashMap<String, ArrayList<Lock>> lockTable = new HashMap<String, ArrayList<Lock>>();  
  
  Site (int id) {
	this.id = id; 
	variables = new HashMap<String,Variable>();
	lockTable = new HashMap<String,ArrayList<Lock>>();
  }

  static Site getSite (ArrayList<Site> list, int the_id) {
    for (Site the_site : list) {
      if (the_site.id == the_id) {
        return the_site; 
      }
    }
    return null;
  }

  // to check if a variable exist on any of the working sites
  // if a variable doesn't exist, then return false
  static boolean canAccess(ArrayList<Site> list, String the_v_name) { 
    for (Site site : list) {
      if (site.variables.containsKey(the_v_name)) {
        return true;
      }
    }
    return false;
  }

  boolean v_writable (String the_t_name, String the_v_name) {  
	// to check if it is writable
    if (this.lockTable.containsKey(the_v_name)) {
      for ( Lock lock : this.lockTable.get(the_v_name)) {
        if ( ! lock.the_holder.name.equals(the_t_name)) {
          return false; 
        }

      }
      return true;
    }
    else {
      return true;
    }
  }

  boolean shouldAbort(Transaction the_transaction, String the_v_name) {
    // if the transaction is expecting a lock from an older transaction
    // it is aborted!
    if (this.lockTable.containsKey(the_v_name)) {
      for (Lock lock : this.lockTable.get(the_v_name)) {
        Transaction holder = lock.the_holder;
        if (holder.StartTime < the_transaction.StartTime) {  

          return true;
        }
      }
      return false;
    }else {
      return false;
    }

  }

  void addEXLock (String the_v_name, Lock the_lock) {   
    ArrayList<Lock> lockList = new ArrayList<Lock>();
    lockList.add(the_lock);
    this.lockTable.put(the_v_name, lockList);

  }

  void deleteLock (LockType the_type, String the_holder_name, String the_var_name) {
    if (this.lockTable.containsKey(the_var_name)) {
      for (int i=this.lockTable.get(the_var_name).size()-1; i>=0; i--) {
        Lock lock = this.lockTable.get(the_var_name).get(i);
        if (lock.the_type == the_type && lock.the_holder.name.equals(the_holder_name) && lock.the_var_name.equals(the_var_name)) {
          this.lockTable.get(the_var_name).remove(lock);
        }
      }
      if (this.lockTable.get(the_var_name).size() ==0) {
        this.lockTable.remove(the_var_name);
      }
    }else {
      return;
    }
  }

  void addVariable (Variable v) {  
	// initialization 
    this.variables.put(v.name , v);
  }

  static void dumpAllSites(ArrayList<Site> list) {
    for (Site site : list) {
      Site.dump1Site(site);
    }
  }

  static void dumpAllVariables(ArrayList<Site> list, String the_v_name) {
    for (Site site : list) {
      ArrayList<Variable> list2 = new ArrayList<Variable>();
      Iterator<String> the_keySetIterator = site.variables.keySet().iterator();
      while(the_keySetIterator.hasNext()){
        String key = the_keySetIterator.next();
        if (key.equals(the_v_name)) {
          list2.add(site.variables.get(key));
          continue;
        }
      }
      if (list2.size() != 0) {
        System.out.print("Site"+site.id+": ");
        System.out.print(list2.get(0).name+": "+list2.get(0).value+"; ");
        System.out.println();
      }
    }
  }

  static void dump1Site(Site the_site) {
    System.out.print("Site"+the_site.id+": ");
    Iterator<String> the_keySetIterator = the_site.variables.keySet().iterator();
    ArrayList<Variable> vars = new ArrayList<Variable>();
    while(the_keySetIterator.hasNext()){
      String key = the_keySetIterator.next();
      vars.add(the_site.variables.get(key));
    }
    
    Collections.sort(vars, new Comparator<Variable>() {
		@Override
		public int compare(Variable the_v1, Variable the_v2) {
			int id1 = Integer.parseInt(the_v1.name.substring(1));
		    int id2 = Integer.parseInt(the_v2.name.substring(1));
		    return id1-id2;
		}
    });
    
    for (Variable the_v : vars) {
      System.out.print(the_v.name+": "+the_v.value+"; ");
    }
    System.out.println();
  }
}
