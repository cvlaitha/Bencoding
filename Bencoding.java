import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class Bencoding {
    public Bencoding() {
        
    }
   
    private static String encode(Object valueToEncode) throws Exception {  
        if(valueToEncode instanceof ByteBuffer) {
			byte[] byteString = ((ByteBuffer) valueToEncode).array();
            return encode(Integer.toString(byteString.length).getBytes());
        }
        else if(valueToEncode instanceof Integer) {
            return encode((Integer)valueToEncode);
        }
        else if(valueToEncode instanceof Long) {
            return encode((Long)valueToEncode);
        }
        else if(valueToEncode instanceof List) {
            return encode((List)valueToEncode);
        }
        else if(valueToEncode instanceof Map) {
            return encode((Map)valueToEncode);
        }
        else
            throw new Exception("Object type is not supported " + valueToEncode.getClass().getName());
    }
    
    public static String encode(String stringToEncode) {
        return(stringToEncode.length() + ":" + stringToEncode);
    }
    
    
    public static String encode(Integer intToEncode) {
        return("i" + Integer.toString(intToEncode) + "e");
    }

    public static String encode(Long longToEncode) {
        return("i" + Long.toString(longToEncode) + "e");
    }
    
    public static String encode(List listToEncode) throws Exception {
        Iterator i = listToEncode.iterator();
        StringBuilder encodedString = new StringBuilder();
        encodedString.append("l");
        
        while(i.hasNext()) {
            try {
                Object o = i.next();
                encodedString.append(encode(o));
            }
            catch(Exception ex) {
                throw new Exception("Exception in encoding the list", ex);
            }
        }
        encodedString.append("e");
        
        return(encodedString.toString());
    }
    
       public static String encode(Map dicToEncode) throws Exception {
        StringBuilder encodedString = new StringBuilder();
        encodedString.append('d');
        
        try {
            
            TreeMap sortedMap = new TreeMap(dicToEncode);
            Iterator i = sortedMap.entrySet().iterator();
            
            while(i.hasNext()) {
                Map.Entry pairs = (Map.Entry)i.next();
				if(!(pairs.getKey() instanceof String)){
                    throw new Exception("Keys in dictionary was not a" +
                            "string.");
                }
                encodedString.append(encode((String)pairs.getKey()));
                encodedString.append(encode(pairs.getValue()));
            }
        }
        catch(Exception ex) {
            throw new Exception("Exception in enoding the dictionary", ex);
        }
        encodedString.append('e');
        
        return(encodedString.toString());
    }

    public static HashMap<String, Object> decodeDictionary(String encodedValue) throws Exception {
		char nextChar = encodedValue.charAt(0);
		encodedValue = encodedValue.substring(1);
		if (nextChar != 'd')
			throw new Exception("The next item in the bencoded string is not a Dictionary.");
		HashMap<String, Object> dictionary = new HashMap<>();
		
		while (encodedValue.charAt(0) != 'e') {
			String key = decodeString(encodedValue);
			char token = encodedValue.charAt(0);
			Object value = null;
			if (token == 'i')
				value = decodeInteger(encodedValue);
			else if (token == 'l')
				value = decodeList(encodedValue);
			else if (token == 'd')
				value = decodeDictionary(encodedValue);
			else
				value = decodeString(encodedValue);
			dictionary.put(key, value);
		}
		nextChar = encodedValue.charAt(0);
		encodedValue = encodedValue.substring(1);
		return dictionary;
	}
    public static ArrayList<Object> decodeList(String encodedValue) throws Exception {
		char nextChar = encodedValue.charAt(0);
		encodedValue = encodedValue.substring(1);
		if (nextChar != 'l')
			throw new Exception("The next item in the bencoded string is not a List.");
		ArrayList<Object> list = new ArrayList<Object>();
		while (encodedValue.charAt(0) != 'e') {
			char token = encodedValue.charAt(0);
			if (token == 'i')
				list.add(decodeInteger(encodedValue));
			else if (token == 'd')
				list.add(decodeDictionary(encodedValue));
			else if (token == 'l')
				list.add(decodeList(encodedValue));
			else
				list.add(decodeString(encodedValue));
		}
		nextChar = encodedValue.charAt(0);
		encodedValue = encodedValue.substring(1); 
		return list;
	}

	public static Object decodeInteger(String encodedValue) throws Exception {
		char nextChar = encodedValue.charAt(0);
		encodedValue = encodedValue.substring(1);
		if (nextChar != 'i')
			throw new Exception("The next item in the bencoded string is not an Integer.");
		String[] data = encodedValue.split("e");
		Object o = null;
		if (isInt(data[0]))
			o = Integer.parseInt(data[0]);
		else
			o = Long.parseLong(data[0]);
		encodedValue = encodedValue.substring(data[0].length() + 1);
		return o;
	}

	public static String decodeString(String encodedValue) throws Exception {
		String data[] = encodedValue.split(":", 2);
		int headerLength = data[0].length() + 1; 
		if (isInt(data[0])) {
			int stringLength = Integer.parseInt(data[0]);
			String string = data[1].substring(0, stringLength);
			encodedValue = encodedValue.substring(headerLength + stringLength);
			return string;
		} else {
			throw new Exception("The next item in the bencoded string is not a String. " + encodedValue);
		}
	}
	
	private static boolean isInt(String strData){
		try {
			Integer.parseInt(strData);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
