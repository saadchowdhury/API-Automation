package util

import com.xlson.groovycsv.CsvParser
import com.xlson.groovycsv.PropertyMapper
import model.CsvTestRow

class FileUtils {
    static String test
    File[] fileArray = null
    private def headers



    List<CsvTestRow> readFile() {
        List<CsvTestRow> testRows = []

        for(int i=0 ; i< fileArray.length ; i++){
            headers = loadFile(fileArray[i], true).getAt(0).values
            println(headers)
            loadFile(fileArray[i]).each { row ->
                PropertyMapper datarow = row
                def ignoreValue = getRowValue(datarow,"ignore")
                String envCont = getRowValue(datarow,"environments")
                if((!ignoreValue.toString().equalsIgnoreCase("true"))){
                    if((envCont==null) || envCont.contains(PropertiesData.selectedEnv)){
                        testRows.add(CsvTestRow.builder().headers(headers).row(row).build())}
                }

            }
        }


        testRows
    }




    void prepareFilePath(String className) {
        Class aClass = getClass()
        String path = aClass.getResource("/test-files").getPath()
        String fileName = className+".csv"
        String file = path + "/" + fileName
        String[] files = [path + "/" + fileName ]
        this.fileArray = files.collect { new File(it) }
    }


    private def loadFile(File file, boolean readFirstLine = false){
        CsvParser.parseCsv(file.getText(), separator: "," , readFirstLine: readFirstLine)

    }

    private def getRowValue(def row, String property){
        try{
            row.propertyMissing(property)
        }catch(MissingPropertyException e){
            null
        }
    }

}
