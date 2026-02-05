package com.example.fitnessapp3.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.ToIntFunction;

public class StringOccurrenceCounter implements ToIntFunction<String> {
    private final Map<String, Integer> sToCount;

    public StringOccurrenceCounter(){
        sToCount = new HashMap<>();
    }

    public int applyAsInt(String s){
        if (s==null){
            throw new NullPointerException();
        }else{
            Integer prev = sToCount.getOrDefault(s, 0);
            assert prev !=null;
            sToCount.put(s, prev + 1);
            return prev;
        }

    }

    public Map<String, Integer> getCountMap(){
        return sToCount;
    }
}
