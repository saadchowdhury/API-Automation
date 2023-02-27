import kong.unirest.HttpResponse
import model.ApiRequest
import model.CsvTestRow
import model.TestCase
import util.*
import spock.lang.Shared
import spock.lang.Specification

class CustomSpecification extends Specification {
    @Shared
    TestDataProcessor testDataProcessor
    @Shared
    FileUtils fileUtils
    @Shared
    HttpResponse curlResponse
    @Shared
    def curlBody
    @Shared
    ApiRequest curlRequest
    @Shared
    PropertiesData propertiesData = new PropertiesData()

    def setupSpec() {
        propertiesData.init()
        def className = getClass().getSimpleName()
        testDataProcessor = new TestDataProcessor(propertiesData)
        fileUtils = new FileUtils()
        fileUtils.prepareFilePath(className)
    }

    def "#csvTestRow.row.test_case_no: #csvTestRow.row.description"(CsvTestRow csvTestRow) {

        given: "Api request parameters"
        TestCase testCase = testDataProcessor.processTestCase(csvTestRow)

        when: "Send API request"
        ApiRequest apiRequest = testCase.apiRequest
        println "----------------\nAPI request:\n-------------\n" + apiRequest
        HttpResponse httpResponse = RequestExecutor.executeRequest(apiRequest)
        println "---------------\nAPI response:\n------------\n"
        println "\nResponse Status: " + httpResponse.status
        println "\nResponse Headers:\n---------\n" + httpResponse.getHeaders()
        println "\nResponse Headers:\n---------\n" + httpResponse.getBody()
        def jsonBody = JsonParser.toJson(httpResponse.getBody())
        try{
            if(!testCase.requestCurl.isEmpty()){
                curlRequest = CurlVisitor.parseCurl(testCase.requestCurl)
                curlResponse = RequestExecutor.executeRequest(curlRequest)
                curlBody = JsonParser.toJson(curlResponse.getBody())
            }
        }catch(NullPointerException){
                curlRequest = null
                curlResponse = null
                curlBody = null
        }


        then: "Validate response"
        httpResponse.status == testCase.expectedStatus
        //Validation.validate(jsonBody, testCase.validation)
        try{
            if(!testCase.validation.isEmpty()){
                ValidationVisitor.validate(testCase.validation, jsonBody, curlBody)

                if(!testCase.requestCurl.isEmpty()){
                    httpResponse.getBody() == curlResponse.getBody()
                }
            }
        }catch(NullPointerException){
                testCase.validation = null
        }

        where:
        csvTestRow << fileUtils.readFile()
    }

    
}