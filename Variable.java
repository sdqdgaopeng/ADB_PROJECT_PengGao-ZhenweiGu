// Zhenwei Gu and Peng Gao

enum Vtype {clean, dirty};

public class Variable  {
  
  String name; 
  int value; 
  double getVal() {
    return this.value; 
  }
  void setVal(int the_val) {
    this.value = the_val;
  }
  Variable(String the_name , int the_val) {
    this.value = the_val; 
    this.name = the_name; 
  }

}
