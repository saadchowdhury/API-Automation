package util
import groovy.json.JsonSlurper

class JsonParser {
    static def toJson(def input){
        return new JsonSlurper().parseText(input)
    }

    static def getValueByKey(def obj, String keyString){
        String[] keys = keyString.split("\\.")
        for(key in keys){
            obj = obj.getAt(key)
            if(!(obj instanceof Map)){
                break
            }

        }
        return obj
    }

    static boolean isValidKey(def obj, String key){
        def value = getValueByKey(obj,key)
        return value == null ? false : true
    }

}
