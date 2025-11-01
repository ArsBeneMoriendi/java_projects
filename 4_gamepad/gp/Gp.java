package gp; 

public class Gp
{
    private final int MAX_VALUES = 21;
    private int values[];
    public Gp_values gp_values;
    static public native int   gp_open(int vendor_id, int product_id); 
    static public native int[] gp_read_data();
 
    public Gp_values get_values()  
    { 
        int i;
        
        values = gp_read_data();
        
        for(i = 0; i < gp_values.MAX_BUTTONS; i++)
        {
            if(values[i] == 1) { gp_values.buttons[i] = true;  }
            else               { gp_values.buttons[i] = false; }
        }
        
        if(values[6] == 1) { gp_values.analog_mode = true; }
        else               { gp_values.analog_mode = false; }
    
        if(gp_values.analog_mode)
        {
            gp_values.LX = values[17];
            gp_values.LY = values[18];
            gp_values.RX = values[19];
            gp_values.RY = values[20];
        }
        return gp_values; 
    }
   
    public Gp()
    {
        values = new int[MAX_VALUES];
        gp_values = new Gp_values();
        try 
        {
            System.loadLibrary("gamepad");
        }
        catch (UnsatisfiedLinkError e) 
        {
            System.out.println(e);
        }
    } 
}


