package util

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer
import constant.Constants
import expression.ValidationBaseVisitor
import expression.ValidationLexer
import expression.ValidationParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CodePointCharStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree

import javax.json.Json
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import java.util.regex.Matcher
import java.util.regex.Pattern


class ValidationVisitor extends ValidationBaseVisitor<Object> {
    def responseBody
    def CurlBody
    def extractInts( String input ) {
        input.findAll( /\d+/ )*.toInteger()
    }

    static def validate(String expression, def body, def curlBody) {
        CodePointCharStream input = CharStreams.fromString(expression)
        ValidationLexer lexer = new ValidationLexer(input)
        CommonTokenStream tokenStream = new CommonTokenStream(lexer)
        ValidationParser parser = new ValidationParser(tokenStream)
        ParseTree tree = parser.validationStatement()
        ValidationVisitor visitor = new ValidationVisitor()
        visitor.responseBody = body;
        visitor.CurlBody = curlBody
        visitor.visit(tree)
    }


    @Override
    Object visitExistKey(ValidationParser.ExistKeyContext ctx) {

        ctx.keyString().each {
            String key = it.getText();
            assert JsonParser.isValidKey(responseBody, key)
        }
        true
    }

    @Override
    Object visitDatatypekey(ValidationParser.DatatypekeyContext ctx) {

        String key = ctx.keyString(0).getText()
        String expectedValue = ctx.keyString(1).getText()
        String actualValue = JsonParser.getValueByKey(responseBody, key).getClass().getSimpleName()
        if (expectedValue.equalsIgnoreCase("ArrayOfString")){
            assert Constants.ARRAY(actualValue).equalsIgnoreCase(expectedValue)
        }else if(expectedValue.equalsIgnoreCase("Map")){
            assert Constants.MAPTYPE(actualValue).equalsIgnoreCase(expectedValue)
        }else if(expectedValue.equalsIgnoreCase("List")){
            assert Constants.ARRAY_LIST(actualValue).equalsIgnoreCase(expectedValue)
        }else {
            assert actualValue == expectedValue
        }
        return true

    }

    @Override
    Object visitEqualcheck(ValidationParser.EqualcheckContext ctx) {

        ctx.keyString().each {
            String key = it.getText()
            String expectedValue = ctx.Identifier().getText()
            String actualValue = JsonParser.getValueByKey(responseBody,key).toString()
            println expectedValue + actualValue
            assert actualValue.equalsIgnoreCase(expectedValue)
        }
        return true
    }

    @Override
    Object visitResponseCheck(ValidationParser.ResponseCheckContext ctx) {
        def keyVal = ctx.keyString(0).getText()
        def resValue = JsonParser.getValueByKey(responseBody,keyVal)
        def keyValue = ctx.keyString(1).getText()
        def resCurlValue = JsonParser.getValueByKey(CurlBody,keyValue)
        assert  resValue == resCurlValue
        return true
    }

    @Override
    Object visitExistArrayKey(ValidationParser.ExistArrayKeyContext ctx) {
        String key = ctx.keyList().getText()
        String key2 = key.replaceAll('\\[\\]', '')
        println key2
        def split = key2.split("\\.")
        def listLast = split.length - 1
        def listKey = split[listLast]
        def list = JsonParser.getValueByKey(responseBody, key2)
        boolean flag = false
        if (list != null) {
            if (listKey == "list") {
                flag = true
            } else
                flag = list.getAt(listKey)[0] != null
        }
        assert flag == true
        return flag
    }

    @Override
    Object visitCheckComb(ValidationParser.CheckCombContext ctx) {
        def key = ctx.keyString(0).getText()
        def key2 = ctx.keyString(1).getText()
        String ResponseValue = JsonParser.getValueByKey(responseBody, key)
        String CurlValue = JsonParser.getValueByKey(CurlBody, key2)

        assert CurlValue.equalsIgnoreCase(ResponseValue)

        return true
    }


