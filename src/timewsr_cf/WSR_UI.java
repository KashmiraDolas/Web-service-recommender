package timewsr_cf;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class WSR_UI implements ActionListener 
{
    JTextField location,category;  
    JButton submit,reset;
    JLabel l1,l2;
    public  void WSR_UI(){  
        JFrame f= new JFrame(); 
        
        l1=new JLabel("Enter your location");
        l1.setBounds(50, 50, 150, 20);
        
        location=new JTextField();  
        location.setBounds(50,75,150,20); 
        
        l2=new JLabel("Enter your category");
        l2.setBounds(50, 115, 150, 20);
        category=new JTextField();  
        category.setBounds(50,140,150,20);  
        //tf3=new JTextField();  
        //tf3.setBounds(50,150,150,20);  
        //tf3.setEditable(false);   
        submit=new JButton("Submit");  
        submit.setBounds(50,175,70,50);  
        reset=new JButton("Reset");  
        reset.setBounds(120,175,70,50);  
        submit.addActionListener(this);  
        reset.addActionListener(this); 
        
        f.add(l1);
        f.add(location);
        f.add(l2);
        f.add(category);
        f.add(submit);
        f.add(reset);  
        
        f.setSize(300,300);  
        f.setLayout(null);  
        f.setVisible(true);  
    }  
    
    
    public void actionPerformed(ActionEvent e) 
    {  
        if(e.getSource()==reset)
        {
            //getContentPane().removeAll();
            location.setText("");
            category.setText("");

        }
        
        else if(e.getSource()==submit)
        {
        String s1=location.getText();  
        String s2=category.getText();  
        
        
        System.out.println(s1);
        System.out.println(s2);
        }
        //String result=String.valueOf(c);  
       // tf3.setText(result);  
    }  
public static void main(String[] args) 
{  
    new WSR_UI();  
} 
}


