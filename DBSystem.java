// Zhenwei Gu and Peng Gao

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBSystem {

  // the site list
  ArrayList<Site> sites = new ArrayList<Site>(); 
  
  // the Transaction Manager: TM
  TransactionManager TM = new TransactionManager();  
  // the Command Manager: CM
  CommandManager CM; 
 
  HashMap<String, Integer> the_Snapshot;
  
  ArrayList<Site> failSites = new ArrayList<Site>();

  void Initialize() throws IOException {

    the_Snapshot = new HashMap<String, Integer>();
    
    this.TM = new TransactionManager(); 
    
    for (int i =1; i<=10; i++) {
      Site s = new Site (i);
      this.sites.add(s);
    }
    
    // initialize variables and sites, 
    for (int i =1; i<=20; i++) {
      this.the_Snapshot.put('x'+Integer.toString(i), 10*i);

      if (i%2 == 1) {  
    	// odd variables 
        Variable v = new Variable('x'+Integer.toString(i) , 10*i);
        Site s = Site.getSite(this.sites,1+i%10);
        s.addVariable(v);
      }else {  
    	// even variables
        for (int j=1; j<=10; j++ ){
          Variable v = new Variable('x'+Integer.toString(i) , 10*i);
          
          Site s = Site.getSite(this.sites, j);
          //get Site
          s.addVariable(v);
        }
      }

    }
    
    	// get information for the input source: input file or standard input
        System.out.println("Please specify your input type. \"F\" for from file, \"T\" for from terminal");
        BufferedReader br = 
            new BufferedReader(new InputStreamReader(System.in));
        String line;
        char the_Type; 
        String the_Path=""; 
        
        
        if ((line=br.readLine())!=null){
          if (line.startsWith("F") || line.startsWith("f") ) {
            the_Type = 'F';
            System.out.println("Please provide the path to your file. ");
            line=br.readLine();
            if (line != null) {
              the_Path = line;
            }else {
              System.out.println("Path cannot be empty. "); 
            }
          }else {
            the_Type = 'T';
          }
        }else {
          System.out.println("Input type cannot be empty. ");
          return;
        }

    
    // To initialize a command manager: CM
    CM = new CommandManager();
    CM.getCommands(the_Type, the_Path);
  }

  // called after every regular commit, except for those RO transactions
  void updateSnapShot(Transaction t) {  
	// t wants to commit
    for (Lock lock : t.lockTable) {
      if (lock.the_type == LockType.exclusive) {  
    	  
    	// this transaction will do the behavior to write
        Variable v = Site.getSite(this.sites,lock.the_siteId).variables.get(lock.the_var_name);
        this.the_Snapshot.put(v.name, v.value);
      }
    }

  }


  void handleCommand (String the_command, int the_time) {
	  
	System.out.println(the_command);

	// parse command using regular expression
    Pattern pattern_begin = Pattern.compile("begin\\(([^ ]*)\\)");
    Pattern pattern_beginRO = Pattern.compile("beginRO\\(([^ ]*)\\)");
    Pattern pattern_R = Pattern.compile("R\\(([^ ]*)\\)");
    Pattern pattern_W = Pattern.compile("W\\(([^ ]*)\\)");
    Pattern pattern_dump = Pattern.compile("dump\\(([^ ]*)\\)");
    Pattern pattern_end = Pattern.compile("end\\(([^ ]*)\\)");
    Pattern pattern_fail = Pattern.compile("fail\\(([^ ]*)\\)");
    Pattern pattern_recover = Pattern.compile("recover\\(([^ ]*)\\)");

    Matcher matcher_begin = pattern_begin.matcher(the_command);
    Matcher matcher_beginRO = pattern_beginRO.matcher(the_command);
    Matcher matcher_R = pattern_R.matcher(the_command);
    Matcher matcher_W = pattern_W.matcher(the_command);
    Matcher matcher_dump = pattern_dump.matcher(the_command);
    Matcher matcher_end = pattern_end.matcher(the_command);
    Matcher matcher_fail = pattern_fail.matcher(the_command);
    Matcher matcher_recover = pattern_recover.matcher(the_command);

    // transactions are created! 
    if (matcher_begin.find()) {  
      // (regular) transaction begins!
      String my_t_name = matcher_begin.group(1);
      TM.begin(my_t_name, the_time, this.the_Snapshot);
    }
    if (matcher_beginRO.find()) {  
      // read-only transaction begins!
      String t_name = matcher_beginRO.group(1);
      TM.beginRO(t_name, the_time, this.the_Snapshot);

    }
    if (matcher_R.find()) {  
      // carry out read 
      String my_t_v = matcher_R.group(1);
      String[] parts = my_t_v.split(",");
      String my_t_name = parts[0];
      String the_v_name = parts[1];
      if (TM.Aborted(my_t_name)) {  
    	// this transaction is aborted
        System.out.println(my_t_name+" has aborted. \""+the_command+"\" cannot be completed. ");
        return;
      }
      Transaction t = this.TM.getTransaction(my_t_name);
      if ( !Site.canAccess(this.sites, the_v_name) && t.type == TransactionType.ReadOnly) {
        // if we cannot get access to the variable, and if this transaction is read-only;
        // then it will wait

        TM.waitList.add(the_command);
        int num = TM.pendingOperations.get(my_t_name);
        num++;
        TM.pendingOperations.put(my_t_name, num);
        return; 
      }
      boolean success = this.read(t, the_v_name);
      if (success == false) {
        if (TM.abortList.contains(t.name)) {
          return;
        }
        
        TM.waitList.add(the_command);
        int num = TM.pendingOperations.get(my_t_name);
        num++;
        TM.pendingOperations.put(my_t_name, num);
      }
    }
    if (matcher_W.find()) {  
      // carry out 'write' 
      String the_t_v_newVal = matcher_W.group(1);
      String[] the_parts = the_t_v_newVal.split(",");
      String the_t_name = the_parts[0];
      String the_v_name = the_parts[1];
      String the_new_val = the_parts[2];
      if ( !Site.canAccess(this.sites, the_v_name)) {
        System.out.println(the_v_name+" do not exist on any site. ");
        return; 
      }
      if (TM.Aborted(the_t_name)) {  
    	// this transaction is aborted!
        System.out.println(the_t_name+" has aborted. \""+the_command+"\" cannot be completed. ");
        return;
      }
      Transaction t = this.TM.getTransaction(the_t_name);
      boolean success = this.write(t, the_v_name, Integer.parseInt(the_new_val));
      if (success == false) {
        if (TM.abortList.contains(t.name)) {
          return;
        }
   
        TM.waitList.add(the_command);
        int num = TM.pendingOperations.get(the_t_name);
        num++;
        TM.pendingOperations.put(the_t_name, num);
      }
    }
    if (matcher_dump.find()) {  
      //carry out 'dump'!
      String content = matcher_dump.group(1);
      if (content.equals("")) {  
    	// to dump all sites!
        Site.dumpAllSites(this.sites);
        return;
      }
      if (content.startsWith("x")) {  
    	// to dump a variable on all sites
        Site.dumpAllVariables(this.sites, content);
      }else {  
    	// to dump one site
        Site site = Site.getSite(this.sites, Integer.parseInt(content));
        Site.dump1Site(site);
      }


    }
    if (matcher_end.find()) {  
      // transaction ends! 
      String the_t_name = matcher_end.group(1);
      if (TM.Aborted(the_t_name)) {  
    	// this transaction is aborted!
        System.out.println(the_t_name+" has aborted. \""+the_command+"\" cannot be completed. ");
        return;
      }
      if (TM.Aborted(the_t_name)) {  
    	// this transaction is aborted!
        System.out.println(the_t_name+" has aborted. ");
        return;
      }
      Transaction t = TM.getTransaction(the_t_name);
      boolean success = commit(t);
      if (success == false) {
 
        TM.waitList.add(the_command);
      }
    }
    if (matcher_fail.find()) {  
      // this transaction fails!
      String siteId = matcher_fail.group(1);
      this.fail(Integer.parseInt(siteId));
    }
    if (matcher_recover.find()) { 
      // a site is recovered 
      String siteId = matcher_recover.group(1);
      this.recover(Integer.parseInt(siteId));
    }

  }

  void handleAllCommands() {
    for (int i=0; i< this.CM.getList().size(); i++ ) { 
      // get the command list
      int time = i+1;
      
      ArrayList<String> list = this.CM.getList().get(i);
      
      for (String command : list) {  
    	
        handleCommand(command, time);
        
        ArrayList<String> copy_waitList = new ArrayList<String>();
        for(String pending : TM.waitList) {  
        
          copy_waitList.add(pending);
        }

        TM.waitList.clear();     // reset TM.pendingOperations
    
        Iterator<String> the_keySetIterator = TM.pendingOperations.keySet().iterator();
        while(the_keySetIterator.hasNext()){
          String key = the_keySetIterator.next();
          TM.pendingOperations.put(key, 0);
        }
        for(String pending : copy_waitList) {  
        
          handleCommand(pending, time);
        }
      }
    }
    while (TM.waitList.size()!=0) {
      ArrayList<String> copy_waitList = new ArrayList<String>();
      for(String pending_command : TM.waitList) {  
    	
        copy_waitList.add(pending_command);
      }

      TM.waitList.clear();

      Iterator<String> the_keySetIterator = TM.pendingOperations.keySet().iterator();
      while(the_keySetIterator.hasNext()){
        String key = the_keySetIterator.next();
        TM.pendingOperations.put(key, 0);
      }
      for(String pending_command : copy_waitList) {  

        handleCommand(pending_command, this.CM.getList().size()+1);
      }
    }

  }


  // the read operations
  boolean read(Transaction t, String the_v_name) {
    int siteId = 0;
    
    int val = 0;
    
    if (t.type == TransactionType.ReadOnly) {  
      // read value
      
      val = t.snapshot.get(the_v_name);
      System.out.println("Transaction "+t.name+" reads "+the_v_name+" as "+val+". ");
      return true; 
    }else {  
  
      if (t.hasSLock(the_v_name)) {
        siteId = t.getSLock(the_v_name).the_siteId;
        
        val = Site.getSite(this.sites, siteId).variables.get(the_v_name).value;
        
        System.out.println("Transaction "+t.name+" reads "+val+" from Site"+siteId+"'s "+the_v_name+". ");
        
        return true;
      }
      if (t.hasEXLock(the_v_name)) {
        siteId = t.getEXLock(the_v_name).the_siteId;
        val = Site.getSite(this.sites, siteId).variables.get(the_v_name).value;
        System.out.println("Transaction "+t.name+" reads "+val+" from Site"+siteId+"'s "+the_v_name+". "); 
        return true;
      }
      // to get S on one site;
      // to iterate all site to do the operations above 
      // if succeed, then printout result
      // if failed, add it to the waiting-list
      for (Site the_site : this.sites) {
        if (the_site.variables.containsKey(the_v_name)) {
          val = the_site.variables.get(the_v_name).value;
          if (the_site.lockTable.containsKey(the_v_name)) {  // can not get the lock
            if (the_site.lockTable.get(the_v_name).get(0).the_type == LockType.exclusive) {
              Transaction holder = the_site.lockTable.get(the_v_name).get(0).the_holder;
              if (holder.StartTime < t.StartTime) {  // younger waits for lock from older, should abort
                this.abort(t);
                System.out.println("Because it has to wait for an older transaction. ");
                return false;
              }
              continue;  
              // go ahead;
            }else {   
              // shared-lock
              // acquire the lock
              // to add lock to the site's lock-table
              // to add lock to  thetransaction's lock-table
              siteId = the_site.id;
              Lock lock = new Lock(LockType.shared, t, siteId, the_v_name);
              ArrayList<Lock> lockList = the_site.lockTable.get(the_v_name);
              lockList.add(lock);
              
              the_site.lockTable.put(the_v_name, lockList);
              
              t.lockTable.add(lock);
              
              System.out.println("Transaction "+t.name+" reads "+val+" from Site"+siteId+"'s "+the_v_name+". ");
              
              return true;
            }
          }else {
            // acquire the lock
            // to add lock to the site's lock-table
            // to add lock to the transaction's lock-table
            siteId = the_site.id;
            Lock lock = new Lock(LockType.shared, t, siteId, the_v_name);
            ArrayList<Lock> lockList = new ArrayList<Lock>();
            lockList.add(lock);
            the_site.lockTable.put(the_v_name, lockList);
            t.lockTable.add(lock);
            System.out.println("Transaction "+t.name+" reads "+val+" from Site"+siteId+"'s "+the_v_name+". ");
            return true;
          }
        }else {
          continue; 
          // go ahead;
        }
      }

      return false;
    }

  }
  
  // write 
  boolean write(Transaction t, String the_v_name, int the_newVal) {
    if (t.hasEXLock(the_v_name)) {
      System.out.println("Transaction "+t.name+" writes "+the_v_name+" to all possible sites with new value as "+the_newVal+". ");
      return true;
    }
    // to try to get ex-locks from all sites, 
    // to iterate all sites to do operations above 
    // if succeed, then write and printout result
    // if failed, do not get any lock, add to waiting-list, and return false
    int the_var_appearence = 0;
    int the_v_writable_appearence = 0;
    for (Site site : this.sites) {
    	
      if (site.variables.containsKey(the_v_name)) {
        the_var_appearence++;
        
        if ( site.v_writable(t.name, the_v_name)) {
          the_v_writable_appearence++;
        }

      }else {

      }

    }
    if (the_var_appearence == the_v_writable_appearence) {  
      // get all ex-locks
      // acquire ex-lock
      // add lock to lock-table of this site
      // add lock to lock-table this transaction
      // write 
      for (Site the_site : this.sites) {
        if (the_site.variables.containsKey(the_v_name)) {
          Lock lock = new Lock(LockType.exclusive, t, the_site.id, the_v_name);
          the_site.addEXLock(the_v_name, lock);
          t.lockTable.add(lock);
          Variable var = the_site.variables.get(the_v_name);
          var.setVal(the_newVal);
          the_site.variables.put(the_v_name, var);
        }
      }
      System.out.println("Transaction "+t.name+" writes "+the_v_name+" to all possible sites with new value as "+the_newVal+". ");
      return true;
    }else {
      for (Site site : this.sites) {
        // for any lock if it is waiting for is older than the transaction
        // it is aborted!
        if (site.shouldAbort(t, the_v_name) == true) {
          this.abort(t);
          System.out.println("Because it has to wait for an older transaction. ");
          break;
        }
      }
      return false; 
    }

  }


  // committed 
  boolean commit(Transaction t) {
    // to release all locks of this transaction 
    // to release all locks of related sites
    // to update snapshot via the transaction's lock table 
    if (TM.pendingOperations.containsKey(t.name)) {
      if (TM.pendingOperations.get(t.name)>0 ) {  
    	  
   
        return false;
      }
    }

    updateSnapShot(t); 
    for (Lock the_lock : t.lockTable) {
      int siteId = the_lock.the_siteId;
      Site site = Site.getSite(this.sites,siteId);
      site.deleteLock(the_lock.the_type, the_lock.the_holder.name, the_lock.the_var_name);
    }
    t.lockTable.clear();
    System.out.println("Transaction "+t.name+" commits successfully. ");
    return true;
  }

  // abort!
  void abort (Transaction t) {
    // to release all locks of this transaction 
    // to release all locks of related sites
    // to back all writes via its snapshot

    if (TM.Aborted(t.name)) { 
      
      return;
    }

    for (Lock the_lock : t.lockTable) {
      int siteId = the_lock.the_siteId;
      Site site = Site.getSite(this.sites, siteId);

      // to back the variable the lock is on 
      Variable var = site.variables.get(the_lock.the_var_name);
      var.value = this.the_Snapshot.get(the_lock.the_var_name);
      site.variables.put(the_lock.the_var_name, var);

      site.deleteLock(the_lock.the_type, the_lock.the_holder.name, the_lock.the_var_name);
      if (the_lock.the_type == LockType.exclusive) {
        Variable v = site.variables.get(the_lock.the_var_name);
        v.value = t.snapshot.get(the_lock.the_var_name);
        site.variables.put(the_lock.the_var_name, v);
      }
    }
    t.lockTable.clear();
    TM.abortList.add(t.name);  // add to TM's abort list
    System.out.print("We abort "+t.name+". ");
  }

  // failed
  void fail(int siteId) { 
	// fails
    // abort all transactions 
    System.out.println("Site"+siteId+" has failed. ");
    
    Site site = Site.getSite(this.sites,siteId);
    
    HashMap<String , ArrayList<Lock>> to_copy_lock_table = new HashMap<String , ArrayList<Lock>>();
    Iterator<String> keySetIterator = site.lockTable.keySet().iterator();
    while(keySetIterator.hasNext()){
    	
      String var = keySetIterator.next();
      ArrayList<Lock> lockList = site.lockTable.get(var);
      to_copy_lock_table.put(var, lockList);
    }

    Iterator<String> the_other_keySetIterator = to_copy_lock_table.keySet().iterator();
    while(the_other_keySetIterator.hasNext()){
      String var = the_other_keySetIterator.next();
      ArrayList<Lock> lockList = to_copy_lock_table.get(var);
      for (int i = lockList.size()-1; i>=0; i--) {
        Lock lock = lockList.get(i);
        this.abort(lock.the_holder);
        System.out.println("Because its site failed. ");
      }
    }


    // add to failed sites list
    this.failSites.add(site);
    // remove 
    this.sites.remove(site);
  }

  // recover 
  void recover(int siteId) {
    // Site.dumpAllVariables(this.sites,"x8");
    // to remove from fail site list
    // to update values via initial values
    // to add to sites list
    Site site = Site.getSite(this.failSites, siteId);
    this.failSites.remove(site);
    Iterator<String> the_keySetIterator = site.variables.keySet().iterator();
    while(the_keySetIterator.hasNext()){
      String the_v_name = the_keySetIterator.next();
      Variable var = site.variables.get(the_v_name);
      var.value = this.the_Snapshot.get(the_v_name);
      site.variables.put(the_v_name, var);
    }

    // to check every ex-lock, 
    // if there is one ex-lock on a variable, 
    // add ex-lock to it on this site 
    Iterator<String> it2 = site.variables.keySet().iterator();
    while(it2.hasNext()){  
   
      String the_v_name = it2.next();
      for (Site otherSite: this.sites){ 
        if (otherSite.lockTable.containsKey(the_v_name)) {
          if (otherSite.lockTable.get(the_v_name).get(0).the_type == LockType.exclusive) {
            // to add ex-lock 
            Transaction holder = otherSite.lockTable.get(the_v_name).get(0).the_holder;
       
            Lock lock = new Lock(LockType.exclusive, holder, siteId, the_v_name);
            ArrayList<Lock> lockList = new ArrayList<Lock>();
            lockList.add(lock);
            site.lockTable.put(the_v_name, lockList);
            holder.lockTable.add(lock);
            int new_val = Site.getSite(this.sites, holder.getLock(the_v_name).the_siteId).variables.get(the_v_name).value; 
            // should write the_new_val to this site
            Variable variable = site.variables.get(the_v_name);
            variable.value = new_val;
            site.variables.put(the_v_name, variable);
            break;
          }
        }

      }
    }
    this.sites.add(siteId-1,site);
    System.out.println("Site"+siteId+" has recoverd. ");
  }

  static public void main (String[] args) throws IOException {
    DBSystem DB = new DBSystem();
    DB.Initialize();
    DB.handleAllCommands();
  }

}
