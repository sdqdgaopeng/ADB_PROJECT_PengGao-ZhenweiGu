// Zhenwei Gu and Peng Gao

import java.io.BufferedReader;

import java.io.FileReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CommandManager {

  ArrayList<ArrayList<String>> list;

  ArrayList<ArrayList<String>> getList () {
    return this.list;
  }
  
  CommandManager () {
    this.list = new ArrayList<ArrayList<String>>();
  }

  void getCommands (char the_Type, String the_Path) throws IOException {
    BufferedReader br ; 
    if (the_Type == 'f' || the_Type == 'F' ) {
      br = new BufferedReader( new FileReader(the_Path) );
    }else{
      if (the_Type == 't' || the_Type == 'T' ) {
        br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Please input instructions. ");
        System.out.println("End the input by entering a new line followed by \" the end\". "); 
      }else {
        System.out.println("Invalid input. ");
        return;
      }
    }
    String line;
    
    while ((line=br.readLine())!=null){
      if (line.length() ==0) {
        continue; 
      }
      ArrayList<String> line_Commands = new ArrayList<>();
      if ( line.toLowerCase().startsWith("the end")) {
        return;
      }
      if (line.contains(";")) {
        String[] the_commands = line.split(";");
        for (String the_command : the_commands) {
          line_Commands.add(the_command.replaceAll("\\s",""));
        }
      }else {
        line_Commands.add(line.replaceAll("\\s",""));
      }
      list.add(line_Commands);
    }
    br.close();
  }
}
