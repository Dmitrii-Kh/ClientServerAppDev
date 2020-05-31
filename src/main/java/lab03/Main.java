package lab03;

import java.util.concurrent.ConcurrentHashMap;

public class Main {
    static ConcurrentHashMap<Integer, Integer> hs = new ConcurrentHashMap<>();
    static ConcurrentHashMap<Integer, Integer> hs2 = new ConcurrentHashMap<>();


    public static void main(String[] args) {
        hs2.put(1,1);
        hs2.put(2,1);

        hs.put(1,1);
        hs.put(2,2);

        for(Integer userId : hs.keySet()){
            if(hs2.containsKey(userId) && hs2.get(userId).compareTo(hs.get(userId)) == 0){
                hs2.remove(userId);
                hs.remove(userId);
            }
        }
        hs2.putAll(hs);

        System.out.println(hs2);
    }


}
