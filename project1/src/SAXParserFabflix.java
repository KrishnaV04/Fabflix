//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
//import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.parsers.SAXParser;
//import javax.xml.parsers.SAXParserFactory;
//
//import org.xml.sax.Attributes;
//import org.xml.sax.SAXException;
//
//import org.xml.sax.helpers.DefaultHandler;
//
//public class SAXParserFabflix extends DefaultHandler {
//    private void parseDocument() {
//        //get a factory
//        SAXParserFactory spf = SAXParserFactory.newInstance();
//        try {
//            //get a new instance of parser
//            SAXParser sp = spf.newSAXParser();
//
//            //parse the file and also register this class for call backs
//            sp.parse("mains243.xml", this);
//
//        } catch (SAXException | ParserConfigurationException | IOException se) {
//            se.printStackTrace();
//        }
//    }
//
//    @Override
//    public void startElement(String uri, String localName, String qName, Attributes attributes) {
//        if (qName.equalsIgnoreCase("directorfilms")) {
//
//        } else if (qName.equalsIgnoreCase("directorfilms")
//    }
//
//    @Override
//    public void characters(char[] ch, int start, int length) {
//        // Handle character data events.
//    }
//
//    @Override
//    public void endElement(String uri, String localName, String qName) {
//        // Handle end element events.
//    }
//}