    @Override
    Object visitResponseValidityForBrkt(ValidationParser.ResponseValidityForBrktContext ctx) {
        boolean flag = true
        String keyofResponse = ctx.keyList(0).getText()
        String keyofCurl = ctx.keyList(1).getText()
        keyofResponse = keyofResponse.replaceAll('\\[\\]', '')
        keyofCurl = keyofCurl.replaceAll('\\[\\]', '')
        def valueFromResponse = JsonParser.getValueByKey(responseBody, keyofResponse)
        def valueFromCurl = JsonParser.getValueByKey(CurlBody, keyofCurl)

        def splitKey = keyofResponse.split("\\.")
        def listLastIndex = splitKey.length - 1
        def listKey = splitKey[listLastIndex]
        def actualValue = valueFromResponse.getAt(listKey)

        def splitKeyForCurl = keyofCurl.split("\\.")
        def listLastIndexForCurl = splitKeyForCurl.length - 1
        def listKeyForCurl = splitKeyForCurl[listLastIndexForCurl]
        def expectedValue = valueFromCurl.getAt(listKeyForCurl)
        if(expectedValue[0] == null || actualValue[0] == null) {flag = false}

        assert actualValue == expectedValue
        assert flag == true
        return flag
    }

    @Override
    Object visitDatatypeOfList(ValidationParser.DatatypeOfListContext ctx) {
        ctx.keyList().each {
            String key = it.getText();
            String expectedDataType = ctx.keyString().getText()
            key = key.replaceAll('\\[\\]', '')
            println key
            def list = JsonParser.getValueByKey(responseBody, key)
            println list
            def splitKey = key.split("\\.")
            def listLastIndex = splitKey.length - 1
            def listKey = splitKey[listLastIndex]
            println listKey
            def actualValue = list.getAt(listKey).getAt(0).getClass().getSimpleName()
            assert actualValue.equalsIgnoreCase(expectedDataType)
        }
        true
    }

    @Override
    Object visitCalculateValue(ValidationParser.CalculateValueContext ctx) {
        def op = ctx.Operator()
        String expectedVal = ctx.Identifier().getText()
        println op
        int i = 0
        def res
        boolean fnumber = true
        ctx.keyString().each{
           String key = it.getText()
            println key
            def num = JsonParser.getValueByKey(responseBody,key).toString()
            println num
            if(fnumber==true){
                fnumber = false
                res = num
                println res
            }else{
                res = res + op[i] + num
                i++
                println res
            }
        }
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        def total = engine.eval(res).toString()
        println total;
        assert total == expectedVal;
    }

    @Override
    Object visitCompareValue(ValidationParser.CompareValueContext ctx) {
        def key = ctx.keyString().getText()
        println key
        def op = ctx.CompareOperator().getText()
        println "operator "+op
        int expectedVal = ctx.Identifier().getText().toInteger()
        println "expected "+expectedVal
        int actualVal = JsonParser.getValueByKey(responseBody,key)
        println "actual "+actualVal
        switch(op){
            case ">":
                assert actualVal > expectedVal
                break
            case "<":
                assert actualVal < expectedVal
                break
            case "=":
                assert actualVal == expectedVal
                break
            case ">=":
                assert actualVal >= expectedVal
                break
            case "<=":
                assert actualVal <= expectedVal
                break
            case "!=":
                assert actualVal != expectedVal
                break
        }
        return true
    }

    @Override
    Object visitEqualForListCheck(ValidationParser.EqualForListCheckContext ctx) {
        def expectedValue = ctx.keyString().getText()
        println "eX " + expectedValue
        def keyofResponse = ctx.keyList().getText()
        println "eX " + keyofResponse
        def result = extractInts(keyofResponse)
        keyofResponse = keyofResponse.replaceAll('\\[\\d+\\]', '')

        def valueFromResponse = JsonParser.getValueByKey(responseBody, keyofResponse)
        def splittedKey = keyofResponse.split("\\.")
        def lastElement = splittedKey.length - 1
        def lastKey = splittedKey[lastElement]
        def specificValue = valueFromResponse.getAt(lastKey)
        def actualValue = specificValue[result[0]].toString()

        assert actualValue == expectedValue

        true
    }

    @Override
    Object visitContainCheck(ValidationParser.ContainCheckContext ctx) {
        def key = ctx.keyString().getText()
        println key
        def actualVal = JsonParser.getValueByKey(responseBody,key).toString()
        println actualVal
        def expectedVal = ctx.Identifier().getText()
        println expectedVal
        assert actualVal.contains(expectedVal)
    }

