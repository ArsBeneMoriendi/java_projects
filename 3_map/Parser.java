import java.io.File;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.StringTokenizer;

class Handler_1 extends DefaultHandler 
{
    String loc_name;
    int lasy = 0;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
    throws SAXException 
    {
        for (int i=0; i < attributes.getLength(); i++)
        {
            loc_name = attributes.getQName(i);   

            if (loc_name.equalsIgnoreCase("id") && attributes.getValue(loc_name).equalsIgnoreCase("lasy"))
            {
                System.out.println("W lesie !!!");
                System.out.println("attr name: : " + loc_name );
                lasy = 1;
            }

            if(lasy == 1)
            {
                if (qName=="path" && loc_name == "d")
                {
                    System.out.println("attr name: : " + loc_name );

                    StringTokenizer st = new StringTokenizer(attributes.getValue(loc_name), " ,Mlz");
                    java.util.List<Double> coor_list = new java.util.ArrayList<>();
                    while(st.hasMoreTokens())
                    {

                        coor_list.add(Double.parseDouble(st.nextToken()));
                    }


                    //Konwersja listy na tablice
                    Double[] tablica = coor_list.toArray(new Double[0]);
                    System.out.println("-------------------");
                    for (int c=0; c<tablica.length; c++)
                    {
                        System.out.println(tablica[c]);
                    }
                    System.out.println(tablica.length);
                    for (int j = 0; j < tablica.length-2; j++)
                    {
                        tablica[j+2] = tablica[j] + tablica[j+2];
                    }
                    System.out.println("-*****************");

                    for (Double s : tablica)
                    {
                        System.out.println(s);
                    }
                    System.out.println("?????????????????????????");
                    System.out.println(tablica.length);
                }
            }
        }     
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) 
    throws SAXException 
    {
        if (qName.equalsIgnoreCase("g"))
        {
            System.out.println("End Element :" + qName);
            if(lasy == 1) lasy = 0;
        }
    }   

    @Override
    public void characters(char ch[], int start, int length) throws SAXException 
    {
        System.out.println(new String(ch, start, length));
    }
}

public class Parser_1 
{
    public static void main(String[] args) 
    {
        try 
        {
            File inputFile = new File("points.xml");
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            Handler_1 handler_1 = new Handler_1();
            saxParser.parse(inputFile, handler_1);     
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }   
}
