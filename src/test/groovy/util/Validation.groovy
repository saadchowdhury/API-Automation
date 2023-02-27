package util

class Validation {
    static void validate(def body, String statement) {
//        def str = statement.split(",")

        String[] str
        if (!statement.contains(",")) {
            str = statement
        } else {
            str = statement.split("\n|,")
        }

        for (int i = 0; i < str.size(); i++) {
            if (str[i].contains("==")) {
                def arr = str[i].split("==")
                def actualValue = JsonParser.getValueByKey(body, arr[0].trim())
                def expectedValue = arr[1].trim()
                if (expectedValue.isInteger()) {
                    expectedValue = Integer.parseInt(expectedValue)
                }
                assert actualValue == expectedValue
            } else if (str[i].startsWith("exist")) {
                def keyString = (str[i] - "exist").trim()
                boolean isExist = JsonParser.isValidKey(body, keyString)
                assert isExist
            } else if (str[i].startsWith("datatype")) {
                def keyString = (str[i] - "datatype").trim()
                def arr = keyString.split(" +")
                def actualValue = JsonParser.getValueByKey(body, arr[0].trim()).getClass().name
                def arr2 = actualValue.split("\\.")
                def actual = arr2[2].trim()
                def expectedValue = arr[1].trim()
                assert actual == expectedValue
            }

        }
    }

}