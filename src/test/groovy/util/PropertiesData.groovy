package util

class PropertiesData {
    def data = [:]
    static def selectedEnv

    void init(){
        Properties properties = new Properties()
        File propertiesFile = new File("gradle.properties")
        propertiesFile.withInputStream {
            properties.load(it)
        }
        selectedEnv = properties.get("env").toLowerCase()
        println selectedEnv + "from properties data"
        loadDataProperties(selectedEnv)
    }

    void loadDataProperties(String selectedEnv){
        Properties properties = new Properties()
        File propertiesFile = new File("src/test/resources/data/" + selectedEnv + "/data.properties")
        propertiesFile.withInputStream {properties.load(it)}
        properties.each(){
            prop, val -> data.put(prop, val)
        }
    }

    def getDataMap(){
        data
    }
    def getDataByKey(String key){
        data.get(key)
    }
}
