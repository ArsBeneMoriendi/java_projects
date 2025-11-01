
package gp;

public class Gp_values
{
    public final int MAX_BUTTONS = 17;
    
    public boolean buttons[], analog_mode;
    public int LX, LY, RX, RY;
  
    /* 
    0  TL
    1  TR    
    2  ZL
    3  ZR
    4  SELECT
    5  START
    6  ANALOG
    7  1
    8  2
    9  3
    10 4    
    11 N
    12 E
    13 S
    14 W
    15 L_PRESSED
    16 R_PRESSED 
    */
   
    public Gp_values()
    {
        int i;
   
        buttons = new boolean[MAX_BUTTONS];

        for(i = 0; i < MAX_BUTTONS; i++) 
        {
            buttons[i] = false;
        }

        analog_mode = false;

        LX = 0;
        LY = 0;
        RX = 0;
        RY = 0;
    }
}