    @Override
    Object visitContainListCheck(ValidationParser.ContainListCheckContext ctx) {
        def expectedValue = ctx.Identifier().getText()
        println "eX " + expectedValue
        def keyofResponse = ctx.keyList().getText()
        println "eX " + keyofResponse
        def result = extractInts(keyofResponse)
        keyofResponse = keyofResponse.replaceAll('\\[\\d+\\]', '')

        def valueFromResponse = JsonParser.getValueByKey(responseBody, keyofResponse)
        def splittedKey = keyofResponse.split("\\.")
        def lastElement = splittedKey.length - 1
        def lastKey = splittedKey[lastElement]
        def specificValue = valueFromResponse.getAt(lastKey)
        def actualValue = specificValue[result[0]].toString()

        assert actualValue.contains(expectedValue)

        true
    }

    @Override
    Object visitStartsWithCheck(ValidationParser.StartsWithCheckContext ctx) {
        def key = ctx.keyString().getText()
        println key
        def expectedVal = ctx.Identifier().getText()
        println expectedVal
        def actualVal = JsonParser.getValueByKey(responseBody,key).toString()
        println actualVal + " actual value"
        assert actualVal.startsWith(expectedVal)
    }

    @Override
    Object visitStartswithListCheck(ValidationParser.StartswithListCheckContext ctx) {
        def expectedValue = ctx.Identifier().getText()
        println "eX " + expectedValue
        def keyofResponse = ctx.keyList().getText()
        println "eX " + keyofResponse
        def result = extractInts(keyofResponse)
        keyofResponse = keyofResponse.replaceAll('\\[\\d+\\]', '')

        def valueFromResponse = JsonParser.getValueByKey(responseBody, keyofResponse)
        def splittedKey = keyofResponse.split("\\.")
        def lastElement = splittedKey.length - 1
        def lastKey = splittedKey[lastElement]
        def specificValue = valueFromResponse.getAt(lastKey)
        def actualValue = specificValue[result[0]].toString()
        println actualValue
        assert actualValue.startsWith(expectedValue)
    }

    @Override
    Object visitEndsWithCheck(ValidationParser.EndsWithCheckContext ctx) {
        def key = ctx.keyString().getText()
        println key
        def expectedVal = ctx.Identifier().getText()
        println expectedVal
        def actualVal = JsonParser.getValueByKey(responseBody,key).toString()
        println actualVal + " actual value"
        assert actualVal.endsWith(expectedVal)
    }

    @Override
    Object visitEndswithListCheck(ValidationParser.EndswithListCheckContext ctx) {
        def expectedValue = ctx.Identifier().getText()
        println "eX " + expectedValue
        def keyofResponse = ctx.keyList().getText()
        println "eX " + keyofResponse
        def result = extractInts(keyofResponse)
        keyofResponse = keyofResponse.replaceAll('\\[\\d+\\]', '')

        def valueFromResponse = JsonParser.getValueByKey(responseBody, keyofResponse)
        def splittedKey = keyofResponse.split("\\.")
        def lastElement = splittedKey.length - 1
        def lastKey = splittedKey[lastElement]
        def specificValue = valueFromResponse.getAt(lastKey)
        def actualValue = specificValue[result[0]].toString()
        println actualValue
        assert actualValue.endsWith(expectedValue)
    }

    @Override
    Object visitConcatMultipleString(ValidationParser.ConcatMultipleStringContext ctx) {
        def actualString = ""
        def expectedString = ctx.Identifier().getText()
        println "expected string : " + expectedString
        ctx.keyString().each {
            def key = it.getText()
            def singleString = JsonParser.getValueByKey(responseBody,key).toString()
            actualString = actualString + singleString
        }
        println "actual string : " + actualString
        assert actualString == expectedString
    }

    @Override
    Object visitLengthOfString(ValidationParser.LengthOfStringContext ctx) {
        def key = ctx.keyString().getText()
        println "key : " + key
        def expectedValue = ctx.Identifier().getText()
        println "expected Value : " + expectedValue
        def actualString = JsonParser.getValueByKey(responseBody,key).toString()
        def actualValue = actualString.length().toString()
        println "actual value : " + actualValue
        assert expectedValue == actualValue
    }

    @Override
    Object visitSizeOfString(ValidationParser.SizeOfStringContext ctx) {
        def expectedSize = ctx.Identifier().toString()
        def expectedSizeInt = Integer.parseInt(expectedSize)
        def key = ctx.keyString().getText()
        println "key : " + key
        println "expected size : " + expectedSize
        def valueFromResponse = JsonParser.getValueByKey(responseBody, key)
        def actualSize = valueFromResponse.size()
        println "actual size : " + actualSize
        assert actualSize == expectedSizeInt
        return true
    }

