// Zhenwei Gu and Peng Gao

enum LockType {shared, exclusive};

public class Lock {
  
  LockType the_type; 
  Transaction the_holder;
  int the_siteId;
  String the_var_name;
  
  Lock (LockType type, Transaction holder, int siteId, String var_name) {
    this.the_type=type;
    this.the_holder=holder;
    this.the_siteId=siteId;
    this.the_var_name=var_name;
  }
  
}