    @Override
    Object visitRoundingOfValue(ValidationParser.RoundingOfValueContext ctx) {
        println "round of working"
        def key = ctx.keyString().getText()
        println "key : " + key
        def word = ctx.roundOfWord().getText()
        println "word : " + word
        def expectedValue = ctx.Identifier().getText()
        def expVal = Double.parseDouble(expectedValue)
        println "expected value : " + expVal
        def calculatedValue
        def value = JsonParser.getValueByKey(responseBody,key)
        println "actual value " + value

        switch (word){
            case "floor":
                calculatedValue = Math.floor(value)
                break
            case "ceil":
                calculatedValue = Math.ceil(value)
                break
            case "round":
                calculatedValue =  Math.round(value)
                break
        }
        println "rounded value : " + calculatedValue

        assert expVal == calculatedValue
    }

    @Override
    Object visitExtremaOfValue(ValidationParser.ExtremaOfValueContext ctx) {
        def word = ctx.extrema().getText()
        println "keyword : " + word
        def key = ctx.keyList().getText()
        println "key : " + key

        key = key.replaceAll('\\[\\]', '')

        def valueFromResponse = JsonParser.getValueByKey(responseBody, key)
        def splittedKey = key.split("\\.")
        def lastElement = splittedKey.length - 1
        def lastKey = splittedKey[lastElement]
        def listValue = valueFromResponse.getAt(lastKey)


        def expectedVal = ctx.Identifier().getText()
        println "expected Value : " + expectedVal
        def actualValue
        if(word == 'max'){
            actualValue = (Collections.max(listValue)).toString()
            println "actual value : " + actualValue
        } else if(word == 'min'){
            actualValue = (Collections.min(listValue)).toString()
            println "actual value : " + actualValue
        }
        assert expectedVal == actualValue

    }


    static String getRegexFromPattern(String pattern) {
        String regex = pattern.replace("yyyy", "\\d{4}")
                .replace("MM", "\\d{2}")
                .replace("dd", "\\d{2}")
                .replace("HH", "\\d{2}")
                .replace("mm", "\\d{2}")
                .replace("ss", "\\d{2}")
        return "^" + regex + "\$"
    }


    @Override
    Object visitCheckDateFormat(ValidationParser.CheckDateFormatContext ctx) {
        boolean key = true
        String formatedDate = ''
        ctx.Identifier().each {
            def idntfr = it.getText()
            if(key){
                key = false
                formatedDate = idntfr
            } else {
                formatedDate = formatedDate + ' ' + idntfr
            }
        }
        def key2 = ctx.keyString().getText()
        def date = JsonParser.getValueByKey(responseBody, key2).toString()
        assert date.matches(getRegexFromPattern(formatedDate))
        return true
    }

    @Override
    Object visitRegexCheck(ValidationParser.RegexCheckContext ctx) {
        boolean key
        String regex = ctx.REGEX().getText()
        regex = regex.replace("REGEX",'')
        regex = regex.replace('\"',"")
        regex = regex.replace(" ",'')
        String value = ctx.keyString().getText()
        String data = JsonParser.getValueByKey(responseBody,value)
        Pattern pattern = Pattern.compile(regex);
        if(data!=null){
            Matcher matcher = pattern.matcher(data);
            if(matcher.matches())
                key = true;
        }
        if(key == false)
            println("Regex not match for \n regex: $regex \n Data: $data")
        assert  key == true
        return true
    }

    @Override
    Object visitSortingMethod(ValidationParser.SortingMethodContext ctx) {
        def sortingWay = ctx.sortingVar().getText()
        def key = ctx.keyString(0).getText()
        def valueFromResponse = JsonParser.getValueByKey(responseBody, key)
        def newList = []
        newList.addAll(valueFromResponse)
        def sortToolVal = ctx.keyString(1).getText()
        def sortedList
        def isSorted = true

        if(sortingWay.equals("asc"))
            sortedList = newList.sort { a, b -> a[sortToolVal] <=> b[sortToolVal] }
        else
            sortedList = newList.sort { a, b -> b[sortToolVal] <=> a[sortToolVal] }
        for(int i = 0; i <sortedList.size(); i++){
            def list1 = sortedList[i].values().toList()
            def list2 =valueFromResponse[i].values().toList()
            isSorted = isSorted && list1 == list2


        }

        assert isSorted == true
    }
}